package eu.cec.digit.circabc.repo.log;

import java.util.UUID;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.log.ibatis.LogDaoService;
import eu.cec.digit.circabc.service.log.LogTransformService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;

public class LogTransformServiceImpl implements LogTransformService {

    private static final String PAYLOAD = "payload";
    private static final String TITLE = "title";
    private static final String PERMISSIONS = "permissions";
    private static final Log logger = LogFactory.getLog(LogTransformServiceImpl.class);

    @Override
    public LogRecordDAO transform(LogRestDAO logRestDAO) {
        LogRecordDAO dbLogRecord = new LogRecordDAO();

        long activityID = logDaoService.getActivityID(logRestDAO.getTemplateID());
        dbLogRecord.setActivityID((int) activityID);
        NodeRef nodeRef;
        if (isValidUUID(logRestDAO.getPathOneValue())) {
            nodeRef = apiToolBox.getNodeRef(logRestDAO.getPathOneValue());
        } else {
            String method = "POST";
            String template = "/circabc/repositories/{id}/transaction";
            long templateId = logDaoService.getTemplateID(method, template);
            if (logRestDAO.getTemplateID() == templateId) {
                nodeRef = getExternalRepoNodeRef(logRestDAO.getInfo());
            } else {
                // use circabc root node
                nodeRef = managementService.getCircabcNodeRef();
            }

        }

        if (nodeRef == null) {
            dbLogRecord.setDocumentID(logRestDAO.getNodeID());
            dbLogRecord.setPath(logRestDAO.getNodePath());
            if (logRestDAO.getNodeParent() != null) {
                setContainerData(dbLogRecord, new NodeRef(logRestDAO.getNodeParent()));
            } else {
                setContainerData(dbLogRecord, managementService.getCircabcNodeRef());
            }

        } else {
            long documentID = apiToolBox.getDatabaseID(nodeRef);
            dbLogRecord.setUuid(nodeRef.getId());
            dbLogRecord.setDocumentID(documentID);
            String path = apiToolBox.getCircabcPath(nodeRef, true);
            if (path.equals("")) {
                path = apiToolBox.getCircabcPathForArchivedNode(nodeRef, true);
            }
            dbLogRecord.setPath(path);

            setContainerData(dbLogRecord, nodeRef);
        }

        String info = logRestDAO.getInfo();

        try {
            info = processInfo(dbLogRecord.getActivityID(), logRestDAO);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Fail to process info: ", e);
            }

        }
        dbLogRecord.setInfo(info);

