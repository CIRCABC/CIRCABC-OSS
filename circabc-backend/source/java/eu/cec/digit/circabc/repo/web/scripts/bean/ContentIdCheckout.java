package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv;
import eu.cec.digit.circabc.model.DocumentModel;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;

import org.alfresco.model.ApplicationModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.alfresco.service.namespace.QName;

import javax.servlet.http.HttpServletResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Checkout - Checks the given item out (given the id)
 *
 * @author schwerr
 */
public class ContentIdCheckout extends CircabcDeclarativeWebScript {

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

        String editInline = req.getParameter("editInline");

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        if (!this.nodeService.exists(nodeRef)) {
            throw new IllegalArgumentException("The item with id '" + id + "' could not be found.");
        }

        try {

            if (!this.currentUserPermissionCheckerService.hasAlfCheckoutPermission(id)) {
                throw new AccessDeniedException("Cannot checkout, not enough permissions");
            }

            if (nodeService.hasAspect(nodeRef, DocumentModel.ASPECT_URLABLE)) {
                throw new IllegalArgumentException("URLs cannot be checked out.");
            }

            MLPropertyInterceptor.setMLAware(false);

            NodeRef workingCopyNodeRef = this.cociContentBusinessSrv.checkOut(nodeRef);

            if (editInline != null && editInline.equals("true")) {
                final Map<QName, Serializable> editProps = new HashMap<>(1, 1.0f);
                editProps.put(ApplicationModel.PROP_EDITINLINE, true);
                this.nodeService.addAspect(workingCopyNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE, editProps);
            }

            model.put("workingCopyId", workingCopyNodeRef.getId());
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
