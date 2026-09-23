package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AppMessageApi;
import io.swagger.model.db.DistributionEmailDAO;
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
 * Alfresco declarative web script that handles the deletion (HTTP DELETE) of an application
 * distribution email subscription.
 *
 * <p>The endpoint reads the {@code id} template variable from the request URL, resolves the
 * corresponding {@link io.swagger.model.db.DistributionEmailDAO} and, if the entry exists,
 * removes the associated application distribution post emails via {@link AppMessageApi}.
 *
 * <p>Access is restricted: the deletion is only performed when the current user is either the
 * owner of the subscribed email address or a CIRCABC administrator. Otherwise the request is
 * rejected. The endpoint reports the following outcomes through the response {@link Status}:
 *
 * <ul>
 *   <li>{@code 404 Not Found} when no distribution email matches the given id;</li>
 *   <li>{@code 403 Forbidden} when the current user is not allowed to perform the deletion;</li>
 *   <li>{@code 400 Bad Request} when the supplied id is not a valid integer;</li>
 *   <li>{@code 500 Internal Server Error} for any other unexpected failure.</li>
 * </ul>
 *
 * @author beaurpi
 */
public class AppDistributionMailsDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(AppDistributionMailsDelete.class);

  /**
   * API used to look up and remove application distribution email subscriptions.
   */
  @Autowired
  private AppMessageApi appMessageApi;

  /**
   * Service used to verify the current user's permissions, i.e. whether the user owns the target
   * email address or is a CIRCABC administrator.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the distribution mail deletion.
   *
   * <p>Parses the {@code id} template variable, resolves the matching distribution email and, when
   * the current user is authorized, removes the related application distribution post emails. On
   * error or when the entry is not found, an appropriate HTTP status is set on the response and
   * {@code null} is returned so the framework renders the status page.
   *
   * @param req the web script request; the {@code id} of the distribution email to delete is
   *     extracted from its template variables
   * @param status the response status used to signal not found, forbidden, bad request or internal
   *     server errors
   * @param cache the cache directives for the response
   * @return an empty model map on successful deletion, or {@code null} when an error status has
   *     been set on the response
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

    try {
      Integer idInt = Integer.parseInt(id);
      DistributionEmailDAO result =
        appMessageApi.getSubscribedDistributionEmailById(idInt);
      if (result == null) {
        status.setCode(Status.STATUS_NOT_FOUND);
        status.setMessage("Distribution email not found");
        status.setRedirect(true);
        return null; // NOSONAR
      } else {
        if (
          currentUserPermissionCheckerService.isCurrentUserEmailEqualTo(
            result.getEmailAddress()
          ) ||
          currentUserPermissionCheckerService.isCircabcAdmin()
        ) {
          appMessageApi.removeAppDistributionPostEmails(idInt);
        } else {
          status.setCode(Status.STATUS_FORBIDDEN);
          status.setMessage("Access denied");
          status.setRedirect(true);
          return null; // NOSONAR
        }
      }
    } catch (AccessDeniedException ade) {
      logger.error("Access denied to delete distribution mail", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (NumberFormatException e) {
      logger.error("Invalid ID format: " + id, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Invalid ID format");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error deleting distribution mail", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
