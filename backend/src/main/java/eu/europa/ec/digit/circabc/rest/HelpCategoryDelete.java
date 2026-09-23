package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.HelpApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code DELETE} endpoint that removes a help
 * category from the CIRCABC help system.
 *
 * <p>The category to delete is identified by the {@code id} template variable taken from the
 * request URL. An optional {@code language} request parameter controls the content locale used
 * while performing the operation: when it is absent the interceptor is left multilingual-aware,
 * and when it is supplied the corresponding {@link java.util.Locale} is applied and
 * multilingual awareness is disabled for the duration of the call.</p>
 *
 * <p>Only Alfresco administrators or CIRCABC administrators are allowed to delete a help
 * category; any other caller is rejected. The actual deletion is delegated to
 * {@link io.swagger.api.HelpApi#deleteHelpCategory(String)}. Failures are translated into the
 * appropriate HTTP status codes (403 for permission problems, 500 for invalid arguments or
 * unexpected errors).</p>
 *
 * @author beaurpi
 */
public class HelpCategoryDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(HelpCategoryDelete.class);

  /**
   * API used to perform help-related operations, in particular the deletion of a help category.
   */
  @Autowired
  private HelpApi helpApi;

  /**
   * Service used to check whether the current user has the administrative privileges required to
   * delete a help category.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the web script request to delete a help category.
   *
   * <p>Reads the {@code id} template variable to determine which category to delete and the
   * optional {@code language} request parameter to set the content locale. After verifying that
   * the caller is an Alfresco or CIRCABC administrator, it delegates the deletion to
   * {@link io.swagger.api.HelpApi#deleteHelpCategory(String)}. The original multilingual-aware
   * state of the {@link org.alfresco.repo.node.MLPropertyInterceptor} is always restored before
   * returning.</p>
   *
   * @param req the web script request, providing the {@code id} template variable and the
   *     optional {@code language} parameter
   * @param status the response status; set to an error code (403 or 500) with a redirect when the
   *     operation fails
   * @param cache the cache control for the response
   * @return an empty model map on success, or {@code null} when an error occurs and the status has
   *     been set to redirect to an error response
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
          "Cannot delete help category, not enough permission"
        );
      }

      String id = templateVars.get("id");

      if ("".equals(id)) {
        throw new InvalidArgumentException();
      }

      helpApi.deleteHelpCategory(id);
    } catch (InvalidArgumentException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Invalid argument");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
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
