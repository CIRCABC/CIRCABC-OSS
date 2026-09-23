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
 * Alfresco Web Script endpoint that reports whether a pending "delete request"
 * exists for a given interest group.
 *
 * <p>As implied by the {@code Get} suffix in the class name, this endpoint is
 * bound to an HTTP {@code GET} request. It reads the interest group identifier
 * from the URL template variable {@code groupId} and optionally a
 * {@code language} query parameter used to control multilingual (ML) content
 * resolution.</p>
 *
 * <p>Access is restricted to group administrators: if the current user is not
 * an administrator of the target group, the request is rejected with an HTTP
 * {@code 403 Forbidden} response. On success, the endpoint returns a model
 * containing the boolean flag {@code isPending}, indicating whether a group
 * delete request is currently awaiting processing.</p>
 */
public class IsDeleteRequestPendingGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(IsDeleteRequestPendingGet.class);

  /**
   * API used to query category/group related operations, in particular to
   * check for an existing group delete request.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Service used to verify that the current user has the required permissions
   * (group administrator) to perform this operation.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the web script request and determines whether a delete request is
   * pending for the interest group identified by the {@code groupId} URL
   * template variable.
   *
   * <p>When the {@code language} request parameter is provided, the content and
   * UI locale are set accordingly and ML awareness is disabled so that
   * localized values are resolved; otherwise ML awareness is enabled. The
   * previous ML-aware state is always restored before returning.</p>
   *
   * <p>The current user must be an administrator of the target group. If not,
   * the response status is set to {@code 403 Forbidden}. Invalid node
   * references result in a {@code 400 Bad Request}, and any other failure
   * results in a {@code 500 Internal Server Error}.</p>
   *
   * @param req the web script request; supplies the {@code groupId} template
   *     variable and the optional {@code language} parameter
   * @param status the response status, updated with the appropriate HTTP code
   *     and message when an error occurs
   * @param cache the cache control directives for the response
   * @return a model map containing the {@code isPending} boolean flag on
   *     success, or {@code null} when an error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupId = templateVars.get("groupId");

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
      if (!currentUserPermissionCheckerService.isGroupAdmin(groupId)) {
        throw new AccessDeniedException(
          "Impossible to edit the category group request. Not enough permissions"
        );
      }
      boolean isPending = this.categoriesApi.existsGroupDeleteRequest(groupId);
      model.put("isPending", isPending);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied checking delete request pending status for groupId: " +
          groupId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference checking delete request pending status for groupId: " +
          groupId,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error checking delete request pending status for groupId: " + groupId,
        e
      );
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
