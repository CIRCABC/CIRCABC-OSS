package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.exception.InvalidIdException;
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
 * Alfresco Web Script endpoint that handles HTTP {@code GET} requests to retrieve all help
 * articles belonging to a given help category.
 *
 * <p>The target category is identified by the {@code id} template variable taken from the request
 * URL. The endpoint is intentionally public: no security/permission check is performed, so the
 * help content is accessible to everyone.
 *
 * <p>Supported request inputs:
 *
 * <ul>
 *   <li>{@code id} (URL template variable) &ndash; the identifier of the help category whose
 *       articles are requested. Must not be empty.
 *   <li>{@code language} (query parameter, optional) &ndash; when provided, the response is
 *       localized to the given language and multilingual (ML) awareness is disabled so that the
 *       localized values are returned. When omitted, ML awareness is enabled.
 *   <li>{@code skipcontent} (query parameter, optional) &ndash; when {@code true}, the article
 *       bodies are not loaded; otherwise the full content is returned.
 * </ul>
 *
 * <p>On success the returned model contains an {@code articles} entry with the list of articles,
 * which is rendered by the associated FreeMarker template. Invalid input results in a
 * {@code 400 Bad Request} and unexpected failures in a {@code 500 Internal Server Error}.
 *
 * @author beaurpi
 */
public class HelpCategoryArticlesGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(HelpCategoryArticlesGet.class);

  /**
   * Business API used to load the help articles of a category. Injected by Spring.
   */
  @Autowired
  private HelpApi helpApi;

  /**
   * Executes the endpoint logic: resolves the requested help category, optionally applies the
   * requested locale, and populates the model with the category's articles.
   *
   * <p>Handles multilingual awareness around the call so that the original ML state is always
   * restored, and translates errors into the appropriate HTTP status codes.
   *
   * @param req the web script request, providing the {@code id} template variable and the optional
   *     {@code language} and {@code skipcontent} query parameters
   * @param status the response status, updated to {@code 400} on invalid input or {@code 500} on
   *     unexpected errors
   * @param cache the cache control object for the response
   * @return a model map containing the {@code articles} entry on success, or {@code null} when an
   *     error occurred and a redirect status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

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
      String id = templateVars.get("id");

      String skipContent = req.getParameter("skipcontent");
      boolean loadContent = true;
      if (skipContent != null) {
        loadContent = !Boolean.parseBoolean(skipContent);
      }

      if ("".equals(id)) {
        throw new InvalidIdException("Help category ID cannot be empty");
      }

      model.put("articles", helpApi.getCategoryArticles(id, loadContent));
    } catch (InvalidIdException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage(e.getMessage());
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
