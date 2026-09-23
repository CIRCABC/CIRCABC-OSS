package eu.europa.ec.digit.circabc.rest.service.log;

import io.swagger.api.CircabcApi;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.db.LogRecordDAO;
import io.swagger.model.db.LogRestDAO;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.util.UUID;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link LogTransformService}.
 *
 * <p>Converts an incoming REST audit-log entry ({@link LogRestDAO}), as captured from a CIRCABC
 * REST request, into a persistable audit-log record ({@link LogRecordDAO}) suitable for storage in
 * the CIRCABC logging tables.
 *
 * <p>The transformation resolves the Alfresco {@link NodeRef} the request acted upon, enriches the
 * record with node, path and Interest Group container metadata, derives a human-readable activity
 * description from the request payload, and captures the outcome (date, success flag and user).
 *
 * <p>Human-readable descriptions are produced per activity type by parsing the JSON request body;
 * any failure while parsing or processing the body is handled gracefully by falling back to the raw
 * request info so that logging never breaks the originating operation.
 */
public class LogTransformServiceImpl implements LogTransformService {

  /** JSON key holding the request body payload within a stored log entry. */
  private static final String PAYLOAD = "payload";
  /** JSON key for a profile/localised title object. */
  private static final String TITLE = "title";
  /** JSON key for the permissions object of a profile. */
  private static final String PERMISSIONS = "permissions";
  /** Logger used to report JSON parsing and info-processing failures. */
  private static final Log logger = LogFactory.getLog(
    LogTransformServiceImpl.class
  );

  /** Alfresco node service used to inspect node aspects when resolving container data. */
  @Autowired
  private NodeService nodeService;

  /** CIRCABC API used to resolve the repository root node reference. */
  @Autowired
  CircabcApi circabcApi;

  /** DAO service used to look up activity and template identifiers for a log entry. */
  @Autowired
  private LogDaoService logDaoService;

  /** Utility helper providing node resolution, path and Interest Group lookups. */
  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Transforms a REST audit-log entry into a persistable audit-log record.
   *
   * <p>Resolves the target node, populates node/path and Interest Group container metadata, derives
   * a human-readable activity description from the request payload, and records the date, success
   * flag (based on the HTTP status code) and the acting user.
   *
   * @param logRestDAO the REST log entry captured from the incoming request; must not be {@code
   *     null}
   * @return the populated {@link LogRecordDAO} ready to be persisted
   */
  @Override
  public LogRecordDAO transform(LogRestDAO logRestDAO) {
    LogRecordDAO dbLogRecord = new LogRecordDAO();
    long activityID = logDaoService.getActivityID(logRestDAO.getTemplateID());
    dbLogRecord.setActivityID((int) activityID);

    NodeRef nodeRef = resolveNodeRef(logRestDAO);
    populateNodeData(dbLogRecord, nodeRef, logRestDAO);
    dbLogRecord.setInfo(
      processInfoSafe(dbLogRecord.getActivityID(), logRestDAO)
    );
    dbLogRecord.setDate(logRestDAO.getLogDate());
    dbLogRecord.setIsOK(isStatusOK(logRestDAO) ? 1 : 0);
    dbLogRecord.setUser(logRestDAO.getUserName());
    return dbLogRecord;
  }

  private NodeRef resolveNodeRef(LogRestDAO logRestDAO) {
    if (isValidUUID(logRestDAO.getPathOneValue())) {
      return apiToolBox.getNodeRef(logRestDAO.getPathOneValue());
    }
    long templateId = logDaoService.getTemplateID(
      "POST",
      "/circabc/repositories/{id}/transaction"
    );
    if (logRestDAO.getTemplateID() == templateId) {
      return getExternalRepoNodeRef(logRestDAO.getInfo());
    }
    return circabcApi.getCircabcNodeRef();
  }

  private void populateNodeData(
    LogRecordDAO dbLogRecord,
    NodeRef nodeRef,
    LogRestDAO logRestDAO
  ) {
    if (nodeRef == null) {
      dbLogRecord.setDocumentID(logRestDAO.getNodeID());
      dbLogRecord.setPath(logRestDAO.getNodePath());
      NodeRef containerRef =
        logRestDAO.getNodeParent() != null
          ? new NodeRef(logRestDAO.getNodeParent())
          : circabcApi.getCircabcNodeRef();
      setContainerData(dbLogRecord, containerRef);
    } else {
      dbLogRecord.setUuid(nodeRef.getId());
      dbLogRecord.setDocumentID(apiToolBox.getDatabaseID(nodeRef));
      String path = apiToolBox.getCircabcPath(nodeRef, true);
      dbLogRecord.setPath(
        path.isEmpty()
          ? apiToolBox.getCircabcPathForArchivedNode(nodeRef, true)
          : path
      );
      setContainerData(dbLogRecord, nodeRef);
    }
  }

