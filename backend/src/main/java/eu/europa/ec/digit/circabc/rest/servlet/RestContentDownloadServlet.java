package eu.europa.ec.digit.circabc.rest.servlet;

import io.swagger.model.LogRecord;
import io.swagger.model.alfresco.CircabcModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * REST servlet that streams the binary content of a repository node back to the
 * caller as a file download.
 *
 * <p>Mapped to {@code /rest/download/*} (HTTP {@code GET}), the trailing path
 * segment of the URL is interpreted as the node identifier (UUID). For example
 * {@code GET /rest/download/<uuid>} downloads the content of the node with that
 * identifier.
 *
 * <p>On each request the servlet:
 * <ol>
 *   <li>lazily initializes its Alfresco/CIRCABC services (see
 *       {@link AbstractRestContentServlet#init()});</li>
 *   <li>authenticates the caller from an optional Basic {@code Authorization}
 *       header or {@code ticket}/{@code alf_ticket} request parameter;</li>
 *   <li>resolves the node from the path, checking the workspace, archive and
 *       version stores in turn;</li>
 *   <li>validates that the node exists, is a content node and is readable by the
 *       caller;</li>
 *   <li>records a "Download Content" audit entry via the {@code LogService}; and</li>
 *   <li>streams the content back with an {@code attachment} {@code Content-Disposition}
 *       header so the browser saves it under the node's file name.</li>
 * </ol>
 *
 * <p>Validation and runtime failures are translated into the appropriate HTTP
 * status codes (400, 403, 404 or 500).
 */
@WebServlet("/rest/download/*")
public class RestContentDownloadServlet extends AbstractRestContentServlet {

  /** Serialization version identifier for this {@code HttpServlet}. */
  private static final long serialVersionUID = 1L;
  /** Commons-logging logger scoped to this servlet class. */
  private static final Log logger = LogFactory.getLog(
    RestContentDownloadServlet.class
  );

  /**
   * {@inheritDoc}
   *
   * @return the logger for this servlet
   */
  @Override
  protected Log getLogger() {
    return logger;
  }

  /**
   * Handles the download request: authenticates the caller, resolves and
   * validates the target node, audits the operation and streams the node's
   * content back as a file attachment.
   *
   * <p>If the node reference is missing, the node cannot be found, is not a
   * content node, is not readable, or has no content, an appropriate HTTP error
   * status is sent and no content is streamed. Unexpected failures result in an
   * HTTP {@code 500 Internal Server Error} and mark the audit record as failed.
   *
   * @param request the incoming HTTP request whose path info carries the node id
   * @param response the HTTP response used to stream content or report errors
   * @throws ServletException if the servlet encounters a general failure
   * @throws IOException if reading the content or writing the response fails
   */
  @Override
  @SuppressWarnings("java:S1989")
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    if (authenticationService == null) init();

    LogRecord logRecord = null;
    authenticate(request);
    String id = extractId(request, response);
    if (id == null) return;

    try {
      NodeRef nodeRef = resolveNodeRef(id);
      String validationError = validateNode(nodeRef);
      if (validationError != null) {
        response.sendError(
          getDownloadErrorCode(validationError),
          validationError
        );
        return;
      }

      ContentReader contentReader = getContentReader(nodeRef);
      if (contentReader == null) {
        response.sendError(
          HttpServletResponse.SC_NOT_FOUND,
          "Content not found."
        );
        return;
      }

      String fileName = (String) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_NAME
      );
      logRecord = buildLogRecord(
        nodeRef,
        fileName,
        "Download Content",
        "Download file: "
      );
      logService.log(logRecord);

      streamContent(response, contentReader, fileName);
    } catch (Exception e) {
      handleError(response, logRecord, e);
    }
  }

  /**
   * Extracts the node identifier from the request path info (the portion after
   * {@code /rest/download}), stripping the leading slash.
   *
   * <p>If no path info is present, an HTTP {@code 400 Bad Request} error is sent
   * and {@code null} is returned.
   *
   * @param request the incoming HTTP request
   * @param response the HTTP response, used to report a missing identifier
   * @return the node identifier, or {@code null} if it was absent
   * @throws IOException if sending the error response fails
   */
  private String extractId(
    HttpServletRequest request,
    HttpServletResponse response
  ) throws IOException {
    String id = request.getPathInfo();
    if (id == null || id.isEmpty()) {
      response.sendError(
        HttpServletResponse.SC_BAD_REQUEST,
        "Missing or invalid nodeRef parameter."
      );
      return null;
    }
    return id.substring(1);
  }

  /**
   * Resolves a node identifier to a {@link NodeRef}, searching the available
   * stores in order: the workspace {@code SpacesStore}, then the archive
   * {@code SpacesStore}, then the {@code version2Store}. The first store in
   * which the node exists wins; if none contains it, the {@code version2Store}
   * reference is returned (and will fail later validation).
   *
   * @param id the node identifier (UUID)
   * @return the resolved node reference
   */
  private NodeRef resolveNodeRef(String id) {
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    if (!nodeService.exists(nodeRef)) {
      nodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, id);
    }
    if (!nodeService.exists(nodeRef)) {
      nodeRef = new NodeRef(new StoreRef("workspace", "version2Store"), id);
    }
    return nodeRef;
  }

  /**
   * Validates that a node can be downloaded: it must exist, be a content node
   * (standard content or CIRCABC customization content) and be readable by the
   * current user.
   *
   * @param nodeRef the node to validate
   * @return {@code null} if the node is valid for download, otherwise a
   *         human-readable error message describing why it is not
   */
  private String validateNode(NodeRef nodeRef) {
    if (!nodeService.exists(nodeRef)) return "Node not found.";
    QName nodeType = nodeService.getType(nodeRef);
    if (
      !ContentModel.TYPE_CONTENT.equals(nodeType) &&
      !CircabcModel.TYPE_CUSTOMIZATION_CONTENT.equals(nodeType)
    ) return "The node is not of type content.";
    if (
      !permissionService
        .hasPermission(nodeRef, PermissionService.READ)
        .equals(AccessStatus.ALLOWED)
    ) return "You do not have permission to access this file.";
    return null;
  }

  /**
   * Maps a validation error message to an appropriate HTTP status code.
   *
   * <p>Messages containing {@code "not found"} map to {@code 404 Not Found},
   * those containing {@code "permission"} map to {@code 403 Forbidden}, and all
   * others map to {@code 400 Bad Request}.
   *
   * @param error the validation error message to inspect
   * @return the corresponding HTTP status code
   */
  private int getDownloadErrorCode(String error) {
    if (error.contains("not found")) return HttpServletResponse.SC_NOT_FOUND;
    if (error.contains("permission")) return HttpServletResponse.SC_FORBIDDEN;
    return HttpServletResponse.SC_BAD_REQUEST;
  }

  /**
   * Determines which CIRCABC service a node belongs to.
   *
   * <p>Overrides {@link AbstractRestContentServlet#determineService(NodeRef)} to
   * inspect the aspects of the node <em>itself</em> (rather than its parent),
   * because on download the service-classifying aspect is carried by the content
   * node.
   *
   * @param nodeRef the node whose owning service should be determined
   * @return {@code "Library"}, {@code "Information"} or {@code "Newsgroup"}
   *         depending on the node's aspect, or {@code null} if none matches
   */
  @Override
  protected String determineService(NodeRef nodeRef) {
    // Download checks aspects on the node itself, not its parent
    java.util.Set<QName> aspects = nodeService.getAspects(nodeRef);
    if (aspects.contains(CircabcModel.ASPECT_LIBRARY)) return "Library";
    if (aspects.contains(CircabcModel.ASPECT_INFORMATION)) return "Information";
    if (aspects.contains(CircabcModel.ASPECT_NEWSGROUP)) return "Newsgroup";
    return null;
  }

  /**
   * Obtains a {@link ContentReader} for the node's content, trying the standard
   * {@code cm:content} property first and falling back to the CIRCABC content
   * property.
   *
   * @param nodeRef the node to read content from
   * @return a content reader, or {@code null} if the node has no readable content
   */
  private ContentReader getContentReader(NodeRef nodeRef) {
    ContentReader reader = contentService.getReader(
      nodeRef,
      ContentModel.PROP_CONTENT
    );
    return reader != null
      ? reader
      : contentService.getReader(nodeRef, CircabcModel.PROP_CONTENT);
  }

  /**
   * Streams the node content to the HTTP response as a file attachment.
   *
   * <p>Sets the {@code Content-Type} from the content reader's MIME type and a
   * {@code Content-Disposition: attachment} header using the given file name,
   * then copies the content stream to the response output stream.
   *
   * @param response the HTTP response to stream to
   * @param contentReader the reader providing the node's content and MIME type
   * @param fileName the file name presented to the client for the download
   * @throws IOException if reading the content or writing the response fails
   */
  private void streamContent(
    HttpServletResponse response,
    ContentReader contentReader,
    String fileName
  ) throws IOException {
    response.setContentType(contentReader.getMimetype());
    response.setHeader(
      "Content-Disposition",
      "attachment; filename=\"" + fileName + "\""
    );
    try (
      InputStream in = contentReader.getContentInputStream();
      OutputStream out = response.getOutputStream()
    ) {
      IOUtils.copy(in, out);
      out.flush();
    }
  }

  /**
   * Handles an unexpected failure during download by marking the audit record as
   * failed (if one was created), sending an HTTP {@code 500 Internal Server
   * Error} to the client and logging the underlying exception.
   *
   * @param response the HTTP response used to report the error
   * @param logRecord the audit record to flag as failed, or {@code null} if none
   *        had been created yet
   * @param e the exception that caused the failure
   * @throws IOException if sending the error response fails
   */
  private void handleError(
    HttpServletResponse response,
    LogRecord logRecord,
    Exception e
  ) throws IOException {
    if (logRecord != null) {
      logRecord.setOK(false);
      logService.log(logRecord);
    }
    response.sendError(
      HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
      "An error occurred while downloading the file."
    );
    if (logger.isErrorEnabled()) logger.error(
      "An error occurred while downloading the file.",
      e
    );
  }

  /**
   * Authenticates the caller from the request if credentials are supplied.
   *
   * <p>Extracts a ticket via {@link #extractTicket(HttpServletRequest)} and, when
   * it looks like an Alfresco ticket (prefixed with {@code TICKET_}), validates
   * it against the {@link AuthenticationService}. Failures are logged but not
   * propagated, so requests without valid credentials proceed and are rejected
   * later by permission checks.
   *
   * @param request the incoming HTTP request
   */
  private void authenticate(HttpServletRequest request) {
    try {
      String ticket = extractTicket(request);
      if (ticket != null && ticket.startsWith("TICKET_")) {
        authenticationService.validate(ticket);
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) logger.error("Can not get user details", e);
    }
  }

  /**
   * Extracts an authentication ticket from the request.
   *
   * <p>Prefers a Base64-encoded {@code Basic} {@code Authorization} header; if
   * absent, falls back to the {@code ticket} request parameter and then the
   * {@code alf_ticket} parameter.
   *
   * @param request the incoming HTTP request
   * @return the extracted ticket, or {@code null} if none is present
   */
  private String extractTicket(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Basic ")) {
      return new String(
        Base64.getDecoder().decode(authHeader.substring(6)),
        StandardCharsets.UTF_8
      );
    }
    String ticket = request.getParameter("ticket");
    return ticket != null ? ticket : request.getParameter("alf_ticket");
  }
}
