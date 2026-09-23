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
 * Alfresco web script endpoint that handles HTTP {@code GET} requests to retrieve a single help
 * category.
 *
 * <p>The target category is identified by the {@code id} template variable extracted from the
 * request URL. This endpoint is intentionally public: no security check is performed, so the help
 * category can be read by anyone.
 *
 * <p>Localization is controlled by the optional {@code language} request parameter:
 *
 * <ul>
 *   <li>When {@code language} is absent, the multilingual (ML) aware mode is enabled so that the
 *       repository resolves multilingual properties automatically.
 *   <li>When {@code language} is provided, the corresponding {@link java.util.Locale} is applied as
 *       the content and UI locale and ML aware mode is disabled, forcing values in that language.
 * </ul>
 *
 * <p>On success the returned model contains a single {@code category} entry with the resolved help
 * category. Invalid input (an empty {@code id}) results in a {@code 400 Bad Request}, while any
 * other failure results in a {@code 500 Internal Server Error}. The previous ML aware state is
 * always restored before the method returns.
 *
 * @author beaurpi
 */
public class HelpCategoryGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(HelpCategoryGet.class);

  /**
   * API used to look up help categories from the underlying help service.
   */
  @Autowired
  private HelpApi helpApi;

  /**
   * Retrieves the help category identified by the {@code id} template variable and adds it to the
   * response model under the {@code category} key.
   *
   * <p>If the optional {@code language} request parameter is supplied, the matching locale is
   * applied and multilingual awareness is disabled; otherwise multilingual awareness is enabled.
   * The original multilingual aware state is restored before the method returns.
   *
   * <p>Errors are reported through the {@code status} object rather than by propagating exceptions:
   * an empty {@code id} yields {@link Status#STATUS_BAD_REQUEST} and any other error yields
   * {@link Status#STATUS_INTERNAL_SERVER_ERROR}, in both cases returning {@code null}.
   *
   * @param req the web script request; provides the {@code id} template variable and the optional
   *     {@code language} parameter
   * @param status the response status used to signal errors and trigger redirects
   * @param cache the cache directives for the response
   * @return the response model containing the {@code category} entry, or {@code null} when an error
   *     status has been set
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
        throw new InvalidIdException("Help category ID cannot be empty");
      }

      model.put("category", helpApi.getHelpCategory(id));
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
