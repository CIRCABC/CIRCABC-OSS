package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ForumsApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.SupportedLanguages;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that updates an existing forum.
 *
 * <p>The class name implies an HTTP {@code PUT} request. The forum to update is
 * identified by the {@code id} template variable taken from the request URL.
 * The request body is expected to be a JSON object describing the new forum
 * metadata, containing:
 *
 * <ul>
 *   <li>{@code name} - the forum name</li>
 *   <li>{@code title} - a JSON object of localized titles keyed by language
 *       code</li>
 *   <li>{@code description} - a JSON object of localized descriptions keyed by
 *       language code</li>
 * </ul>
 *
 * <p>Before applying the update, the endpoint verifies that the current
 * authority has Alfresco write permission on the target node. The actual update
 * is delegated to {@link ForumsApi#updateForum(String, Node)}. Failures are
 * translated into the appropriate HTTP status codes (403 for access denied, 400
 * for invalid node references or types, and 500 for parsing/IO errors).
 *
 * @author schwerr
 */
public class ForumsUpdatePut extends CircabcDeclarativeWebScript {

  /** JSON key holding the forum name in the request body. */
  public static final String FORUM_NAME = "name";
  /** JSON key holding the localized forum titles in the request body. */
  public static final String FORUM_TITLE = "title";
  /** JSON key holding the localized forum descriptions in the request body. */
  public static final String FORUM_DESCRIPTION = "description";
  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ForumsUpdatePut.class);

  /** API providing the forum business operations, notably the update. */
  @Autowired
  private ForumsApi forumsApi;

  /** Service used to check the current user's Alfresco permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Parses the JSON request body into a {@link Node} carrying the forum
   * metadata to update.
   *
   * <p>The forum name is read from the {@link #FORUM_NAME} key. The
   * {@link #FORUM_TITLE} and {@link #FORUM_DESCRIPTION} keys, when present as
   * JSON objects, are read entry by entry for every
   * {@link SupportedLanguages#availableLangCodes supported language code}.
   *
   * @param req the web script request whose content holds the JSON payload
   * @return a {@link Node} populated with the parsed name, titles and
   *         descriptions
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  private static Node parseForumJSON(WebScriptRequest req)
    throws IOException, ParseException {
    Node body = new Node();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    body.setName(String.valueOf(json.get(FORUM_NAME)));

    Object descriptions = json.get(FORUM_DESCRIPTION);

    if (descriptions instanceof JSONObject jsonDescriptions) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (jsonDescriptions.containsKey(code)) {
          body
            .getDescription()
            .put(code, String.valueOf(jsonDescriptions.get(code)));
        }
      }
    }

    Object titles = json.get(FORUM_TITLE);

    if (titles instanceof JSONObject jsonTitles) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (jsonTitles.containsKey(code)) {
          body.getTitle().put(code, String.valueOf(jsonTitles.get(code)));
        }
      }
    }

    return body;
  }

  /**
   * Executes the forum update request.
   *
   * <p>Resolves the target forum id from the {@code id} template variable,
   * verifies the current user has write permission on it, parses the JSON body
   * and delegates the update to {@link ForumsApi#updateForum(String, Node)}.
   *
   * @param req the web script request, providing the {@code id} template
   *            variable and the JSON body
   * @param status the response status, set to the relevant HTTP error code and
   *               marked for redirect when a failure occurs
   * @param cache the web script cache directives
   * @return an (empty) model map on success, or {@code null} when an error has
   *         been signalled through {@code status}
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
        !this.currentUserPermissionCheckerService.hasAlfrescoWritePermission(id)
      ) {
        throw new AccessDeniedException(
          "Current Authority cannot update the forum, not enough permission"
        );
      }

      Node body = parseForumJSON(req);

      this.forumsApi.updateForum(id, body);
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
