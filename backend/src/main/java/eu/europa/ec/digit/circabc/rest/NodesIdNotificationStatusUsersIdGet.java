package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NotificationsApi;
import io.swagger.model.Subscribed;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that reports whether a given user is subscribed to
 * notifications for a specific node.
 *
 * <p>The class name follows the CIRCABC naming convention
 * {@code <Entity>...<Method>}, which maps to the HTTP <b>GET</b> request
 * {@code /nodes/{id}/notification/status/users/{userId}}. The two path segments
 * are extracted from the web script URL template:
 *
 * <ul>
 *   <li>{@code id} &ndash; the node reference identifier whose notification
 *       configuration is being queried.</li>
 *   <li>{@code userId} &ndash; the user for whom the subscription status is
 *       requested.</li>
 * </ul>
 *
 * <p>Access is restricted: the endpoint only allows the currently authenticated
 * user to query their own subscription status. Any other combination results in
 * an {@code AccessDeniedException} that is translated into an HTTP
 * {@code 403 Forbidden} response.
 *
 * <p>The response model exposes a single {@code result} entry holding a
 * {@link io.swagger.model.Subscribed} flag that is rendered as JSON by the
 * associated FreeMarker template.
 */
public class NodesIdNotificationStatusUsersIdGet extends DeclarativeWebScript {

  /** Reusable log message fragment inserted between the node id and the user id. */
  private static final String AND_USER = " and user: ";

  /** Logger used to record access, validation and unexpected error conditions. */
  static final Log logger = LogFactory.getLog(
    NodesIdNotificationStatusUsersIdGet.class
  );

  /** Business API used to resolve the user's notification subscription state. */
  @Autowired
  private NotificationsApi notificationsApi;

  /** Service used to verify that the request targets the current user's own data. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request and determines whether the requested user is
   * subscribed to notifications for the requested node.
   *
   * <p>The {@code id} and {@code userId} path variables are read from the URL
   * template. The current user must equal {@code userId}; otherwise access is
   * denied. On success the resulting {@link io.swagger.model.Subscribed} flag is
   * placed under the {@code result} key of the returned model.
   *
   * <p>Errors are handled internally by setting the appropriate HTTP status and
   * returning {@code null}: {@code 403 Forbidden} for permission failures,
   * {@code 400 Bad Request} for invalid node references, and
   * {@code 500 Internal Server Error} for any other failure. The multilingual
   * (ML) awareness flag is always restored before returning.
   *
   * @param req the web script request carrying the {@code id} and {@code userId}
   *            URL template variables
   * @param status the response status object used to signal HTTP result codes
   *               and redirects on failure
   * @param cache the response cache control object
   * @return a model map containing the {@code result} subscription flag on
   *         success, or {@code null} when an error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");
    String userId = templateVars.get("userId");

    try {
      if (
        !(this.currentUserPermissionCheckerService.isCurrentUserEqualTo(userId))
      ) {
        throw new AccessDeniedException(
          "Impossible to remove notification configuration, not enough permission"
        );
      }

      Subscribed subscribed = new Subscribed();
      subscribed.setSubscribed(
        this.notificationsApi.isUsersubscribedForNotification(id, userId)
      );
      model.put("result", subscribed);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when checking notification status for node: " +
          id +
          AND_USER +
          userId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when checking notification status for node: " +
          id +
          AND_USER +
          userId,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when checking notification status for node: " +
          id +
          AND_USER +
          userId,
        e
      );
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
