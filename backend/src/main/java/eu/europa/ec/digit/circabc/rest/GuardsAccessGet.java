package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GuardsApi;
import io.swagger.exception.NonExistingNodeException;
import io.swagger.model.GuardAuthorization;
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
 * Read-only Alfresco web script endpoint that resolves the current user's guard
 * access for a given node.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention, so it
 * handles an HTTP {@code GET} request. It delegates to
 * {@link io.swagger.api.GuardsApi#guardsAccessIdGet(String)} to compute a
 * {@link io.swagger.model.GuardAuthorization} describing whether access is granted
 * for the requested node.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} — the node identifier, supplied as a URL template variable.</li>
 *   <li>{@code language} — optional request parameter selecting the content locale.
 *       When absent, multilingual (ML) awareness is enabled so values are returned in
 *       their raw multilingual form; when present, the content and UI locale are set
 *       and ML awareness is disabled.</li>
 * </ul>
 *
 * <p>Error handling: an {@link org.alfresco.repo.security.permissions.AccessDeniedException}
 * yields a {@link io.swagger.model.GuardAuthorization} with access not granted; an
 * {@link org.alfresco.service.cmr.repository.InvalidNodeRefException} maps to HTTP 400
 * (Bad Request); a {@link io.swagger.exception.NonExistingNodeException} maps to HTTP 204
 * (No Content).</p>
 */
public class GuardsAccessGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GuardsAccessGet.class);

  /**
   * API used to resolve guard access authorization for a node. Injected by Spring.
   */
  @Autowired
  private GuardsApi guardsApi;

  /**
   * Executes the endpoint: resolves the guard access authorization for the node
   * identified by the {@code id} URL template variable, honouring the optional
   * {@code language} request parameter for locale/multilingual handling.
   *
   * @param req the web script request, providing the {@code id} template variable
   *            and the optional {@code language} parameter
   * @param status the web script response status; set to {@code 400} for an invalid
   *               node reference or {@code 204} when the node does not exist
   * @param cache the web script cache directives (unused)
   * @return a model map containing the {@code result} entry with a
   *         {@link io.swagger.model.GuardAuthorization}, or {@code null} when an error
   *         status and redirect have been set
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
      model.put("result", this.guardsApi.guardsAccessIdGet(id));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      GuardAuthorization result = new GuardAuthorization();
      result.setGranted(false);
      model.put("result", result);
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (NonExistingNodeException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_NO_CONTENT);
      status.setMessage("Node does not exist");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
