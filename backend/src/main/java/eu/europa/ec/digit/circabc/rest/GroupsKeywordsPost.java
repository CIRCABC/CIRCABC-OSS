package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.KeywordJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that handles the creation of a keyword for an interest
 * group.
 *
 * <p>Mapped to an HTTP {@code POST} request (as implied by the {@code Post} suffix
 * in the class name), this endpoint creates a new keyword definition within the
 * interest group identified by the {@code igId} template variable in the request
 * URL. The keyword payload is read from the JSON request body.
 *
 * <p>Only users who are administrators of the target group are allowed to create a
 * keyword. If the caller lacks that privilege, the endpoint responds with HTTP
 * {@code 403 Forbidden}. Malformed input (invalid node reference, unparseable JSON
 * body or an I/O error while reading the request) results in an HTTP
 * {@code 400 Bad Request} response.
 *
 * <p>On success the created keyword is returned to the FreeMarker template under the
 * {@code keyword} model key.
 */
public class GroupsKeywordsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsKeywordsPost.class);

  /** API providing the business logic for managing interest group keywords. */
  @Autowired
  private KeywordsApi keywordsApi;

  /** Service used to verify that the current user has group administrator rights. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Creates a new keyword for the interest group referenced by the request.
   *
   * <p>The interest group identifier is extracted from the {@code igId} URL template
   * variable and the keyword definition is parsed from the JSON request body. The
   * current user must be an administrator of the target group; otherwise access is
   * denied.
   *
   * @param req the web script request; supplies the {@code igId} template variable
   *     and the JSON body describing the keyword to create
   * @param status the response status, set to {@code 403} when the user is not a
   *     group administrator and to {@code 400} when the request is malformed
   * @param cache the web script response cache directives
   * @return a model map containing the created keyword under the {@code keyword} key
   *     on success, or {@code null} when the request fails and a status code has been
   *     set for the redirect
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

    try {
      if (!this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
        throw new AccessDeniedException(
          "Not enought permissions to create a new keyword"
        );
      }

      KeywordDefinition body = KeywordJsonParser.parseJsonFullKeyword(req);
      model.put("keyword", this.keywordsApi.groupsIdKeywordsPost(id, body));
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
}
