package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.HistoryApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript entry to create a revocation job of user memberships
 *
 * @author beaurpi
 */
public class HistoryMembershipsLogsDelete extends DeclarativeWebScript {

    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private HistoryApi historyApi;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String groupId = templateVars.get("groupId");
        String userId = templateVars.get("userId");

        try {

            if (!(this.currentUserPermissionCheckerService.isAlfrescoAdmin()
                    || this.currentUserPermissionCheckerService.isCircabcAdmin()
                    || this.currentUserPermissionCheckerService.isGroupAdmin(groupId))) {
                throw new AccessDeniedException(
                        "Cannot clean the logs of the membership, not enough permissions");
            }

            historyApi.cleanMembershipsLogs(groupId, userId);

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (Exception e) {
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage(e.getMessage());
            status.setException(e);
            status.setRedirect(true);
            return null;
        }
        return model;
    }

    /**
     * @param currentUserPermissionCheckerService the currentUserPermissionCheckerService to set
     */
    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    /**
     * @param historyApi the historyApi to set
     */
    public void setHistoryApi(HistoryApi historyApi) {
        this.historyApi = historyApi;
    }
}
