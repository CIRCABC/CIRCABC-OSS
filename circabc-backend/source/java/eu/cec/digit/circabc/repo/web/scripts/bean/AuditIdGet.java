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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuditIdGet extends DeclarativeWebScript {

    private LogService logService;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {

            MLPropertyInterceptor.setMLAware(false);

            if (!this.currentUserPermissionCheckerService.isCircabcAdmin()
                    && !this.currentUserPermissionCheckerService.isCategoryAdmin(id)
                    && !this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
                throw new AccessDeniedException("Access denied:" + id);
            }

            List<LogSearchResultDAO> results;
            NodeRef nodeRef = Converter.createNodeRefFromId(id);

            if (!this.nodeService.exists(nodeRef)) {
                throw new IllegalArgumentException("The item with id '" + id + "' could not be found.");
            }

            long igID = (Long) this.nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);

            String user = req.getParameter("userId").isEmpty() ? null : req.getParameter("userId");
            String service = req.getParameter("service").isEmpty() ? null : req.getParameter("service");
            String method = req.getParameter("activity").isEmpty() ? null : req.getParameter("activity");
            Date fromDate = Converter.convertStringToDate(req.getParameter("from"));
            Date toDate = Converter.convertStringToDate(req.getParameter("to"));

            results = this.logService.search(igID, user, service, method, fromDate, toDate);
            model.put("logResults", results);
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
     * @param logService the logService to set
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
