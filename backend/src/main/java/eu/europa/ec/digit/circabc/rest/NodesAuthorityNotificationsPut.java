package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.NotificationsApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that updates the notification subscription of a
 * given authority (user) on a specific node.
 *
 * <p>The class name follows the CIRCABC convention {@code <Entity><Method>},
 * so this endpoint handles the HTTP {@code PUT} verb. It is typically bound to
 * a URL of the form {@code .../nodes/{id}/notifications/{authority}}, where:
 *
 * <ul>
 *   <li>{@code id} — the node reference identifier whose notification
 *       subscription is being changed;</li>
 *   <li>{@code authority} — the user (authority) whose subscription is
 *       updated.</li>
 * </ul>
 *
 * <p>The request body carries the new notification setting (quotes are stripped
 * before being forwarded to the service layer). An optional {@code language}
 * request parameter controls the content {@link Locale} and toggles the
 * {@link MLPropertyInterceptor} multilingual awareness for the duration of the
 * call.
 *
 * <p>Authorization rules: the change is only permitted when the current user is
 * the target {@code authority} itself, or holds the
 * {@link LibraryPermissions#LIBADMIN} library permission, or holds the
 * {@link NewsGroupPermissions#NWSADMIN} newsgroup permission on the node.
 * Otherwise an {@link AccessDeniedException} is raised and mapped to an HTTP
 * {@code 403} response.
 *
 * @see CircabcDeclarativeWebScript
 * @see NotificationsApi
 */
public class NodesAuthorityNotificationsPut
  extends CircabcDeclarativeWebScript
{

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    NodesAuthorityNotificationsPut.class
  );

  /**
   * API used to persist the notification subscription change for the target
   * node and authority.
   */
  @Autowired
  private NotificationsApi notificationsApi;

  /**
   * Alfresco authentication service used to resolve the current user name for
   * the self-modification authorization check.
   */
  @Autowired
  private AuthenticationService authenticationService;

  /**
   * Service used to verify that the current user holds the required library or
   * newsgroup administration permissions on the node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the endpoint logic: validates permissions and updates the
   * notification subscription of the given authority on the given node.
   *
   * <p>Reads the {@code id} and {@code authority} URL template variables and
   * the optional {@code language} request parameter. On success the
   * subscription value from the request body is forwarded to
   * {@link NotificationsApi#nodesIdNotificationsAuthorityPut(String, String, String)}.
   * On failure the appropriate HTTP status is set on {@code status} and
   * {@code null} is returned so the framework renders the error response.
   *
   * @param req the incoming web script request; provides the URL template
   *     variables ({@code id}, {@code authority}), the optional
   *     {@code language} parameter and the request body
   * @param status the response status object, populated with the HTTP status
   *     code and message when an error occurs
   * @param cache the response cache directives object
   * @return an empty model map on success, or {@code null} when an error has
   *     been handled and mapped to an HTTP error status
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
    String id = templateVars.get("id");
    String authority = templateVars.get("authority");

    try {
      // only the current user can change its own conf or libadmin or newsadmin
      if (
        !(this.authenticationService.getCurrentUserName().equals(authority) ||
          this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
            id,
            LibraryPermissions.LIBADMIN
          ) ||
          this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
            id,
            NewsGroupPermissions.NWSADMIN
          ))
      ) {
        throw new AccessDeniedException(
          "Impossible to change the notification subscription"
        );
      }

      String body = req.getContent().getContent();
      body = body.trim();
      body = body.replace("\"", "");
      this.notificationsApi.nodesIdNotificationsAuthorityPut(
        id,
        authority,
        body
      );
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied: " + ade.getMessage(), ade);
      }
      return null; // NOSONAR
    } catch (InvalidNodeRefException | IOException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Invalid request: " + inre.getMessage(), inre);
      }
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Unexpected error: " + e.getMessage(), e);
      }
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
