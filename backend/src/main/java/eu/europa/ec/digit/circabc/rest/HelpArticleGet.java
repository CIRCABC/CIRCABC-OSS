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
 * Alfresco Web Script endpoint that handles the HTTP {@code GET} request for retrieving a single
 * help article.
 *
 * <p>The endpoint is publicly accessible: no security/authentication check is performed, so any
 * caller may read help content. The help article to retrieve is identified by the {@code id}
 * template variable extracted from the request URL. An optional {@code language} request parameter
 * controls localisation of the returned content:
 *
 * <ul>
 *   <li>when {@code language} is absent, the interceptor is set to be multilingual-aware
 *       ({@link MLPropertyInterceptor#setMLAware(boolean)} with {@code true}) so the raw
 *       multilingual value is returned;
 *   <li>when {@code language} is provided, the content and UI {@link Locale} are set accordingly
 *       and the interceptor resolves the value for that specific locale.
 * </ul>
 *
 * <p>The resolved article is placed into the response model under the {@code "article"} key and
 * subsequently rendered by the associated FreeMarker template. On error the method sets an
 * appropriate HTTP status code ({@code 400} for an invalid/empty id, {@code 500} otherwise) and
 * returns {@code null}.
 */
public class HelpArticleGet extends DeclarativeWebScript {

  /** Logger used to report errors raised while resolving the requested help article. */
  static final Log logger = LogFactory.getLog(HelpArticleGet.class);

  /** Business API used to look up help articles by their identifier. */
  @Autowired
  private HelpApi helpApi;

  /**
   * Resolves and returns the help article identified by the {@code id} template variable.
   *
   * <p>Reads the optional {@code language} request parameter to configure multilingual awareness
   * and locale before delegating to {@link HelpApi#getHelpArticle(String)}. The original
   * multilingual-awareness state of {@link MLPropertyInterceptor} is always restored before the
   * method returns. This endpoint performs no security check and is accessible to everyone.
   *
   * @param req the incoming web script request; supplies the {@code id} template variable and the
   *     optional {@code language} parameter
   * @param status the response status holder; updated with an error code, message and redirect flag
   *     when the article cannot be resolved
   * @param cache the response cache control settings
   * @return a model map containing the resolved article under the {@code "article"} key, or
   *     {@code null} when an error occurs (in which case {@code status} is populated accordingly)
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

      if ("".equals(id)) {
        throw new InvalidIdException("Help article ID cannot be empty");
      }

      model.put("article", helpApi.getHelpArticle(id));
    } catch (InvalidIdException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage(e.getMessage());
      status.setRedirect(true);
      return null; // NOSONAR
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
