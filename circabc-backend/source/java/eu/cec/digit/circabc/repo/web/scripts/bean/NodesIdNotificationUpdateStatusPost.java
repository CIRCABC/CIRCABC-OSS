package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.notification.NotificationStatus;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import io.swagger.api.NotificationsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class NodesIdNotificationUpdateStatusPost extends CircabcDeclarativeWebScript {

    static final Log logger = LogFactory.getLog(NodesIdNotificationUpdateStatusPost.class);

    private NotificationsApi notificationsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {

            if (!(this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
                    id, LibraryPermissions.LIBADMIN)
                    || this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
                    id, NewsGroupPermissions.NWSADMIN)
                    || this.currentUserPermissionCheckerService.isGroupAdmin(id))) {
                throw new AccessDeniedException(
                        "Impossible to remove notification configuration, not enough permission");
            }

            String authority = req.getParameter("authority");

            String notificationStatus = req.getParameter("status");
            NotificationStatus mappedNotificationStatus = NotificationStatus.UNSUBSCRIBED;
            if ("true".equalsIgnoreCase(notificationStatus)
                    || "subscribed".equalsIgnoreCase(notificationStatus)) {
                mappedNotificationStatus = NotificationStatus.SUBSCRIBED;
            }

            MLPropertyInterceptor.setMLAware(false);

            this.notificationsApi.setNotificationStatus(id, authority, mappedNotificationStatus);
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
