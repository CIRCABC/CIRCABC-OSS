package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.model.ApplicantAction;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.ApplicantActionJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script handling the HTTP {@code PUT} request that updates the status of
 * membership applicants for a given Interest Group.
 *
 * <p>The endpoint expects the Interest Group identifier as the {@code igId} URL template variable
 * and an {@link io.swagger.model.ApplicantAction} JSON payload in the request body describing which
 * applicant(s) to act upon. The optional {@code action} query parameter selects the operation to
 * perform and is honoured only when set to {@code "clean"} or {@code "decline"}; any other value
 * results in an empty action being applied. The optional {@code language} query parameter controls
 * multilingual (ML) awareness and the content/UI locale used while processing the request.</p>
 *
 * <p>Before mutating anything, the caller must hold the
 * {@link io.swagger.model.permissions.DirectoryPermissions#DIRMANAGEMEMBERS} permission on the
 * Interest Group; otherwise the request is rejected with HTTP 403 (Forbidden). The actual update is
 * delegated to {@link io.swagger.api.GroupsApi#groupsIdMembersApplicantsPut(String,
 * io.swagger.model.ApplicantAction)}.</p>
 */
public class GroupsApplicantsPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsApplicantsPut.class);

  /**
   * API facade used to apply the applicant status update on the Interest Group.
   */
  @Autowired
  private GroupsApi groupsApi;

  /**
   * Service used to verify that the current user holds the permissions required to manage
   * membership applicants.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the {@code PUT} request that updates the status of Interest Group membership
   * applicants.
   *
   * <p>The Interest Group id is read from the {@code igId} URL template variable and the requested
   * operation from the {@code action} query parameter (only {@code "clean"} and {@code "decline"}
   * are accepted). The applicant details are parsed from the JSON request body. Multilingual
   * awareness and the locale are configured from the {@code language} query parameter and restored
   * to their previous state once processing completes.</p>
   *
   * @param req the web script request, providing the {@code igId} template variable, the
   *     {@code action} and {@code language} query parameters, and the JSON body
   * @param status the response status, set to {@code 403} on permission failures or {@code 400} on
   *     malformed input
   * @param cache the cache directives for the response
   * @return an (empty) model map on success, or {@code null} when the request is rejected and a
   *     redirect status has been set
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

    String action = "";
    String actionQueryParam = req.getParameter("action");
    if (
      "clean".equals(actionQueryParam) || "decline".equals(actionQueryParam)
    ) {
      action = actionQueryParam;
    }

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
          id,
          DirectoryPermissions.DIRMANAGEMEMBERS
        )
      ) {
        throw new AccessDeniedException(
          "Current Authority update applicant's status, not enough permission"
        );
      }
      ApplicantAction body = ApplicantActionJsonParser.parseJSON(req);
      body.setAction(action);
      this.groupsApi.groupsIdMembersApplicantsPut(id, body);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
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
