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
 * Alfresco Web Script endpoint that serves the collection of highlighted help
 * articles.
 *
 * <p>As implied by the {@code Get} suffix in the class name, this endpoint
 * responds to HTTP {@code GET} requests. It retrieves the articles flagged as
 * "highlighted" through the {@link HelpApi} and exposes them under the
 * {@code articles} key of the returned model, which is subsequently rendered by
 * the associated FreeMarker template.
 *
 * <p>An optional {@code language} request parameter controls how multilingual
 * content is resolved:
 * <ul>
 *   <li>When absent, the multilingual (ML) property interceptor is enabled so
 *   that language-aware content is returned according to the repository
 *   defaults.</li>
 *   <li>When present, the given language is used to set the content and UI
 *   locale, and the ML interceptor is disabled so that the article text is
 *   resolved for that specific locale.</li>
 * </ul>
 *
 * <p>On any failure the response is flagged with an HTTP 500 status and a
 * redirect to the standard error handling.
 *
 * @author beaurpi
 */
public class HelpArticlesHighlightedGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(HelpArticlesHighlightedGet.class);

  /**
   * API facade providing access to help content operations, including the
   * retrieval of highlighted articles. Injected by Spring.
   */
  @Autowired
  private HelpApi helpApi;

  /**
   * Handles the {@code GET} request for highlighted help articles.
   *
   * <p>Reads the optional {@code language} request parameter to configure the
   * content/UI locale and the multilingual property interceptor, then fetches
   * the highlighted articles from {@link HelpApi#getHighlightedArticles()}. The
   * previous ML-aware state is always restored before returning.
   *
   * @param req the web script request, from which the optional {@code language}
   *            parameter is read
   * @param status the response status; set to
   *               {@link Status#STATUS_INTERNAL_SERVER_ERROR} with a redirect
   *               when retrieval fails
   * @param cache the cache directives for the response
   * @return a model map containing the highlighted articles under the
   *         {@code articles} key, or {@code null} if an error occurred and the
   *         error status/redirect has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

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
      model.put("articles", helpApi.getHighlightedArticles());
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
