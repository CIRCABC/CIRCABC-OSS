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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script that handles the removal of an administrator
 * from a CIRCABC category.
 *
 * <p>The class name implies an HTTP {@code DELETE} request. The endpoint removes
 * the association between a given category and one of its administrator users.
 * The category and the user to be removed are supplied as URL template
 * variables:
 *
 * <ul>
 *   <li>{@code id} &ndash; the identifier of the target category.</li>
 *   <li>{@code userId} &ndash; the identifier of the user to remove from the
 *       category administrators.</li>
 * </ul>
 *
 * <p>An optional {@code language} request parameter controls locale handling and
 * multilingual (ML) property awareness while the operation is performed.
 *
 * <p>Only a CIRCABC administrator or an administrator of the targeted category is
 * allowed to perform this operation; otherwise the request is rejected with an
 * HTTP 403 (Forbidden) status. Invalid node references result in an HTTP 400
 * (Bad Request), and any other failure yields an HTTP 500 (Internal Server
 * Error).
 */
public class CategoryAdminsDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryAdminsDelete.class);

  /** API used to perform category-related operations, such as removing an admin. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Service used to check the permissions of the currently authenticated user. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Removes the specified user from the administrators of the given category.
   *
   * <p>The category {@code id} and the {@code userId} are read from the URL
   * template variables. When a {@code language} request parameter is provided,
   * the content and UI locales are set accordingly and ML awareness is disabled;
   * otherwise ML awareness is enabled. The original ML awareness state is always
   * restored before the method returns.
   *
   * <p>Access is granted only to CIRCABC administrators or administrators of the
   * targeted category. Errors are translated into appropriate HTTP status codes
   * on the response instead of being propagated:
   *
   * <ul>
   *   <li>{@link Status#STATUS_FORBIDDEN} when the user lacks the required
   *       permissions.</li>
   *   <li>{@link Status#STATUS_BAD_REQUEST} when the category node reference is
   *       invalid.</li>
   *   <li>{@link Status#STATUS_INTERNAL_SERVER_ERROR} for any other
   *       failure.</li>
   * </ul>
   *
   * @param req the web script request, providing the URL template variables and
   *     the optional {@code language} parameter
   * @param status the response status object, used to signal errors to the caller
   * @param cache the cache control object for the response
   * @return an empty model map on success, or {@code null} when an error status
   *     has been set on the response
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
    String userId = templateVars.get("userId");

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

      this.categoriesApi.categoriesIdAdminsDelete(categoryId, userId);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied for user trying to delete category admin. Category: " +
          categoryId +
          ", User: " +
          userId,
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
        "Unexpected error when deleting category admin. Category: " +
          categoryId +
          ", User: " +
          userId,
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
