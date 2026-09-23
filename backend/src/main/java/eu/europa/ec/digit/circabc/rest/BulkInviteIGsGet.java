package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.model.db.IGData;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.List;
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
 * Alfresco read-only (HTTP {@code GET}) web script endpoint that retrieves the list of Interest
 * Groups (IGs) eligible for a bulk-invite operation.
 *
 * <p>Given a category and a source Interest Group, this endpoint returns the other Interest Groups
 * within that category whose members can be bulk-invited into the source group. Before querying,
 * it verifies that the current user has Alfresco read permission on both the category node and the
 * source Interest Group node.
 *
 * <p>Request parameters (both mandatory):
 *
 * <ul>
 *   <li>{@code categoryId} &ndash; node reference identifier of the category to look under.
 *   <li>{@code igId} &ndash; node reference identifier of the source Interest Group.
 * </ul>
 *
 * <p>On success the response model exposes the resolved list under the {@code igs} key. Permission,
 * validation and unexpected failures are translated into the appropriate HTTP status codes
 * (403, 400 and 500 respectively).
 */
public class BulkInviteIGsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(BulkInviteIGsGet.class);

  /** API used to resolve the Interest Groups available for a bulk invite. */
  @Autowired
  private UsersApi usersApi;

  /** Service used to check the current user's Alfresco read permissions on the target nodes. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request: resolves the Interest Groups eligible for a bulk invite within
   * the given category, relative to the supplied source Interest Group.
   *
   * <p>The {@code categoryId} and {@code igId} request parameters are both required. The current
   * user must hold Alfresco read permission on each corresponding node. When successful, the
   * returned model contains the resolved list of {@link IGData} under the {@code igs} key.
   *
   * <p>Errors are reported through {@code status} rather than by propagating exceptions to the
   * caller: an {@link AccessDeniedException} yields {@code 403 Forbidden}, an
   * {@link InvalidNodeRefException} yields {@code 400 Bad Request}, and any other failure yields
   * {@code 500 Internal Server Error}; in those cases {@code null} is returned. The
   * {@link MLPropertyInterceptor} ML-aware flag is saved and restored around the processing.
   *
   * @param req the web script request; must provide the {@code categoryId} and {@code igId}
   *     parameters
   * @param status the web script status, used to signal error outcomes to the client
   * @param cache the web script cache directive for the response
   * @return the response model containing the eligible Interest Groups under the {@code igs} key,
   *     or {@code null} when an error status has been set
   * @throws IllegalArgumentException if {@code categoryId} or {@code igId} is missing or blank
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    String categoryId = req.getParameter("categoryId");

    if ((categoryId == null) || categoryId.trim().isEmpty()) {
      throw new IllegalArgumentException("'categoryId' cannot be empty.");
    }

    String currentIgId = req.getParameter("igId");

    if ((currentIgId == null) || currentIgId.trim().isEmpty()) {
      throw new IllegalArgumentException("'igId' cannot be empty.");
    }

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
          categoryId
        )
      ) {
        throw new AccessDeniedException("No access on node: " + categoryId);
      }
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
          currentIgId
        )
      ) {
        throw new AccessDeniedException("No access on node: " + currentIgId);
      }

      List<IGData> igs = this.usersApi.getBulkInviteIGs(
        categoryId,
        currentIgId
      );

      model.put("igs", igs);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied to user for categoryId: " +
          categoryId +
          " or igId: " +
          currentIgId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference for categoryId: " +
          categoryId +
          " or igId: " +
          currentIgId,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error processing bulk invite IGs request", e);
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
