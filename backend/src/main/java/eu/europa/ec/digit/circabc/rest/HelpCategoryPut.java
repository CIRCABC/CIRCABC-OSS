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
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that updates an existing help category.
 *
 * <p>Bound to an HTTP {@code PUT} request (as implied by the {@code Put} suffix
 * in the class name), this endpoint replaces the details of the help category
 * identified by the {@code id} template variable in the request URL with the
 * data supplied in the JSON request body.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} template variable &mdash; identifier of the help category to
 *       update; must not be empty.</li>
 *   <li>{@code language} request parameter (optional) &mdash; when present, the
 *       content and UI locale are set to this language and multilingual (ML)
 *       awareness is disabled so that a single-language value is written;
 *       when absent, ML awareness is enabled so the multilingual value is
 *       preserved.</li>
 *   <li>JSON request body &mdash; parsed into a {@link HelpCategory} describing
 *       the new state of the category.</li>
 * </ul>
 *
 * <p>Only Alfresco administrators or CIRCABC administrators are permitted to
 * update a help category; other callers receive an HTTP 403 (Forbidden)
 * response. Malformed request bodies result in an HTTP 400 (Bad Request)
 * response.</p>
 */
public class HelpCategoryPut extends CircabcDeclarativeWebScript {

  /** API providing the business logic for reading and updating help content. */
  @Autowired
  private HelpApi helpApi;

  /** Service used to verify that the current user has administrator rights. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the PUT request that updates a help category.
   *
   * <p>Resolves the target category from the {@code id} template variable,
   * verifies that the caller is an Alfresco or CIRCABC administrator, parses
   * the {@link HelpCategory} from the request body and delegates the update to
   * {@link HelpApi#updateHelpCategory(String, HelpCategory)}. Locale and
   * multilingual awareness are configured based on the optional
   * {@code language} parameter and always restored to their previous values
   * before returning.</p>
   *
   * @param req    the web script request, providing the {@code id} template
   *               variable, the optional {@code language} parameter and the
   *               JSON body describing the category
   * @param status the response status, set to 403 when the caller lacks
   *               permission or to 400 when the request body cannot be parsed
   * @param cache  the response cache control settings
   * @return a model map containing the updated {@code category} under the key
   *         {@code "category"}, or {@code null} when an access-denied or
   *         parsing error has been signalled via {@code status}
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

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

      String id = templateVars.get("id");

      if ("".equals(id)) {
        throw new InvalidArgumentException();
      }

      HelpCategory category = HelpJsonParser.parseCategory(req);
      model.put("category", helpApi.updateHelpCategory(id, category));
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
