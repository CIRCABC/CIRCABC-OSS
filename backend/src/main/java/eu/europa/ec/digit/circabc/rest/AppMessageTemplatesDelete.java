package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AppMessageApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script that handles the deletion of an application
 * message template.
 *
 * <p>This endpoint implements the HTTP {@code DELETE} behavior implied by the
 * class name ({@code AppMessageTemplatesDelete}). It removes the application
 * message template identified by the {@code id} path variable extracted from
 * the request URL.</p>
 *
 * <p>Access is restricted: the caller must be either an Alfresco administrator
 * or a CIRCABC administrator. Any other caller receives an HTTP
 * {@code 403 Forbidden} response. Unexpected failures are reported as HTTP
 * {@code 500 Internal Server Error}. On success the deletion is delegated to
 * {@link AppMessageApi#deleteAppMessageTemplate(Integer)} and an empty model map is
 * returned.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (path variable) &mdash; the identifier of the application
 *   message template to delete.</li>
 * </ul>
 */
public class AppMessageTemplatesDelete extends CircabcDeclarativeWebScript {

  /** Logger for reporting access-denied and error conditions. */
  static final Log logger = LogFactory.getLog(AppMessageTemplatesDelete.class);

  /** API providing the business logic for application message templates. */
  @Autowired
  private AppMessageApi appMessageApi;

  /** Service used to verify the current user's administrative permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Deletes the application message template identified by the {@code id}
   * template variable of the request URL.
   *
   * <p>The current user must be an Alfresco administrator or a CIRCABC
   * administrator. If the permission check fails, the response status is set to
   * {@link Status#STATUS_FORBIDDEN}. Any other failure results in
   * {@link Status#STATUS_INTERNAL_SERVER_ERROR}. In both error cases the method
   * returns {@code null} after configuring the redirect status.</p>
   *
   * @param req the web script request, whose service match provides the
   *     {@code id} template variable identifying the template to delete
   * @param status the response status, updated to reflect success or the type
   *     of failure encountered
   * @param cache the cache control settings for the response
   * @return an (empty) model map on successful deletion, or {@code null} when
   *     an access-denied or internal error occurs
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

    try {
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException("Not enough permissions");
      }

      String idStr = templateVars.get("id");
      int id = -1;
      if (idStr != null) {
        id = Integer.parseInt(idStr);
      }

      appMessageApi.deleteAppMessageTemplate(id);
    } catch (AccessDeniedException ade) {
      if (logger.isErrorEnabled()) {
        logger.error("Access denied when deleting app message template", ade);
      }
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error deleting app message template", e);
      }
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
