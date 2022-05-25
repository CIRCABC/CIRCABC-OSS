package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.GroupsApi;
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

public class ImportZipFilePost extends CircabcDeclarativeWebScript {

    static final Log logger = LogFactory.getLog(ImportZipFilePost.class);
    private GroupsApi groupsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String folderId = templateVars.get("folderId");

        try {

            if (!this.currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(folderId)) {
                throw new AccessDeniedException("Cannot import ZIP, not enough permissions");
            }

            //FIX by ALMO - DIGITCIRCABC-4834 Bulk Import
            boolean notifyUser = "true".equals(req.getParameter("notifyUser"));
            boolean deleteFile = "true".equals(req.getParameter("deleteFile"));
            boolean disableNotification = "true".equals(req.getParameter("disableNotification"));
            
            String encoding = req.getParameter("encoding");
            if (!("UTF-8".equalsIgnoreCase(encoding) || "CP437".equalsIgnoreCase(encoding))) {
                throw new IllegalArgumentException("The 'encoding' must be UTF-8 or CP437");
            }

            String mimeType = null;
            String fileName = null;

            InputStream fileInputStream = null;

            FormData form = (FormData) req.parseContent();

            if ((form == null) || !form.getIsMultiPart()) {
                throw new IllegalArgumentException("Not a multipart request.");
            }

            //ALMO - it seems that there is only one form field, the field with type File.
            //It's the reason why I extracted the parameters notifyUser, deleteFile, disableNotification and encoding from the WebScriptRequest
            for (FormData.FormField field : form.getFields()) {

                if (field.getIsFile()) {
                    mimeType = field.getMimetype();
                    fileName = field.getFilename();
                    fileInputStream = field.getInputStream();
                }
            }

            this.groupsApi.importZipFile(
                    folderId,
                    fileInputStream,
                    fileName,
                    mimeType,
                    notifyUser,
                    deleteFile,
                    disableNotification,
                    encoding);

            model.put("message", "ok");

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
     * @param groupsApi the groupsApi to set
     */
    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}