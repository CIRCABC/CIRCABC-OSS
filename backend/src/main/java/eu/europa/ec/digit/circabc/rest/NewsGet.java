package eu.europa.ec.digit.circabc.rest;

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
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that retrieves the "News" information for a given
 * node.
 *
 * <p>The class name implies an HTTP {@code GET} request. Given the node
 * identifier supplied as the {@code id} URL template variable, this endpoint
 * loads the associated news information and exposes it to the response template
 * under the {@code newsInfo} model key.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} (URL template variable) &ndash; the identifier of the node
 *       whose news information is requested.</li>
 *   <li>{@code language} (optional request parameter) &ndash; when provided, the
 *       content and interface locale are set to this language and multilingual
 *       (ML) awareness is disabled so that a single-language value is returned;
 *       when absent, ML awareness is enabled to return the multilingual value.</li>
 * </ul>
 *
 * <p>Before returning the data, the endpoint verifies that the current user has
 * Alfresco read permission on the target node. Failures are translated into the
 * appropriate HTTP status codes: {@code 403 Forbidden} when access is denied,
 * {@code 400 Bad Request} for an invalid node reference, and
 * {@code 500 Internal Server Error} for any other unexpected error.
 */
public class NewsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NewsGet.class);

  /**
   * API used to load the news information for a given node identifier.
   */
  @Autowired
  private InformationApi informationApi;

  /**
   * Service used to check that the current user holds the required Alfresco
   * permissions (read access) on the requested node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the webscript request by loading the news information for the node
   * identified by the {@code id} URL template variable.
   *
   * <p>If the {@code language} request parameter is provided, the content and
   * interface locale are set accordingly and multilingual awareness is disabled;
   * otherwise multilingual awareness is enabled. The original multilingual
   * awareness state is always restored before the method returns.
   *
   * <p>Read permission on the target node is verified first. On success, the
   * resulting news information is placed in the returned model under the
   * {@code newsInfo} key. On failure, the appropriate error status is set on the
   * {@code status} object and {@code null} is returned so that the error is
   * rendered instead of a model.
   *
   * @param req the webscript request; provides the {@code id} template variable
   *            and the optional {@code language} parameter
   * @param status the response status, used to signal error conditions
   *               (forbidden, bad request or internal server error)
   * @param cache the cache directives for the response
   * @return a model map containing the {@code newsInfo} entry on success, or
   *         {@code null} when an error status has been set
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
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)
      ) {
        throw new AccessDeniedException(
          "Not enough permission to get the news"
        );
      }
      model.put("newsInfo", this.informationApi.newsIdGet(id));
    } catch (AccessDeniedException ade) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied when getting news", ade);
      }
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Invalid node reference when getting news", inre);
      }
      return null; // NOSONAR
    } catch (Exception e) {
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Unexpected error getting news", e);
      }
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
