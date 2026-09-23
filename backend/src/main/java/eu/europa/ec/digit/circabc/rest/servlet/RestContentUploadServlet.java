package eu.europa.ec.digit.circabc.rest.servlet;

import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.Converter;
import io.swagger.util.FileUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet that handles multipart file uploads into the CIRCABC (Alfresco) repository.
 *
 * <p>Mapped to {@code POST /rest/upload/*}, where the trailing path segment carries the
 * identifier (UID) of the parent node the uploaded content should be created under. The
 * servlet authenticates the caller, validates that the target parent exists, is writable
 * (i.e. the caller holds {@link org.alfresco.service.cmr.security.PermissionService#CREATE_CHILDREN})
 * and is a valid destination type (a folder or an information-news node), then streams each
 * uploaded file into a new {@code cm:content} node.</p>
 *
 * <p>Each file is stored inside its own retrying transaction, running as the system user, and
 * given a unique name to avoid collisions. On success the servlet responds with HTTP 200 and a
 * JSON array describing the created nodes (their {@code id} and final {@code fileName}); on
 * failure it emits an appropriate HTTP error status and message.</p>
 */
@WebServlet("/rest/upload/*")
public class RestContentUploadServlet extends AbstractRestContentServlet {

  /** Commons Logging logger for this servlet. */
  private static final Log logger = LogFactory.getLog(
    RestContentUploadServlet.class
  );

  /**
   * {@inheritDoc}
   *
   * @return the logger used by this servlet
   */
  @Override
  protected Log getLogger() {
    return logger;
  }

  /**
   * Handles an HTTP POST upload request.
   *
   * <p>Lazily initialises the servlet's Alfresco service references, extracts and validates the
   * parent node UID from the request path, authenticates the caller, validates the destination
   * parent node and finally delegates the multipart parsing and node creation to
   * {@link #processFileUploads}. Any unexpected failure is logged and reported as an HTTP 500
   * response.</p>
   *
   * @param req the incoming HTTP request, expected to carry the parent UID in its path and the
   *            file(s) as multipart form data
   * @param resp the HTTP response used to return the upload result or an error
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    try {
      if (authenticationService == null) init();

      String uid = extractUid(req, resp);
      if (uid == null) return;

      if (!authenticateUser(req, resp)) return;

      NodeRef parentRef = Converter.createNodeRefFromId(uid);
      String validationError = validateParent(parentRef);
      if (validationError != null) {
        sendError(resp, getErrorCode(validationError), validationError);
        return;
      }

      processFileUploads(req, resp, parentRef, new ArrayList<>());
    } catch (Exception e) {
      if (logger.isErrorEnabled()) logger.error(
        "Unexpected error during content upload",
        e
      );
      sendError(
        resp,
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "An unexpected error occurred during upload"
      );
    }
  }

  /**
   * Validates that a node is a suitable destination for uploaded content.
   *
   * @param parentRef the reference of the intended parent node
   * @return {@code null} if the node exists, is writable by the caller and is an accepted
   *         destination type; otherwise a human-readable error message describing why the node
   *         is not a valid upload target
   */
  private String validateParent(NodeRef parentRef) {
    if (!nodeService.exists(parentRef)) return "Content unavailable";
    if (
      permissionService
        .hasPermission(parentRef, PermissionService.CREATE_CHILDREN)
        .equals(AccessStatus.DENIED)
    ) return "Access denied - cannot create file";
    QName type = nodeService.getType(parentRef);
    if (
      !type.equals(ContentModel.TYPE_FOLDER) &&
      !type.equals(CircabcModel.TYPE_INFORMATION_NEWS)
    ) return "Destination is not folder type";
    return null;
  }

  /**
   * Parses the multipart request and uploads each submitted file under the given parent node.
   *
   * <p>Form fields and empty/nameless items are skipped; every other item is stored via
   * {@link #handleUpload}. On success responds with HTTP 200 and a JSON array of the uploaded
   * file descriptors, otherwise logs the failure and returns an HTTP 500 response.</p>
   *
   * @param req the incoming HTTP request containing the multipart payload
   * @param resp the HTTP response used to return the result or an error
   * @param parentRef the reference of the parent node to create the content under
   * @param uploadedFiles a mutable list accumulating one map (per uploaded file) describing the
   *                      created node; also used as the response body
   */
  private void processFileUploads(
    HttpServletRequest req,
    HttpServletResponse resp,
    NodeRef parentRef,
    List<Map<String, String>> uploadedFiles
  ) {
    try {
      DiskFileItemFactory factory = DiskFileItemFactory.builder().get();
      JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> upload =
        new JakartaServletFileUpload<>(factory);
      for (DiskFileItem item : upload.parseRequest(req)) {
        if (
          !item.isFormField() &&
          item.getName() != null &&
          !item.getName().isEmpty()
        ) {
          uploadedFiles.add(handleUpload(item, parentRef));
        }
      }
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setHeader("Content-Type", "application/json");
      writeJsonResponse(resp, uploadedFiles);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) logger.error(
        "Error processing file upload",
        e
      );
      sendError(
        resp,
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "Error processing file upload"
      );
    }
  }

  /**
   * Stores a single uploaded file within a retrying transaction, executed as the system user.
   *
   * @param item the parsed multipart item representing the uploaded file
   * @param parentRef the reference of the parent node to create the content under
   * @return a map describing the created node, containing its {@code id} and final
   *         {@code fileName} (empty if the item had no content)
   */
  private Map<String, String> handleUpload(
    DiskFileItem item,
    NodeRef parentRef
  ) {
    String filename = FilenameUtils.getName(item.getName());
    RetryingTransactionHelper txnHelper =
      transactionService.getRetryingTransactionHelper();
    RetryingTransactionCallback<Map<String, String>> callback = () ->
      AuthenticationUtil.runAs(
        (RunAsWork<Map<String, String>>) () ->
          doUpload(item, parentRef, filename),
        AuthenticationUtil.SYSTEM_USER_NAME
      );
    return txnHelper.doInTransaction(callback, false, true);
  }

  /**
   * Creates the repository node for an uploaded file and writes its content.
   *
   * <p>Files with no content are ignored and produce an empty result. A unique name is generated
   * to avoid collisions within the parent, a {@code cm:content} node is created and the file's
   * bytes are streamed into it with a guessed MIME type. When uploading into an information-news
   * node the content-notify behaviour is temporarily disabled to suppress notifications, and
   * restored afterwards. The action is recorded through the audit/log service.</p>
   *
   * @param item the parsed multipart item representing the uploaded file
   * @param parentRef the reference of the parent node to create the content under
   * @param filename the (sanitised) original file name to base the new node's name on
   * @return a map describing the created node, containing its {@code id} and final
   *         {@code fileName}; empty when the item had no content
   * @throws IOException if reading the uploaded content or writing it to the repository fails
   */
  private Map<String, String> doUpload(
    DiskFileItem item,
    NodeRef parentRef,
    String filename
  ) throws IOException {
    Map<String, String> result = new HashMap<>();
    if (item.getSize() <= 0) {
      if (logger.isWarnEnabled()) logger.warn(
        "Ignored file '" + filename + "' as there was no content"
      );
      return result;
    }

    NodeService innerNodeService = getServiceRegistry(
      getServletContext()
    ).getNodeService();
    BehaviourFilter policyBehaviourFilter = getCircabcServiceRegistry(
      getServletContext()
    ).getBehaviourFilter();
    boolean isInfoNews = innerNodeService
      .getType(parentRef)
      .equals(CircabcModel.TYPE_INFORMATION_NEWS);
    boolean wasEnabled = !policyBehaviourFilter.isEnabled();

    try {
      if (isInfoNews) policyBehaviourFilter.disableBehaviour(
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
      );

      String newfilename = FileUtil.generateUniqueFilename(
        nodeService,
        parentRef,
        filename
      );
      Map<QName, Serializable> props = new HashMap<>();
      props.put(ContentModel.PROP_NAME, newfilename);

      NodeRef nodeRef = innerNodeService
        .createNode(
          parentRef,
          ContentModel.ASSOC_CONTAINS,
          QName.createQName(
            ContentModel.PROP_NAME.getNamespaceURI(),
            newfilename
          ),
          ContentModel.TYPE_CONTENT,
          props
        )
        .getChildRef();

      result.put("id", nodeRef.getId());
      result.put("fileName", newfilename);

      ContentWriter writer = contentService.getWriter(
        nodeRef,
        ContentModel.PROP_CONTENT,
        true
      );
      writer.setMimetype(guessMimetype(filename));
      writer.putContent(item.getInputStream());

      logService.log(
        buildLogRecord(
          nodeRef,
          filename,
          "Upload document",
          "Upload file:  with original name"
        )
      );
    } finally {
      if (wasEnabled && isInfoNews) policyBehaviourFilter.enableBehaviour(
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
      );
    }
    return result;
  }
}
