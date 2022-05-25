package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.ContentApi;
import io.swagger.model.OfficeEditResult;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
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
import java.util.List;
import java.util.Map;

public class CheckEditInOfficeGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(CheckEditInOfficeGet.class);

    private ContentApi contentApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {
            MLPropertyInterceptor.setMLAware(false);

            String[] nodeIds = req.getParameterValues("nodeIds");

            for (String nodeId : nodeIds) {
                if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(nodeId)) {
                    throw new AccessDeniedException(
                            "Cannot check Office edition, not enough permissions. Node: " + nodeId);
                }
            }

            List<OfficeEditResult> results = this.contentApi.checkEditInOffice(nodeIds);

            model.put("results", results);
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
     * @param contentApi the contentApi to set
     */
    public void setContentApi(ContentApi contentApi) {
        this.contentApi = contentApi;
    }

    /**
     * @param currentUserPermissionCheckerService the currentUserPermissionCheckerService to set
     */
    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
