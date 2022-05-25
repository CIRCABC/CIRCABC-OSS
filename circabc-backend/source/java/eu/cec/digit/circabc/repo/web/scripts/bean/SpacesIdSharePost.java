package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import io.swagger.api.SpacesApi;
import io.swagger.model.Share;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript entry to add a new share for the given space
 *
 * @author schwerr
 */
public class SpacesIdSharePost extends CircabcDeclarativeWebScript {

    private SpacesApi spacesApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    private static Share getShare(String shareBody) throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(shareBody);

        String igId = (String) json.get("igId");
        String permission = (String) json.get("permission");

        return new Share(igId, null, permission);
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String spaceId = templateVars.get("id");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {

            if (!this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
                    spaceId, LibraryPermissions.LIBMANAGEOWN)) {
                throw new AccessDeniedException("Cannot post the share space, not enough permissions");
            }

            MLPropertyInterceptor.setMLAware(false);

            boolean notifyLeaders = "true".equals(req.getParameter("notifyLeaders"));

            String shareBody = req.getContent().getContent();

            Share share = getShare(shareBody);

            this.spacesApi.addShare(spaceId, share, notifyLeaders);

            model.put("message", "ok");
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (Exception e) {
            status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
            status.setMessage(e.getMessage());
            status.setException(e);
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
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
