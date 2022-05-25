package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.AresBridgeApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class ExternalRepositoryGet extends CircabcDeclarativeWebScript {

    private AresBridgeApi aresBridgeApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        if (currentUserPermissionCheckerService.isGuest()) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        }

        try {
            model.put("repos", this.aresBridgeApi.getAvailableExternalRepositories());
        } catch (Exception e) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
        }

        return model;
    }

    public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
        return currentUserPermissionCheckerService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    public AresBridgeApi getAresBridgeApi() {
        return aresBridgeApi;
    }

    public void setAresBridgeApi(AresBridgeApi aresBridgeApi) {
        this.aresBridgeApi = aresBridgeApi;
    }
}
