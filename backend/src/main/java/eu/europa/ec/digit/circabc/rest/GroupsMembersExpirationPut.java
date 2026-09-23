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
 * REST webscript endpoint that updates the membership expiration date for a
 * given user within an Interest Group.
 *
 * <p>Implied HTTP method: {@code PUT} (as indicated by the {@code Put} suffix
 * in the class name). The endpoint expects the target Interest Group and user
 * to be provided as URL template variables:
 *
 * <ul>
 *   <li>{@code igId} — the identifier of the Interest Group (directory) whose
 *       membership is being updated;</li>
 *   <li>{@code userId} — the identifier of the member whose expiration date is
 *       being set.</li>
 * </ul>
 *
 * <p>The new expiration date is supplied through the {@code expirationDate}
 * request parameter and must represent a moment in the future. The caller must
 * hold the {@link DirectoryPermissions#DIRMANAGEMEMBERS} permission on the
 * Interest Group; otherwise the request is rejected with an
 * {@link AccessDeniedException} translated to an HTTP 403 response.
 *
 * <p>Validation failures are reported through HTTP status codes rather than by
 * throwing exceptions: a missing or past {@code expirationDate} yields
 * {@code 400 Bad Request}, an unparseable date yields {@code 406 Not
 * Acceptable}, and an invalid node reference yields {@code 400 Bad Request}.
 */
public class GroupsMembersExpirationPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsMembersExpirationPut.class);

  /** API facade providing the group and membership operations. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify the current user's permissions on a directory. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the request to update a member's expiration date.
   *
   * <p>Resolves the {@code igId} and {@code userId} template variables, verifies
   * that the current user may manage members of the Interest Group, validates the
   * {@code expirationDate} request parameter, and delegates the update to
   * {@link GroupsApi#groupsIdMembersUserIdExpirationPut(String, String, Date)}.
   *
   * @param req the incoming web script request, providing the {@code igId} and
   *     {@code userId} template variables and the {@code expirationDate} parameter
   * @param status the response status; set to an error code (and marked as a
   *     redirect) when permission checks or validation fail
   * @param cache the response cache directives
   * @return an empty model map on success, or {@code null} when an error status
   *     has been set (permission denied, bad request or unparseable date)
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
          "Not enough rights for updating expiration date"
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
        status.setMessage("Bad request");
        status.setRedirect(true);
        return null; // NOSONAR
      }
      this.groupsApi.groupsIdMembersUserIdExpirationPut(
        id,
        userId,
        expirationDate
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
      status.setMessage("Bad request");
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
