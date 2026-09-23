package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
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
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that returns the
 * list of categories a given user belongs to (or otherwise has access to).
 *
 * <p>The target user is identified by the {@code userId} template variable taken from the
 * request URL. Access is restricted: the categories are only returned when the caller is the
 * user itself, an Alfresco administrator, or a CIRCABC administrator; otherwise an
 * {@link org.alfresco.repo.security.permissions.AccessDeniedException} is raised and the
 * response is set to {@code 403 Forbidden}.</p>
 *
 * <p>An optional {@code language} request parameter controls localisation of the returned
 * multilingual content. When it is absent the script runs in ML-aware mode (returning all
 * language variants); when present the content and UI locale are set accordingly and ML-awareness
 * is disabled so that content is resolved for that single locale. The original ML-aware flag is
 * always restored once processing completes.</p>
 *
 * <p>The resulting categories are exposed to the response template under the {@code categories}
 * key of the model map.</p>
 *
 * @author beaurpi
 */
public class UserCategoriesGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UserCategoriesGet.class);

  /**
   * API providing user-related business operations, used here to fetch the categories
   * associated with a given user.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Service used to verify the current caller's identity and administrative privileges
   * before disclosing another user's categories.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request by resolving and returning the categories of the user
   * identified by the {@code userId} URL template variable.
   *
   * <p>Applies optional localisation based on the {@code language} request parameter,
   * enforces access control, and populates the response model. On error the appropriate HTTP
   * status is set on {@code status} and {@code null} is returned so that the framework renders
   * the error response.</p>
   *
   * @param req    the web script request; supplies the {@code userId} template variable and the
   *               optional {@code language} query parameter
   * @param status the web script status, updated with the relevant HTTP status code and message
   *               when the request cannot be fulfilled
   * @param cache  the web script cache directives for the response
   * @return a model map containing the {@code categories} entry when successful, or {@code null}
   *         when an error occurred and the status was set to a redirect
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
    try {
      String userId = templateVars.get("userId");
      if (userId != null) {
        if (
          this.currentUserPermissionCheckerService.isCurrentUserEqualTo(
            userId
          ) ||
          this.currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          this.currentUserPermissionCheckerService.isCircabcAdmin()
        ) {
          model.put("categories", this.usersApi.getUserCategories(userId));
        } else {
          throw new AccessDeniedException("Operation is not allowed");
        }
      }
    } catch (InvalidNodeRefException inre) {
      logger.error(inre.getMessage(), inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error(ade.getMessage(), ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
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
