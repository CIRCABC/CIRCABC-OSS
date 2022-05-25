package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Updates the content of a working copy that has already been checked out without checking it in
 *
 * @author schwerr
 */
public class ContentIdUpdate extends CircabcDeclarativeWebScript {

    private NodeService nodeService;
    private CociContentBusinessSrv cociContentBusinessSrv;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        NodeRef workingCopyRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);

        if (!this.nodeService.exists(workingCopyRef)) {
            throw new IllegalArgumentException("The item with id '" + id + "' could not be found.");
        }

        if (!this.nodeService.hasAspect(workingCopyRef, ContentModel.ASPECT_WORKING_COPY)) {
            throw new IllegalArgumentException("The item with id '" + id + "' is not a working copy.");
        }

        try {

            if (!this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
                    id, LibraryPermissions.LIBMANAGEOWN)) {
                throw new AccessDeniedException("Cannot update content, not enough permissions");
            }

            if (!this.currentUserPermissionCheckerService.isWorkingCopyOwner(
                    workingCopyRef.getId())) {
                throw new AccessDeniedException("Cannot update content, not enough permissions");                
            }

            MLPropertyInterceptor.setMLAware(false);

            FormData form = (FormData) req.parseContent();

            if ((form == null) || !form.getIsMultiPart()) {
                throw new IllegalArgumentException("Not a multipart request.");
            }

            // Find the File Upload file, and process the contents
            boolean processed = false;

            for (FormData.FormField field : form.getFields()) {
                if (field.getIsFile()) {
                    String mimeType = field.getMimetype();
                    try (InputStream inputStream = field.getInputStream()) {
                        this.cociContentBusinessSrv.update(workingCopyRef, inputStream, mimeType);
                    }
                    processed = true;
                    break;
                }
            }

            if (!processed) {
                throw new IllegalArgumentException("Uploaded file could not be found.");
            }
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
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    /**
     * @param cociContentBusinessSrv the cociContentBusinessSrv to set
     */
    public void setCociContentBusinessSrv(CociContentBusinessSrv cociContentBusinessSrv) {
        this.cociContentBusinessSrv = cociContentBusinessSrv;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
