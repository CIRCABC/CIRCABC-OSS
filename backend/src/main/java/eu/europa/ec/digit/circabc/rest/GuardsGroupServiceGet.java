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
 * REST webscript endpoint that resolves the guard (authorization) status of a
 * given service within an Interest Group.
 *
 * <p>As implied by the {@code Get} suffix in the class name, this endpoint
 * handles an HTTP {@code GET} request. It reads the Interest Group identifier
 * and the service name from the request URL template variables ({@code id} and
 * {@code name} respectively) and delegates to
 * {@link GuardsApi#guardsGroupIdServiceNameGet(String, String)} to determine
 * whether access to the requested service is granted.</p>
 *
 * <p>The optional {@code language} request parameter controls localization:
 * when omitted, the response is rendered in a multilingual (ML-aware) fashion;
 * when supplied, the content and UI locale are set to the given language and
 * ML-awareness is disabled. The original ML-awareness state is always restored
 * once processing completes.</p>
 *
 * <p>If access is denied ({@link AccessDeniedException}), a
 * {@link GuardAuthorization} result with {@code granted = false} is returned
 * instead of propagating the error. An {@link InvalidNodeRefException} results
 * in an HTTP {@code 400 Bad Request} response.</p>
 */
public class GuardsGroupServiceGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GuardsGroupServiceGet.class);

  /**
   * API providing the business logic to evaluate guard (authorization) status
   * for services of an Interest Group. Injected by Spring.
   */
  @Autowired
  private GuardsApi guardsApi;

  /**
   * Executes the endpoint logic: resolves the guard authorization for the
   * service identified by the {@code id} (Interest Group) and {@code name}
   * (service) URL template variables, honouring the optional {@code language}
   * request parameter for localization.
   *
   * @param req the web script request; supplies the {@code id} and
   *     {@code name} template variables and the optional {@code language}
   *     parameter
   * @param status the response status holder, set to
   *     {@link Status#STATUS_BAD_REQUEST} when the node reference is invalid
   * @param cache the web script response cache control object
   * @return a model map containing a {@code result} entry with the
   *     {@link GuardAuthorization} outcome, or {@code null} when the request
   *     is invalid and a redirect to the bad-request response is triggered
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupIp = templateVars.get("id");
    String serviceName = templateVars.get("name");

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
      model.put(
        "result",
        this.guardsApi.guardsGroupIdServiceNameGet(groupIp, serviceName)
      );
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
