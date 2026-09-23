package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.InformationApi;
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
 * Alfresco declarative web script backing the DELETE endpoint for a news item.
 *
 * <p>The web script deletes a single news node identified by the {@code id}
 * template variable in the request URL. The {@code Delete} suffix in the class
 * name maps to the HTTP DELETE method exposed by the corresponding web script
 * descriptor.
 *
 * <p>Request inputs:
 * <ul>
 *   <li>{@code id} (URL template variable): the identifier of the news node to
 *       delete.</li>
 *   <li>{@code language} (optional request parameter): when supplied, the
 *       content and UI locale are set to this language and multilingual (ML)
 *       awareness is disabled so that a specific translation is targeted; when
 *       omitted, ML awareness is enabled.</li>
 * </ul>
 *
 * <p>Before deletion the current user must hold Alfresco delete permission on
 * the node, otherwise the request is rejected. Failures are translated into the
 * appropriate HTTP status codes: {@code 403 Forbidden} for permission issues,
 * {@code 400 Bad Request} for an invalid node reference, and
 * {@code 500 Internal Server Error} for any other error.
 */
public class NewsDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NewsDelete.class);

  /** API used to perform the actual deletion of the news item. */
  @Autowired
  private InformationApi informationApi;

  /** Service used to verify that the current user is allowed to delete the node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the news deletion request.
   *
   * <p>Resolves the {@code id} template variable, configures the locale / ML
   * awareness based on the optional {@code language} parameter, verifies the
   * caller's delete permission, records the node state before deletion and then
   * deletes the news item. On any error the response status is set accordingly
   * and {@code null} is returned so the framework renders the error status
   * rather than a success template. The original ML awareness flag is always
   * restored.
   *
   * @param req the web script request; provides the {@code id} template
   *     variable and the optional {@code language} parameter
   * @param status the response status, updated to reflect forbidden, bad
   *     request or internal error outcomes
   * @param cache the response cache control settings
   * @return an empty model on successful deletion, or {@code null} when an
   *     error occurred and an error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

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
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoDeletePermission(
          id
        )
      ) {
        throw new AccessDeniedException(
          "Not enough permission to delete the news"
        );
      }
      this.recordBeforeDelete(id);
      this.informationApi.newsIdDelete(id);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied when deleting news", ade);
      }
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Invalid node reference when deleting news", inre);
      }
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Unexpected error deleting news", e);
      }
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
