package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.model.ApplicantAction;
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
 * Alfresco declarative web script backing the HTTP {@code POST} endpoint that submits the current
 * user's membership application to an Interest Group.
 *
 * <p>The endpoint is resolved from the {@code igId} template variable in the request URL, which
 * identifies the target Interest Group. The request body is expected to contain an
 * {@link io.swagger.model.ApplicantAction} describing the membership application, parsed via
 * {@link ApplicantActionJsonParser}. An optional {@code language} request parameter controls the
 * content locale used while processing the request.</p>
 *
 * <p>Before the application is registered, the current authority must hold Alfresco read
 * permission on the Interest Group node; otherwise an {@link AccessDeniedException} is raised and
 * the response is set to {@code 403 Forbidden}. Malformed input results in {@code 400 Bad
 * Request}. The actual application logic is delegated to {@link GroupsApi}.</p>
 */
public class GroupsApplicantsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsApplicantsPost.class);

  /** API providing the group-related business operations, including membership applications. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current user has the required Alfresco permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code POST} request that submits the current user's membership application for the
   * Interest Group identified by the {@code igId} URL template variable.
   *
   * <p>When a {@code language} request parameter is supplied, the corresponding locale is applied
   * to the content and UI, and multilingual (ML) awareness is disabled; otherwise ML awareness is
   * enabled. The original ML-aware state is always restored before the method returns. The current
   * authority must have Alfresco read permission on the target node; the request body is parsed
   * into an {@link ApplicantAction} and forwarded to {@link GroupsApi}.</p>
   *
   * @param req the web script request, providing the {@code igId} template variable, the optional
   *     {@code language} parameter and the JSON body describing the application
   * @param status the response status, set to {@code 403 Forbidden} on permission failure or
   *     {@code 400 Bad Request} on invalid input
   * @param cache the cache control settings for the response
   * @return an empty model map on success, or {@code null} when the request fails and a redirect
   *     status has been set
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
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)
      ) {
        throw new AccessDeniedException(
          "Current Authority cannot post his membership application, not enough permission"
        );
      }
      ApplicantAction body = ApplicantActionJsonParser.parseJSON(req);
      this.groupsApi.groupsIdMembersApplicantsPost(id, body);
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
