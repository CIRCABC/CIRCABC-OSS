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
 * Read-only Alfresco web script endpoint handling HTTP {@code GET} requests to
 * retrieve the list of CIRCABC help links.
 *
 * <p>The endpoint is publicly accessible and performs no security check, so the
 * help links are returned to any caller regardless of authentication. Delegation
 * to the business layer is done through {@link HelpApi#getHelpLinks()} and the
 * resulting collection is exposed to the response template under the
 * {@code links} model key.</p>
 *
 * <p>An optional {@code language} request parameter controls how multilingual
 * content is resolved:</p>
 * <ul>
 *   <li>When absent, the {@link MLPropertyInterceptor} is left ML-aware so that
 *       full multilingual values are returned.</li>
 *   <li>When present, the given language is used to build a {@link Locale} that
 *       is set as both the content and UI locale, and the interceptor is
 *       switched off so that values are resolved for that single locale.</li>
 * </ul>
 *
 * <p>The original ML-aware state of the interceptor is always restored once the
 * request has been processed.</p>
 *
 * @author beaurpi
 */
public class HelpLinksGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(HelpLinksGet.class);

  /**
   * Business API used to fetch the help links exposed by this endpoint.
   */
  @Autowired
  private HelpApi helpApi;

  /**
   * Handles the {@code GET} request by collecting the help links to render.
   *
   * <p>Reads the optional {@code language} request parameter to configure locale
   * handling, retrieves the help links via {@link HelpApi#getHelpLinks()}, and
   * places them in the returned model under the {@code links} key. On failure the
   * response status is set to {@link Status#STATUS_INTERNAL_SERVER_ERROR} with a
   * redirect and {@code null} is returned. The {@link MLPropertyInterceptor}
   * ML-aware state is always restored before returning.</p>
   *
   * @param req    the incoming web script request; may carry an optional
   *               {@code language} parameter
   * @param status the response status, updated to signal an internal server
   *               error when link retrieval fails
   * @param cache  the response cache directives (unused)
   * @return a model map containing the {@code links} entry, or {@code null} when
   *         an error occurred and the error status has been set
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
      model.put("links", helpApi.getHelpLinks());
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
