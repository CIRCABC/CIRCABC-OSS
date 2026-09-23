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
 * Alfresco declarative web script that handles the deletion of a logo attached to a group.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention, so this endpoint
 * implements the HTTP {@code DELETE} behavior for a group logo. Given a group identifier and a
 * logo identifier taken from the request URL, it removes the corresponding logo from the group.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} (URL template variable) - the identifier of the group whose logo is deleted.</li>
 *   <li>{@code logoId} (URL template variable) - the identifier of the logo to delete.</li>
 *   <li>{@code language} (optional request parameter) - when supplied, the content and UI locale
 *       are set accordingly and multilingual (ML) awareness is disabled; otherwise ML awareness is
 *       enabled.</li>
 * </ul>
 *
 * <p>The operation is only permitted for group administrators; otherwise an
 * {@link AccessDeniedException} is raised and the response is set to HTTP 403 Forbidden. Invalid
 * node references or customization problems result in an HTTP 400 Bad Request.
 */
public class GroupLogosDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupLogosDelete.class);

  /** API providing the group-related business operations, including logo deletion. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current user has group administrator permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the logo deletion for the requested group.
   *
   * <p>Reads the {@code id} and {@code logoId} URL template variables and the optional
   * {@code language} request parameter, configures the multilingual (ML) awareness and locale
   * accordingly, verifies that the current user is an administrator of the group, and then deletes
   * the specified logo. The previous ML awareness state is always restored before returning.
   *
   * @param req the web script request, providing the URL template variables ({@code id},
   *     {@code logoId}) and the optional {@code language} parameter
   * @param status the response status, set to {@code 403 Forbidden} when the user lacks
   *     permission or {@code 400 Bad Request} on invalid input
   * @param cache the cache directives for the response
   * @return an empty model map on success, or {@code null} when an error occurs and the response
   *     is redirected with an error status
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
          "Cannot delete a logo, not enough permissions"
        );
      }

      if (groupId != null) {
        this.groupsApi.deleteLogo(groupId, logoId);
      }
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | CustomizationException inre) {
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
