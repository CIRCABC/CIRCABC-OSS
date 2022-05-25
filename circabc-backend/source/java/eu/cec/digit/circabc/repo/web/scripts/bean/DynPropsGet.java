package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class DynPropsGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(DynPropsGet.class);

    private DynamicPropertiesApi dynamicPropertiesApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {
            if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)) {
                throw new AccessDeniedException("Cannot access dynamic property");
            }
            model.put("dp", this.dynamicPropertiesApi.dynpropsIdGet(id));
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
        }

        return model;
    }

    /**
     * @return the dynamicPropertiesApi
     */
    public DynamicPropertiesApi getDynamicPropertiesApi() {
        return this.dynamicPropertiesApi;
    }

    /**
     * @param dynamicPropertiesApi the dynamicPropertiesApi to set
     */
    public void setDynamicPropertiesApi(DynamicPropertiesApi dynamicPropertiesApi) {
        this.dynamicPropertiesApi = dynamicPropertiesApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
