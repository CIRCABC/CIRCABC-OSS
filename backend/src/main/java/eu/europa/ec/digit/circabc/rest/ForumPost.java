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
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that creates a new (sub)forum.
 *
 * <p>As implied by the class name, this endpoint handles the HTTP {@code POST} method. Given the
 * node id of a parent forum (taken from the {@code id} URL template variable), it creates a new
 * subforum underneath it using the {@link io.swagger.model.Node} description supplied in the JSON
 * request body.
 *
 * <p>Before the subforum is created the current user must hold the
 * {@link NewsGroupPermissions#NWSMODERATE} permission on the parent node; otherwise the request is
 * rejected with an HTTP 403 (Forbidden) response. Invalid node references or types result in an
 * HTTP 400 (Bad Request), and parsing/IO problems result in an HTTP 500 (Internal Server Error).
 */
public class ForumPost extends CircabcDeclarativeWebScript {

  /** JSON/model key holding the forum name. */
  public static final String FORUM_NAME = "name";
  /** JSON/model key holding the forum title. */
  public static final String FORUM_TITLE = "title";
  /** JSON/model key holding the forum description. */
  public static final String FORUM_DESCRIPTION = "description";
  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ForumPost.class);

  /** API used to perform forum-related business operations, such as creating subforums. */
  @Autowired
  private ForumsApi forumsApi;

  /** Service used to verify the current user's permissions on the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Creates a new subforum under the parent forum identified by the {@code id} URL template
   * variable, using the {@link Node} description provided in the JSON request body.
   *
   * <p>The current user must hold the {@link NewsGroupPermissions#NWSMODERATE} permission on the
   * parent node. When an error occurs the appropriate HTTP status is set on {@code status} and
   * {@code null} is returned so that the framework renders the status page instead of the normal
   * template.
   *
   * @param req the web script request; provides the {@code id} template variable and the JSON body
   *     describing the forum to create
   * @param status the response status to populate (e.g. 403, 400 or 500) when the request fails
   * @param cache the cache directives for the response
   * @return a model map containing the created forum under the {@code "forum"} key, or {@code null}
   *     if an error occurred and a status/redirect has been set
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
      Node body = NodeJsonParser.parseSimpleJSON(req);

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
          id,
          NewsGroupPermissions.NWSMODERATE
        )
      ) {
        throw new AccessDeniedException(
          "Current Authority cannot create a new forum, not enough permission"
        );
      }

      model.put("forum", this.forumsApi.forumsIdSubforumsPost(id, body));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException | ParseException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Error");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error(ERROR_OCCURRED, ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
