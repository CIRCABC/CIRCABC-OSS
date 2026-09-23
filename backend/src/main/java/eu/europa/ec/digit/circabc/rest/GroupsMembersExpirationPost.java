package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that sets (or updates) the membership expiration date
 * for a specific user within an interest group's directory.
 *
 * <p>As implied by the {@code Post} suffix, this endpoint handles an HTTP
 * {@code POST} request. It is bound to a URL that carries the interest group
 * identifier ({@code igId}) and the target user identifier ({@code userId}) as
 * path (template) variables.
 *
 * <p>Expected inputs:
 * <ul>
 *   <li>{@code igId} (path variable) &ndash; the interest group / directory id.</li>
 *   <li>{@code userId} (path variable) &ndash; the id of the member whose
 *       expiration date is being set.</li>
 *   <li>{@code expirationDate} (request parameter) &ndash; the new expiration
 *       date; it must be parseable and must be in the future.</li>
 *   <li>{@code profileId} (request parameter) &ndash; the profile assigned to
 *       the member.</li>
 *   <li>{@code alfrescoGroup} (request parameter) &ndash; the underlying
 *       Alfresco group the membership belongs to.</li>
 * </ul>
 *
 * <p>The caller must hold the {@link DirectoryPermissions#DIRMANAGEMEMBERS}
 * permission on the directory; otherwise the request is rejected with an HTTP
 * {@code 403 Forbidden}. Validation and processing failures are translated into
 * appropriate HTTP error statuses ({@code 400 Bad Request},
 * {@code 406 Not Acceptable}).
 */
public class GroupsMembersExpirationPost extends CircabcDeclarativeWebScript {

  /** Status message returned when the request is malformed or missing required parameters. */
  public static final String BAD_REQUEST = "Bad request";
  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    GroupsMembersExpirationPost.class
  );

  /** API used to apply the membership expiration change to the interest group. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current user has the required directory permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code POST} request that sets a member's expiration date.
   *
   * <p>The interest group id and user id are read from the URL template
   * variables, while the expiration date, profile id and Alfresco group are read
   * from request parameters. The current user's directory permissions are checked
   * and the supplied values are validated before delegating to
   * {@link GroupsApi#groupsIdMembersUserIdExpirationPost(String, String, Date, String, String)}.
   *
   * @param req    the web script request carrying the template variables and
   *               request parameters
   * @param status the response status, updated with an HTTP error code, message
   *               and redirect flag when validation, permission or processing
   *               errors occur
   * @param cache  the response cache directives
   * @return an (empty) model map on success, or {@code null} when the request is
   *         rejected and an error status has been set on {@code status}
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");
    String userId = templateVars.get("userId");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
          id,
          DirectoryPermissions.DIRMANAGEMEMBERS
        )
      ) {
        throw new AccessDeniedException(
          "Not enough rights for adding expiration date"
        );
      }

      Date expirationDate;

      if (req.getParameter("expirationDate") != null) {
        expirationDate = Converter.convertStringToDate(
          req.getParameter("expirationDate")
        );
        // Validate that the expiration date is in the future
        if (!expirationDate.after(new Date())) {
          status.setCode(Status.STATUS_BAD_REQUEST);
          status.setMessage("Expiration date must be in the future");
          status.setRedirect(true);
          return null; // NOSONAR
        }
      } else {
        status.setCode(Status.STATUS_BAD_REQUEST);
        status.setMessage(BAD_REQUEST);
        status.setRedirect(true);
        return null; // NOSONAR
      }
      String profileId = req.getParameter("profileId");
      String alfrescoGroup = req.getParameter("alfrescoGroup");

      if (profileId == null || alfrescoGroup == null) {
        status.setCode(Status.STATUS_BAD_REQUEST);
        status.setMessage(BAD_REQUEST);
        status.setRedirect(true);
        return null; // NOSONAR
      }

      this.groupsApi.groupsIdMembersUserIdExpirationPost(
        id,
        userId,
        expirationDate,
        profileId,
        alfrescoGroup
      );
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage(BAD_REQUEST);
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (java.text.ParseException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage("Bad body");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
