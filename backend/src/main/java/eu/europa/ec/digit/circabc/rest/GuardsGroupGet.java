package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.model.GuardAuthorization;
import io.swagger.util.CurrentUserPermissionCheckerService;
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
 * Alfresco Web Script endpoint that answers whether the current user is allowed
 * to access a given Interest Group.
 *
 * <p>This endpoint responds to the HTTP {@code GET} method (as implied by the
 * {@code Get} suffix in the class name). The Interest Group identifier is taken
 * from the {@code id} URL template variable, and an optional {@code language}
 * request parameter controls the content locale used while resolving the group.
 * When {@code language} is supplied, multilingual (ML) awareness is disabled and
 * the given locale is applied; otherwise ML awareness is enabled.</p>
 *
 * <p>The response model contains a single {@code result} entry holding a
 * {@link io.swagger.model.GuardAuthorization} whose {@code granted} flag reflects
 * whether access is permitted. If access is denied, {@code granted} is set to
 * {@code false}. If the supplied identifier does not resolve to a valid node, the
 * response is set to HTTP {@code 400 Bad Request}.</p>
 */
public class GuardsGroupGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GuardsGroupGet.class);

  /**
   * Service used to check whether the currently authenticated user is authorized
   * to access the requested Interest Group.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Determines whether the current user may access the requested Interest Group
   * and builds the web script response model.
   *
   * <p>Reads the Interest Group identifier from the {@code id} URL template
   * variable and the optional {@code language} request parameter, configures the
   * content locale and ML awareness accordingly, then delegates the access check
   * to {@link CurrentUserPermissionCheckerService#canAccessInterestGroup(String)}.
   * The original ML awareness state is always restored before returning.</p>
   *
   * @param req the web script request, providing the {@code id} template variable
   *            and the optional {@code language} parameter
   * @param status the web script status, set to {@link Status#STATUS_BAD_REQUEST}
   *               when the identifier does not resolve to a valid node
   * @param cache the web script cache directives
   * @return a model map containing a {@code result} entry with a
   *         {@link io.swagger.model.GuardAuthorization}, or {@code null} when the
   *         request is invalid and a redirect status has been set
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
      GuardAuthorization guardAuthorization = new GuardAuthorization();
      guardAuthorization.setGranted(
        this.currentUserPermissionCheckerService.canAccessInterestGroup(groupIp)
      );
      model.put("result", guardAuthorization);
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
