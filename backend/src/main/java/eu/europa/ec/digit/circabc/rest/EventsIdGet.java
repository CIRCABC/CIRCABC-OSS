package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import io.swagger.api.EventsApi;
import io.swagger.model.Appointment;
import io.swagger.model.Event;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that retrieves a single Event/Meeting (an
 * {@link Appointment}) by its node identifier.
 *
 * <p>The class name follows the CIRCABC convention {@code <Entity><Method>},
 * so this endpoint implements the HTTP <b>GET</b> operation for an event
 * identified by the {@code id} path variable (for example
 * {@code /events/{id}}). The {@code id} is read from the webscript URL
 * template variables.
 *
 * <p>Behavior:
 * <ul>
 *   <li>Verifies the current authority has Alfresco read permission on the
 *       node; otherwise an {@link AccessDeniedException} is raised and the
 *       response is set to HTTP 403 (Forbidden).</li>
 *   <li>Loads the appointment via {@link EventsApi#eventsIdGet(String)} with
 *       multilingual (ML) property interception temporarily disabled so raw
 *       property values are returned.</li>
 *   <li>Splits the appointment's invited users into internal
 *       users/profiles and external e-mail addresses (values containing
 *       {@code "@"} are treated as external e-mails).</li>
 *   <li>Resolves the owning Interest Group root id via {@link EventService}.</li>
 * </ul>
 *
 * <p>On success it returns a model map consumed by the associated FreeMarker
 * template to render the JSON response. On error it returns {@code null} after
 * setting an appropriate HTTP status (403 for access denial, 406 otherwise).
 *
 * @author schwerr
 */
public class EventsIdGet extends DeclarativeWebScript {

  /** Logger used to report errors that occur while serving the request. */
  static final Log logger = LogFactory.getLog(EventsIdGet.class);

  /** API used to load the {@link Appointment} (event or meeting) by its id. */
  @Autowired
  private EventsApi eventsApi;

  /** Service used to resolve the Interest Group root node for the event. */
  @Autowired
  private EventService eventService;

  /** Service used to check the current user's Alfresco read permission. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request for a single event/meeting.
   *
   * <p>Reads the {@code id} path variable, checks read permission, loads the
   * corresponding {@link Appointment}, separates invited internal
   * users/profiles from external e-mail addresses, and populates the response
   * model.
   *
   * <p>The model returned on success contains:
   * <ul>
   *   <li>{@code appointment} - the loaded {@link Appointment}</li>
   *   <li>{@code appointmentId} - the requested event id</li>
   *   <li>{@code igId} - the owning Interest Group root id</li>
   *   <li>{@code isEvent} - {@code true} if the appointment is an {@link Event}</li>
   *   <li>{@code invitedExternalEmails} - invited external e-mail addresses</li>
   *   <li>{@code invitedUsersOrProfiles} - invited internal users/profiles</li>
   * </ul>
   *
   * @param req the webscript request; its service match template variables
   *     must contain the {@code id} of the event to retrieve
   * @param status the response status; updated to 403 (Forbidden) on access
   *     denial or 406 (Not Acceptable) on any other error
   * @param cache the response cache directives
   * @return the model map used to render the JSON response, or {@code null}
   *     if an error occurred (in which case {@code status} carries the
   *     failure details)
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)
      ) {
        throw new AccessDeniedException(
          "Current Authority cannot access the event, not enough permission"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      Appointment appointment = this.eventsApi.eventsIdGet(id);

      List<String> invitedUsersOrProfiles = new ArrayList<>();
      List<String> invitedExternalEmails = new ArrayList<>();

      for (String invitedUser : appointment.getInvitedUsers()) {
        if (invitedUser.contains("@")) {
          invitedExternalEmails.add(invitedUser);
        } else {
          invitedUsersOrProfiles.add(invitedUser);
        }
      }

      model.put("appointment", appointment);
      model.put("appointmentId", id);
      model.put("igId", this.eventService.getIGRoot(id).getId());
      model.put("isEvent", appointment instanceof Event);
      model.put("invitedExternalEmails", invitedExternalEmails);
      model.put("invitedUsersOrProfiles", invitedUsersOrProfiles);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
