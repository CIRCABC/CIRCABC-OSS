package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.event.AppointmentUpdateInfo;
import eu.cec.digit.circabc.service.event.UpdateMode;
import eu.cec.digit.circabc.service.profile.permissions.EventPermissions;
import io.swagger.api.EventsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript entry to update an Event/Meeting given its id
 *
 * @author schwerr
 */
public class EventsIdPut extends CircabcDeclarativeWebScript {

    private final Log logger = LogFactory.getLog(EventsIdPut.class);

    private EventsApi eventsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        String updateMode = req.getParameter("updateMode");
        String appointmentUpdateInfo = req.getParameter("updateInfo");

        if (!("Single".equals(updateMode)
                || "AllOccurences".equals(updateMode)
                || "FuturOccurences".equals(updateMode))) {
            throw new IllegalArgumentException(
                    "Invalid update mode. Must be " + "'Single', 'AllOccurences' or 'FuturOccurences'");
        }

        if (!("GeneralInformation".equals(appointmentUpdateInfo)
                || "RelevantSpace".equals(appointmentUpdateInfo)
                || "Audience".equals(appointmentUpdateInfo)
                || "ContactInformation".equals(appointmentUpdateInfo)
                || "All".equals(appointmentUpdateInfo))) {
            throw new IllegalArgumentException(
                    "Invalid update info. Must be "
                            + "'GeneralInformation', 'RelevantSpace', 'Audience' or 'ContactInformation'");
        }

        try {

            if (!this.currentUserPermissionCheckerService.hasAnyOfEventPermission(
                    id, EventPermissions.EVEADMIN)) {
                throw new AccessDeniedException(
                        "You cannot update this appointment, you don't have enough permission. "
                                + "You need to have EveAdmin permission.");
            }

            String appointmentBody = req.getContent().getContent();

            this.eventsApi.eventsIdPut(
                    id,
                    appointmentBody,
                    AppointmentUpdateInfo.valueOf(appointmentUpdateInfo),
                    UpdateMode.valueOf(updateMode));

        } catch (AccessDeniedException ade) {
            this.logger.error("Forbidden: " + ade);
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage(ade.getMessage());
            status.setRedirect(true);
            return null;
        } catch (Exception e) {
            this.logger.error("Exception: " + e);
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
