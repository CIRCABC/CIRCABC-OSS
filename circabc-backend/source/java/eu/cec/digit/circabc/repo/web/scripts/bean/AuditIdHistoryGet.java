package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.repo.log.LogSearchResultDAO;
import eu.cec.digit.circabc.service.log.LogService;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Webscript entry to get the history of an item (document, event. etc.) given its id
 *
 * @author schwerr
 */
public class AuditIdHistoryGet extends DeclarativeWebScript {

    private LogService logService;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        String pageStr = req.getParameter("page");
        long page = 0;
        try {
            page = (((pageStr == null) || pageStr.isEmpty()) ? 1 : Long.parseLong(pageStr));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong numeric value for 'page': " + page, e);
        }

        String limitStr = req.getParameter("limit");
        long limit = 0;
        try {
            limit = (((limitStr == null) || limitStr.isEmpty()) ? 25 : Long.parseLong(limitStr));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong numeric value for 'limit': " + limit, e);
        }

        if (page <= 0) {
            throw new IllegalArgumentException("Values for 'page' must be > 0");
        }

        if (limit < 0) {
            throw new IllegalArgumentException("Values for 'limit' must be >= 0");
        }

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {

            MLPropertyInterceptor.setMLAware(false);

            NodeRef nodeRef = Converter.createNodeRefFromId(id);

            if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)) {
                throw new AccessDeniedException("No access on node:" + id);
            }

            if (!this.nodeService.exists(nodeRef)) {
                throw new IllegalArgumentException("The item with id '" + id + "' could not be found.");
            }

            Long documentId = (Long) this.nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);

            List<LogSearchResultDAO> results;
            long historyCount;

            if (limit == 0) {

                results = this.logService.getHistory(documentId,id);

                historyCount = results.size();
            } else {
                historyCount = this.logService.countHistory(documentId,id);

                long startRecord = (page - 1) * limit;

                results = this.logService.getHistory(documentId, id, startRecord, limit);
            }

            model.put("logResults", results);
            model.put("total", historyCount);
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (Exception e) {
            status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
            status.setMessage(e.getMessage());
            status.setException(e);
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param logService the logService to set
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
