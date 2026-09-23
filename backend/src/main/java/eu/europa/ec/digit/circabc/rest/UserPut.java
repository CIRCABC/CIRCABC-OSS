package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco web script endpoint handling the HTTP {@code PUT} request that updates
 * an existing user's profile.
 *
 * <p>The endpoint is addressed with the target user's identifier taken from the
 * {@code userId} URL template variable and expects a JSON body describing the
 * profile fields to update (for example {@code firstname}, {@code lastname},
 * {@code email}, {@code phone}, UI/content languages, avatar, visibility and the
 * extended properties listed in {@link #PROPERTY_KEYS}). An optional
 * {@code language} request parameter controls the locale used for multilingual
 * (ML) content resolution.
 *
 * <p>Only the authenticated user themselves or an Alfresco administrator may
 * update a given profile; any other caller receives an HTTP 403 (Forbidden)
 * response. The updated user is placed in the model under the {@code user} key
 * for template rendering. The actual persistence is delegated to
 * {@link UsersApi#usersUserIdPut(String, User)}.
 */
public class UserPut extends CircabcDeclarativeWebScript {

  /** JSON/property key toggling whether the user receives global notifications. */
  public static final String GLOBAL_NOTIFICATION_ENABLED =
    "globalNotificationEnabled";
  /** Property key for the user's title. */
  public static final String TITLE = "title";
  /** Property key for the user's organisation. */
  public static final String ORGANISATION = "organisation";
  /** Property key for the user's postal address. */
  public static final String POSTAL_ADDRESS = "postalAddress";
  /** Property key for the user's free-text description. */
  public static final String DESCRIPTION = "description";
  /** Property key for the user's URL / website address. */
  public static final String URL_ADDRESS = "urlAddress";
  /** Property key for the user's signature. */
  public static final String SIGNATURE = "signature";
  /** Logger used to report failures encountered while updating a user. */
  static final Log logger = LogFactory.getLog(UserPut.class);

  /**
   * Whitelist of extended profile property keys that are read from the
   * {@code properties} object of the incoming JSON body and copied onto the
   * {@link User} to be updated. Any key not present in this array is ignored.
   */
  private static final String[] PROPERTY_KEYS = {
    TITLE,
    ORGANISATION,
    POSTAL_ADDRESS,
    DESCRIPTION,
    "fax",
    URL_ADDRESS,
    GLOBAL_NOTIFICATION_ENABLED,
    SIGNATURE,
  };

  /** API facade delegating the actual user update to the business layer. */
  @Autowired
  private UsersApi usersApi;

  /** Service used to verify the caller is allowed to update the target user. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the user update request.
   *
   * <p>Resolves the {@code userId} URL template variable, checks that the caller
   * is authorised to update that user, parses the JSON request body into a
   * {@link User} and delegates the update to
   * {@link UsersApi#usersUserIdPut(String, User)}. The resulting user is added to
   * the returned model under the {@code user} key. The multilingual awareness flag
   * is restored to its original value once processing completes.
   *
   * @param req the web script request; supplies the {@code userId} template
   *     variable, the optional {@code language} parameter and the JSON body
   * @param status the response status, updated with the appropriate HTTP code and
   *     message when an error occurs
   * @param cache the response cache directives (unused)
   * @return the model map containing the updated {@code user}, or {@code null}
   *     when an error has been handled and reported through {@code status}
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    setupLocale(req.getParameter("language"));

    try {
      String userId = req.getServiceMatch().getTemplateVars().get("userId");
      validatePermission(userId);

      if (userId != null) {
        User body = parseUserJSON(req);
        model.put("user", usersApi.usersUserIdPut(userId, body));
      }
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  /**
   * Configures the locale used for multilingual property resolution.
   *
   * <p>When no language is supplied, ML awareness is enabled so that the full set
   * of localized values is returned. When a language is provided, the content and
   * UI locales are set to it and ML awareness is disabled so that values are
   * resolved for that specific locale.
   *
   * @param language the requested language tag, or {@code null} to enable ML-aware
   *     handling
   */
  private void setupLocale(String language) {
    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }
  }

  /**
   * Ensures the caller is permitted to update the given user.
   *
   * <p>The update is only allowed when the current user is updating their own
   * profile or is an Alfresco administrator.
   *
   * @param userId the identifier of the user being updated
   * @throws AccessDeniedException if the current user is neither the target user
   *     nor an Alfresco administrator
   */
  private void validatePermission(String userId) {
    if (
      !currentUserPermissionCheckerService.isCurrentUserEqualTo(userId) &&
      !currentUserPermissionCheckerService.isAlfrescoAdmin()
    ) {
      throw new AccessDeniedException(
        "Impossible to update the LDAP DB info of somebody else"
      );
    }
  }

  /**
   * Parses the JSON request body into a {@link User} instance.
   *
   * <p>Scalar fields are copied only when present in the body (see
   * {@link #setIfNotNull}); the {@code visibility} flag is applied only when it is
   * a boolean; and extended properties are extracted through
   * {@link #parseProperties(JSONObject, User)}.
   *
   * @param req the web script request whose content holds the JSON body
   * @return a {@link User} populated with the supplied fields
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  private static User parseUserJSON(WebScriptRequest req)
    throws IOException, ParseException {
    User body = new User();
    JSONObject json = (JSONObject) new JSONParser().parse(
      req.getContent().getContent()
    );

    setIfNotNull(body::setUserId, json.get("userId"));
    setIfNotNull(body::setFirstname, json.get("firstname"));
    setIfNotNull(body::setLastname, json.get("lastname"));
    setIfNotNull(body::setEmail, json.get("email"));
    setIfNotNull(body::setPhone, json.get("phone"));
    setIfNotNull(body::setUiLang, json.get("uiLang"));
    setIfNotNull(body::setContentFilterLang, json.get("contentFilterLang"));
    setIfNotNull(body::setAvatar, json.get("avatar"));

    Object visibility = json.get("visibility");
    if (visibility instanceof Boolean b) {
      body.setVisibility(b);
    }

    parseProperties(json, body);
    return body;
  }

  /**
   * Invokes the given setter with the string form of the value, but only when the
   * value is non-{@code null}.
   *
   * @param setter the setter to invoke with the resolved string value
   * @param value the raw JSON value; ignored when {@code null}
   */
  private static void setIfNotNull(
    java.util.function.Consumer<String> setter,
    Object value
  ) {
    if (value != null) {
      setter.accept(String.valueOf(value));
    }
  }

  /**
   * Extracts the whitelisted extended properties from the {@code properties}
   * object of the JSON body and sets them on the target user.
   *
   * <p>Only keys contained in {@link #PROPERTY_KEYS} are considered; if the body
   * contains no {@code properties} object the user is left unchanged.
   *
   * @param json the parsed JSON request body
   * @param body the {@link User} to populate with the extended properties
   */
  private static void parseProperties(JSONObject json, User body) {
    JSONObject suppliedProperties = (JSONObject) json.get("properties");
    if (suppliedProperties == null) return;

    Map<String, String> properties = new HashMap<>();
    for (String key : PROPERTY_KEYS) {
      if (suppliedProperties.get(key) != null) {
        properties.put(key, String.valueOf(suppliedProperties.get(key)));
      }
    }
    body.setProperties(properties);
  }

  /**
   * Logs the given error and populates the response status so the failure is
   * reported to the client.
   *
   * @param status the response status to update
   * @param code the HTTP status code to set
   * @param message the short message describing the error
   * @param e the exception that caused the failure
   * @return {@code null} to signal that no model should be rendered
   */
  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e
  ) {
    logger.error(message + " while updating user: " + e.getMessage(), e);
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }
}
