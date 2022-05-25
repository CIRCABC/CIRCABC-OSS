package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.repo.log.LogActivityDAO;
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

public class AuditActivitiesGet extends DeclarativeWebScript {

    private LogService logService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private NodeService nodeService;

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

            NodeRef nodeRef = Converter.createNodeRefFromId(id);

            if (!this.nodeService.exists(nodeRef)) {
                throw new IllegalArgumentException("The item with id '" + id + "' could not be found.");
            }

            long igID = (Long) this.nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);


            List<LogActivityDAO> activities = this.logService.getActivitiesById(igID);

            model.put("activities", activities);
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
