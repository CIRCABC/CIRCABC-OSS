package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.PermissionsApi;
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
 * REST webscript endpoint that returns the permission definition of a node.
 *
 * <p>Handles the HTTP {@code GET} request (as implied by the {@code Get} suffix
 * in the class name) for a node identified by the {@code id} template variable
 * in the URL. It delegates to {@link PermissionsApi#getNodeIdPermissionsGet(String)}
 * to build the permission definition and places the result in the model under
 * the {@code definition} key for FreeMarker rendering.</p>
 *
 * <p>Access is restricted: the current user must hold either the library
 * administration permission ({@link LibraryPermissions#LIBADMIN}) or the
 * newsgroup administration permission ({@link NewsGroupPermissions#NWSADMIN})
 * on the target node, otherwise an {@link AccessDeniedException} is raised and
 * translated to an HTTP {@code 403 Forbidden} response.</p>
 *
 * <p>An optional {@code language} request parameter controls localization: when
 * absent the endpoint operates in multilingual-aware mode; when present it sets
 * the content and UI locale accordingly for the duration of the request.</p>
 */
public class NodesPermissionsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesPermissionsGet.class);

  /**
   * API providing access to node permission definitions.
   */
  @Autowired
  private PermissionsApi permissionsApi;

  /**
   * Service used to verify that the current user holds the permissions
   * required to read the node's permission definition.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Builds the response model containing the permission definition of the
   * requested node.
   *
   * <p>Reads the {@code id} template variable to identify the node and the
   * optional {@code language} request parameter to configure localization. It
   * checks that the current user has library or newsgroup administration
   * rights on the node before retrieving its permission definition. On error,
   * the appropriate HTTP status is set on {@code status}, a redirect is
   * requested, and {@code null} is returned. The multilingual-aware flag is
   * always restored before the method returns.</p>
   *
   * @param req the web script request, providing the {@code id} template
   *     variable and the optional {@code language} parameter
   * @param status the web script response status, updated with an error code
   *     and message when the request cannot be fulfilled
   * @param cache the web script response cache control settings
   * @return a model map with the node permission {@code definition}, or
   *     {@code null} when an error occurred and an error status was set
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
          "Impossible to get the permission for authorities, not enough permissions"
        );
      }

      model.put("definition", this.permissionsApi.getNodeIdPermissionsGet(id));
    } catch (AccessDeniedException ade) {
      logger.error("Error getting node permissions: " + ade.getMessage(), ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference: " + inre.getMessage(), inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error getting node permissions: " + e.getMessage(),
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
