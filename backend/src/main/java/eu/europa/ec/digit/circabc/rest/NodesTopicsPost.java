package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.ContentApi;
import io.swagger.model.Node;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NodeJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that creates a new topic (discussion thread) under a
 * given node.
 *
 * <p>This endpoint handles an HTTP {@code POST} request (as implied by the
 * {@code Post} suffix in the class name) targeting a parent node identified by
 * the {@code id} template variable in the request URL. The parent node is
 * typically a newsgroup/forum container in which the topic is created.
 *
 * <p>Processing performed by {@link #executeImpl(WebScriptRequest, Status,
 * Cache)}:
 * <ul>
 *   <li>Verifies that the current user holds the Alfresco
 *       {@code AddChildren} permission on the parent node; otherwise the
 *       request is rejected with HTTP 403 (Forbidden).</li>
 *   <li>Parses the request body into a {@link Node} describing the topic to
 *       create. When no explicit name is supplied, the topic name defaults to
 *       the default-locale value of the topic title.</li>
 *   <li>Delegates the actual creation to
 *       {@link ContentApi#contentIdTopicsPost(String, Node)} and exposes the
 *       created topic in the model under the {@code "topic"} key.</li>
 * </ul>
 *
 * <p>Error handling maps failures to HTTP status codes: access errors to 403,
 * invalid node references / malformed request bodies to 400 (Bad Request), and
 * any other unexpected failure to 500 (Internal Server Error).
 *
 * @see CircabcDeclarativeWebScript
 * @see ContentApi#contentIdTopicsPost(String, Node)
 */
public class NodesTopicsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesTopicsPost.class);

  /**
   * API used to perform content operations, including creating the topic under
   * the target node.
   */
  @Autowired
  private ContentApi contentApi;

  /**
   * Service used to check the current user's Alfresco permissions on the target
   * node before allowing topic creation.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Creates a new topic under the node identified by the {@code id} template
   * variable.
   *
   * <p>The method checks the current user's {@code AddChildren} permission,
   * parses the topic definition from the request body, derives a default name
   * from the title when none is provided, and creates the topic through the
   * {@link ContentApi}. On failure the appropriate HTTP status is set on
   * {@code status} and {@code null} is returned.
   *
   * @param req the web script request; must supply the parent node
   *     {@code id} as a template variable and a JSON body describing the topic
   * @param status the response status to populate (used to signal 403, 400 or
   *     500 on error)
   * @param cache the cache directive for the response
   * @return a model map containing the created topic under the {@code "topic"}
   *     key on success, or {@code null} when an error occurred and an error
   *     status has been set
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
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
          id
        )
      ) {
        throw new AccessDeniedException(
          "cannot create the topic, not enough permissions"
        );
      }

      Node body = NodeJsonParser.parseSimpleJSON(req);

      MLText title = Converter.toMLText(body.getTitle());
      if ((body.getName() == null) || body.getName().isEmpty()) {
        body.setName(title.getDefaultValue());
      }

      model.put("topic", this.contentApi.contentIdTopicsPost(id, body));
    } catch (AccessDeniedException ade) {
      logger.error("Access denied creating topic: " + ade.getMessage(), ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error("Error creating topic: " + inre.getMessage(), inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error creating topic: " + e.getMessage(), e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
