package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.FavouritesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author beaurpi
 */
public class UsersFavouritesDelete extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(UsersFavouritesDelete.class);

    private FavouritesApi favouritesApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private AuthenticationService authenticationService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        try {
            String userId = templateVars.get("userId");

            if (!(this.authenticationService.getCurrentUserName().equals(userId)
                    || this.authenticationService.getCurrentUserName().equals("admin"))) {
                throw new AccessDeniedException("Cannot delete user favourite of somebody else");
            }

            String nodeId = templateVars.get("nodeId");
            if ((userId != null) && (nodeId != null)) {
                this.favouritesApi.usersUserIdFavouritesNodeIdDelete(userId, nodeId);
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
        }

        return model;
    }

    /**
     * @return the favouritesApi
     */
    public FavouritesApi getFavouritesApi() {
        return this.favouritesApi;
    }

    /**
     * @param favouritesApi the favouritesApi to set
     */
    public void setFavouritesApi(FavouritesApi favouritesApi) {
        this.favouritesApi = favouritesApi;
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
