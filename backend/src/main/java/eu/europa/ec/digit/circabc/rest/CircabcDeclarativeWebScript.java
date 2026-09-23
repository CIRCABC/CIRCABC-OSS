package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.LogRestRecord;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Base class for all CIRCABC Alfresco Web Script endpoints.
 *
 * <p>It extends Alfresco's {@link DeclarativeWebScript} and centralizes the
 * cross-cutting REST auditing concern: after every request has been executed,
 * it captures details about the call (HTTP method, resolved URL, matched URI
 * template, path/query parameters and any JSON payload) and persists them as a
 * {@link LogRestRecord} through the {@link LogService}.</p>
 *
 * <p>Concrete endpoint classes in this package (named {@code <Entity><Method>},
 * e.g. {@code GroupGet}, {@code GroupsMembersPost}) subclass this type and
 * override {@code executeImpl()} to implement their specific behavior. Mutating
 * endpoints inherit the request logging performed here via
 * {@link #executeFinallyImpl(WebScriptRequest, Status, Cache, Map)}.</p>
 */
public class CircabcDeclarativeWebScript extends DeclarativeWebScript {

  /** Shared logger for this class and its subclasses. */
  static final Log logger = LogFactory.getLog(
    CircabcDeclarativeWebScript.class
  );

  /** Platform-specific line separator used when formatting the logged info string. */
  private static final String LINE_SEPARATOR = System.getProperty(
    "line.separator"
  );

  /** Maximum length, in characters, allowed for the persisted info string. */
  private static final int INFO_MAX_SIZE = 4000;

  /** Maximum length, in characters, allowed for the persisted request URL. */
  private static final int URL_MAX_SIZE = 510;

  /** Service used to persist REST audit records. */
  @Autowired
  protected LogService logService;

  /** Toolbox providing helpers to resolve CIRCABC-specific node metadata. */
  @Autowired
  protected ApiToolBox apiToolBox;

  /**
   * Node service used without security enforcement, required to resolve the
   * parent of a node that is about to be (or has just been) deleted.
   */
  @Autowired
  @Qualifier("nodeService")
  protected NodeService unsecureNodeService;

  /** Parent node captured before a delete, so it can still be recorded afterwards. */
  protected NodeRef nodeParent;

  /** CIRCABC path of the affected node, captured before a delete for auditing. */
  protected String nodePath;

  /** Database identifier of the affected node, captured before a delete for auditing. */
  protected long nodeID;

  /**
   * Captures the metadata of a node before it is deleted so that it can still be
   * recorded in the audit log once the node no longer exists.
   *
   * <p>Resolves and stores the parent node ({@link #nodeParent}), the CIRCABC
   * path ({@link #nodePath}) and the database identifier ({@link #nodeID}). When
   * the node has no owning Interest Group, its primary parent is used instead.</p>
   *
   * @param deletedNodeId the identifier of the node that is about to be deleted
   */
  protected void recordBeforeDelete(String deletedNodeId) {
    NodeRef deletedNodeRef = Converter.createNodeRefFromId(deletedNodeId);
    nodeParent = apiToolBox.getCurrentInterestGroup(deletedNodeRef);
    if (nodeParent == null) {
      nodeParent = unsecureNodeService
        .getPrimaryParent(deletedNodeRef)
        .getParentRef();
    }
    nodePath = apiToolBox.getCircabcPath(deletedNodeRef, true);
    nodeID = apiToolBox.getDatabaseID(deletedNodeRef);
  }

  /**
   * Invoked by the Web Script framework after the request has been executed.
   *
   * <p>Delegates to the superclass and then, unless the call is excluded by
   * {@link #shouldNotLog(String, WebScriptRequest, String)}, builds a
   * {@link LogRestRecord} describing the request and persists it via the
   * {@link LogService}.</p>
   *
   * @param req    the web script request being processed
   * @param status the response status of the executed request
   * @param cache  the cache directives for the response
   * @param model  the model produced by the executed web script
   */
  @Override
  protected void executeFinallyImpl(
    WebScriptRequest req,
    Status status,
    Cache cache,
    Map<String, Object> model
  ) {
    super.executeFinallyImpl(req, status, cache, model);

    String method = this.getDescription().getMethod();
    String user = AuthenticationUtil.getFullyAuthenticatedUser();

    if (shouldNotLog(method, req, user)) {
      return;
    }

    LogRestRecord logRestRecord = buildLogRecord(req, status, method, user);
    this.logService.logRest(logRestRecord);
  }

  private LogRestRecord buildLogRecord(
    WebScriptRequest req,
    Status status,
    String method,
    String user
  ) {
    String url = req.getURL();
    String template = req.getServiceMatch().getTemplate();
    Map<String, Integer> templateVariableOrder = getTemplateVariableOrder(
      template
    );
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String[] parameterNames = req.getParameterNames();

    String payload = extractPayload(req);
    String info = buildInfoString(templateVars, parameterNames, req, payload);

    logDebugInfo(user, method, template, url, status.getCode(), info);

    LogRestRecord logRestRecord = new LogRestRecord();
    logRestRecord.setDate(new Date());
    logRestRecord.setInfo(truncate(info, INFO_MAX_SIZE));
    logRestRecord.setMethod(method);
    logRestRecord.setStatusCode(status.getCode());
    logRestRecord.setTemplate(template);
    logRestRecord.setUrl(truncate(url, URL_MAX_SIZE));
    logRestRecord.setUser(user);
    logRestRecord.setNodePath(nodePath);
    logRestRecord.setNodeID(nodeID);
    if (nodeParent != null) {
      logRestRecord.setNodeParent(nodeParent.toString());
    }

    populatePathParameters(templateVariableOrder, templateVars, logRestRecord);
    return logRestRecord;
  }

  private String extractPayload(WebScriptRequest req) {
    if (
      req.getContent() == null ||
      !"application/json".equalsIgnoreCase(req.getContent().getMimetype())
    ) {
      return null;
    }
    try {
      return req.getContent().getContent();
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Could not get content json", e);
      }
      return null;
    }
  }

  private String buildInfoString(
    Map<String, String> templateVars,
    String[] parameterNames,
    WebScriptRequest req,
    String payload
  ) {
    int pathSize = templateVars.size();
    int querySize = parameterNames.length;
    boolean hasContent = payload != null;

    if (pathSize == 0 && querySize == 0 && !hasContent) {
      return "";
    }

    StringBuilder info = new StringBuilder(128);
    info.append('{');

    if (pathSize > 0) {
      appendPathParameters(templateVars, info);
      if (querySize > 0 || hasContent) {
        info.append(',');
      }
    }

    if (querySize > 0) {
      appendQueryParameters(req, parameterNames, info);
      if (hasContent) {
        info.append(',');
      }
    }

    if (hasContent) {
      info.append("\"payload\":").append(payload);
    }

    info.append('}');
    return info.toString();
  }

  private void appendPathParameters(
    Map<String, String> templateVars,
    StringBuilder info
  ) {
    info.append("\"pathParameters\":{").append(LINE_SEPARATOR);
    int step = 0;
    int size = templateVars.size();
    for (Map.Entry<String, String> entry : templateVars.entrySet()) {
      step++;
      info
        .append('"')
        .append(entry.getKey())
        .append("\":\"")
        .append(entry.getValue())
        .append('"');
      info.append(LINE_SEPARATOR);
      if (step < size) {
        info.append(',');
      }
    }
    info.append('}');
  }

  private void appendQueryParameters(
    WebScriptRequest req,
    String[] parameterNames,
    StringBuilder info
  ) {
    info.append("\"queryParameters\":{");
    int step = 0;
    int size = parameterNames.length;
    for (String parameter : parameterNames) {
      step++;
      info
        .append('"')
        .append(parameter)
        .append("\":\"")
        .append(req.getParameter(parameter))
        .append('"');
      info.append(LINE_SEPARATOR);
      if (step < size) {
        info.append(',');
      }
    }
    info.append('}');
  }

  private void populatePathParameters(
    Map<String, Integer> templateVariableOrder,
    Map<String, String> templateVars,
    LogRestRecord logRestRecord
  ) {
    for (Map.Entry<String, String> entry : templateVars.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      Integer order = templateVariableOrder.get(key);
      setPathParameter(logRestRecord, order, key, value);
    }
  }

  private void setPathParameter(
    LogRestRecord logRestRecord,
    Integer order,
    String key,
    String value
  ) {
    if (order == null) return;
    switch (order) {
      case 1:
        logRestRecord.setPathOneName(key);
        logRestRecord.setPathOneValue(value);
        break;
      case 2:
        logRestRecord.setPathTwoName(key);
        logRestRecord.setPathTwoValue(value);
        break;
      case 3:
        logRestRecord.setPathThreeName(key);
        logRestRecord.setPathThreeValue(value);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + order);
    }
  }

  private boolean shouldNotLog(
    String method,
    WebScriptRequest req,
    String user
  ) {
    return (
      method.equals("GET") &&
      req.getServiceMatch().getTemplate().equals("/circabc/groups/{id}") &&
      (user.equals("guest") || "false".equals(req.getParameter("log")))
    );
  }

  private Map<String, Integer> getTemplateVariableOrder(String template) {
    Matcher m = Pattern.compile("\\{(\\w*?)\\}").matcher(template);
    Map<String, Integer> result = new HashMap<>();
    int position = 0;
    while (m.find()) {
      position++;
      result.put(m.group(1), position);
    }
    return result;
  }

  private String truncate(String value, int maxSize) {
    if (value == null) return null;
    return value.length() > maxSize ? value.substring(0, maxSize) : value;
  }

  private void logDebugInfo(
    String user,
    String method,
    String template,
    String url,
    int statusCode,
    String info
  ) {
    if (logger.isInfoEnabled()) {
      logger.info("user: " + user);
      logger.info("method: " + method);
      logger.info("template: " + template);
      logger.info("url: " + url);
      logger.info("statusCode: " + statusCode);
      logger.info("info: " + info);
    }
  }
}
