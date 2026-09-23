package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that creates a shared space link.
 *
 * <p>Handles an HTTP {@code POST} request (as implied by the {@code Post} suffix in the class name)
 * that creates a shared link pointing to an existing space. The source space is identified by the
 * {@code id} path variable, while the target parent space and the link metadata are supplied as
 * request parameters:
 *
 * <ul>
 *   <li>{@code id} (path variable) &mdash; identifier of the space to be shared.
 *   <li>{@code parentId} (request parameter) &mdash; identifier of the parent space where the shared
 *       link is created.
 *   <li>{@code title} (request parameter) &mdash; optional title for the shared space link.
 *   <li>{@code description} (request parameter) &mdash; optional description for the shared space
 *       link.
 * </ul>
 *
 * <p>Before creating the link, the endpoint verifies that the current user holds the Alfresco
 * "add children" permission on the target parent space. On success the returned model contains a
 * {@code message} entry set to {@code "ok"}. Permission failures result in an HTTP
 * {@code 403 Forbidden} response, while any other error results in an HTTP {@code 406 Not
 * Acceptable} response.
 *
 * @see CircabcDeclarativeWebScript
 */
public class SpacesIdCreateSharePost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacesIdCreateSharePost.class);

  /** API providing the space operations, including creation of shared space links. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to verify the current user's Alfresco permissions on target nodes. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Creates a shared space link for the requested space.
   *
   * <p>Reads the source space {@code id} from the URL template variables and the {@code parentId},
   * {@code title} and {@code description} from the request parameters, checks that the current user
   * can add children to the parent space, and delegates the creation to
   * {@link SpacesApi#createSharedSpaceLink(String, String, String, String)}. Multilingual property
   * interception is temporarily disabled during the operation and restored afterwards.
   *
   * @param req the web script request; provides the {@code id} template variable and the
   *     {@code parentId}, {@code title} and {@code description} parameters
   * @param status the response status, updated to {@code 403 Forbidden} on permission errors or
   *     {@code 406 Not Acceptable} on other failures
   * @param cache the response cache control settings
   * @return a model map containing a {@code message} entry set to {@code "ok"} on success, or
   *     {@code null} when an error occurs and a redirect status has been set
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
    String parentId = req.getParameter("parentId");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
          parentId
        )
      ) {
        throw new AccessDeniedException(
          "Cannot add the shared space, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      String title = req.getParameter("title");
      String description = req.getParameter("description");

      this.spacesApi.createSharedSpaceLink(
        spaceId,
        parentId,
        title,
        description
      );

      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied creating shared space with id: " +
          spaceId +
          " in parent: " +
          parentId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error creating shared space with id: " +
          spaceId +
          " in parent: " +
          parentId,
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
