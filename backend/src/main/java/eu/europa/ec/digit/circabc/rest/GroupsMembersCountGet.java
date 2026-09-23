package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that handles the HTTP {@code GET} request for counting the members
 * of an Interest Group (IG).
 *
 * <p>The endpoint expects the target Interest Group identifier to be supplied as the {@code id}
 * template variable in the request URL. Before returning any data it verifies that the current user
 * holds at least directory access ({@link DirectoryPermissions#DIRACCESS}) on the given group;
 * otherwise the request is rejected.
 *
 * <p>On success the endpoint delegates to {@link GroupsApi#countMembersInIg(String)} and exposes the
 * resulting member count in the response model under the {@code count} key, which is rendered by the
 * associated FreeMarker template.
 *
 * <p>Error handling:
 * <ul>
 *   <li>Insufficient permissions result in an HTTP {@code 403 Forbidden} response.</li>
 *   <li>An invalid or unresolvable group identifier results in an HTTP {@code 400 Bad Request}
 *       response.</li>
 * </ul>
 */
public class GroupsMembersCountGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsMembersCountGet.class);

  /**
   * API providing group-related business operations, used here to count the members of an
   * Interest Group.
   */
  @Autowired
  private GroupsApi groupsApi;

  /**
   * Service used to verify that the current user holds the required permissions on the target
   * Interest Group before the member count is returned.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request and computes the number of members in the requested Interest
   * Group.
   *
   * <p>The Interest Group identifier is read from the {@code id} template variable of the request.
   * The current user must have {@link DirectoryPermissions#DIRACCESS} on that group; otherwise the
   * response status is set to {@code 403 Forbidden}. If the identifier does not resolve to a valid
   * node, the response status is set to {@code 400 Bad Request}. The multilingual-awareness flag is
   * saved on entry and restored on exit.
   *
   * @param req the web script request; must expose the Interest Group id via the {@code id}
   *            template variable
   * @param status the web script response status, updated when an error occurs
   * @param cache the cache directives for the response
   * @return a model map containing the {@code count} of members, or {@code null} when an error has
   *         been reported through the {@code status} object
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
          id,
          DirectoryPermissions.DIRACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Not enough rights for get the member count"
        );
      }

      int result = this.groupsApi.countMembersInIg(id);
      model.put("count", result);
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
