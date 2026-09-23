package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
import io.swagger.model.AdminContactRequest;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.CategoryJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code POST} endpoint that
 * sets (or updates) the administrative contact information for a Category.
 *
 * <p>The endpoint is bound to a URL carrying the target category identifier as
 * the {@code id} template variable (e.g. {@code /categories/{id}/adminContact}).
 * The request body is expected to be a JSON payload describing the admin
 * contact, which is parsed into an {@link io.swagger.model.AdminContactRequest}
 * and delegated to {@link io.swagger.api.CategoriesApi}.
 *
 * <p>Behavioral notes:
 * <ul>
 *   <li>Guest users are rejected with {@code 403 Forbidden}.</li>
 *   <li>An optional {@code language} request parameter controls the content
 *       locale: when absent the script runs in multilingual-aware mode,
 *       otherwise the given locale is applied and multilingual awareness is
 *       disabled for the duration of the call.</li>
 *   <li>Invalid node references, malformed JSON or I/O problems result in a
 *       {@code 400 Bad Request}; any other failure results in a
 *       {@code 500 Internal Server Error}.</li>
 * </ul>
 */
public class CategoryAdminContactPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryAdminContactPost.class);

  /**
   * API facade used to persist the admin contact for the target category.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to determine the current user's identity/role, in particular
   * whether the caller is an unauthenticated guest.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the POST request that assigns the administrative contact of a
   * category.
   *
   * <p>Reads the {@code id} template variable to identify the category and the
   * optional {@code language} request parameter to select the content locale,
   * parses the JSON body into an {@link io.swagger.model.AdminContactRequest}
   * and delegates persistence to
   * {@link io.swagger.api.CategoriesApi#categoriesIdAdminContactPost(String, io.swagger.model.AdminContactRequest)}.
   * The previous multilingual-awareness flag is always restored before
   * returning.
   *
   * @param req the incoming web script request; supplies the {@code id}
   *            template variable, the optional {@code language} parameter and
   *            the JSON body
   * @param status the response status; set to {@code 403}, {@code 400} or
   *               {@code 500} (with redirect) when the request cannot be
   *               fulfilled
   * @param cache the response cache directives for this request
   * @return an empty model map on success, or {@code null} when an error status
   *         has been set on the response
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String categoryId = templateVars.get("id");

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
    try {
      if (currentUserPermissionCheckerService.isGuest()) {
        throw new AccessDeniedException("Method not allowed for guest users");
      }
      // no special permission check required here.
      AdminContactRequest body = CategoryJsonParser.parseAdminContactRequest(
        req
      );
      this.categoriesApi.categoriesIdAdminContactPost(categoryId, body);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Error accessing category admin contact for id: " + categoryId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(
        "Error processing category admin contact request for id: " + categoryId,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error in category admin contact for id: " + categoryId,
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
