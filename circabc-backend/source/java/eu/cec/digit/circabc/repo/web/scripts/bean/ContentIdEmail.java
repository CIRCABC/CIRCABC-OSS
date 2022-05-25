package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.business.api.mail.MailMeContentBusinessSrv;
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
 * Sends an email with the current content to the given user
 *
 * @author schwerr
 */
public class ContentIdEmail extends CircabcDeclarativeWebScript {

    MailMeContentBusinessSrv mailMeContentBusinessSrv;
    NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {

            if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)) {
                throw new AccessDeniedException("Cannot read content, not enough permissions");
            }

            MLPropertyInterceptor.setMLAware(false);

            NodeRef contentRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);

            if (!ContentModel.TYPE_CONTENT.equals(this.nodeService.getType(contentRef))) {
                throw new IllegalArgumentException("The item with id '" + id + "' is not a content item.");
            }

            String userId = req.getParameter("userId");

            boolean result;

            if ((userId == null) || userId.isEmpty()) {
                result = this.mailMeContentBusinessSrv.send(contentRef, true);
            } else {
                result = this.mailMeContentBusinessSrv.send(contentRef, userId, true);
            }

            model.put("result", result);
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
     * @param mailMeContentBusinessSrv the mailMeContentBusinessSrv to set
     */
    public void setMailMeContentBusinessSrv(MailMeContentBusinessSrv mailMeContentBusinessSrv) {
        this.mailMeContentBusinessSrv = mailMeContentBusinessSrv;
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
