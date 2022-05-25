package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.AppMessageApi;
import io.swagger.model.PagedEmails;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author beaurpi
 */
public class AppDistributionMailsGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(AppDistributionMailsGet.class);

    private AppMessageApi appMessageApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        String pageStr = req.getParameter("page");
        int page = 0;
        try {
            page = (((pageStr == null) || pageStr.isEmpty()) ? 1 : Integer.parseInt(pageStr));
        } catch (NumberFormatException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Wrong numeric value for 'page': " + page, e);
            }
        }

        String limitStr = req.getParameter("limit");
        int limit = 0;
        try {
            limit = (((limitStr == null) || limitStr.isEmpty()) ? 25 : Integer.parseInt(limitStr));
        } catch (NumberFormatException e) {
            if (logger.isErrorEnabled()) {
                throw new IllegalArgumentException("Wrong numeric value for 'limit': " + limit, e);
            }
        }

        String query = req.getParameter("search");

        try {

            if (!(currentUserPermissionCheckerService.isAlfrescoAdmin()
                    || currentUserPermissionCheckerService.isCircabcAdmin())) {
                throw new AccessDeniedException("");
            }
            PagedEmails emails=  appMessageApi.getAppDistributionEmails(page, limit, query);
            
            model.put("emails", emails);
            model.put("total", emails.getTotal());
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        }

        return model;
    }

    /**
     * @return the appMessageApi
     */
    public AppMessageApi getAppMessageApi() {
        return appMessageApi;
    }

    /**
     * @param appMessageApi the appMessageApi to set
     */
    public void setAppMessageApi(AppMessageApi appMessageApi) {
        this.appMessageApi = appMessageApi;
    }

    /**
     * @return the currentUserPermissionCheckerService
     */
    public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
        return currentUserPermissionCheckerService;
    }

    /**
     * @param currentUserPermissionCheckerService the currentUserPermissionCheckerService to set
     */
    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
