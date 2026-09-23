package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.TopicsApi;
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
 * Alfresco Web Script endpoint that updates an existing discussion topic.
 *
 * <p>As implied by the {@code Put} suffix in the class name, this endpoint handles HTTP
 * {@code PUT} requests. The topic to update is identified by the {@code id} template variable
 * taken from the request URL. The request body is expected to be a JSON object describing the
 * updated topic and is parsed into a {@link Node} before being passed to
 * {@link io.swagger.api.TopicsApi#updateTopic(String, Node)}.</p>
 *
 * <p>Recognised JSON fields are:</p>
 * <ul>
 *   <li>{@code name} - the topic name (see {@link #TOPIC_NAME}).</li>
 *   <li>{@code title} - an i18n map of language code to localized title (see {@link #TOPIC_TITLE}).</li>
 *   <li>{@code description} - an i18n map of language code to localized description
 *       (see {@link #TOPIC_DESCRIPTION}).</li>
 *   <li>{@code properties} - additional properties such as {@code security_ranking} and
 *       {@code expiration_date} (see {@link #TOPIC_PROPERTIES}).</li>
 * </ul>
 *
 * <p>Before performing the update, the endpoint verifies that the current user has Alfresco write
 * permission on the target node. Errors are mapped to the appropriate HTTP status codes (for
 * example {@code 403 Forbidden} for access denial and {@code 400 Bad Request} for an invalid node
 * reference or type).</p>
 */
public class TopicsUpdatePut extends CircabcDeclarativeWebScript {

  /** JSON field name holding the topic's (non-localized) name. */
  public static final String TOPIC_NAME = "name";
  /** JSON field name holding the topic's localized title map (language code to value). */
  public static final String TOPIC_TITLE = "title";
  /** JSON field name holding the topic's localized description map (language code to value). */
  public static final String TOPIC_DESCRIPTION = "description";
  /** JSON field name holding the topic's additional properties object. */
  public static final String TOPIC_PROPERTIES = "properties";
  /** Logger used to report errors raised while updating a topic. */
  static final Log logger = LogFactory.getLog(TopicsUpdatePut.class);

  /** API providing the topic business operations, notably {@code updateTopic}. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to check whether the current user is allowed to modify the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code PUT} request that updates a topic.
   *
   * <p>Reads the {@code id} template variable from the request URL, checks the current user's write
   * permission on that node, parses the JSON request body into a {@link Node} and delegates the
   * update to {@link TopicsApi#updateTopic(String, Node)}.</p>
   *
   * @param req the incoming web script request; supplies the {@code id} template variable and the
   *     JSON body
   * @param status the response status object, populated with an error code and message when the
   *     update fails
   * @param cache the cache control object for the response (unused by this endpoint)
   * @return an empty model map on success, or {@code null} when an error occurred (in which case the
   *     {@code status} object carries the error details)
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    String id = req.getServiceMatch().getTemplateVars().get("id");

    try {
      validatePermission(id);
      Node body = parseForumJSON(req);
      topicsApi.updateTopic(id, body);
    } catch (AccessDeniedException ade) {
      return handleError(status, Status.STATUS_FORBIDDEN, "Access denied", ade);
    } catch (InvalidNodeRefException inre) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        "Bad request",
        inre
      );
    } catch (IOException | ParseException e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Error",
        e
      );
    } catch (InvalidTypeException ite) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        "Bad noderef type",
        ite
      );
    } catch (Exception e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error",
        e
      );
    }
    return model;
  }

  /**
   * Ensures the current user is allowed to update the given node.
   *
   * @param id the node reference identifier of the topic to update
   * @throws AccessDeniedException if the current user does not have Alfresco write permission on the
   *     node
   */
  private void validatePermission(String id) {
    if (!currentUserPermissionCheckerService.hasAlfrescoWritePermission(id)) {
      throw new AccessDeniedException(
        "Cannot update the topic, not enough permissions"
      );
    }
  }

  /**
   * Parses the JSON request body into a {@link Node} representing the topic update.
   *
   * <p>Extracts the {@code name} field, the localized {@code title} and {@code description} maps and
   * the additional {@code properties}.</p>
   *
   * @param req the web script request carrying the JSON body
   * @return a {@link Node} populated from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  private static Node parseForumJSON(WebScriptRequest req)
    throws IOException, ParseException {
    Node body = new Node();
    JSONObject json = (JSONObject) new JSONParser().parse(
      req.getContent().getContent()
    );

    body.setName(String.valueOf(json.get(TOPIC_NAME)));
    parseI18nField(json, TOPIC_DESCRIPTION, body.getDescription());
    parseI18nField(json, TOPIC_TITLE, body.getTitle());
    parseProperties(json, body);

    return body;
  }

  /**
   * Copies the localized values of an i18n JSON field into the given target map.
   *
   * <p>The field is expected to be a JSON object mapping language codes to strings. Only language
   * codes listed in {@link SupportedLanguages#availableLangCodes} are considered. If the field is
   * absent or is not a JSON object, the target map is left unchanged.</p>
   *
   * @param json the parsed JSON request body
   * @param fieldName the name of the i18n field to read (e.g. {@code title} or {@code description})
   * @param target the map to populate with language code to localized value entries
   */
  private static void parseI18nField(
    JSONObject json,
    String fieldName,
    Map<String, String> target
  ) {
    Object field = json.get(fieldName);
    if (!(field instanceof JSONObject)) return;

    JSONObject fieldJson = (JSONObject) field;
    for (String code : SupportedLanguages.availableLangCodes) {
      if (fieldJson.containsKey(code)) {
        target.put(code, String.valueOf(fieldJson.get(code)));
      }
    }
  }

  /**
   * Copies the {@code security_ranking} and {@code expiration_date} entries from the JSON
   * {@code properties} object into the given node's properties.
   *
   * <p>If the {@code properties} field is absent, the node is left unchanged.</p>
   *
   * @param json the parsed JSON request body
   * @param body the node whose properties are to be populated
   */
  private static void parseProperties(JSONObject json, Node body) {
    @SuppressWarnings("unchecked")
    Map<String, String> properties = (Map<String, String>) json.get(
      TOPIC_PROPERTIES
    );
    if (properties != null) {
      body
        .getProperties()
        .put("security_ranking", properties.get("security_ranking"));
      body
        .getProperties()
        .put("expiration_date", properties.get("expiration_date"));
    }
  }

  /**
   * Logs the given error and populates the response status so the client receives the appropriate
   * HTTP error code and message.
   *
   * @param status the response status object to update
   * @param code the HTTP status code to set on the response
   * @param message the human-readable error message to log and return to the client
   * @param e the exception that triggered the error, logged for diagnostics
   * @return {@code null}, signalling to the web script framework that the request failed
   */
  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e
  ) {
    if (logger.isErrorEnabled()) {
      logger.error(message + " when updating topic", e);
    }
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }
}
