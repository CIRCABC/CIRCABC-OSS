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
 * Read-only Alfresco web script endpoint that searches the CIRCABC help content.
 *
 * <p>Mapped to an HTTP {@code GET} request (as implied by the {@code Get} suffix in the class
 * name), this endpoint delegates the actual search to {@link HelpApi#searchHelp(String)} and
 * exposes the results under the {@code search} key of the returned model map, which is then
 * rendered by the associated FreeMarker template.
 *
 * <p>Request parameters:
 * <ul>
 *   <li>{@code q} - the search query string used to look up matching help entries.</li>
 *   <li>{@code language} - optional ISO language code. When provided, the content and UI locale
 *       are set accordingly and multilingual (ML) property resolution is disabled so that help
 *       content is returned in the requested language. When absent, ML awareness is enabled so
 *       that language resolution follows the default multilingual behavior.</li>
 * </ul>
 *
 * <p>This endpoint performs no security check and is accessible to everyone. On any failure it
 * responds with HTTP 500 (Internal Server Error).
 */
public class HelpSearchGet extends DeclarativeWebScript {

  /** Logger used to report errors that occur while executing the help search. */
  static final Log logger = LogFactory.getLog(HelpSearchGet.class);

  /** Business API that performs the actual help content search. */
  @Autowired
  private HelpApi helpApi;

  /**
   * Handles the help search request.
   *
   * <p>Reads the {@code language} and {@code q} request parameters, adjusts the locale and
   * multilingual awareness accordingly, and performs the search via {@link HelpApi#searchHelp(String)}.
   * The original {@link MLPropertyInterceptor} awareness flag is always restored before returning.
   *
   * @param req the web script request carrying the {@code q} (query) and optional {@code language}
   *     parameters
   * @param status the response status; set to {@code 500} (Internal Server Error) when the search
   *     fails
   * @param cache the cache directives for the response
   * @return a model map containing the search results under the {@code search} key, or {@code null}
   *     if an error occurred and an internal server error status was set
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

    String query = req.getParameter("q");

    try {
      // no security check - it is accessible to everyone
      model.put("search", helpApi.searchHelp(query));
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
