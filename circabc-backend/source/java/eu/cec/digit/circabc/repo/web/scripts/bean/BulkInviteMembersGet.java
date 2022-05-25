package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.user.BulkImportUserData;
import io.swagger.api.UsersApi;
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
import java.util.*;

public class BulkInviteMembersGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(BulkInviteMembersGet.class);

    private UsersApi usersApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        String igIds = req.getParameter("igIds");

        if ((igIds == null) || igIds.trim().isEmpty()) {
            throw new IllegalArgumentException("'igIds' cannot be empty.");
        }

        String destinationIGId = req.getParameter("destinationIGId");

        if ((destinationIGId == null) || destinationIGId.trim().isEmpty()) {
            throw new IllegalArgumentException("'destinationIGId' cannot be empty.");
        }

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {

            List<String> igs = new ArrayList<>();

            StringTokenizer tokenizer = new StringTokenizer(igIds, ",");

            while (tokenizer.hasMoreTokens()) {

                String nodeId = tokenizer.nextToken().trim();

                if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(nodeId)) {
                    throw new AccessDeniedException("No access on node:" + nodeId);
                }

                igs.add(nodeId);
            }

            List<BulkImportUserData> members = this.usersApi.getBulkInviteMembers(igs, destinationIGId);

            model.put("members", members);
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
