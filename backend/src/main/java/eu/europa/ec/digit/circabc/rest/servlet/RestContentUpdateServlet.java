package eu.europa.ec.digit.circabc.rest.servlet;

import io.swagger.model.alfresco.aspect.DisableNotificationThreadLocal;
import io.swagger.util.Converter;
import io.swagger.util.FileUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet that handles updating the binary content of an existing Alfresco node
 * through a multipart HTTP {@code POST} to {@code /rest/update/*}.
 *
 * <p>The trailing path segment of the request URL carries the target node
 * identifier (UID). Before the upload is processed the servlet authenticates the
 * caller, verifies that the target node exists, that the caller holds
 * {@link PermissionService#WRITE_CONTENT} permission on it, and that the node is
 * of type {@link ContentModel#TYPE_CONTENT}.
 *
 * <p>The request body is expected to contain exactly one multipart file item.
 * Two optional request parameters influence the update:
 * <ul>
 *   <li>{@code notify} - when {@code false} the notification thread-local is
 *   disabled so that no update notifications are sent (defaults to {@code true});</li>
 *   <li>{@code generateNewFileName} - when {@code true} a unique file name is
 *   generated for the updated node to avoid clashes (defaults to {@code true}).</li>
 * </ul>
 *
 * <p>The content write and property update run as the system user inside a
 * retrying transaction. On success the servlet responds with HTTP 200 and a JSON
 * array describing the updated file(s).
 *
 * @see AbstractRestContentServlet
 */
@WebServlet("/rest/update/*")
public class RestContentUpdateServlet extends AbstractRestContentServlet {

  /** Maximum size (3&nbsp;MB) kept in memory before uploaded content is buffered to disk. */
  private static final int MEMORY_THRESHOLD = 1024 * 1024 * 3;

  private static final long serialVersionUID = 1L;

  /** Logger for this servlet. */
  private static final Log logger = LogFactory.getLog(
    RestContentUpdateServlet.class
  );

  /**
   * Returns the logger used by this servlet, allowing the abstract base class to
   * emit messages under this concrete servlet's category.
   *
   * @return the {@link Log} instance for this servlet
   */
  @Override
  protected Log getLogger() {
    return logger;
  }

  /**
   * Handles the multipart {@code POST} request that updates the content of an
   * existing node.
   *
   * <p>The method lazily initialises the servlet's Alfresco service references,
   * extracts and validates the target node UID from the request path,
   * authenticates the caller, validates the target node, and finally delegates
   * the multipart parsing and content update to
   * {@link #processFileUpload(HttpServletRequest, HttpServletResponse, NodeRef)}.
   * Any unexpected failure results in an HTTP 500 error response.
   *
   * @param req the incoming HTTP request carrying the target UID in its path and
   *     the file to upload in its multipart body
   * @param resp the HTTP response used to report success or an error
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    try {
      if (authenticationService == null) init();

      String uid = extractUid(req, resp);
      if (uid == null) return;

      if (!authenticateUser(req, resp)) return;

      NodeRef targetRef = Converter.createNodeRefFromId(uid);
      String validationError = validateTarget(targetRef);
      if (validationError != null) {
        sendError(resp, getErrorCode(validationError), validationError);
        return;
      }

      processFileUpload(req, resp, targetRef);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) logger.error(
        "Unexpected error during content update",
        e
      );
      sendError(
        resp,
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "An unexpected error occurred during update"
      );
    }
  }

  /**
   * Validates that the target node is a suitable destination for a content update.
   *
   * @param targetRef the reference of the node to be updated
   * @return {@code null} if the target is valid; otherwise a human-readable
   *     message describing why the update cannot proceed (node missing, write
   *     permission denied, or wrong node type)
   */
  private String validateTarget(NodeRef targetRef) {
    if (!nodeService.exists(targetRef)) return "Content unavailable";
    if (
      permissionService
        .hasPermission(targetRef, PermissionService.WRITE_CONTENT)
        .equals(AccessStatus.DENIED)
    ) return "Access denied - cannot update file";
    if (
      !nodeService.getType(targetRef).equals(ContentModel.TYPE_CONTENT)
    ) return "Destination is not content type";
    return null;
  }

  /**
   * Parses the multipart request, enforces the single-file constraint, reads the
   * {@code notify} and {@code generateNewFileName} parameters, performs the
   * content update and writes the JSON response.
   *
   * <p>If the request does not contain exactly one file item an HTTP 400 error is
   * returned; any processing failure results in an HTTP 500 error.
   *
   * @param req the incoming multipart HTTP request
   * @param resp the HTTP response to write the result or error to
   * @param targetRef the reference of the validated node whose content is updated
   */
  private void processFileUpload(
    HttpServletRequest req,
    HttpServletResponse resp,
    NodeRef targetRef
  ) {
    try {
      DiskFileItemFactory factory = DiskFileItemFactory.builder()
        .setBufferSize(MEMORY_THRESHOLD)
        .setPath(System.getProperty("java.io.tmpdir"))
        .get();
      JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> upload =
        new JakartaServletFileUpload<>();
      upload.setFileItemFactory(factory);

      List<DiskFileItem> fileItems = upload.parseRequest(req);
      if (fileItems.size() != 1) {
        sendError(
          resp,
          HttpServletResponse.SC_BAD_REQUEST,
          "Too many files in the request"
        );
        return;
      }

      Boolean notify = parseBoolean(req.getParameter("notify"), true);
      Boolean generateNewFileName = parseBoolean(
        req.getParameter("generateNewFileName"),
        true
      );

      DiskFileItem item = fileItems.get(0);
      List<Map<String, String>> uploadedFiles = new ArrayList<>();
      if (
        !item.isFormField() &&
        item.getName() != null &&
        !item.getName().isEmpty()
      ) {
        uploadedFiles.add(
          handleUpdate(item, targetRef, generateNewFileName, notify)
        );
      }

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setHeader("Content-Type", "application/json");
      writeJsonResponse(resp, uploadedFiles);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) logger.error(
        "Error during content upload",
        e
      );
      sendError(
        resp,
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "Error processing file update"
      );
    }
  }

  /**
   * Parses a boolean request parameter, falling back to a default when the value
   * is absent.
   *
   * @param value the raw parameter value, may be {@code null}
   * @param defaultValue the value to return when {@code value} is {@code null}
   * @return the parsed boolean, or {@code defaultValue} when no value was supplied
   */
  private Boolean parseBoolean(String value, boolean defaultValue) {
    return value != null ? Boolean.parseBoolean(value) : defaultValue;
  }

  /**
   * Performs the actual content update for a single uploaded file within a
   * retrying transaction, running as the system user.
   *
   * <p>When the uploaded item has content, the node's name is updated (optionally
   * with a freshly generated unique file name), the binary content is written and
   * the operation is logged. Empty uploads are ignored with a warning and yield an
   * empty result map. Notifications are suppressed when {@code notify} is
   * {@code false}.
   *
   * @param item the uploaded file item whose content is written to the node
   * @param targetRef the reference of the node being updated
   * @param generateNewFileName when {@code true} a unique file name is generated,
   *     otherwise the original uploaded file name is used
   * @param notify when {@code false} update notifications are disabled
   * @return a map containing the updated node {@code id} and {@code fileName}, or an
   *     empty map when the upload had no content
   */
  private Map<String, String> handleUpdate(
    DiskFileItem item,
    NodeRef targetRef,
    Boolean generateNewFileName,
    Boolean notify
  ) {
    String filename = FilenameUtils.getName(item.getName());
    RetryingTransactionHelper txnHelper =
      transactionService.getRetryingTransactionHelper();
    RetryingTransactionCallback<Map<String, String>> callback = () ->
      AuthenticationUtil.runAs(
        (RunAsWork<Map<String, String>>) () -> {
          Map<String, String> result = new HashMap<>();
          new DisableNotificationThreadLocal().set(!notify);

          if (item.getSize() > 0) {
            String newfilename = Boolean.TRUE.equals(generateNewFileName)
              ? FileUtil.generateUniqueFilename(
                  nodeService,
                  targetRef,
                  filename
                )
              : filename;
            nodeService.setProperty(
              targetRef,
              ContentModel.PROP_NAME,
              newfilename
            );
            result.put("id", targetRef.getId());
            result.put("fileName", newfilename);

            ContentWriter writer = contentService.getWriter(
              targetRef,
              ContentModel.PROP_CONTENT,
              true
            );
            writer.setMimetype(guessMimetype(filename));
            writer.putContent(item.getInputStream());
            logService.log(
              buildLogRecord(
                targetRef,
                filename,
                "UpdateDocument",
                "Update content : with original name"
              )
            );
          } else if (logger.isWarnEnabled()) {
            logger.warn(
              "Ignored file '" + filename + "' as there was no content"
            );
          }
          return result;
        },
        AuthenticationUtil.SYSTEM_USER_NAME
      );
    return txnHelper.doInTransaction(callback, false, true);
  }
}
