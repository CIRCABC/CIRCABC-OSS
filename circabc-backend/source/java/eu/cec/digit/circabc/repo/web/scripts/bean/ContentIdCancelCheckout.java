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

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Cancel checkout - cancels the checkout of the given item (given the id of the original document)
 *
 * @author schwerr
 */
public class ContentIdCancelCheckout extends CircabcDeclarativeWebScript {

    private CociContentBusinessSrv cociContentBusinessSrv;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        NodeRef orginalNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);

        NodeRef workingCopyRef = cociContentBusinessSrv.getWorkingCopy(orginalNodeRef);

        if (!this.nodeService.exists(workingCopyRef)) {
            throw new IllegalArgumentException(
                    "the working copy for document with id '" + id + "' could not be found.");
        }

        if (!this.nodeService.hasAspect(workingCopyRef, ContentModel.ASPECT_WORKING_COPY)) {
            throw new IllegalArgumentException("The item with id '" + id + "' is not a working copy.");
        }

        try {

            MLPropertyInterceptor.setMLAware(false);

            if (!this.currentUserPermissionCheckerService.hasAlfCancelCheckoutPermission(
                    workingCopyRef.getId())) {
                throw new AccessDeniedException("Cannot cancel checkout, not enough permissions");
            }
            boolean isWorkingCopyOwner = this.currentUserPermissionCheckerService.isWorkingCopyOwner(
                    workingCopyRef.getId());
            boolean isLibAdmin = this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(workingCopyRef.getId(), LibraryPermissions.LIBADMIN);
            
            if (!isWorkingCopyOwner && !isLibAdmin) {
                throw new AccessDeniedException("Cannot checkin, not enough permissions");
            }

            NodeRef nodeRef = this.cociContentBusinessSrv.cancelCheckOut(workingCopyRef);

            model.put("originalNodeId", nodeRef.getId());
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


    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
