package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ForumsApi;
import io.swagger.model.Node;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NodeJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script backing the HTTP {@code POST} endpoint that creates new content
 * (a topic/post) inside a forum node.
 *
 * <p>The forum is identified by the {@code id} template variable taken from the request URL, and
 * the content to create is supplied as a JSON body that is deserialized into a {@link Node}. Before
 * delegating to {@link ForumsApi#forumsIdContentPost(String, Node)}, the endpoint verifies that the
 * current authority holds the {@link NewsGroupPermissions#NWSPOST} permission on the target forum.
 *
 * <p>On success the created content node is returned to the FreeMarker template under the
 * {@code post} model key. Permission failures are reported as HTTP {@code 403 Forbidden}, while
 * malformed input or repository errors are reported as HTTP {@code 400 Bad Request}.
 *
 * @author beaurpi
 */
public class ForumContentPost extends CircabcDeclarativeWebScript {

  /** API used to create the content within the target forum. */
  @Autowired
  private ForumsApi forumsApi;

  /** Service used to check that the current authority is allowed to post in the forum. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming request: creates a new content item in the forum identified by the
   * {@code id} template variable, using the JSON payload of the request as the node definition.
   *
   * <p>The current authority must hold the {@link NewsGroupPermissions#NWSPOST} permission on the
   * forum; otherwise the request is rejected. Error conditions set the appropriate HTTP status on
   * {@code status} and return {@code null} to trigger a redirect to the status template.
   *
   * @param req the web script request; provides the {@code id} template variable and the JSON body
   * @param status the response status, updated to {@code 403} or {@code 400} on failure
   * @param cache the response cache directives (unused)
   * @return the model map containing the created node under the {@code post} key, or {@code null} if
   *     the request failed and a status redirect was issued
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      if (id != null) {
        Node body = NodeJsonParser.parseSimpleJSON(req);

        if (
          !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
            id,
            NewsGroupPermissions.NWSPOST
          )
        ) {
          throw new AccessDeniedException(
            "Current Authority cannot create a topic in the forum, not enough permission"
          );
        }

        model.put("post", this.forumsApi.forumsIdContentPost(id, body));
      }
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }

  /**
   * Returns the API used to create content within a forum.
   *
   * @return the forumsApi
   */
  public ForumsApi getForumsApi() {
    return this.forumsApi;
  }
}
