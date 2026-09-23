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
 * REST webscript endpoint that resolves the current user's edition (edit)
 * authorization for a given content node.
 *
 * <p>As the class name suffix {@code Get} implies, this endpoint is bound to an
 * HTTP {@code GET} request. It reads the node identifier from the URL template
 * variable {@code id} and delegates to {@link GuardsApi#guardsEditionIdGet(String)}
 * to determine whether the caller is allowed to edit the node, returning the
 * resulting {@link GuardAuthorization} under the {@code result} key of the model.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} — URL template variable identifying the target node.</li>
 *   <li>{@code language} — optional request parameter selecting the content
 *       {@link Locale}. When absent, multilingual (ML) awareness is enabled so
 *       properties are resolved in a language-agnostic way; when present, the
 *       content and UI locales are set accordingly and ML awareness is disabled.</li>
 * </ul>
 *
 * <p>Error handling:
 * <ul>
 *   <li>{@link AccessDeniedException} — a non-granted {@link GuardAuthorization}
 *       is returned rather than an error status.</li>
 *   <li>{@link InvalidNodeRefException} — responds with HTTP 400 (Bad Request).</li>
 *   <li>{@link NonExistingNodeException} — responds with HTTP 204 (No Content).</li>
 * </ul>
 */
public class GuardsEditionGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GuardsEditionGet.class);

  /**
   * Service facade providing guard/authorization operations; injected by Spring.
   */
  @Autowired
  private GuardsApi guardsApi;

  /**
   * Executes the webscript: resolves the edition authorization for the node
   * identified by the {@code id} URL template variable, applying the optional
   * {@code language} request parameter to control locale and multilingual
   * property resolution.
   *
   * @param req the incoming web script request; supplies the {@code id} template
   *            variable and the optional {@code language} parameter
   * @param status the response status holder, used to signal bad request (400)
   *               or no content (204) outcomes
   * @param cache the cache directives for the response
   * @return a model map containing the {@code result} entry holding the
   *         {@link GuardAuthorization}, or {@code null} when an error status and
   *         redirect have been set on {@code status}
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
      model.put("result", this.guardsApi.guardsEditionIdGet(id));
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
