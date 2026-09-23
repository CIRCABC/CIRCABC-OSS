package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.EventsApi;
import io.swagger.model.UpdateMode;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative webscript that handles the deletion of an Event/Meeting.
 *
 * <p>Mapped to an HTTP {@code DELETE} request (as implied by the {@code Delete}
 * suffix of the class name), this endpoint removes the event identified by the
 * {@code id} URL template variable. The caller must have the
 * {@link EventPermissions#EVEADMIN} permission on the target event; otherwise the
 * request is rejected with an HTTP 403 (Forbidden) status.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} &ndash; URL template variable identifying the event to delete.</li>
 *   <li>{@code updateMode} &ndash; mandatory request parameter (parsed into an
 *       {@link UpdateMode}) controlling how the deletion is applied, e.g. to a
 *       single occurrence or the whole recurring series.</li>
 * </ul>
 *
 * <p>The actual deletion is delegated to {@link EventsApi#eventsIdDelete}, and a
 * pre-delete record is captured via {@code recordBeforeDelete} for auditing/logging.</p>
 *
 * @author schwerr
 */
public class EventsIdDelete extends CircabcDeclarativeWebScript {

  /** API providing the business operations used to delete the event. */
  @Autowired
  private EventsApi eventsApi;

  /** Service used to verify that the current user holds the required event permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Deletes the event identified by the {@code id} URL template variable.
   *
   * <p>Validates that the mandatory {@code updateMode} request parameter is present,
   * checks that the current user has {@link EventPermissions#EVEADMIN} permission on
   * the event, records the event state before deletion and then delegates the
   * deletion to {@link EventsApi#eventsIdDelete}. Errors are translated into the
   * appropriate HTTP status codes on the response.</p>
   *
   * @param req    the incoming webscript request; supplies the {@code id} template
   *               variable and the {@code updateMode} parameter
   * @param status the response status object, updated to signal forbidden access
   *               (403) or a non-acceptable outcome (406) when errors occur
   * @param cache  the response cache control object
   * @return an (empty) model map on success, or {@code null} when the request
   *         fails and a redirect status has been set
   * @throws IllegalArgumentException if the mandatory {@code updateMode} parameter
   *                                  is missing or empty
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

    if ((updateMode == null) || updateMode.isEmpty()) {
      throw new IllegalArgumentException(
        "'updateMode' parameter is mandatory."
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
          "Current Authority cannot delete the event, not enough permission"
        );
      }
      this.recordBeforeDelete(id);
      this.eventsApi.eventsIdDelete(id, UpdateMode.valueOf(updateMode));
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
    }

    return model;
  }
}
