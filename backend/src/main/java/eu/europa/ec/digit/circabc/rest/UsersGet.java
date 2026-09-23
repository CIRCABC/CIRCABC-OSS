package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.EmailUtil;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that searches for users.
 *
 * <p>Implements the HTTP {@code GET} operation implied by the class name
 * ({@code UsersGet}). Given a search query it returns the matching users in the
 * response model under the {@code "users"} key, which the associated FreeMarker
 * template renders as JSON.
 *
 * <p>Key request inputs:
 * <ul>
 *   <li>{@code query} (required) - the search term used to look up users.</li>
 *   <li>{@code filter} (optional) - when set to {@code "false"} the result set
 *       is returned unfiltered; any other value (or absence) keeps filtering
 *       enabled.</li>
 * </ul>
 *
 * <p>Access rules enforced by the endpoint:
 * <ul>
 *   <li>Guest users are not allowed to perform the search.</li>
 *   <li>External users may only search by a valid email address.</li>
 * </ul>
 *
 * <p>Validation and authorization failures are translated into the appropriate
 * HTTP status codes ({@code 400 Bad Request}, {@code 403 Forbidden} or
 * {@code 500 Internal Server Error}).
 */
public class UsersGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UsersGet.class);

  /**
   * API providing the user search business logic.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Service used to determine the current user's status (guest, external, etc.)
   * in order to enforce access rules.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the user search request.
   *
   * <p>Reads the {@code query} and {@code filter} request parameters, enforces
   * the guest and external-user access rules, and populates the response model
   * with the matching users under the {@code "users"} key. On error the
   * appropriate HTTP status is set on the {@link Status} object and
   * {@code null} is returned so the framework renders the status response.
   *
   * @param req    the web script request carrying the {@code query} and
   *               optional {@code filter} parameters
   * @param status the status object used to report the outcome of the request
   * @param cache  the cache directives for the response
   * @return a model map containing the matching users under the {@code "users"}
   *         key, or {@code null} when the request fails and a status response is
   *         rendered instead
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    String query = req.getParameter("query");
    String filterString = req.getParameter("filter");
    boolean filter = true;

    if (filterString != null && filterString.equalsIgnoreCase("false")) {
      filter = false;
    }

    try {
      if (query == null || query.isEmpty()) {
        throw new InvalidArgumentException("Empty Query");
      } else if (currentUserPermissionCheckerService.isGuest()) {
        throw new AccessDeniedException("Method not allowed for guest");
      }
      //if the user is external he can only search on email addresses
      boolean isExternalUser =
        currentUserPermissionCheckerService.isExternalUser();
      if (isExternalUser && !EmailUtil.isValidEmailAddress(query)) {
        throw new InvalidArgumentException(
          "Please enter a valid email address"
        );
      } else {
        model.put(
          "users",
          this.usersApi.usersGet(query, filter, isExternalUser)
        );
      }
    } catch (InvalidArgumentException inre) {
      logger.error("Invalid argument in users search", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage(inre.getMessage());
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error("Access denied in users search", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error in users search", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("An unexpected error occurred");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
