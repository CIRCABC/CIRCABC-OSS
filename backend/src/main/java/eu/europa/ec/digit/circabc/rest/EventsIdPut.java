package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.EventsApi;
import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.UpdateMode;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative webscript handling the HTTP {@code PUT} request that updates an existing
 * Event/Meeting (appointment) identified by its node id.
 *
 * <p>The endpoint resolves the target appointment from the {@code id} template variable in the URL
 * and expects two query parameters:
 *
 * <ul>
 *   <li>{@code updateMode} &mdash; the scope of the update; must be one of {@code Single},
 *       {@code AllOccurences} or {@code FuturOccurences}.
 *   <li>{@code updateInfo} &mdash; the section of the appointment to update; must be one of
 *       {@code GeneralInformation}, {@code RelevantSpace}, {@code Audience},
 *       {@code ContactInformation} or {@code All}.
 * </ul>
 *
 * <p>The appointment payload itself is read from the request body. Before applying the change the
 * webscript verifies that the current user holds the {@link EventPermissions#EVEADMIN} permission on
 * the target node. On success the actual update is delegated to {@link EventsApi#eventsIdPut}.
 *
 * <p>Invalid {@code updateMode} or {@code updateInfo} values raise an
 * {@link IllegalArgumentException}. A missing permission results in an HTTP {@code 403 Forbidden}
 * response, while any other failure results in an HTTP {@code 406 Not Acceptable} response.
 *
 * @author schwerr
 */
public class EventsIdPut extends CircabcDeclarativeWebScript {

  /** Logger used to report permission and processing errors. */
  private final Log logger = LogFactory.getLog(EventsIdPut.class);

  /** Business API that performs the actual event/appointment update. */
  @Autowired
  private EventsApi eventsApi;

  /** Service used to check whether the current user holds the required event permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code PUT} request that updates an event/appointment.
   *
   * <p>Reads the {@code id} template variable and the {@code updateMode} / {@code updateInfo} query
   * parameters, validates them, checks that the current user has {@link EventPermissions#EVEADMIN}
   * permission on the target node, and then delegates the update to {@link EventsApi#eventsIdPut}
   * using the request body as the appointment payload.
   *
   * @param req the webscript request; supplies the {@code id} template variable, the
   *     {@code updateMode} and {@code updateInfo} parameters, and the appointment body content
   * @param status the response status, set to {@code 403 Forbidden} when the user lacks the required
   *     permission or {@code 406 Not Acceptable} when any other error occurs
   * @param cache the webscript cache directives for the response
   * @return an empty model map on success, or {@code null} when the request fails and an error
   *     status/redirect has been set
   * @throws IllegalArgumentException if {@code updateMode} or {@code updateInfo} is not one of the
   *     accepted values
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
    String updateMode = req.getParameter("updateMode");
    String appointmentUpdateInfo = req.getParameter("updateInfo");

    if (
      !("Single".equals(updateMode) ||
        "AllOccurences".equals(updateMode) ||
        "FuturOccurences".equals(updateMode))
    ) {
      throw new IllegalArgumentException(
        "Invalid update mode. Must be " +
          "'Single', 'AllOccurences' or 'FuturOccurences'"
      );
    }

    if (
      !("GeneralInformation".equals(appointmentUpdateInfo) ||
        "RelevantSpace".equals(appointmentUpdateInfo) ||
        "Audience".equals(appointmentUpdateInfo) ||
        "ContactInformation".equals(appointmentUpdateInfo) ||
        "All".equals(appointmentUpdateInfo))
    ) {
      throw new IllegalArgumentException(
        "Invalid update info. Must be " +
          "'GeneralInformation', 'RelevantSpace', 'Audience' or 'ContactInformation'"
      );
    }

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfEventPermission(
          id,
          EventPermissions.EVEADMIN
        )
      ) {
        throw new AccessDeniedException(
          "You cannot update this appointment, you don't have enough permission. " +
            "You need to have EveAdmin permission."
        );
      }

      String appointmentBody = req.getContent().getContent();

      this.eventsApi.eventsIdPut(
        id,
        appointmentBody,
        AppointmentUpdateInfo.valueOf(appointmentUpdateInfo),
        UpdateMode.valueOf(updateMode)
      );
    } catch (AccessDeniedException ade) {
      this.logger.error("Forbidden: " + ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage(ade.getMessage());
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      this.logger.error("Exception: " + e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
