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
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that checks whether the
 * current user is authorized to administer the members of a given group.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention: it handles a
 * {@code GET} request for the "group members admin" guard. The endpoint resolves the group
 * identifier from the URL template variable {@code id}, delegates the authorization check to
 * {@link GuardsApi#guardsGroupIdMembersAdminGet(String)} and exposes the resulting
 * {@link GuardAuthorization} under the {@code result} key of the returned model map, which the
 * associated FreeMarker template renders as JSON.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} &ndash; URL template variable identifying the target group.</li>
 *   <li>{@code language} &ndash; optional request parameter selecting the content locale; when
 *       absent, multilingual (ML) aware property resolution is enabled instead.</li>
 * </ul>
 *
 * <p>If the caller lacks the required permission an {@link AccessDeniedException} is caught and a
 * non-granted {@link GuardAuthorization} is returned; an {@link InvalidNodeRefException} results in
 * an HTTP {@code 400 Bad Request} response.
 */
public class GuardsGroupMembersAdminGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GuardsGroupMembersAdminGet.class);

  /**
   * API facade providing the guard/authorization business logic, injected by Spring.
   */
  @Autowired
  private GuardsApi guardsApi;

  /**
   * Handles the incoming {@code GET} request and builds the response model.
   *
   * <p>Reads the {@code id} template variable and the optional {@code language} parameter,
   * configures the content locale and ML-aware property resolution accordingly, then queries
   * {@link GuardsApi#guardsGroupIdMembersAdminGet(String)} to determine whether the current user
   * may administer the group's members. The previous ML-aware state is always restored before the
   * method returns.
   *
   * @param req the web script request; supplies the {@code id} template variable and the optional
   *     {@code language} parameter
   * @param status the web script response status; set to {@code 400 Bad Request} when the group
   *     reference is invalid
   * @param cache the web script cache directives (unused)
   * @return a model map containing the {@link GuardAuthorization} under the {@code result} key, or
   *     {@code null} when the request is rejected as a bad request
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupId = templateVars.get("id");

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
      model.put("result", this.guardsApi.guardsGroupIdMembersAdminGet(groupId));
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
