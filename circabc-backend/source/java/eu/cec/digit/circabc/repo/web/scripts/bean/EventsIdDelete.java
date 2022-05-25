package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.event.UpdateMode;
import eu.cec.digit.circabc.service.profile.permissions.EventPermissions;
import io.swagger.api.EventsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript entry to delete an Event/Meeting given its id
 *
 * @author schwerr
 */
public class EventsIdDelete extends CircabcDeclarativeWebScript {

    private EventsApi eventsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        String updateMode = req.getParameter("updateMode");

        if ((updateMode == null) || updateMode.isEmpty()) {
            throw new IllegalArgumentException("'updateMode' parameter is mandatory.");
        }

        try {
            if (!this.currentUserPermissionCheckerService.hasAnyOfEventPermission(
                    id, EventPermissions.EVEADMIN)) {
                throw new AccessDeniedException(
                        "Current Authority cannot delete the event, not enough permission");
            }
            this.recordBeforeDelete(id);
            this.eventsApi.eventsIdDelete(id, UpdateMode.valueOf(updateMode));
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
}
