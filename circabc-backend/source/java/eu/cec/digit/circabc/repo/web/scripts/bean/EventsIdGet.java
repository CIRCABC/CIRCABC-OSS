package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.event.Appointment;
import eu.cec.digit.circabc.service.event.Event;
import eu.cec.digit.circabc.service.event.EventService;
import io.swagger.api.EventsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript entry to get an Event/Meeting given its id
 *
 * @author schwerr
 */
public class EventsIdGet extends DeclarativeWebScript {

    private EventsApi eventsApi;
    private EventService eventService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {

            if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)) {
                throw new AccessDeniedException(
                        "Current Authority cannot access the event, not enough permission");
            }

            MLPropertyInterceptor.setMLAware(false);

            Appointment appointment = this.eventsApi.eventsIdGet(id);

            model.put("appointment", appointment);
            model.put("appointmentId", id);
            model.put("igId", this.eventService.getIGRoot(id).getId());
            model.put("isEvent", appointment instanceof Event);
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
     * @param eventsApi the eventsApi to set
     */
    public void setEventsApi(EventsApi eventsApi) {
        this.eventsApi = eventsApi;
    }

    /**
     * @param eventService the eventService to set
     */
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
