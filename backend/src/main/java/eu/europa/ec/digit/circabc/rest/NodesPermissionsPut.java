package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.PermissionsApi;
import io.swagger.model.PermissionDefinition;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.PermissionDefinitionJsonParser;
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
 * REST webscript endpoint that updates the permission definition of a node.
 *
 * <p>This class backs an HTTP {@code PUT} request (implied by the {@code Put}
 * suffix in the class name) on a node identified by the {@code id} path
 * template variable. It applies the permission changes carried in the JSON
 * request body to that node.
 *
 * <p>Before any change is made, the caller must hold administrative rights on
 * the target node: either the {@link LibraryPermissions#LIBADMIN} library
 * permission or the {@link NewsGroupPermissions#NWSADMIN} newsgroup permission.
 * Otherwise the request is rejected with an HTTP {@code 403 Forbidden}.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} (path variable) &ndash; identifier of the node whose
 *       permissions are updated.</li>
 *   <li>{@code language} (optional query parameter) &ndash; controls the
 *       multilingual (ML) awareness and the content/interface locale used while
 *       processing the request.</li>
 *   <li>request body &ndash; a JSON {@link PermissionDefinition} describing the
 *       permissions to apply.</li>
 * </ul>
 *
 * <p>On success the resulting {@link PermissionDefinition} is returned in the
 * model under the {@code definition} key for rendering by the associated
 * FreeMarker template.
 */
public class NodesPermissionsPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesPermissionsPut.class);

  /** API providing the business logic for reading and updating node permissions. */
  @Autowired
  private PermissionsApi permissionsApi;

  /** Service used to verify that the current user holds the required permissions on the node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the permission update for the requested node.
   *
   * <p>Reads the optional {@code language} parameter to configure ML awareness
   * and locale, resolves the node {@code id} from the URL template variables,
   * verifies that the current user has administrative permissions, then parses
   * the JSON body into a {@link PermissionDefinition} and applies it via
   * {@link PermissionsApi#nodeIdPermissionsPut(String, PermissionDefinition)}.
   * The previous ML-awareness state is always restored before returning.
   *
   * <p>Error handling sets the appropriate HTTP status and returns
   * {@code null}: {@code 403} when the user lacks permissions, {@code 400} for
   * an invalid node reference or malformed request body, and {@code 500} for
   * any other unexpected error.
   *
   * @param req the incoming web script request, providing the {@code language}
   *            parameter, the {@code id} template variable and the JSON body
   * @param status the response status to populate on success or failure
   * @param cache the cache directives for the response
   * @return a model map containing the updated permission definition under the
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
          ))
      ) {
        throw new AccessDeniedException(
          "Impossible to update the permission for authorities, not enough permissions"
        );
      }

      PermissionDefinition body = PermissionDefinitionJsonParser.parseJSON(req);
      model.put(
        "definition",
        this.permissionsApi.nodeIdPermissionsPut(id, body)
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied updating node permissions: " + ade.getMessage(),
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(
        "Error updating node permissions: " + inre.getMessage(),
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error updating node permissions: " + e.getMessage(),
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
