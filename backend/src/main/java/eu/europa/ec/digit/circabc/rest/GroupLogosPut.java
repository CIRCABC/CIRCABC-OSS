package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.exception.CustomizationException;
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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script backing the HTTP {@code PUT} endpoint that selects the active
 * logo of an Interest Group.
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, where the trailing
 * {@code Put} denotes the HTTP {@code PUT} verb. The endpoint marks a previously uploaded logo
 * (identified by {@code logoId}) as the currently selected logo for the group identified by
 * {@code id}.
 *
 * <p>Inputs are taken from the request:
 * <ul>
 *   <li>{@code id} (URL template variable) &ndash; the identifier of the target group.</li>
 *   <li>{@code logoId} (URL template variable) &ndash; the identifier of the logo to select.</li>
 *   <li>{@code language} (optional query parameter) &ndash; when supplied, the content and UI
 *       locale are set to this language and multilingual (ML) awareness is disabled; otherwise
 *       ML awareness is enabled.</li>
 * </ul>
 *
 * <p>Only group administrators may invoke this operation. When the caller lacks the required
 * permission the response is set to {@link Status#STATUS_FORBIDDEN}; an invalid node reference
 * results in {@link Status#STATUS_BAD_REQUEST}; and a customization failure results in
 * {@link Status#STATUS_INTERNAL_SERVER_ERROR}.
 */
public class GroupLogosPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupLogosPut.class);

  /** API providing group-related business operations, including logo selection. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current user has group-administrator permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code PUT} request that selects a group's active logo.
   *
   * <p>Reads the {@code id} and {@code logoId} template variables and the optional
   * {@code language} query parameter, configures the multilingual/locale context accordingly,
   * verifies that the current user is an administrator of the group, and delegates the selection
   * to {@link GroupsApi#putSelectedLogo(String, String)}. The multilingual awareness flag is
   * always restored to its original value before returning.
   *
   * @param req the web script request providing the URL template variables and query parameters
   * @param status the response status, updated with an appropriate HTTP error code when the
   *     operation cannot be completed
   * @param cache the response cache directives
   * @return an empty model map on success, or {@code null} when an error occurs and the status
   *     has been set to redirect to an error response
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
    String logoId = templateVars.get("logoId");

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
      if (!this.currentUserPermissionCheckerService.isGroupAdmin(groupId)) {
        throw new AccessDeniedException(
          "Cannot update a selected logo, not enough permissions"
        );
      }

      if (groupId != null) {
        this.groupsApi.putSelectedLogo(groupId, logoId);
      }
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
    } catch (CustomizationException e) {
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
