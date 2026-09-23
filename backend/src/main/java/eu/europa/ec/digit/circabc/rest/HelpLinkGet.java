package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.HelpApi;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Read-only Alfresco web script that resolves a contextual help link.
 *
 * <p>Backing the HTTP {@code GET} endpoint implied by its {@code HelpLinkGet}
 * name, this web script looks up the help link associated with a given
 * identifier and exposes it under the {@code link} key of the returned model
 * map (rendered by the corresponding FreeMarker template).
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} &ndash; mandatory URL template variable identifying the help
 *       resource to resolve.</li>
 *   <li>{@code language} &ndash; optional request parameter selecting the locale
 *       of the returned link. When absent, the multilingual (ML) aware behavior
 *       is enabled so Alfresco returns the value for the current context locale;
 *       when supplied, the content and UI locale are pinned to that language and
 *       ML awareness is disabled.</li>
 * </ul>
 *
 * <p>The endpoint performs no security check and is intentionally accessible to
 * everyone.
 */
public class HelpLinkGet extends DeclarativeWebScript {

  /** Logger used to report unexpected failures while resolving the help link. */
  static final Log logger = LogFactory.getLog(HelpLinkGet.class);

  /** Business API used to resolve the help link for a given identifier. */
  @Autowired
  private HelpApi helpApi;

  /**
   * Resolves the help link for the requested identifier and locale, populating
   * the response model.
   *
   * <p>Reads the {@code id} URL template variable and the optional
   * {@code language} request parameter, adjusts the multilingual property
   * behavior accordingly, and stores the resolved link under the {@code link}
   * model key. The previous ML-aware state is always restored before returning.
   * On failure the HTTP status is set to {@code 500 Internal Server Error} and
   * {@code null} is returned.
   *
   * @param req    the incoming web script request, providing the {@code id}
   *               template variable and optional {@code language} parameter
   * @param status the response status, set to an internal server error if
   *               resolution fails
   * @param cache  the cache directives for the response
   * @return a model map containing the resolved help link under the
   *         {@code link} key, or {@code null} if an error occurred
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
      // no security check - it is accessible to everyone
      model.put("link", helpApi.getHelpLink(id));
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
