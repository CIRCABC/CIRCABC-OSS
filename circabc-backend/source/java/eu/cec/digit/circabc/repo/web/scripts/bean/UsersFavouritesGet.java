package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.FavouritesApi;
import io.swagger.model.PagedNodes;
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

/**
 * @author beaurpi
 */
public class UsersFavouritesGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(UsersFavouritesGet.class);

    private static final int START_PAGE = 0;
    private static final int DEFAULT_NUMBER_RESULTS = 25;
    private FavouritesApi favouritesApi;
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

        String page = req.getParameter("page");
        int nbPage = START_PAGE;
        if (page != null) {
            nbPage = ((Integer.parseInt(page) == 0) ? 0 : (Integer.parseInt(page) - 1));
        }

        String limit = req.getParameter("limit");
        int nbLimit = DEFAULT_NUMBER_RESULTS;
        if (limit != null) {
            nbLimit = Integer.parseInt(limit);
        }

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        try {
            String userId = templateVars.get("userId");

            if (!(this.authenticationService.getCurrentUserName().equals(userId)
                    || this.authenticationService.getCurrentUserName().equals("admin"))) {
                throw new AccessDeniedException("Cannot get user favourite of somebody else");
            }

            if (userId != null) {
                PagedNodes favs = this.favouritesApi.usersUserIdFavouritesGet(userId, nbPage, nbLimit);
                model.put("data", favs.getData());
                model.put("total", favs.getTotal());
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
