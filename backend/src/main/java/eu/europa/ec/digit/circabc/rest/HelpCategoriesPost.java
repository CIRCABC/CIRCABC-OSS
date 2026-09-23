package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpCategory;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.HelpJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script handling the HTTP {@code POST} endpoint that
 * creates a new help category.
 *
 * <p>The endpoint accepts a JSON body describing the help category to create
 * and delegates the creation to {@link HelpApi#createHelpCategory(HelpCategory)}.
 * Only Alfresco administrators or CIRCABC administrators are allowed to create
 * help categories; any other user receives an HTTP {@code 403 Forbidden}
 * response.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>An optional {@code language} request parameter. When absent, the node
 *       properties are read in a multilingual-aware (ML-aware) manner. When
 *       present, the corresponding {@link Locale} is applied to the request and
 *       ML-awareness is disabled so that values are resolved for that locale.</li>
 *   <li>A JSON request body parsed into a {@link HelpCategory} via
 *       {@link HelpJsonParser#parseCategory(WebScriptRequest)}.</li>
 * </ul>
 *
 * <p>On success the created category is placed in the model under the
 * {@code "category"} key for rendering by the associated FreeMarker template.</p>
 */
public class HelpCategoriesPost extends CircabcDeclarativeWebScript {

  /** API providing help-related business operations, such as creating help categories. */
  @Autowired
  private HelpApi helpApi;

  /** Service used to verify whether the current user has administrator privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Creates a new help category from the incoming request.
   *
   * <p>Applies the optional {@code language} parameter to control ML-awareness,
   * verifies that the current user is an Alfresco or CIRCABC administrator,
   * parses the JSON body into a {@link HelpCategory} and creates it. The
   * original ML-awareness state is always restored before returning.</p>
   *
   * @param req    the web script request, providing the optional {@code language}
   *               parameter and the JSON body describing the help category
   * @param status the response status, updated to {@code 403 Forbidden} on
   *               access-denied errors or {@code 400 Bad Request} on parsing/IO errors
   * @param cache  the response cache directives
   * @return a model map containing the created category under the {@code "category"}
   *         key, or {@code null} if an error occurred and a redirect status was set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String language = req.getParameter("language");
    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    try {
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException(
          "Cannot create help category, not enough permission"
        );
      }

      HelpCategory category = HelpJsonParser.parseCategory(req);
      model.put("category", helpApi.createHelpCategory(category));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException | ParseException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Internal error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
