package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.SpacesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class SpacesIdCreateSharePost extends CircabcDeclarativeWebScript {

    private SpacesApi spacesApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String spaceId = templateVars.get("id");
        boolean mlAware = MLPropertyInterceptor.isMLAware();
        String parentId = req.getParameter("parentId");

        try {

            if (!this.currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(parentId)) {
                throw new AccessDeniedException("Cannot add the shared space, not enough permissions");
            }

            MLPropertyInterceptor.setMLAware(false);

            String title = req.getParameter("title");
            String description = req.getParameter("description");

            this.spacesApi.createSharedSpaceLink(spaceId, parentId, title, description);

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
