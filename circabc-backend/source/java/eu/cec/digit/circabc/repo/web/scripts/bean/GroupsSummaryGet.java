package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.repo.log.ActivityCountDAO;
import io.swagger.api.GroupsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsSummaryGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupsSummaryGet.class);

    private GroupsApi groupsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        String servicePath = req.getServicePath();

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

        try {
            MLPropertyInterceptor.setMLAware(true);

            String id = templateVars.get("id");
            boolean calculate = "true".equals(req.getParameter("calculate"));

            if (!this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
                throw new AccessDeniedException("Not enough rights for accessing group statistics");
            }

            if (servicePath.endsWith("statistics")) {
                model.put("statistics", this.groupsApi.getIGSummaryStatistics(id, calculate, false));
            }
            if (servicePath.endsWith("timeline")) {
                List<ActivityCountDAO> list = this.groupsApi.getIGSummaryTimeline(id);
                model.put("timeline", list);
            }
            if (servicePath.endsWith("structure")) {
                model.put("structure", this.groupsApi.getIGSummaryStructure(id));
            }
        } catch (InvalidNodeRefException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    /**
     * @param groupsApi the groupsApi to set
     */
    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
