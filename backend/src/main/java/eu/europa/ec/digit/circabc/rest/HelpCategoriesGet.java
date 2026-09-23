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
 * Read-only Alfresco web script backing an HTTP {@code GET} endpoint that returns the list of
 * available help categories.
 *
 * <p>The endpoint is public: no security/authentication check is performed, so the help categories
 * are accessible to everyone. An optional {@code language} request parameter controls the locale
 * used to resolve multilingual (ML) content:
 *
 * <ul>
 *   <li>When {@code language} is absent, ML awareness is enabled so that values are returned in
 *       their multilingual form.
 *   <li>When {@code language} is provided, the corresponding {@link java.util.Locale} is set as the
 *       content and UI locale and ML awareness is disabled, so that values are resolved for that
 *       specific language.
 * </ul>
 *
 * <p>The resolved categories are placed under the {@code categories} key of the model consumed by
 * the associated FreeMarker template. On failure the response is set to HTTP 500 (internal server
 * error). The previous ML-awareness state is always restored before returning.
 *
 * @author beaurpi
 */
public class HelpCategoriesGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(HelpCategoriesGet.class);

  /** API providing access to the help-related business operations, injected by Spring. */
  @Autowired
  private HelpApi helpApi;

  /**
   * Handles the {@code GET} request by resolving the requested locale and retrieving the help
   * categories.
   *
   * @param req the web script request; may carry an optional {@code language} parameter used to
   *     select the content/UI locale
   * @param status the response status, set to {@link Status#STATUS_INTERNAL_SERVER_ERROR} if
   *     retrieving the categories fails
   * @param cache the web script cache directives for the response
   * @return the model map containing the {@code categories} entry, or {@code null} if an error
   *     occurred (in which case an error status is set on {@code status})
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
      // no security check - it is accessible to everyone
      model.put("categories", helpApi.getHelpCategories());
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
