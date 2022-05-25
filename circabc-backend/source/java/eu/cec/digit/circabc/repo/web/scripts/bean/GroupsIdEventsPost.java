package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.EventPermissions;
import io.swagger.api.EventsApi;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript entry to create a new Event/Meeting
 *
 * @author schwerr
 */
public class GroupsIdEventsPost extends CircabcDeclarativeWebScript {

    private EventsApi eventsApi;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String igId = templateVars.get("igId");

        try {
            NodeRef groupRef = Converter.createNodeRefFromId(igId);
            NodeRef evtNodeRef =
                    this.nodeService.getChildByName(groupRef, ContentModel.ASSOC_CONTAINS, "Events");

            if (!this.currentUserPermissionCheckerService.hasAnyOfEventPermission(
                    evtNodeRef.getId(), EventPermissions.EVEADMIN)) {
                throw new AccessDeniedException(
                        "You cannot create an appointment here, you don't have enough permission. "
                                + "You need to have EveAdmin permission.");
            }

            String appointmentBody = req.getContent().getContent();

            this.eventsApi.groupsIdEventsPost(igId, appointmentBody);
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage(ade.getMessage());
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
     * @param eventsApi the eventsApi to set
     */
    public void setEventsApi(EventsApi eventsApi) {
        this.eventsApi = eventsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    /**
     * @return the nodeService
     */
    public NodeService getNodeService() {
        return this.nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
