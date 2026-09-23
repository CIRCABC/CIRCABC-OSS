package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.model.Applicant;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.List;
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
 * REST endpoint (HTTP GET) that returns the list of membership applicants for a
 * given Interest Group (IG).
 *
 * <p>This Alfresco Web Script resolves the target Interest Group from the
 * {@code igId} URL template variable, verifies that the current user is allowed
 * to manage the group's members, and then delegates to {@link GroupsApi} to
 * retrieve the pending applicants. The resulting {@link Applicant} list is
 * exposed to the response template under the {@code applicants} model key.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code igId} — URL template variable identifying the Interest Group.</li>
 *   <li>{@code language} — optional request parameter selecting the content
 *       locale. When absent, multilingual (ML) aware retrieval is enabled so
 *       that all language variants are returned; when present, the given locale
 *       is applied for content and UI resolution.</li>
 * </ul>
 *
 * <p>Access is restricted to callers holding the
 * {@link DirectoryPermissions#DIRMANAGEMEMBERS} permission on the group;
 * otherwise the endpoint responds with HTTP 403 (Forbidden). An invalid group
 * reference results in HTTP 400 (Bad Request).</p>
 */
public class GroupsApplicantsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsApplicantsGet.class);

  /**
   * API used to retrieve the membership applicants of an Interest Group.
   */
  @Autowired
  private GroupsApi groupsApi;

  /**
   * Service used to verify that the current user holds the required directory
   * permissions before applicants are disclosed.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request and builds the response model containing the
   * Interest Group's membership applicants.
   *
   * <p>The {@code igId} URL template variable identifies the group. When the
   * optional {@code language} request parameter is provided, the corresponding
   * locale is applied and ML-aware retrieval is disabled; otherwise ML-aware
   * retrieval is enabled. The previous ML-aware state is always restored before
   * returning.</p>
   *
   * @param req the web script request, providing the {@code igId} template
   *     variable and the optional {@code language} parameter
   * @param status the response status, updated to 403 (Forbidden) when the
   *     caller lacks the required permission or 400 (Bad Request) when the group
   *     reference is invalid
   * @param cache the response cache directives
   * @return a model map with the {@code applicants} key holding the list of
   *     {@link Applicant} objects on success, or {@code null} when an error
   *     status and redirect have been set
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

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
          id,
          DirectoryPermissions.DIRMANAGEMEMBERS
        )
      ) {
        throw new AccessDeniedException(
          "Current Authority read applicants, not enough permission"
        );
      }
      List<Applicant> result = this.groupsApi.groupsIdMembersApplicantsGet(id);
      model.put("applicants", result);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
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
