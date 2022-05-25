package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionService;
import io.swagger.util.CurrentUserPermissionCheckerService;

import org.alfresco.model.ApplicationModel;
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
 * Checkin - checks the given item in (given the id of the original document) - minorChange and
 * keepCheckedOut default to false
 *
 * @author schwerr
 */
public class ContentIdCheckin extends CircabcDeclarativeWebScript {

    private CociContentBusinessSrv cociContentBusinessSrv;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private CircabcRenditionService circabcRenditionService;

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
                    "The working copy for document with id '" + id + "' could not be found.");
        }

        if (!this.nodeService.hasAspect(workingCopyRef, ContentModel.ASPECT_WORKING_COPY)) {
            throw new IllegalArgumentException("The item with id '" + id + "' is not a working copy.");
        }

        boolean minor = false;

        try {
            String minorChangeString = req.getParameter("minorChange");
            if (minorChangeString != null) {
                minor = Boolean.parseBoolean(minorChangeString);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("The 'minorChange' parameter must be a boolean.");
        }

        boolean keepCheckOut = false;

        try {
            String keepCheckOutString = req.getParameter("keepCheckedOut");
            if (keepCheckOutString != null) {
                keepCheckOut = Boolean.parseBoolean(keepCheckOutString);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("The 'keepCheckedOut' parameter must be a boolean.");
        }

        boolean endEditInline = false;

        try {
            String endEditInlineString = req.getParameter("endEditInline");
            if (endEditInlineString != null) {
                endEditInline = Boolean.parseBoolean(endEditInlineString);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("The 'endEditInline' parameter must be a boolean.");
        }

        String versionNote = req.getParameter("comment");

        try {

            if (!this.currentUserPermissionCheckerService.hasAlfCheckinPermission(
                    workingCopyRef.getId())) {
                throw new AccessDeniedException("Cannot checkin, not enough permissions");
            }

            if (!this.currentUserPermissionCheckerService.isWorkingCopyOwner(
                    workingCopyRef.getId())) {
                throw new AccessDeniedException("Cannot checkin, not enough permissions");
            }
            MLPropertyInterceptor.setMLAware(false);

            if (endEditInline) {
                this.nodeService.removeAspect(workingCopyRef, ApplicationModel.ASPECT_INLINEEDITABLE);
            }

            NodeRef checkedInNodeRef =
                    this.cociContentBusinessSrv.checkIn(
                            workingCopyRef, minor, (versionNote == null) ? "" : versionNote, keepCheckOut);

            this.circabcRenditionService.addRequest(checkedInNodeRef);
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
     * @param circabcRenditionService the circabcRenditionService to set
     */
    public void setCircabcRenditionService(CircabcRenditionService circabcRenditionService) {
        this.circabcRenditionService = circabcRenditionService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
