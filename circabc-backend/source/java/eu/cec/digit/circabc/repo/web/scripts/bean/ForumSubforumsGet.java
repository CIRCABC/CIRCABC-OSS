package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.ForumsApi;
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
import java.util.Objects;

/**
 * @author beaurpi
 */
public class ForumSubforumsGet extends DeclarativeWebScript {

    private ForumsApi forumsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        String language = req.getParameter("language");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        String sort = req.getParameter("sort");
        String order = req.getParameter("order");

        if (language == null) {
            MLPropertyInterceptor.setMLAware(true);
        } else {
            Locale locale = new Locale(language);
            I18NUtil.setContentLocale(locale);
            I18NUtil.setLocale(locale);
            MLPropertyInterceptor.setMLAware(false);
        }
        try {
            if (id != null) {
                model.put("id", id);

                if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)) {
                    throw new AccessDeniedException(
                            "Current Authority cannot access the forum, not enough permission");
                }

                if (!Objects.equals(sort, "") && !Objects.equals(order, "")) {
                    model.put("nodes", this.forumsApi.forumsIdSubforumsGet(id, sort + "_" + order));
                } else {
                    model.put("nodes", this.forumsApi.forumsIdSubforumsGet(id));
                }
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
     * @return the forumsApi
     */
    public ForumsApi getForumsApi() {
        return this.forumsApi;
    }

    /**
     * @param forumsApi the forumsApi to set
     */
    public void setForumsApi(ForumsApi forumsApi) {
        this.forumsApi = forumsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
