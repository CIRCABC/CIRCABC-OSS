package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import io.swagger.api.TopicsApi;
import io.swagger.model.PagedNodes;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
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
public class TopicRepliesGet extends DeclarativeWebScript {

    private static final int START_PAGE = 0;

    private static final int DEFAULT_NUMBER_RESULTS = 25;

    private TopicsApi topicsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

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

        int nbPage = START_PAGE;
        String page = req.getParameter("page");
        if (page != null) {
            nbPage = ((Integer.parseInt(page) == 0) ? 0 : (Integer.parseInt(page) - 1));
        }

        String limit = req.getParameter("limit");
        int nbLimit = DEFAULT_NUMBER_RESULTS;
        if (limit != null) {
            nbLimit = Integer.parseInt(limit);
        }

        String sort = req.getParameter("order");

        try {

            if (!(this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
                    id, NewsGroupPermissions.NWSACCESS)
                    || this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
                    id, LibraryPermissions.LIBACCESS))) {
                throw new AccessDeniedException(
                        "Cannot get the replies of the topic, not enough permissions");
            }

            if (id != null) {
                PagedNodes nodes = this.topicsApi.getTopicReplies(id, nbPage, nbLimit, sort);
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
     * @return the topicsApi
     */
    public TopicsApi getTopicsApi() {
        return this.topicsApi;
    }

    /**
     * @param topicsApi the topicsApi to set
     */
    public void setTopicsApi(TopicsApi topicsApi) {
        this.topicsApi = topicsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
