package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.UserJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco web script endpoint that handles the HTTP {@code POST} request used to persist a user's
 * preference configuration.
 *
 * <p>The endpoint expects a {@code userId} URL template variable identifying the target user and a
 * JSON request body describing the preference to store. Before saving, it verifies that the
 * currently authenticated user is the same as the {@code userId} being modified; if not, the
 * request is rejected with an HTTP 403 (Forbidden). The JSON body is parsed via
 * {@link UserJsonParser} and delegated to {@link UsersApi#saveUserPreferenceConfiguration(String,
 * String)} for persistence.</p>
 *
 * <p>On success an empty model map is returned. Error conditions are mapped to appropriate HTTP
 * status codes: permission failures to 403 (Forbidden), malformed or invalid input to 400 (Bad
 * Request), and any other unexpected failure to 500 (Internal Server Error).</p>
 *
 * @author beaurpi
 */
public class UserPreferencePost extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UserPreferencePost.class);

  /** API used to persist the user's preference configuration. */
  @Autowired
  private UsersApi usersApi;

  /** Service used to check that the current user is authorized to modify the target user. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the web script: validates permissions and saves the posted user preference
   * configuration.
   *
   * <p>Reads the {@code userId} template variable from the request, ensures the current user is
   * equal to that user, parses the request body as a preference JSON object and persists it. When
   * an error occurs the appropriate HTTP status is set on {@code status} and {@code null} is
   * returned to trigger the error response.</p>
   *
   * @param req the web script request, providing the {@code userId} template variable and the JSON
   *     request body
   * @param status the web script status used to signal success or the relevant HTTP error code
   * @param cache the web script cache directives for the response
   * @return an (empty) model map on success, or {@code null} when an error status has been set
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
    try {
      if (!currentUserPermissionCheckerService.isCurrentUserEqualTo(userId)) {
        throw new AccessDeniedException("Not enough permissions");
      }

      JSONObject preference = UserJsonParser.parsePreferenceAsJson(
        req.getContent().getContent()
      );

      if (userId != null) {
        usersApi.saveUserPreferenceConfiguration(
          userId,
          preference.toJSONString()
        );
      }
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when saving user preference", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException e) {
      logger.error("Invalid input when saving user preference", e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request - invalid input for preference");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when saving user preference", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
