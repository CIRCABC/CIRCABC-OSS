package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class UsersGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(UsersGet.class);

    private UsersApi usersApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        String query = req.getParameter("query");
        String filterString = req.getParameter("filter");
        boolean filter = true;

        if (filterString != null && filterString.equalsIgnoreCase("false")) {
            filter = false;
        }

        try {

            if (query == null) {
                throw new InvalidArgumentException();
            } else if (query.isEmpty()) {
                throw new InvalidArgumentException();
            } else if (currentUserPermissionCheckerService.isGuest()) {
                throw new AccessDeniedException("Method not allowed for guest");
            } else if (!currentUserPermissionCheckerService.isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin()) {
                throw new AccessDeniedException("Method not allowed for not admin users");
            } else {
                model.put("users", this.usersApi.usersGet(query, filter));
            }

        } catch (InvalidArgumentException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Empty Query");
            status.setRedirect(true);
            return null;
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        }

        return model;
    }

    public UsersApi getUsersApi() {
        return this.usersApi;
    }

    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
