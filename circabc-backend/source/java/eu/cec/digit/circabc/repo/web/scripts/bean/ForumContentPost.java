package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import io.swagger.api.ForumsApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NodeJsonParser;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author beaurpi
 */
public class ForumContentPost extends CircabcDeclarativeWebScript {

    private ForumsApi forumsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {
            if (id != null) {
                Node body = NodeJsonParser.parseSimpleJSON(req);

                if (!this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
                        id, NewsGroupPermissions.NWSPOST)) {
                    throw new AccessDeniedException(
                            "Current Authority cannot create a topic in the forum, not enough permission");
                }

                model.put("post", this.forumsApi.forumsIdContentPost(id, body));
            }
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException | ParseException | IOException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
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
