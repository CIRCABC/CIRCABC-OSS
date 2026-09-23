package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that retrieves a single user's profile.
 *
 * <p>Backing the HTTP {@code GET} request implied by the class name, this
 * endpoint resolves the target user from the {@code userId} URL template
 * variable and returns the corresponding {@link User} in the response model
 * under the key {@code "user"}.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code userId} (URL template variable) &ndash; the identifier of the
 *       user to fetch.</li>
 *   <li>{@code language} (optional request parameter) &ndash; when supplied,
 *       the content and UI locale are set accordingly and multilingual
 *       (ML) awareness is disabled so that values are resolved for that
 *       locale; when absent, ML awareness is enabled.</li>
 * </ul>
 *
 * <p>Access control: the full user record is only exposed when the requester
 * is the same as the requested user. Otherwise a reduced projection (user id,
 * first name, last name and avatar information) is returned.</p>
 */
public class UserGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UserGet.class);

  /**
   * API used to look up user data by user id.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Service used to determine whether the requested user matches the
   * currently authenticated user, controlling how much data is exposed.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request for a user profile.
   *
   * <p>Reads the optional {@code language} request parameter to configure the
   * content/UI locale and multilingual awareness, resolves the {@code userId}
   * URL template variable, and populates the response model with the matching
   * user. When the requester is not the same as the requested user, only a
   * reduced set of fields is returned. The previous ML-awareness state is
   * always restored before returning.</p>
   *
   * @param req    the webscript request, providing the {@code language}
   *               parameter and the {@code userId} URL template variable
   * @param status the response status, updated to an error code and set to
   *               redirect when the lookup fails
   * @param cache  the response cache directives
   * @return a model map containing the resolved user under the key
   *         {@code "user"}, or {@code null} when an error occurs and the
   *         status has been set to an error code
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    try {
      String userId = templateVars.get("userId");

      if (userId != null) {
        boolean currentUser =
          currentUserPermissionCheckerService.isCurrentUserEqualTo(userId);
        User userData = this.usersApi.usersUserIdGet(userId);

        if (!currentUser) {
          User basicUser = new User();
          basicUser.setUserId(userData.getUserId());
          basicUser.setFirstname(userData.getFirstname());
          basicUser.setLastname(userData.getLastname());
          basicUser.setAvatar(userData.getAvatar());
          basicUser.setDefaultAvatar(userData.isDefaultAvatar());
          model.put("user", basicUser);
        } else {
          model.put("user", userData);
        }
      }
    } catch (InvalidNodeRefException inre) {
      logger.error(inre.getMessage(), inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error(ade.getMessage(), ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error occurred: " + e.getMessage(), e);
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
