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
 * Alfresco declarative web script backing the HTTP {@code GET} endpoint for category groups.
 *
 * <p>The behaviour depends on whether a category identifier is supplied through the {@code id}
 * URL template variable:
 *
 * <ul>
 *   <li>When no {@code id} is present, the endpoint returns the full list of available categories
 *       under the {@code categories} model key.
 *   <li>When an {@code id} is present, the endpoint verifies that the current user has Alfresco read
 *       permission on that node and, if allowed, returns the interest groups belonging to the
 *       category under the {@code groups} model key (echoing the category {@code id}).
 * </ul>
 *
 * <p>An optional {@code language} request parameter controls localisation of the returned content:
 * when omitted the response is rendered in a multilingual-aware (ML aware) mode; when provided the
 * corresponding {@link Locale} is applied to the content and interface locales and ML awareness is
 * disabled for the duration of the call. The previous ML-aware state is always restored afterwards.
 *
 * <p>Errors are translated into HTTP status codes: access denials become {@code 403 Forbidden},
 * invalid node references become {@code 400 Bad Request}, and any other failure becomes
 * {@code 500 Internal Server Error}.
 */
public class CategoryGroupsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryGroupsGet.class);

  /** API used to look up categories and the interest groups they contain. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Service used to verify that the current user holds Alfresco read permission on a node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the web script logic and builds the model passed to the response template.
   *
   * <p>Reads the optional {@code id} template variable and the optional {@code language} request
   * parameter, applies the appropriate multilingual/locale settings, and populates the model with
   * either the list of all categories or the interest groups of the requested category (after a
   * read-permission check).
   *
   * @param req the incoming web script request; supplies the {@code id} template variable and the
   *     optional {@code language} parameter
   * @param status the response status object, updated with an error code, message and redirect flag
   *     when a category cannot be served
   * @param cache the response cache directives (unused)
   * @return the populated model map, or {@code null} when an error status has been set on the
   *     response
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
      if (categoryId == null) {
        model.put("categories", this.categoriesApi.getCategories());
      } else {
        if (
          !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
            categoryId
          )
        ) {
          throw new AccessDeniedException("No access on node:" + categoryId);
        }

        model.put("id", categoryId);
        model.put(
          "groups",
          this.categoriesApi.getInterestGroupByCategoryId(categoryId)
        );
      }
    } catch (AccessDeniedException ade) {
      logger.error("Access denied to category: " + categoryId, ade);
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
        "Unexpected error getting category groups for: " + categoryId,
        e
      );
      logger.error("Exception details:", e);
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
