package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.SupportedLanguages;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
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
 * Alfresco Declarative Web Script that handles the HTTP {@code POST} creation of a new
 * space (folder) underneath an existing parent node.
 *
 * <p>The endpoint is invoked with the parent node identifier supplied as the {@code id}
 * template variable in the URL. The request body is a JSON document describing the space to
 * create, containing:
 *
 * <ul>
 *   <li>{@code name} - the space name;</li>
 *   <li>{@code title} - a map of localized titles keyed by language code;</li>
 *   <li>{@code description} - a map of localized descriptions keyed by language code;</li>
 *   <li>{@code properties.expiration_date} - an optional expiration date.</li>
 * </ul>
 *
 * <p>An optional {@code language} request parameter controls localization: when omitted the
 * script operates in multilingual (ML aware) mode, otherwise the supplied locale is applied
 * for the duration of the request.
 *
 * <p>Before creating the space, the caller must hold the Alfresco "add children" permission
 * on the parent node; otherwise the request is rejected. The created space is returned in the
 * model under the {@code space} key. Errors are reported via the response {@link Status}
 * (403 for permission failures, 400 for invalid node references/types, and 500 for parsing or
 * unexpected errors).
 *
 * @see CircabcDeclarativeWebScript
 * @see SpacesApi
 */
public class SpacePost extends CircabcDeclarativeWebScript {

  /** JSON field name holding the space name. */
  public static final String SPACE_NAME = "name";
  /** JSON field name holding the localized space titles. */
  public static final String SPACE_TITLE = "title";
  /** JSON field name holding the localized space descriptions. */
  public static final String DESCRIPTION = "description";
  /** JSON field name holding the additional space properties. */
  private static final String PROPERTIES = "properties";
  /** JSON property name holding the optional expiration date. */
  private static final String EXPIRATION_DATE = "expiration_date";
  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacePost.class);

  /** API delegate that performs the actual space creation in the repository. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to verify that the current user holds the required Alfresco permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Copies the localized values found in a JSON source object into the given target map.
   *
   * <p>Only the language codes declared in {@link SupportedLanguages#availableLangCodes} are
   * considered; any other keys present in the source are ignored. If the source is not a
   * {@link JSONObject} the target is left unchanged.
   *
   * @param source the JSON value expected to be a {@link JSONObject} of language code to value
   * @param target the map to populate with the localized values
   */
  private static void copyLocalizedValues(
    Object source,
    Map<String, String> target
  ) {
    if (source instanceof JSONObject jsonSource) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (jsonSource.containsKey(code)) {
          target.put(code, String.valueOf(jsonSource.get(code)));
        }
      }
    }
  }

  /**
   * Parses the incoming request body into a {@link Node} describing the space to create.
   *
   * <p>Reads the space name, the localized titles and descriptions, and the optional
   * {@code expiration_date} property from the JSON payload.
   *
   * @param req the web script request whose content holds the JSON space definition
   * @return a {@link Node} populated with the parsed name, titles, descriptions and properties
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  private static Node parseSpaceJSON(WebScriptRequest req)
    throws IOException, ParseException {
    Node body = new Node();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    body.setName(String.valueOf(json.get(SPACE_NAME)));

    copyLocalizedValues(json.get(SPACE_TITLE), body.getTitle());
    copyLocalizedValues(json.get(DESCRIPTION), body.getDescription());

    JSONObject properties = (JSONObject) json.get(PROPERTIES);
    if (properties != null && properties.containsKey(EXPIRATION_DATE)) {
      Object expirationDate = properties.get(EXPIRATION_DATE);
      if (expirationDate != null) {
        body
          .getProperties()
          .put(EXPIRATION_DATE, String.valueOf(expirationDate));
      }
    }

    return body;
  }

  /**
   * Executes the space creation request.
   *
   * <p>Applies the requested locale (or multilingual mode when no {@code language} parameter is
   * given), verifies that the current user may add children to the parent node identified by the
   * {@code id} template variable, parses the request body and delegates the creation to
   * {@link SpacesApi#spacesIdSpacesPost(String, Node)}. On success the created space is placed in
   * the returned model under the {@code space} key. On failure the appropriate HTTP status is set
   * on the response and {@code null} is returned. The previous ML-aware state is always restored.
   *
   * @param req the web script request, providing the {@code language} parameter, the {@code id}
   *     template variable and the JSON body
   * @param status the response status used to report success or failure
   * @param cache the response cache directives (unused)
   * @return a model map containing the created space under the {@code space} key, or {@code null}
   *     if an error occurred and the status was set accordingly
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
        !this.currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
          id
        )
      ) {
        throw new AccessDeniedException(
          "Cannot create the space, not enough permissions"
        );
      }

      Node body = parseSpaceJSON(req);

      model.put("space", this.spacesApi.spacesIdSpacesPost(id, body));
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when trying to create space in parent: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference for parent space: " + id, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException | ParseException e) {
      logger.error("Error parsing JSON for space creation in parent: " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Error");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error(
        "Invalid node type for space creation in parent: " + id,
        ite
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error during space creation in parent: " + id,
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
