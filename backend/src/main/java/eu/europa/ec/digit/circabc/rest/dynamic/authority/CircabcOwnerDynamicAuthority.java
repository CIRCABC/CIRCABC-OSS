package eu.europa.ec.digit.circabc.rest.dynamic.authority;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.EqualsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * CIRCABC-specific implementation of Alfresco's {@link DynamicAuthority} for the
 * {@code OWNER} authority.
 *
 * <p>Alfresco evaluates dynamic authorities at permission-check time to decide, on a
 * per-node basis, whether the current user should be treated as holding a given
 * authority. This implementation determines whether a user should be granted the
 * {@link PermissionService#OWNER_AUTHORITY OWNER} authority on a node.</p>
 *
 * <p>The authority is granted when:</p>
 * <ul>
 *   <li>the node is not a CIRCABC node (or has no associated Interest Group) and the
 *       user is the node owner; or</li>
 *   <li>the node belongs to an Interest Group and the user is a Category Admin of that
 *       group (owner authority is granted unconditionally in this case); or</li>
 *   <li>the node belongs to an Interest Group, the user is still a member of that group
 *       and the user is the node owner.</li>
 * </ul>
 *
 * <p>Users who are no longer members of the owning Interest Group are never granted
 * owner authority, even if they are recorded as the node owner.</p>
 */
@Component
public class CircabcOwnerDynamicAuthority implements DynamicAuthority {

  /** Logger for authority-evaluation tracing. */
  private static final Logger LOGGER = LoggerFactory.getLogger(
    CircabcOwnerDynamicAuthority.class
  );

  /** Alfresco service used to resolve the owner of a node. */
  @Autowired
  private OwnableService ownableService;

  /** CIRCABC service providing node/group resolution and membership checks. */
  @Autowired
  private CircabcDynamicAuthorityService circabcService;

  /**
   * Validates that the required collaborating services have been injected.
   *
   * <p>Invoked by the Spring container after dependency injection.</p>
   *
   * @throws IllegalArgumentException if the ownable service or the CIRCABC service
   *                                  has not been wired
   */
  @PostConstruct
  public void init() {
    if (ownableService == null) {
      throw new IllegalArgumentException("There must be an ownable service");
    }
    if (circabcService == null) {
      throw new IllegalArgumentException("There must be a circabc service");
    }
  }

  /**
   * Determines whether the given user should be granted the {@code OWNER} dynamic
   * authority for the given node.
   *
   * <p>For non-CIRCABC nodes, or CIRCABC nodes with no associated Interest Group,
   * authority is granted only when the user is the node owner. For CIRCABC nodes
   * belonging to an Interest Group, Category Admins are always granted authority,
   * group members are granted it only when they are the node owner, and non-members
   * are never granted it.</p>
   *
   * @param  nodeRef    the reference to the node being evaluated
   * @param  userName   the name of the user whose authority is being checked
   * @return            {@code true} if the user should be treated as owner of the node
   *                    (and, for group nodes, is still a member of the interest group or
   *                    is a Category Admin), {@code false} otherwise
   */
  @Override
  public boolean hasAuthority(NodeRef nodeRef, String userName) {
    LOGGER.info(
      "Checking if user {} has authority for node {}",
      userName,
      nodeRef
    );
    if (!circabcService.isCircabcNode(nodeRef)) {
      LOGGER.info("Node {} is not a circabc node", nodeRef);
      return isOwner(nodeRef, userName);
    }
    NodeRef group = circabcService.findGroup(nodeRef);
    if (group == null) {
      LOGGER.info("Node {} has no group", nodeRef);
      return isOwner(nodeRef, userName);
    } else {
      // If the user is Category Admin of the Interest Group, grant OWNER dynamic authority
      if (circabcService.isCategoryAdmin(group, userName)) {
        LOGGER.info(
          "User {} is Category Admin for group {} — granting OWNER authority",
          userName,
          group
        );
        return true;
      }

      if (circabcService.isGroupMember(group, userName)) {
        LOGGER.info("User {} is a member of group {}", userName, group);
        return isOwner(nodeRef, userName);
      } else {
        LOGGER.info("User {} is not a member of group {}", userName, group);
        return false;
      }
    }
  }

  /**
   * Checks whether the given user is the recorded owner of the node.
   *
   * <p>The ownership lookup is performed as the system user to avoid being blocked by
   * the very permissions this authority helps to evaluate.</p>
   *
   * @param  nodeRef    the reference to the node
   * @param  userName   the name of the user to compare against the node owner
   * @return            {@code true} if the user is the node owner, {@code false} otherwise
   */
  private boolean isOwner(NodeRef nodeRef, String userName) {
    return AuthenticationUtil.runAs(
      new RunAsWork<Boolean>() {
        public Boolean doWork() throws Exception {
          return EqualsHelper.nullSafeEquals(
            ownableService.getOwner(nodeRef),
            userName
          );
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
  }

  /**
   * Returns the name of the authority represented by this dynamic authority.
   *
   * @return the {@link PermissionService#OWNER_AUTHORITY} constant
   */
  @Override
  public String getAuthority() {
    return PermissionService.OWNER_AUTHORITY;
  }

  /**
   * Returns the set of permissions for which this dynamic authority is required.
   *
   * <p>Returning {@code null} indicates that this authority is not restricted to a
   * specific set of permissions and may apply to any permission check.</p>
   *
   * @return {@code null}, meaning the authority is not tied to specific permissions
   */
  @Override
  @SuppressWarnings("java:S1168")
  public Set<PermissionReference> requiredFor() {
    return null;
  }
}
