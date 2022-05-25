package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import io.swagger.api.ContentApi;
import io.swagger.model.MultilingualAspectMetadata;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.MultilingualJsonParser;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ContentMultilingualAspectPost extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(ContentMultilingualAspectPost.class);

    private ContentApi contentApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        try {
            if (!this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
                    id,
                    LibraryPermissions.LIBMANAGEOWN,
                    LibraryPermissions.LIBEDITONLY,
                    LibraryPermissions.LIBFULLEDIT,
                    LibraryPermissions.LIBADMIN)) {
                throw new AccessDeniedException("Cannot update content, not enough permissions");
            }
            MultilingualAspectMetadata body = MultilingualJsonParser.parseAspectMetadata(req);
            this.contentApi.contentIdMultilingualAspectPost(id, body);

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException | ParseException | IOException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
        }
        return model;
    }

    /**
     * @return the contentApi
     */
    public ContentApi getContentApi() {
        return this.contentApi;
    }

    /**
     * @param contentApi the contentApi to set
     */
    public void setContentApi(ContentApi contentApi) {
        this.contentApi = contentApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
