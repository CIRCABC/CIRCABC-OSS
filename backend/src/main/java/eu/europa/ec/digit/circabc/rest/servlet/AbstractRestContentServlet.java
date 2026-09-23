package eu.europa.ec.digit.circabc.rest.servlet;

import eu.europa.ec.digit.circabc.rest.service.CircabcServiceRegistry;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.LogRecord;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.PathUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Base class for CIRCABC REST servlets that handle raw HTTP content transfer
 * (typically file uploads and downloads) outside of the Alfresco Web Scripts
 * framework.
 *
 * <p>Unlike declarative web scripts, these servlets deal directly with the
 * {@link HttpServletRequest}/{@link HttpServletResponse} pair so they can stream
 * binary content. This class centralizes the common concerns shared by such
 * servlets:
 * <ul>
 *   <li>looking up the required Alfresco and CIRCABC services from the Spring
 *       {@link WebApplicationContext} during {@link #init()};</li>
 *   <li>parsing and validating request URIs (UID extraction);</li>
 *   <li>performing HTTP Basic authentication;</li>
 *   <li>writing error and JSON responses;</li>
 *   <li>resolving MIME types and the owning CIRCABC service; and</li>
 *   <li>building audit {@link LogRecord}s for content operations.</li>
 * </ul>
 *
 * <p>Concrete subclasses implement the actual HTTP method handlers
 * (e.g. {@code doGet}/{@code doPost}) and must supply a logger via
 * {@link #getLogger()}.
 */
public abstract class AbstractRestContentServlet extends HttpServlet {

  /** Alfresco service used to validate authentication tickets/credentials and resolve the current user. */
  protected transient AuthenticationService authenticationService;
  /** Alfresco service used to read node properties, aspects and paths. */
  protected transient NodeService nodeService;
  /** Alfresco service used to check node access permissions. */
  protected transient PermissionService permissionService;
  /** CIRCABC service used to record audit log entries for content operations. */
  protected transient LogService logService;
  /** Alfresco service used to manage transactions around repository operations. */
  protected transient TransactionService transactionService;
  /** Alfresco service used to read and write node content (binary streams). */
  protected transient ContentService contentService;
  /** Alfresco service used to resolve MIME types from file extensions. */
  protected transient MimetypeService mimetypeService;
  /** CIRCABC helper providing higher-level operations such as resolving the current Interest Group. */
  protected transient ApiToolBox apiToolBox;

  /**
   * Returns the commons-logging {@link Log} to use for this servlet.
   *
   * <p>Implemented by concrete subclasses so that log output is attributed to
   * the actual servlet class.
   *
   * @return the logger for the concrete servlet
   */
  protected abstract Log getLogger();

  /**
   * Retrieves the Alfresco {@link ServiceRegistry} from the Spring web
   * application context bound to the given servlet context.
   *
   * @param sc the servlet context whose Spring application context holds the bean
   * @return the {@code ServiceRegistry} bean
   */
  public static ServiceRegistry getServiceRegistry(ServletContext sc) {
    return (ServiceRegistry) WebApplicationContextUtils.getRequiredWebApplicationContext(
      sc
    ).getBean("ServiceRegistry");
  }

  /**
   * Retrieves the {@link CircabcServiceRegistry} from the Spring web
   * application context bound to the given servlet context.
   *
   * @param sc the servlet context whose Spring application context holds the bean
   * @return the {@code CircabcServiceRegistry} bean
   */
  public static CircabcServiceRegistry getCircabcServiceRegistry(
    ServletContext sc
  ) {
    return (CircabcServiceRegistry) WebApplicationContextUtils.getRequiredWebApplicationContext(
      sc
    ).getBean(CircabcServiceRegistry.CIRCABC_SERVICE_REGISTRY);
  }

  /**
   * Initializes the servlet by resolving all required Alfresco and CIRCABC
   * services from the Spring {@link WebApplicationContext}.
   *
   * <p>After wiring the standard services, {@link #initAdditionalServices(WebApplicationContext)}
   * is invoked to let subclasses obtain any extra beans they need.
   *
   * @throws ServletException if servlet initialization fails
   */
  @SuppressWarnings("deprecation")
  @Override
  public void init() throws ServletException {
    super.init();
    WebApplicationContext context =
      WebApplicationContextUtils.getRequiredWebApplicationContext(
        getServletContext()
      );
    apiToolBox = (ApiToolBox) context.getBean("apiToolBox");
    logService = (LogService) context.getBean("logService");
    ServiceRegistry serviceRegistry = (ServiceRegistry) context.getBean(
      "ServiceRegistry"
    );
    authenticationService = serviceRegistry.getAuthenticationService();
    nodeService = serviceRegistry.getNodeService();
    contentService = serviceRegistry.getContentService();
    permissionService = serviceRegistry.getPermissionService();
    transactionService = serviceRegistry.getTransactionService();
    mimetypeService = serviceRegistry.getMimetypeService();
    initAdditionalServices(context);
  }

  /**
   * Hook for subclasses to initialize additional services from the Spring
   * context. The default implementation does nothing.
   *
   * @param context the Spring web application context
   */
  protected void initAdditionalServices(WebApplicationContext context) {
    // default: no-op
  }

  /**
   * Extracts the trailing UID segment from the request URI.
   *
   * <p>The URI is expected to have either 4 or 5 slash-separated segments; the
   * UID is the last segment. If the URI is malformed or the UID is empty, an
   * HTTP {@code 400 Bad Request} error is sent on the response and {@code null}
   * is returned.
   *
   * @param req the incoming HTTP request
   * @param resp the HTTP response, used to report validation errors
   * @return the extracted UID, or {@code null} if the request was invalid
   */
  protected String extractUid(
    HttpServletRequest req,
    HttpServletResponse resp
  ) {
    String[] uriParts = req.getRequestURI().split("/");
    if (uriParts.length != 4 && uriParts.length != 5) {
      sendError(
        resp,
        HttpServletResponse.SC_BAD_REQUEST,
        "Request URL malformed"
      );
      return null;
    }
    String uid = uriParts[uriParts.length - 1];
    if (uid == null || uid.isEmpty()) {
      sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing arg UID");
      return null;
    }
    return uid;
  }

  /**
   * Authenticates the caller using the HTTP {@code Authorization} header when a
   * Basic authentication scheme is present.
   *
   * <p>If a Basic credential (an Alfresco authentication ticket) is supplied it
   * is validated against the {@link AuthenticationService}. Requests without a
   * Basic header are treated as already authenticated by the surrounding
   * framework. On validation failure an HTTP {@code 401 Unauthorized} error is
   * sent on the response.
   *
   * @param req the incoming HTTP request
   * @param resp the HTTP response, used to report authentication failures
   * @return {@code true} if authentication succeeded (or no Basic credential was
   *         supplied), {@code false} if authentication failed
   */
  protected boolean authenticateUser(
    HttpServletRequest req,
    HttpServletResponse resp
  ) {
    try {
      String authHdr = req.getHeader("Authorization");
      if (
        authHdr != null &&
        authHdr.length() > 5 &&
        authHdr.substring(0, 5).equalsIgnoreCase("BASIC")
      ) {
        String basicAuth = new String(
          Base64.decodeBase64(authHdr.substring(5).getBytes()),
          java.nio.charset.StandardCharsets.UTF_8
        );
        if (basicAuth.indexOf(":") == -1) authenticationService.validate(
          basicAuth
        );
      }
      return true;
    } catch (Exception e) {
      if (getLogger().isErrorEnabled()) getLogger().error(
        "Authentication failed",
        e
      );
      sendError(
        resp,
        HttpServletResponse.SC_UNAUTHORIZED,
        "Authentication failed"
      );
      return false;
    }
  }

  /**
   * Sends an HTTP error response with the given status code and message.
   *
   * <p>If writing the error fails with an {@link IOException}, the status code
   * is still applied to the response as a best-effort fallback.
   *
   * @param resp the HTTP response to write to
   * @param statusCode the HTTP status code to send
   * @param message the human-readable error message
   */
  protected void sendError(
    HttpServletResponse resp,
    int statusCode,
    String message
  ) {
    try {
      resp.sendError(statusCode, message);
    } catch (IOException e) {
      if (getLogger().isErrorEnabled()) getLogger().error(
        "Failed to send error response: " + statusCode,
        e
      );
      resp.setStatus(statusCode);
    }
  }

  /**
   * Maps an error message to an appropriate HTTP status code.
   *
   * <p>Messages containing {@code "unavailable"} map to {@code 404 Not Found},
   * those containing {@code "denied"} map to {@code 403 Forbidden}, and all
   * others map to {@code 500 Internal Server Error}.
   *
   * @param error the error message to inspect
   * @return the corresponding HTTP status code
   */
  protected int getErrorCode(String error) {
    if (error.contains("unavailable")) return HttpServletResponse.SC_NOT_FOUND;
    if (error.contains("denied")) return HttpServletResponse.SC_FORBIDDEN;
    return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
  }

  /**
   * Writes a JSON representation of the uploaded files to the response writer.
   *
   * <p>Each entry produces an object containing the created node reference
   * (in {@code workspace://SpacesStore/} form), the file name and an
   * {@code ok} status; entries are separated by commas.
   *
   * @param resp the HTTP response to write to
   * @param uploadedFiles the uploaded files, each a map with {@code id} and
   *        {@code fileName} keys
   * @throws IOException if writing to the response fails
   */
  protected void writeJsonResponse(
    HttpServletResponse resp,
    List<Map<String, String>> uploadedFiles
  ) throws IOException {
    for (int i = 0; i < uploadedFiles.size(); i++) {
      Map<String, String> file = uploadedFiles.get(i);
      resp
        .getWriter()
        .append("{\"nodeRef\": \"workspace://SpacesStore/")
        .append(file.get("id"))
        .append("\",");
      resp
        .getWriter()
        .append("\"fileName\": \"")
        .append(file.get("fileName"))
        .append("\",");
      resp.getWriter().append("\"status\":{\"code\": 200,\"name\": \"ok\"}}");
      if (i < uploadedFiles.size() - 1) resp.getWriter().append(",");
    }
  }

  /**
   * Guesses the MIME type of a file from its extension.
   *
   * @param fileName the file name (with or without an extension)
   * @return the resolved MIME type, or {@link MimetypeMap#MIMETYPE_BINARY} if the
   *         extension is missing or unrecognized
   */
  public String guessMimetype(String fileName) {
    int extIndex = fileName.lastIndexOf('.');
    if (extIndex != -1) {
      String mt = mimetypeService
        .getMimetypesByExtension()
        .get(fileName.substring(extIndex + 1).toLowerCase());
      if (mt != null) return mt;
    }
    return MimetypeMap.MIMETYPE_BINARY;
  }

  /**
   * Determines which CIRCABC service a node belongs to by inspecting the
   * aspects of its parent folder.
   *
   * @param nodeRef the node whose owning service should be determined
   * @return {@code "Library"}, {@code "Information"} or {@code "Newsgroup"}
   *         depending on the parent's aspect, or {@code null} if none matches
   */
  protected String determineService(NodeRef nodeRef) {
    Set<QName> aspects = nodeService.getAspects(
      nodeService.getPrimaryParent(nodeRef).getParentRef()
    );
    if (aspects.contains(CircabcModel.ASPECT_LIBRARY)) return "Library";
    if (aspects.contains(CircabcModel.ASPECT_INFORMATION)) return "Information";
    if (aspects.contains(CircabcModel.ASPECT_NEWSGROUP)) return "Newsgroup";
    return null;
  }

  /**
   * Builds an audit {@link LogRecord} describing a content activity on a node.
   *
   * <p>The record is populated with the owning Interest Group and service, the
   * activity type, an informational message, the node and IG database
   * identifiers, the current user (or {@code "guest"}) and the CIRCABC path. If
   * the node has no resolvable Interest Group or service, a mostly empty record
   * is returned. Any exception during construction is logged and a partially
   * populated record is returned rather than propagating the error.
   *
   * @param nodeRef the node the activity relates to
   * @param originalFileName the original file name, appended to the info message
   * @param activity the activity type to record
   * @param infoPrefix the prefix prepended to {@code originalFileName} in the info field
   * @return the populated (or best-effort) log record; never {@code null}
   */
  protected LogRecord buildLogRecord(
    NodeRef nodeRef,
    String originalFileName,
    String activity,
    String infoPrefix
  ) {
    LogRecord logRecord = new LogRecord();
    try {
      NodeRef ig = apiToolBox.getCurrentInterestGroup(nodeRef);
      if (ig == null) return logRecord;

      String service = determineService(nodeRef);
      if (service == null) return logRecord;

      logRecord.setService(service);
      logRecord.setActivity(activity);
      logRecord.setInfo(infoPrefix + originalFileName);
      logRecord.setIgID(
        (Long) nodeService.getProperty(ig, ContentModel.PROP_NODE_DBID)
      );
      logRecord.setDocumentID(
        (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID)
      );
      String user = authenticationService.getCurrentUserName();
      logRecord.setUser(user != null ? user : "guest");
      logRecord.setPath(
        PathUtils.getCircabcPath(nodeService.getPath(nodeRef), true)
      );
      logRecord.setOK(true);
    } catch (Exception e) {
      getLogger().error("Error during logging file download", e);
    }
    return logRecord;
  }
}
