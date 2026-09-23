package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.AresBridgeApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that handles HTTP GET requests to retrieve the list of external
 * repositories available through the ARES bridge integration.
 *
 * <p>The endpoint returns the collection of external repositories that the current user may access,
 * exposing them in the response model under the {@code "repos"} key for rendering by the associated
 * FreeMarker template.
 *
 * <p>Access is restricted: guest users are rejected with an HTTP 403 (Forbidden) response. Any error
 * raised while querying the ARES bridge results in an HTTP 400 (Bad Request) response.
 */
public class ExternalRepositoryGet extends DeclarativeWebScript {

  /** Logger used to record errors that occur while resolving external repositories. */
  static final Log logger = LogFactory.getLog(ExternalRepositoryGet.class);

  /** API used to query the ARES bridge for the external repositories available to the caller. */
  @Autowired
  private AresBridgeApi aresBridgeApi;

  /** Service used to determine the current user's identity and permissions (e.g. guest status). */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request by resolving the external repositories available to the current user.
   *
   * <p>If the current user is a guest, the response status is set to {@link Status#STATUS_FORBIDDEN}
   * and {@code null} is returned. If querying the ARES bridge fails, the response status is set to
   * {@link Status#STATUS_BAD_REQUEST} and {@code null} is returned. Otherwise, the available
   * repositories are placed in the model under the {@code "repos"} key.
   *
   * @param req the incoming web script request
   * @param status the response status, updated to reflect forbidden or bad-request conditions
   * @param cache the cache directives for the response
   * @return a model map containing the available external repositories under {@code "repos"}, or
   *     {@code null} when access is denied or an error occurs
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    if (currentUserPermissionCheckerService.isGuest()) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    try {
      model.put("repos", this.aresBridgeApi.getAvailableExternalRepositories());
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
