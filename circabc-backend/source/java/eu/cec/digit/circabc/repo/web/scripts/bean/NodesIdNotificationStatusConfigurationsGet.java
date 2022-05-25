package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import io.swagger.api.NotificationsApi;
import io.swagger.model.PagedNotificationConfigurations;
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

public class NodesIdNotificationStatusConfigurationsGet extends DeclarativeWebScript {

    static final Log logger = LogFactory.getLog(NodesIdNotificationStatusConfigurationsGet.class);

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

            MLPropertyInterceptor.setMLAware(false);

            String pageStr = req.getParameter("page");
            int page = 0;
            page = getPage(pageStr, page);

            String limitStr = req.getParameter("limit");
            int limit = 0;
            limit = getLimit(limitStr, limit);

            if (page <= 0) {
                throw new IllegalArgumentException("Values for 'page' must be > 0");
            }

            if (limit < 0) {
                throw new IllegalArgumentException("Values for 'limit' must be >= 0");
            }

            String language = req.getParameter("language");
            if ((language == null) || language.isEmpty()) {
                language = "en-US";
            }

            MLPropertyInterceptor.setMLAware(false);

            int startRecord = (page - 1) * limit;

            PagedNotificationConfigurations notifications =
                    this.notificationsApi.getNotifications(id, startRecord, limit, language);

            model.put("data", notifications.getData());
            model.put("total", notifications.getTotal());
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

    private int getLimit(String limitStr, int limit) {
        if ((limitStr != null) && !limitStr.isEmpty()) {
            try {
                limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Wrong numeric value for 'limit': " + limit, e);
            }
        }
        return limit;
    }

    private int getPage(String pageStr, int page) {
        try {
            page = (((pageStr == null) || pageStr.isEmpty()) ? 1 : Integer.parseInt(pageStr));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong numeric value for 'page': " + page, e);
        }
        return page;
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
