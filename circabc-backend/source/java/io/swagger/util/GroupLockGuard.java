package io.swagger.util;

import io.swagger.api.GroupLockApi;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spring-managed helper that enforces the "locked IG" access rule from any
 * webscript, regardless of whether it extends the standard Alfresco
 * {@code DeclarativeWebScript} or the CIRCABC {@code CircabcDeclarativeWebScript}.
 *
 * <p>When an Interest Group is locked, all users other than IG leaders and
 * category admins must be denied access to any content within that IG — both
 * for writes and for reads. Writes are already covered by
 * {@code checkGroupReadOnlyMode} in {@code CircabcDeclarativeWebScript}; this
 * helper closes the read-side gap on GET endpoints that would otherwise still
 * expose data via direct API calls (Swagger, custom HTTP clients, etc.).</p>
 *
 * <p>The check is a no-op when:</p>
 * <ul>
 *   <li>The helper has no {@link GroupLockApi} wired (defensive).</li>
 *   <li>The provided node id cannot be resolved to an existing node.</li>
 *   <li>The node is not scoped under any Interest Group.</li>
 *   <li>The IG is not currently locked.</li>
 *   <li>The current user is an IG leader or category admin.</li>
 * </ul>
 */
public class GroupLockGuard {

  private static final Log logger = LogFactory.getLog(GroupLockGuard.class);

  private static final String LOCKED_MESSAGE = "Interest group is locked";

  private GroupLockApi groupLockApi;
  private NodeService nodeService;
  private ApiToolBox apiToolBox;

  /**
   * Verifies that the current user can access the Interest Group containing the
   * given node. Throws {@link AccessDeniedException} when the IG is locked and
   * the user is neither a leader nor a category admin.
   *
   * @param nodeId the ID of a node inside the IG (space, content, event, topic, etc.)
   * @throws AccessDeniedException if the IG is locked for the current user
   */
  public void checkAccess(final String nodeId) {
    if (groupLockApi == null || nodeId == null) {
      return;
    }

    final NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    if (nodeRef == null || !nodeService.exists(nodeRef)) {
      return;
    }

    final NodeRef igNodeRef = apiToolBox.getCurrentInterestGroup(nodeRef);
    if (igNodeRef == null) {
      return;
    }

    final String igId = igNodeRef.getId();
    if (!groupLockApi.canAccessLockedGroup(igId)) {
      if (logger.isWarnEnabled()) {
        final String username = AuthenticationUtil.getFullyAuthenticatedUser();
        logger.warn(
          "Access blocked on locked IG " +
          igId +
          " for node " +
          nodeId +
          " by user " +
          username
        );
      }
      throw new AccessDeniedException(LOCKED_MESSAGE);
    }
  }

  /**
   * Same as {@link #checkAccess(String)}, but accepts an IG id directly. Useful
   * for endpoints whose URL template already carries the IG id (e.g.
   * {@code /groups/{igId}/events}).
   *
   * @param igId the ID of the Interest Group
   * @throws AccessDeniedException if the IG is locked for the current user
   */
  public void checkAccessByIgId(final String igId) {
    if (groupLockApi == null || igId == null) {
      return;
    }

    if (!groupLockApi.canAccessLockedGroup(igId)) {
      if (logger.isWarnEnabled()) {
        final String username = AuthenticationUtil.getFullyAuthenticatedUser();
        logger.warn(
          "Access blocked on locked IG " + igId + " by user " + username
        );
      }
      throw new AccessDeniedException(LOCKED_MESSAGE);
    }
  }

  public void setGroupLockApi(final GroupLockApi groupLockApi) {
    this.groupLockApi = groupLockApi;
  }

  public void setNodeService(final NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setApiToolBox(final ApiToolBox apiToolBox) {
    this.apiToolBox = apiToolBox;
  }
}
