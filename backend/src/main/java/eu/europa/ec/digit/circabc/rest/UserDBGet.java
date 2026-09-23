package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Read-only REST webscript endpoint that retrieves a user's profile
 * information directly from the underlying LDAP directory database.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention,
 * so this endpoint responds to HTTP {@code GET} requests. The target user is
 * identified by the {@code userId} template variable extracted from the
 * request URL. An optional {@code language} request parameter controls locale
 * handling: when omitted the response is rendered in a multilingual
 * (ML-aware) fashion; when supplied it selects a specific {@link Locale} for
 * the content and disables ML awareness.</p>
 *
 * <p>Access is restricted: a caller may only read the LDAP information of
 * their own account, unless the caller is the {@code admin} user. When the
 * user is found, the resolved profile is placed in the response model under
 * the {@code user} key for rendering by the associated FreeMarker template.</p>
 */
public class UserDBGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UserDBGet.class);

  /**
   * API facade used to look up user profile information from the LDAP
   * directory database.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Alfresco authentication service used to determine the currently
   * authenticated user and to enforce the self-or-admin access check.
   */
  @Autowired
  private AuthenticationService authenticationService;

  /**
   * Handles the incoming GET request by resolving the requested user's LDAP
   * profile and populating the response model.
   *
   * <p>Reads the optional {@code language} request parameter to configure the
   * content locale and multilingual awareness, then reads the {@code userId}
   * template variable to identify the target user. If a {@code userId} is
   * present, the caller must be either that same user or {@code admin};
   * otherwise access is denied. On success the resolved user is stored in the
   * model under the {@code user} key.</p>
   *
   * <p>Errors are handled internally rather than propagated: the appropriate
   * HTTP status is set on {@code status}, a redirect is flagged and
   * {@code null} is returned. The original multilingual awareness state is
   * always restored before returning.</p>
   *
   * @param req    the web script request, providing the {@code language}
   *               parameter and the {@code userId} template variable
   * @param status the response status object, updated with an error code and
   *               message (400, 403 or 500) when the request cannot be served
   * @param cache  the web script cache directives for the response
   * @return a model map containing the resolved user under the {@code user}
   *         key, or {@code null} if an error occurred and the status was set
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
        if (
          !(this.authenticationService.getCurrentUserName().equals(userId) ||
            this.authenticationService.getCurrentUserName().equals("admin"))
        ) {
          throw new AccessDeniedException(
            "Impossible to update the LDAP DB info of somebody else"
          );
        }

        model.put("user", this.usersApi.usersUserIdGetFromLdap(userId));
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
