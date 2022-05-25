package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.UsersApi;
import io.swagger.model.IGData;
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

public class BulkInviteIGsGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(BulkInviteIGsGet.class);

    private UsersApi usersApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        String categoryId = req.getParameter("categoryId");

        if ((categoryId == null) || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("'categoryId' cannot be empty.");
        }

        String currentIgId = req.getParameter("igId");

        if ((currentIgId == null) || currentIgId.trim().isEmpty()) {
            throw new IllegalArgumentException("'igId' cannot be empty.");
        }

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {
            if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(categoryId)) {
                throw new AccessDeniedException("No access on node: " + categoryId);
            }
            if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(currentIgId)) {
                throw new AccessDeniedException("No access on node: " + currentIgId);
            }

            List<IGData> igs = this.usersApi.getBulkInviteIGs(categoryId, currentIgId);

            model.put("igs", igs);
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
     * @param usersApi the usersApi to set
     */
    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
