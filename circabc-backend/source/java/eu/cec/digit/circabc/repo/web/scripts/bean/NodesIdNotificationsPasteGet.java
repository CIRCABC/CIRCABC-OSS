package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.NotificationsApi;
import io.swagger.model.PasteNotificationsState;
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
import java.util.Map;

public class NodesIdNotificationsPasteGet extends DeclarativeWebScript {

    static final Log logger = LogFactory.getLog(NodesIdNotificationsPasteGet.class);

    private NotificationsApi notificationsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {

            if (!(this.currentUserPermissionCheckerService.isGroupAdmin(id))) {
                throw new AccessDeniedException(
                        "Impossible to get the notification configuration, not enough permission");
            }

            MLPropertyInterceptor.setMLAware(false);

            PasteNotificationsState state = this.notificationsApi.getPasteNotificationsState(id);

            model.put("pasteEnabled", state.isPasteEnabled());
            model.put("pasteAllEnabled", state.isPasteAllEnabled());
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    /**
     * @param notificationsApi the notificationsApi to set
     */
    public void setNotificationsApi(NotificationsApi notificationsApi) {
        this.notificationsApi = notificationsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
