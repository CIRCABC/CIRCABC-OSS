package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import io.swagger.api.ClipboardAction;
import io.swagger.api.ClipboardApi;
import io.swagger.util.Converter;
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
import java.util.*;

/**
 * Paste the nodes given by nodeIds by moving them to the folder with id given
 * in the path
 *
 * @author schwerr
 */
public class NodesIdPasteMove extends CircabcDeclarativeWebScript {

    private NodeService nodeService;
    private ClipboardApi clipboardApi;
    private NotificationService notificationService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private NotificationSubscriptionService notificationSubscriptionService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        NodeRef folderNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);

        if (!this.nodeService.exists(folderNodeRef)
                || !ContentModel.TYPE_FOLDER.equals(this.nodeService.getType(folderNodeRef))) {
            throw new IllegalArgumentException("The folder with id '" + id + "' could not be found.");
        }

        String[] nodeIds = req.getParameterValues("nodeIds");

        try {

            if (!this.currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(id)) {
                throw new AccessDeniedException("Impossible to paste the elements, not enough permissions");
            }

            MLPropertyInterceptor.setMLAware(false);

            this.clipboardApi.paste(nodeIds, folderNodeRef, ClipboardAction.MOVE.getValue());

            List<NodeRef> nodeRefs = new ArrayList<>();
            for (String nodeRef : nodeIds) {
                nodeRefs.add(Converter.createNodeRefFromId(nodeRef));
            }

            Set<NotifiableUser> notifiableUsers = notificationSubscriptionService.getNotifiableUsers(folderNodeRef);
            notificationService.notifyNewFiles(folderNodeRef, nodeRefs, notifiableUsers, MailTemplate.NOTIFY_MOVE_BULK);

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
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param clipboardApi the clipboardApi to set
     */
    public void setClipboardApi(ClipboardApi clipboardApi) {
        this.clipboardApi = clipboardApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    /**
     * @param notificationService the notificationService to set
     */
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * @param notificationSubscriptionService the notificationSubscriptionService to
     *                                        set
     */
    public void setNotificationSubscriptionService(NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
    }
}
