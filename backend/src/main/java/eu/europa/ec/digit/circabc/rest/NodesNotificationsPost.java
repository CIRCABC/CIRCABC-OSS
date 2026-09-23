package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NotificationsApi;
import io.swagger.model.NotificationDefinition;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NotificationDefinitionJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script that handles the HTTP {@code POST} request for
 * updating the notification configuration of a node.
 *
 * <p>It backs the REST endpoint {@code POST /nodes/{id}/notifications}, allowing a
 * caller to set the notification definition (the list of authorities that are
 * notified about changes) for the node identified by the {@code id} template
 * variable. The notification definition is read from the JSON request body.</p>
 *
 * <p>Access is restricted: the current user must hold library admin
 * ({@link LibraryPermissions#LIBADMIN}), newsgroup admin
 * ({@link NewsGroupPermissions#NWSADMIN}) or interest group admin rights on the
 * target node. Otherwise an {@link AccessDeniedException} is raised and the
 * response is set to {@code 403 Forbidden}.</p>
 *
 * <p>An optional {@code language} request parameter controls the content locale
 * used while processing the request; when absent, multilingual (ML) awareness is
 * enabled instead.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see NotificationsApi
 */
public class NodesNotificationsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesNotificationsPost.class);

  /**
   * API used to persist the notification definition for the target node.
   */
  @Autowired
  private NotificationsApi notificationsApi;

  /**
   * Service used to verify that the current user has sufficient permissions on
   * the target node before the notification definition is updated.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code POST /nodes/{id}/notifications} request.
   *
   * <p>Resolves the target node from the {@code id} template variable, checks that
   * the current user is a library, newsgroup or group administrator on that node,
   * parses the {@link NotificationDefinition} from the JSON request body and
   * delegates the update to {@link NotificationsApi#nodesIdNotificationsPost}. The
   * resulting definition is exposed to the response template under the
   * {@code definition} model key.</p>
   *
   * <p>Errors are translated into HTTP status codes rather than propagated:
   * insufficient permissions yield {@code 403 Forbidden}, an invalid node
   * reference, malformed JSON or an I/O failure yield {@code 400 Bad Request}, and
   * any other unexpected failure yields {@code 500 Internal Server Error}. In all
   * error cases {@code null} is returned and a redirect status is set.</p>
   *
   * @param req the web script request; provides the {@code id} template variable,
   *            the optional {@code language} parameter and the JSON body
   * @param status the response status, updated with the appropriate HTTP code when
   *               an error occurs
   * @param cache the response cache directives
   * @return a model map containing the persisted notification definition under the
   *         {@code definition} key, or {@code null} if the request failed
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
          ) ||
          this.currentUserPermissionCheckerService.isGroupAdmin(id))
      ) {
        throw new AccessDeniedException(
          "Impossible to post the list of notifiied authorities, not enough permissions"
        );
      }

      NotificationDefinition body = NotificationDefinitionJsonParser.parseJSON(
        req
      );
      model.put(
        "definition",
        this.notificationsApi.nodesIdNotificationsPost(id, body)
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when posting notifications for node " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(
        "Invalid request when posting notifications for node " + id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when posting notifications for node " + id,
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
