package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.EventsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative webscript handling the HTTP {@code POST} endpoint used to respond to a
 * meeting request (calendar event invitation) on behalf of a user.
 *
 * <p>The user is identified by the {@code userId} URL template variable. The invitation to update
 * is identified by the {@code meetingId} request parameter, while {@code action} indicates the
 * response to apply (for example accept or reject) and {@code updateMode} controls how the update
 * is propagated. The endpoint enforces that the currently authenticated user may only update their
 * own events; attempting to update another user's event results in an {@code AccessDeniedException}
 * and an HTTP {@code 403 Forbidden} response.
 *
 * <p>On success the model exposes the applied {@code action} for rendering by the associated
 * FreeMarker template.
 *
 * @author schwerr
 */
public class UsersIdEventsPost extends CircabcDeclarativeWebScript {

  /** Logger used to report access denials and errors while updating an event. */
  static final Log logger = LogFactory.getLog(UsersIdEventsPost.class);

  /** API delegate that carries out the event (meeting request) update business logic. */
  @Autowired
  private EventsApi eventsApi;

  /** Service used to verify that the current user matches the targeted {@code userId}. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the POST request that accepts or rejects a meeting request for the targeted user.
   *
   * <p>Reads the {@code userId} from the URL template variables and the {@code meetingId},
   * {@code action} and {@code updateMode} request parameters. It checks that the current user is
   * the same as {@code userId} before delegating the update to {@link EventsApi}. ML (multilingual)
   * property awareness is disabled for the duration of the call and restored afterwards.
   *
   * @param req the web script request carrying the {@code userId} template variable and the
   *     {@code meetingId}, {@code action} and {@code updateMode} parameters
   * @param status the response status; set to {@code 403 Forbidden} when the current user is not
   *     the targeted user, or {@code 406 Not Acceptable} when any other error occurs
   * @param cache the cache directives for the response
   * @return a model map exposing the applied {@code action} on success, or {@code null} when the
   *     request fails and a redirect status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String userId = templateVars.get("userId");
    String meetingId = req.getParameter("meetingId");
    String action = req.getParameter("action");
    String updateMode = req.getParameter("updateMode");

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      MLPropertyInterceptor.setMLAware(false);

      if (
        !this.currentUserPermissionCheckerService.isCurrentUserEqualTo(userId)
      ) {
        throw new AccessDeniedException(
          "Cannot update the event (accept or reject) of somebody else"
        );
      }

      this.eventsApi.usersIdEventsPost(userId, meetingId, action, updateMode);

      model.put("action", action);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when updating event for user: " +
          userId +
          ", meetingId: " +
          meetingId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error updating event for user: " +
          userId +
          ", meetingId: " +
          meetingId +
          ", action: " +
          action,
        e
      );
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
