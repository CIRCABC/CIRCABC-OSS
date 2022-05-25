package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.AutoUploadApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class AutoUploadDelete extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(AutoUploadDelete.class);

    private AutoUploadApi autoUploadApi;

    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        MLPropertyInterceptor.setMLAware(false);

        try {
            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String igId = templateVars.get("id");

            if (!currentUserPermissionCheckerService.isGroupAdmin(igId)) {
                throw new AccessDeniedException("No access on IG: " + igId);
            }

            String configurationIdStr = req.getParameter("configurationId");

            int configurationId;
            configurationId = getConfigurationId(configurationIdStr);

            this.autoUploadApi.removeAutoUploadEntry(configurationId);

            model.put("result", 1);
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

    private int getConfigurationId(String configurationIdStr) {
        int configurationId;
        try {
            configurationId = Integer.parseInt(configurationIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Wrong numeric value for 'configurationId': " + configurationIdStr, e);
        }
        return configurationId;
    }

    /**
     * @param autoUploadApi the autoUploadApi to set
     */
    public void setAutoUploadApi(AutoUploadApi autoUploadApi) {
        this.autoUploadApi = autoUploadApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
