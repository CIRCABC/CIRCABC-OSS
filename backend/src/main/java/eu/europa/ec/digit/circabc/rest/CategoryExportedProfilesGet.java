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
 * REST webscript endpoint that returns the exported profiles of a given
 * category.
 *
 * <p>The class name follows the CIRCABC convention {@code <Entity><Method>},
 * so this handles an HTTP <strong>GET</strong> request. It retrieves the list
 * of profiles that have been exported for the category identified by the
 * {@code id} URL template variable.
 *
 * <p>Inputs:
 * <ul>
 *   <li>{@code id} (URL template variable) - the identifier of the category
 *       whose exported profiles are requested.</li>
 *   <li>{@code language} (optional request parameter) - when supplied, the
 *       content and UI locale are set accordingly and multilingual (ML)
 *       awareness is disabled so values are resolved in that language; when
 *       absent, ML awareness is enabled.</li>
 *   <li>{@code ignoreIgId} (request parameter) - the interest group id used to
 *       verify that the current user is a directory administrator of that
 *       interest group.</li>
 * </ul>
 *
 * <p>Access is restricted to interest group directory administrators. The
 * endpoint returns HTTP 403 (Forbidden) when access is denied, HTTP 400 (Bad
 * Request) when the category node reference is invalid, and HTTP 500 (Internal
 * Server Error) on any other unexpected failure. On success the model contains
 * the resolved profiles under the {@code profiles} key.
 *
 * @see DeclarativeWebScript
 */
public class CategoryExportedProfilesGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    CategoryExportedProfilesGet.class
  );

  /**
   * API used to resolve the exported profiles for a category.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to verify that the current user is a directory administrator
   * of the interest group before granting access to the exported profiles.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request and builds the response model containing the
   * exported profiles for the requested category.
   *
   * <p>The category id is read from the {@code id} URL template variable. The
   * optional {@code language} request parameter controls locale handling and ML
   * awareness, while the {@code ignoreIgId} request parameter identifies the
   * interest group against which the caller's directory-administrator rights
   * are checked. The original ML awareness state is always restored before the
   * method returns.
   *
   * @param req    the web script request, providing the {@code id} template
   *               variable and the {@code language} and {@code ignoreIgId}
   *               request parameters
   * @param status the response status, updated with an error code, message and
   *               redirect flag when the request cannot be fulfilled
   * @param cache  the response cache directives
   * @return a model map containing the exported profiles under the
   *         {@code profiles} key on success, or {@code null} when an error
   *         status (403, 400 or 500) has been set on the response
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

    String igId = req.getParameter("ignoreIgId");

    try {
      if (
        !this.currentUserPermissionCheckerService.isInterestGroupDirAdmin(igId)
      ) {
        throw new AccessDeniedException(
          "Method not accessible to non dirAdmin user"
        );
      }

      model.put(
        "profiles",
        this.categoriesApi.categoriesIdExportedProfilesGet(categoryId, igId)
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied for user when getting exported profiles for category " +
          categoryId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference for category " + categoryId, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when getting exported profiles for category " +
          categoryId,
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
