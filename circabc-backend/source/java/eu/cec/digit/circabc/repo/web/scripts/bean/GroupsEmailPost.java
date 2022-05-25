package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import io.swagger.api.EmailApi;
import io.swagger.model.EmailDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.EmailJsonParser;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript entry to send an email
 *
 * @author beaurpi
 */
public class GroupsEmailPost extends CircabcDeclarativeWebScript {

    private EmailApi emailApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String igId = templateVars.get("igId");

        try {
            if (!this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
                    igId, DirectoryPermissions.DIRACCESS)) {
                throw new AccessDeniedException(
                        "Cannot contact users because user does not have enough permissions");
            }
            EmailDefinition body = EmailJsonParser.parse(req);
            this.emailApi.groupsIdEmailPost(igId, body);
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
        }

        return model;
    }

    /**
     * @return the emailApi
     */
    public EmailApi getEmailApi() {
        return this.emailApi;
    }

    /**
     * @param emailApi the emailApi to set
     */
    public void setEmailApi(EmailApi emailApi) {
        this.emailApi = emailApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
