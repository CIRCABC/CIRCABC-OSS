package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NotificationsApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
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
 * Alfresco declarative web script that handles the HTTP {@code GET} request for
 * retrieving the notification definition of a given repository node.
 *
 * <p>The endpoint is mapped to a URL of the form {@code .../nodes/{id}/notifications},
 * where {@code id} is the node reference identifier extracted from the URL template
 * variables. It returns the list of authorities notified for the node, exposed in the
 * response model under the {@code definition} key.
 *
 * <p>Access is restricted: the current user must hold either the library administration
 * permission ({@link LibraryPermissions#LIBADMIN}) or the news group administration
 * permission ({@link NewsGroupPermissions#NWSADMIN}) on the target node. Requests that
 * fail this check are rejected with an HTTP {@code 403 Forbidden} status.
 *
 * <p>An optional {@code language} request parameter controls localization: when supplied
 * it sets the content and UI locale and disables multilingual (ML) awareness; when absent
 * the web script runs in ML-aware mode. The previous ML-awareness state is always restored
 * before the method returns.
 */
public class NodesNotificationsGet extends DeclarativeWebScript {

  /** Logger used to report access, node-reference and unexpected errors. */
  static final Log logger = LogFactory.getLog(NodesNotificationsGet.class);

  /** API providing access to node notification definitions. */
  @Autowired
  private NotificationsApi notificationsApi;

  /** Service used to verify the current user's permissions on the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Retrieves the notification definition for the node identified by the {@code id} URL
   * template variable.
   *
   * <p>Reads the optional {@code language} request parameter to configure localization and
   * ML awareness, verifies that the current user has library or news group administration
   * rights on the node, and, when authorized, populates the response model with the node's
   * notification definition under the {@code definition} key. On failure the appropriate
   * HTTP error status is set and {@code null} is returned. The multilingual awareness flag
   * is restored to its original value before returning.
   *
   * @param req the web script request; supplies the {@code id} template variable and the
   *     optional {@code language} parameter
   * @param status the web script response status, updated with the relevant HTTP status code
   *     ({@code 403}, {@code 400} or {@code 500}) when an error occurs
   * @param cache the web script response cache directives
   * @return a model map containing the {@code definition} entry on success, or {@code null}
   *     when an error occurs and an error status has been set
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
    try {
      if (
        !(this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
            id,
            LibraryPermissions.LIBADMIN
          ) ||
          this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
            id,
            NewsGroupPermissions.NWSADMIN
          ))
      ) {
        throw new AccessDeniedException(
          "Impossible to get the list of notifiied authorities, not enough permissions"
        );
      }

      model.put(
        "definition",
        this.notificationsApi.nodesIdNotificationsGet(id)
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when getting notifications for node " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when getting notifications for node " + id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when getting notifications for node " + id,
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
