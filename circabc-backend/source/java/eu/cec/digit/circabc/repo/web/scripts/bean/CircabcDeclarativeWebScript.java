package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.log.LogRestRecord;
import eu.cec.digit.circabc.service.log.LogService;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CircabcDeclarativeWebScript extends DeclarativeWebScript {

    static final Log logger = LogFactory.getLog(CircabcDeclarativeWebScript.class);
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    protected LogService logService;

    protected ApiToolBox apiToolBox;
    protected NodeService notSecuredNodeService;

    protected NodeRef nodeParent;
    protected String nodePath;
    protected long nodeID;

    protected void recordBeforeDelete(String deletedNodeId) {
        NodeRef deletedNodeRef = Converter.createNodeRefFromId(deletedNodeId);

        // put interest group in node parent
        nodeParent = apiToolBox.getCurrentInterestGroup(deletedNodeRef);
        if (nodeParent == null) {
            // if there is no interest group put paren of node
            nodeParent = notSecuredNodeService.getPrimaryParent(deletedNodeRef).getParentRef();
        }
        nodePath = apiToolBox.getCircabcPath(deletedNodeRef, true);
        nodeID = apiToolBox.getDatabaseID(deletedNodeRef);
    }

    @Override
    protected void executeFinallyImpl(
            WebScriptRequest req, Status status, Cache cache, Map<String, Object> model) {
        super.executeFinallyImpl(req, status, cache, model);

        String method = this.getDescription().getMethod();
        String url = req.getURL();
        String template = req.getServiceMatch().getTemplate();
        Map<String, Integer> templateVariableOrder = getTemplateVariableOrder(template);
        int statusCode = status.getCode();
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        String[] parameterNames = req.getParameterNames();
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

        StringBuilder info = new StringBuilder(128);
        // path parameters
        LogRestRecord logRestRecord = new LogRestRecord();

        int pathSize = templateVars.size();
        int querySize = parameterNames.length;
        boolean hasContent = (req.getContent() != null)
        && "application/json".equalsIgnoreCase(req.getContent().getMimetype());
        String payload = null;
        if (hasContent){
            try {
                payload = req.getContent().getContent();
            }catch (IOException e) {
                if (logger.isErrorEnabled()){
                    logger.error("Could not get content json", e);
                }
                hasContent=false;
            }
        }

        if ((pathSize > 0) || (querySize > 0) || hasContent) {
            info.append('{');
        }

        if (pathSize > 0) {
            processPath(templateVariableOrder, templateVars, info, logRestRecord, pathSize);
            if (querySize > 0 || hasContent) {
                info.append(',');
            }
        }
        //  query  parameters

        if (querySize > 0) {
            processQuery(req, parameterNames, info, querySize);
            if (hasContent) {
                info.append(',');
            }
        }
        if (hasContent) {
            info.append("\"payload\":");
            info.append(payload);
        }

        if ((pathSize > 0) || (querySize > 0) || hasContent) {
            info.append('}');
        }

        if (logger.isInfoEnabled()) {
            logger.info("user");
            logger.info(user);
            logger.info("method");
            logger.info(method);
            logger.info("template ");
            logger.info(template);
            logger.info("url");
            logger.info(url);
            logger.info("statusCode");
            logger.info(statusCode);
            logger.info("info");
            logger.info(info.toString());
        }

        logRestRecord.setDate(new Date());
        logRestRecord.setInfo(info.toString());
        logRestRecord.setMethod(method);
        logRestRecord.setStatusCode(statusCode);
        logRestRecord.setTemplate(template);
        logRestRecord.setUrl(url);
        logRestRecord.setUser(user);

        logRestRecord.setNodePath(nodePath);
        logRestRecord.setNodeID(nodeID);
        if (nodeParent != null) {
            logRestRecord.setNodeParent(nodeParent.toString());
        }

        this.logService.logRest(logRestRecord);
    }

    private void processQuery(WebScriptRequest req, String[] parameterNames, StringBuilder info, int querySize) {
        int stepQuery = 0;

        info.append("\"queryParameters\":{");

        for (String parameter : parameterNames) {
            stepQuery += 1;
            info.append('"');
            info.append(parameter);
            info.append('"');
            info.append(':');
            info.append('"');
            info.append(req.getParameter(parameter));
            info.append('"');

            info.append(LINE_SEPARATOR);
            if (stepQuery < querySize) {
                info.append(',');
            }
        }
        info.append('}');
    }

    private void processPath(Map<String, Integer> templateteVariableOrder, Map<String, String> templateVars,
            StringBuilder info, LogRestRecord logRestRecord, int pathSize) {
        info.append("\"pathParameters\":{");
        info.append(LINE_SEPARATOR);
        int stepPath = 0;

        for (Map.Entry<String, String> pathParameter : templateVars.entrySet()) {
            stepPath += 1;
            String key = pathParameter.getKey();
            info.append('"');
            info.append(key);
            info.append('"');
            info.append(':');
            String value = pathParameter.getValue();
            info.append('"');
            info.append(value);
            info.append('"');
            info.append(LINE_SEPARATOR);
            if (stepPath < pathSize) {
                info.append(',');
            }
            Integer order = templateteVariableOrder.get(key);
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
        info.append('}');

    }

    private Map<String, Integer> getTemplateVariableOrder(String template) {
        Matcher m = Pattern.compile("\\{(\\w*?)\\}").matcher(template);
        Map<String, Integer> result = new HashMap<>();
        int position = 0;
        while (m.find()) {
            position += 1;
            result.put(m.group(1), position);
        }
        return result;
    }

    /**
     * @return the logService
     */
    public LogService getLogService() {
        return this.logService;
    }

    /**
     * @param logService the logService to set
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    public NodeService getNotSecuredNodeService() {
        return notSecuredNodeService;
    }

    public void setNotSecuredNodeService(NodeService notSecuredNodeService) {
        this.notSecuredNodeService = notSecuredNodeService;
    }

    public ApiToolBox getApiToolBox() {
        return apiToolBox;
    }

    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }
}
