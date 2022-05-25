package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.DashboardApi;
import io.swagger.model.UserNewsFeed;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class UsersDashboardNewsFeedGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(UsersDashboardNewsFeedGet.class);

    private DashboardApi dashboardApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private AuthenticationService authenticationService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);
        String language = req.getParameter("language");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        if (language == null) {
            MLPropertyInterceptor.setMLAware(true);
        } else {
            Locale locale = new Locale(language);
            I18NUtil.setContentLocale(locale);
            I18NUtil.setLocale(locale);
            MLPropertyInterceptor.setMLAware(false);
        }

        String when = req.getParameter("when");
        if ((when == null) || Objects.equals(when, "")) {
            when = "today";
        }

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String userId = templateVars.get("userId");

        try {

            if (!(this.authenticationService.getCurrentUserName().equals(userId)
                    || this.authenticationService.getCurrentUserName().equals("admin"))) {
                throw new AccessDeniedException("Cannot get user feed dashboard of somebody else");
            }

            UserNewsFeed result = this.dashboardApi.usersUserIdDashboardNewsfeedGet(userId, when);
            model.put("feed", result);
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

    public DashboardApi getDashboardApi() {
        return this.dashboardApi;
    }

    public void setDashboardApi(DashboardApi dashboardApi) {
        this.dashboardApi = dashboardApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    public AuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}
