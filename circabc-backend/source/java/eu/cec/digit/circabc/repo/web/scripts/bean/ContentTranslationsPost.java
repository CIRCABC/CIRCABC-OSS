package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import io.swagger.api.ContentApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ContentTranslationsPost extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(ContentTranslationsPost.class);

    private ContentApi contentApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        try {
            if (!currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
                    id, LibraryPermissions.LIBMANAGEOWN)) {
                throw new AccessDeniedException("Cannot update content, not enough permissions");
            }
            String lang = "";
            String mimeType = "";
            String fileName = "translation";
            InputStream file = null;
            final FormData form = (FormData) req.parseContent();

            if (form == null || !form.getIsMultiPart()) {
                throw new IllegalArgumentException("Not a multipart request.");
            }

            for (FormData.FormField field : form.getFields()) {

                if (field.getIsFile()) {
                    mimeType = field.getMimetype();
                    fileName = field.getFilename();
                    file = field.getInputStream();
                }

                if (field.getName().equals("lang")) {
                    lang = field.getValue();
                }
            }
            if (lang == null || lang.isEmpty() || lang.equals("null")) {
                throw new IllegalArgumentException("Language is not present.");
            }
            model.put(
                    "translationSet",
                    contentApi.contentIdTranslationsPost(id, lang, file, mimeType, fileName));
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException | InvalidAspectException inre) {
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
        return contentApi;
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
