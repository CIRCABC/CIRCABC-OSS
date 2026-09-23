package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.HeadersApi;
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
 * Alfresco Web Script endpoint handling HTTP {@code GET} requests for CIRCABC
 * headers (the top level of the Header &rarr; Category &rarr; Interest Group
 * hierarchy).
 *
 * <p>The concrete behavior depends on the request path and template variables:
 * <ul>
 *   <li>No {@code id} template variable &mdash; returns the full list of
 *       headers under the {@code "headers"} model key.</li>
 *   <li>An {@code id} is present and the service path ends with
 *       {@code "categories"} &mdash; returns the categories belonging to that
 *       header under the {@code "categories"} model key.</li>
 *   <li>An {@code id} is present without the {@code "categories"} suffix
 *       &mdash; returns the single header under the {@code "header"} model
 *       key.</li>
 * </ul>
 *
 * <p>Optional query parameters:
 * <ul>
 *   <li>{@code language} &mdash; when supplied, sets the content and UI locale
 *       and disables multilingual (ML) awareness so values are resolved for
 *       that locale; when omitted, ML awareness is enabled.</li>
 *   <li>{@code guest} &mdash; boolean flag indicating whether the request is
 *       performed as a guest user.</li>
 * </ul>
 *
 * <p>The resulting model map is rendered to JSON by the associated FreeMarker
 * template. Business logic is delegated to {@link HeadersApi}.
 */
public class HeaderGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(HeaderGet.class);

  /** API providing header and category retrieval operations. */
  @Autowired
  private HeadersApi headerApi;

  /**
   * Handles the incoming GET request and builds the response model.
   *
   * <p>Reads the optional {@code language} and {@code guest} query parameters,
   * configures locale and ML awareness accordingly, then populates the model
   * with either all headers, the categories of a header, or a single header
   * depending on the {@code id} template variable and the service path.
   * Multilingual awareness is always restored to its original value before
   * returning.
   *
   * @param req    the web script request, providing query parameters, the
   *               service path and the {@code id} template variable
   * @param status the response status; set to {@code 400 Bad Request} on an
   *               invalid node reference or {@code 403 Forbidden} on an access
   *               denied error
   * @param cache  the response cache directives
   * @return a model map keyed by {@code "headers"}, {@code "categories"} or
   *         {@code "header"} for the template to render, or {@code null} when
   *         an error status with a redirect has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

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

    String guest = req.getParameter("guest");
    boolean isGuest = false;
    if (guest != null) {
      isGuest = Boolean.valueOf(guest);
    }

    String servicePath = req.getServicePath();
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    try {
      String id = templateVars.get("id");
      if (id == null) {
        model.put("headers", this.headerApi.getHeaders(language, isGuest));
      } else if (servicePath.endsWith("categories")) {
        model.put(
          "categories",
          this.headerApi.getCategoriesByHeaderId(id, language, isGuest)
        );
      } else {
        model.put("header", this.headerApi.getHeader(id));
      }
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
