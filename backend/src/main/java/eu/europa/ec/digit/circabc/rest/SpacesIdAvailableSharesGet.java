package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
import io.swagger.model.ShareSpaceItem;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco web script endpoint that handles the HTTP {@code GET} request for
 * retrieving the shared spaces available for a given space.
 *
 * <p>Given the target space identifier supplied as the {@code id} path
 * template variable, this endpoint verifies that the current user has Alfresco
 * read permission on that space and then returns the list of
 * {@link ShareSpaceItem} instances that can be shared with it. The resulting
 * list is exposed to the response template under the {@code shares} model key.
 *
 * <p>Multilingual (ML) property interception is temporarily disabled while the
 * shared spaces are resolved and restored to its previous state afterwards.
 *
 * <p>Error handling:
 * <ul>
 *   <li>If the user lacks read permission, the response status is set to
 *       {@code 403 Forbidden}.</li>
 *   <li>Any other failure results in a {@code 406 Not Acceptable} status
 *       carrying the underlying exception message.</li>
 * </ul>
 */
public class SpacesIdAvailableSharesGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacesIdAvailableSharesGet.class);

  /**
   * API used to resolve the shared spaces available for a given space.
   */
  @Autowired
  private SpacesApi spacesApi;

  /**
   * Service used to check whether the current user holds the required
   * Alfresco permissions on the target space.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request and builds the response model containing
   * the shared spaces available for the requested space.
   *
   * <p>The target space is identified by the {@code id} path template
   * variable. After confirming the current user has Alfresco read permission
   * on the space, the available shared spaces are collected and placed in the
   * returned model under the {@code shares} key.
   *
   * @param req the web script request; its service match must provide the
   *     {@code id} template variable identifying the target space
   * @param status the response status, updated to {@code 403 Forbidden} when
   *     permission is denied or {@code 406 Not Acceptable} on any other error
   * @param cache the response cache directives
   * @return a model map containing the {@code shares} list on success, or
   *     {@code null} when an error occurs and the status has been set for
   *     redirection
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String spaceId = templateVars.get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
          spaceId
        )
      ) {
        throw new AccessDeniedException(
          "Cannot read the shared spaces, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      List<ShareSpaceItem> shares = this.spacesApi.getAvailableSharedSpaces(
        spaceId
      );

      model.put("shares", shares);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied for space with id: " + spaceId, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error getting available shared spaces for space with id: " + spaceId,
        e
      );
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
