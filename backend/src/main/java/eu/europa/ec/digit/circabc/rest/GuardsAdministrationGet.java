package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GuardsApi;
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
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that
 * resolves the administration guard authorization for a given node.
 *
 * <p>The endpoint answers whether the current user is authorized to administer
 * the targeted resource, returning a {@link io.swagger.model.GuardAuthorization}
 * as the {@code result} model entry rendered by the associated FreeMarker
 * template.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} &mdash; template variable identifying the target node whose
 *       administration guard is evaluated.</li>
 *   <li>{@code language} &mdash; optional request parameter selecting the
 *       content/UI {@link java.util.Locale}. When absent the script operates in
 *       multilingual-aware mode; when supplied it pins the content and UI locale
 *       and disables multilingual awareness for the duration of the call.</li>
 * </ul>
 *
 * <p>If the current user is denied access the endpoint degrades gracefully by
 * returning a {@code GuardAuthorization} with {@code granted = false} rather
 * than failing. An invalid node reference results in an HTTP 400 (Bad Request)
 * response.</p>
 */
public class GuardsAdministrationGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GuardsAdministrationGet.class);

  /**
   * API providing the business logic used to resolve guard authorizations.
   * Injected by Spring.
   */
  @Autowired
  private GuardsApi guardsApi;

  /**
   * Handles the web script request by resolving the administration guard
   * authorization for the node identified by the {@code id} template variable.
   *
   * <p>When the optional {@code language} parameter is provided, the content
   * and UI locale are set accordingly and multilingual awareness is disabled;
   * otherwise multilingual awareness is enabled. The previous multilingual
   * awareness state is always restored before the method returns.</p>
   *
   * @param req the web script request; supplies the {@code id} template
   *            variable and the optional {@code language} parameter
   * @param status the web script response status; set to
   *               {@link Status#STATUS_BAD_REQUEST} when the node reference is
   *               invalid
   * @param cache the web script cache control directives
   * @return a model map containing the {@code result} entry with the resolved
   *         {@link io.swagger.model.GuardAuthorization}, or {@code null} when the
   *         request is rejected as a bad request
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
      model.put("result", this.guardsApi.guardsAdministrationIdGet(id));
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
