package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.HeadersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class HeaderDelete extends CircabcDeclarativeWebScript {

    private HeadersApi headerApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {

            if (!this.currentUserPermissionCheckerService.isCircabcAdmin()
                    && !this.currentUserPermissionCheckerService.isAlfrescoAdmin()) {
                throw new AccessDeniedException("Not enough rights for deleting a header");
            }

            this.headerApi.deleteHeader(id);
            Map<String, Object> model = new HashMap<>(7, 1.0f);
            model.put("id", id);
            return model;
        } catch (IllegalArgumentException iae) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Header is not empty");
            status.setRedirect(true);
            return null;
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Invalid id");
            status.setRedirect(true);
            return null;
        }
    }

    public HeadersApi getHeaderApi() {
        return this.headerApi;
    }

    public void setHeaderApi(HeadersApi headerApi) {
        this.headerApi = headerApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
