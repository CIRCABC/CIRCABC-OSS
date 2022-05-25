package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import io.swagger.api.ForumsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class PostAbuseSignalPost extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(PostAbuseSignalPost.class);

    private ForumsApi forumsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        try {
            MLPropertyInterceptor.setMLAware(true);

            String id = templateVars.get("id");

            if (!this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
                    id, NewsGroupPermissions.NWSACCESS, NewsGroupPermissions.NWSPOST)) {
                throw new AccessDeniedException(
                        "cannot report an abuse on the post, not enough permissions");
            }

            String abuseText = req.getParameter("abuseText");

            this.forumsApi.signalAbuse(id, (abuseText == null) ? "" : abuseText);

            model.put("message", "ok");

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidTypeException ite) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad noderef type");
            status.setRedirect(true);
            return null;
        } catch (Exception inre) {
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
