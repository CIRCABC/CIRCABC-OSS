package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.model.BulkImportUserData;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.*;
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
 * Read-only ({@code HTTP GET}) Alfresco webscript endpoint that gathers the members eligible for a
 * bulk invitation into a destination Interest Group (IG).
 *
 * <p>Given a set of source Interest Group node identifiers, the endpoint collects the users that
 * belong to those groups so they can subsequently be invited into a single destination Interest
 * Group. Before a source group is taken into account, the current user's Alfresco read permission
 * on that group's node is verified; a source group the caller cannot read causes the request to be
 * rejected.
 *
 * <p>Request parameters:
 * <ul>
 *   <li>{@code igIds} (multi-valued, required) — the node identifiers of the source Interest Groups
 *       whose members should be collected. Must contain at least one non-empty value.</li>
 *   <li>{@code destinationIGId} (required) — the node identifier of the Interest Group the collected
 *       members are intended to be invited into. Must be non-empty.</li>
 * </ul>
 *
 * <p>On success the response model exposes the resolved members under the {@code members} key as a
 * list of {@link io.swagger.model.BulkImportUserData}. Permission, invalid-node and unexpected
 * errors are translated into the corresponding HTTP status codes ({@code 403}, {@code 400} and
 * {@code 500} respectively) via a redirecting status response.
 */
public class BulkInviteMembersGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(BulkInviteMembersGet.class);

  /**
   * API used to resolve the members eligible for the bulk invitation from the given source Interest
   * Groups.
   */
  @Autowired
  private UsersApi usersApi;

  /**
   * Service used to verify that the current user holds Alfresco read permission on each source
   * Interest Group node before its members are collected.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request: validates the {@code igIds} and {@code destinationIGId}
   * parameters, checks that the current user can read each requested source Interest Group node and
   * collects the members eligible for the bulk invitation.
   *
   * <p>On success the returned model contains the resolved members under the {@code members} key.
   * When an error occurs the appropriate HTTP status is set on {@code status} and {@code null} is
   * returned to trigger the status redirect. The multilingual awareness state altered during
   * processing is always restored before returning.
   *
   * @param req the webscript request; must provide the multi-valued {@code igIds} parameter and the
   *            {@code destinationIGId} parameter
   * @param status the response status, used to signal permission ({@code 403}), bad request
   *               ({@code 400}) or internal error ({@code 500}) conditions
   * @param cache the response cache directives
   * @return a model map containing the {@code members} list on success, or {@code null} when an
   *         error status redirect is set
   * @throws IllegalArgumentException if {@code igIds} is missing/empty or {@code destinationIGId} is
   *                                  missing/blank
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    String[] igIdsArray = req.getParameterValues("igIds");

    if ((igIdsArray == null) || igIdsArray.length == 0) {
      throw new IllegalArgumentException("'igIds' cannot be empty.");
    }

    String destinationIGId = req.getParameter("destinationIGId");

    if ((destinationIGId == null) || destinationIGId.trim().isEmpty()) {
      throw new IllegalArgumentException("'destinationIGId' cannot be empty.");
    }

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      List<String> igs = new ArrayList<>();

      for (String nodeId : igIdsArray) {
        if (nodeId != null && !nodeId.trim().isEmpty()) {
          String trimmedNodeId = nodeId.trim();

          if (
            !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
              trimmedNodeId
            )
          ) {
            throw new AccessDeniedException(
              "No access on node:" + trimmedNodeId
            );
          }

          igs.add(trimmedNodeId);
        }
      }

      List<BulkImportUserData> members = this.usersApi.getBulkInviteMembers(
        igs,
        destinationIGId
      );

      model.put("members", members);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied error in bulk invite members", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference in bulk invite members", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error in bulk invite members", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
