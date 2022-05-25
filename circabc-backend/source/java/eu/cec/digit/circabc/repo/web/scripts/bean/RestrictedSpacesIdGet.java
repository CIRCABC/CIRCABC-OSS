package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.SpacesApi;
import io.swagger.model.PagedNodes;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
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

public class RestrictedSpacesIdGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(RestrictedSpacesIdGet.class);

    private static final int START_PAGE = 0;
    private static final int DEFAULT_NUMBER_RESULTS = 25;
    private SpacesApi spacesApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

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

        String sort = req.getParameter("order");

        String folderOnlyParam = req.getParameter("folderOnly");
        boolean folderOnly = "true".equals(folderOnlyParam);

        String fileOnlyParam = req.getParameter("fileOnly");
        boolean fileOnly = "true".equals(fileOnlyParam);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        try {

            if (((page == null) || Objects.equals(page, "-1") || Objects.equals(page, ""))
                    && ((limit == null) || Objects.equals(limit, "-1") || Objects.equals(limit, ""))) {
                PagedNodes nodes =
                        this.spacesApi.restrictedSpaceGetChildren(id, -1, -1, sort, folderOnly, fileOnly);
                model.put("data", nodes.getData());
                model.put("total", nodes.getTotal());
            } else {
                PagedNodes nodes =
                        this.spacesApi.restrictedSpaceGetChildren(
                                id, nbPage, nbLimit, sort, folderOnly, fileOnly);
                model.put("data", nodes.getData());
                model.put("total", nodes.getTotal());
            }

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
     * @return the spacesApi
     */
    public SpacesApi getSpacesApi() {
        return this.spacesApi;
    }

    /**
     * @param spacesApi the spacesApi to set
     */
    public void setSpacesApi(SpacesApi spacesApi) {
        this.spacesApi = spacesApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
