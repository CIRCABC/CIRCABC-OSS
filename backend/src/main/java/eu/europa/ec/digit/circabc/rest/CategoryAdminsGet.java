package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that handles the HTTP {@code GET} request for retrieving the list of
 * administrators of a given category.
 *
 * <p>The category is identified by the {@code id} template variable taken from the request URL. An
 * optional {@code language} request parameter controls how multilingual (ML) content properties are
 * resolved: when it is absent the endpoint operates in ML-aware mode; when supplied, the given
 * locale is set as the content and UI locale and ML-awareness is disabled so that values are
 * returned for that specific language.
 *
 * <p>Access is restricted: only a CIRCABC administrator or an administrator of the target category
 * is allowed to invoke this endpoint. Unauthorized access results in an HTTP {@code 403 Forbidden}
 * response, an invalid category reference results in an HTTP {@code 400 Bad Request}, and any other
 * failure results in an HTTP {@code 500 Internal Server Error}.
 *
 * <p>On success the model exposes the resolved administrators under the {@code admins} key, which is
 * rendered by the associated FreeMarker template.
 */
public class CategoryAdminsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryAdminsGet.class);

  /** API providing category-related business operations, including admin lookup. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Service used to verify that the current user has the required administrative permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Resolves and returns the administrators of the category identified by the {@code id} template
   * variable.
   *
   * <p>Reads the optional {@code language} request parameter to configure ML-awareness and the
   * content/UI locale, verifies that the current user is a CIRCABC admin or an admin of the target
   * category, and, when authorized, populates the model with the category administrators. The
   * original ML-awareness setting is always restored before returning.
   *
   * @param req the web script request; provides the {@code id} template variable and the optional
   *     {@code language} parameter
   * @param status the web script response status; updated with the appropriate error code, message
   *     and redirect flag when the request cannot be fulfilled
   * @param cache the web script response cache directives
   * @return a model map containing the {@code admins} entry on success, or {@code null} when an
   *     error occurs and the status has been set accordingly
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
      boolean isAllowed =
        currentUserPermissionCheckerService.isCircabcAdmin() ||
        currentUserPermissionCheckerService.isCategoryAdmin(categoryId);
      if (!isAllowed) {
        throw new AccessDeniedException(
          "The user don't have enough permissions"
        );
      }
      model.put("admins", this.categoriesApi.categoriesIdAdminsGet(categoryId));
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied for user when getting category admins for category: " +
          categoryId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference for category: " + categoryId, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error getting category admins for category: " + categoryId,
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