        dbLogRecord.setDate(logRestDAO.getLogDate());
        int isOK = 0;
        if (isStatusOK(logRestDAO)) {
            isOK = 1;
        }
        dbLogRecord.setIsOK(isOK);
        dbLogRecord.setUser(logRestDAO.getUserName());
        return dbLogRecord;
    }

    private NodeRef getExternalRepoNodeRef(String info) {
        JSONParser parser = new JSONParser();
        JSONObject json;
        try {
            json = (JSONObject) parser.parse(info);
        } catch (ParseException e) {
            logger.error("Error when parsing the JSON body.", e);
            return null;
        }
        JSONObject payload = (JSONObject) json.get(PAYLOAD);
        JSONArray nodes = (JSONArray) payload.get("nodes");
        if (!nodes.isEmpty()) {
            JSONObject node = (JSONObject) nodes.get(0);
            String id = (String) node.get("id");
            return Converter.createNodeRefFromId(id);
        } else {
            return null;
        }
    }

    private LogDaoService logDaoService;

    public void setLogDaoService(LogDaoService logDaoService) {
        this.logDaoService = logDaoService;
    }

    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    private ApiToolBox apiToolBox;
    private ManagementService managementService;

    private String processInfo(int activityID, LogRestDAO logRestDAO) {
        String result = "";
        String info = logRestDAO.getInfo();
        String nodePath = logRestDAO.getNodePath();
        String pathTwoValue = logRestDAO.getPathTwoValue();

        JSONParser parser = new JSONParser();
        JSONObject json;
        try {
            json = (JSONObject) parser.parse(info);
        } catch (ParseException e) {
            logger.error("Error when parsing the JSON body.", e);
            return info;
        }
        if (activityID == 9957105) {
            // Update Profile
            result = updateProfile(json, "Update profile %s new permissions: %s");

        } else if (activityID == 9957104) {
            // Remove Profile
            result = removeProfile(nodePath);

        } else if (activityID == 9957066) {
            // Add Profile
            result = updateProfile(json, "Add profile %s new permissions: %s");
        } else if (activityID == 9957063) {
            // Update Members
            StringBuilder resultBuilder = updateMembers(json, "Update Members: ");

            result = resultBuilder.toString();

        } else if (activityID == 9957062) {
            // Invite Users
            StringBuilder resultBuilder = updateMembers(json, "Invite Users: ");

            result = resultBuilder.toString();

        } else if (activityID == 9957005) {
            // Remove Member
            result = String.format("Remove member: %s", pathTwoValue);
        } else if (activityID == 9957065) {
            // Members Apply for Membership
            result = applyForMebership(json);

        } else if (activityID == 9957064) {
            // Members Update Application for Membership
            result = updateApplyForMebership(info, json);

        } else {

            result = info;
        }
        return result;
    }

    private String updateApplyForMebership(String info, JSONObject json) {
        String result;
        JSONObject payload = (JSONObject) json.get(PAYLOAD);
        String userName = (String) payload.get("username");
        String action = (String) payload.get("action");
        if (action.equals("decline")) {
            result = String.format("Membership declined for user : %s", userName);
        } else if (action.equals("submitNew")) {
            result = String.format("The user : %s applied for membership", userName);

        } else if (action.equals("clean")) {
            result = String.format("Membership approved for user : %s", userName);
        } else {
            result = info;
        }
        return result;
    }

    private String applyForMebership(JSONObject json) {
        String result;
        JSONObject payload = (JSONObject) json.get(PAYLOAD);
        String userName = (String) payload.get("username");
        result = String.format("The user : %s applied for membership", userName);
        return result;
    }

    private String removeProfile(String nodePath) {
        String result;
        String[] nodePathElement = nodePath.split("/");
        String profile = nodePathElement[nodePathElement.length - 1];
        result = String.format("Remove profile %s ", profile);
        return result;
    }

    private String updateProfile(JSONObject json, String s) {
        String result;
        JSONObject payload = (JSONObject) json.get(PAYLOAD);
        String profile = (String) ((JSONObject) payload.get(TITLE)).get("en");
        String permissions = ((JSONObject) payload.get(PERMISSIONS)).toJSONString();
        result = String.format(s, profile, permissions);
        return result;
    }

    private StringBuilder updateMembers(JSONObject json, String s) {
        StringBuilder resultBuilder = new StringBuilder(128);
        resultBuilder.append(s);
        JSONObject payload = (JSONObject) json.get(PAYLOAD);
        JSONArray memberships = (JSONArray) payload.get("memberships");
        for (Object membershipObject : memberships) {
            JSONObject membership = (JSONObject) membershipObject;
            JSONObject user = (JSONObject) membership.get("user");
            JSONObject profile = (JSONObject) membership.get("profile");
            String userId = (String) user.get("userId");
            String firstname = (String) user.get("firstname");
            String lastname = (String) user.get("lastname");
            String profileTitle = (String) ((JSONObject) profile.get(TITLE)).get("en");
            String permissions = ((JSONObject) profile.get(PERMISSIONS)).toJSONString();
            resultBuilder.append(String.format("user (%s) %s %s", userId, firstname, lastname));
            resultBuilder.append(String.format("has profile %s with permissions %s", profileTitle, permissions));
            resultBuilder.append("\n");
        }
        return resultBuilder;
    }

    private void setContainerData(LogRecordDAO dbLogRecord, NodeRef nodeRef) {
        if (apiToolBox.getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_ROOT)
                || apiToolBox.getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
            long categoryGroupID = apiToolBox.getDatabaseID(nodeRef);
            dbLogRecord.setIgID(categoryGroupID);
            String categoryName = apiToolBox.getName(nodeRef);
            dbLogRecord.setIgName(categoryName);
        } else {
            NodeRef currentInterestGroup = apiToolBox.getCurrentInterestGroup(nodeRef);
            if (currentInterestGroup == null) {
                currentInterestGroup = apiToolBox.getInterestGroupForArchivedNode(nodeRef);
            }
            long interestGroupID = apiToolBox.getDatabaseID(currentInterestGroup);
            dbLogRecord.setIgID(interestGroupID);
            String interestGroupName = apiToolBox.getName(currentInterestGroup);
            dbLogRecord.setIgName(interestGroupName);
        }
    }

    private boolean isStatusOK(LogRestDAO logRestDAO) {
        return logRestDAO.getStatusCode() >= 200 || logRestDAO.getStatusCode() < 300;
    }

    private boolean isValidUUID(String value) {
        if (value == null) {
            return false;
        }
        try {
            UUID uuid = UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ignore) {
            return false;
        }
    }

}