  private String processInfoSafe(int activityID, LogRestDAO logRestDAO) {
    try {
      return processInfo(activityID, logRestDAO);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) logger.error("Fail to process info: ", e);
      return logRestDAO.getInfo();
    }
  }

  private NodeRef getExternalRepoNodeRef(String info) {
    JSONObject json = parseJson(info);
    if (json.isEmpty()) return null;
    JSONObject payload = (JSONObject) json.get(PAYLOAD);
    JSONArray nodes = (JSONArray) payload.get("nodes");
    if (nodes != null && !nodes.isEmpty()) {
      return Converter.createNodeRefFromId(
        (String) ((JSONObject) nodes.get(0)).get("id")
      );
    }
    return null;
  }

  private String processInfo(int activityID, LogRestDAO logRestDAO) {
    String info = logRestDAO.getInfo();
    JSONObject json = parseJson(info);
    if (json.isEmpty()) return info;

    switch (activityID) {
      case 9957105:
        return updateProfile(json, "Update profile %s new permissions: %s");
      case 9957104:
        return removeProfile(logRestDAO.getNodePath());
      case 9957066:
        return updateProfile(json, "Add profile %s new permissions: %s");
      case 9957063:
        return updateMembers(json, "Update Members: ");
      case 9957062:
        return updateMembers(json, "Invite Users: ");
      case 9957005:
        return String.format("Remove member: %s", logRestDAO.getPathTwoValue());
      case 9957065:
        return applyForMembership(json);
      case 9957064:
        return updateApplyForMembership(info, json);
      default:
        return info;
    }
  }

  private JSONObject parseJson(String info) {
    try {
      return (JSONObject) new JSONParser().parse(info);
    } catch (ParseException e) {
      logger.error("Error when parsing the JSON body.", e);
      return new JSONObject();
    }
  }

  private String updateApplyForMembership(String info, JSONObject json) {
    JSONObject payload = (JSONObject) json.get(PAYLOAD);
    String userName = (String) payload.get("username");
    String action = (String) payload.get("action");
    switch (action) {
      case "decline":
        return String.format("Membership declined for user : %s", userName);
      case "submitNew":
        return String.format("The user : %s applied for membership", userName);
      case "clean":
        return String.format("Membership approved for user : %s", userName);
      default:
        return info;
    }
  }

  private String applyForMembership(JSONObject json) {
    JSONObject payload = (JSONObject) json.get(PAYLOAD);
    return String.format(
      "The user : %s applied for membership",
      payload.get("username")
    );
  }

  private String removeProfile(String nodePath) {
    String[] elements = nodePath.split("/");
    return String.format("Remove profile %s ", elements[elements.length - 1]);
  }

  private String updateProfile(JSONObject json, String format) {
    JSONObject payload = (JSONObject) json.get(PAYLOAD);
    String profile = (String) ((JSONObject) payload.get(TITLE)).get("en");
    String permissions = ((JSONObject) payload.get(PERMISSIONS)).toJSONString();
    return String.format(format, profile, permissions);
  }

  private String updateMembers(JSONObject json, String prefix) {
    StringBuilder sb = new StringBuilder(128).append(prefix);
    JSONObject payload = (JSONObject) json.get(PAYLOAD);
    JSONArray memberships = (JSONArray) payload.get("memberships");
    for (Object obj : memberships) {
      JSONObject membership = (JSONObject) obj;
      JSONObject user = (JSONObject) membership.get("user");
      JSONObject profile = (JSONObject) membership.get("profile");
      sb.append(
        String.format(
          "user (%s) %s %s",
          user.get("userId"),
          user.get("firstname"),
          user.get("lastname")
        )
      );
      sb.append(
        String.format(
          " has profile %s with permissions %s",
          ((JSONObject) profile.get(TITLE)).get("en"),
          ((JSONObject) profile.get(PERMISSIONS)).toJSONString()
        )
      );
      sb.append('\n');
    }
    return sb.toString();
  }

  private void setContainerData(LogRecordDAO dbLogRecord, NodeRef nodeRef) {
    NodeRef containerRef = nodeRef;
    if (
      !nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_ROOT) &&
      !nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)
    ) {
      containerRef = apiToolBox.getCurrentInterestGroup(nodeRef);
      if (containerRef == null) containerRef =
        apiToolBox.getInterestGroupForArchivedNode(nodeRef);
    }
    dbLogRecord.setIgID(apiToolBox.getDatabaseID(containerRef));
    dbLogRecord.setIgName(apiToolBox.getName(containerRef));
  }

  private boolean isStatusOK(LogRestDAO logRestDAO) {
    return (
      logRestDAO.getStatusCode() >= 200 && logRestDAO.getStatusCode() < 300
    );
  }

  private boolean isValidUUID(String value) {
    if (value == null) return false;
    try {
      UUID.fromString(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
