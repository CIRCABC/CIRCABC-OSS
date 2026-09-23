package eu.europa.ec.digit.circabc.rest.dynamic.authority;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Alfresco {@link DynamicAuthority} that grants the CIRCABC leader role
 * ({@code ROLE_CIRCABC_LEADER}) to a user on a given node.
 *
 * <p>Dynamic authorities are evaluated at runtime by the Alfresco permission
 * model to decide whether a user should be treated as holding a particular
 * authority for a specific node, rather than through a statically assigned
 * group or role. This implementation resolves the CIRCABC Interest Group that
 * owns the node and its service type, then delegates to
 * {@link CircabcDynamicAuthorityService} to determine whether the user is an
 * administrator (leader) of that group for that service.</p>
 *
 * <p>The bean is registered as a Spring {@link Component} and wired into the
 * Alfresco permission configuration so that the repository can query it during
 * access checks.</p>
 */
@Component
public class CircabcInterestGroupLeaderDynamicAuthority
  implements DynamicAuthority
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(
    CircabcInterestGroupLeaderDynamicAuthority.class
  );

  /**
   * Service used to resolve CIRCABC-specific information about a node (whether
   * it is a CIRCABC node, its service type, its owning group) and to evaluate
   * administrator membership.
   */
  @Autowired
  private CircabcDynamicAuthorityService circabcService;

  /**
   * Verifies that the required collaborators have been injected once the bean
   * has been constructed.
   *
   * @throws IllegalArgumentException if the CIRCABC dynamic authority service
   *     has not been wired in
   */
  @PostConstruct
  public void init() {
    if (circabcService == null) {
      throw new IllegalArgumentException("There must be a circabc service");
    }
  }

  /**
   * Determines whether the given user holds the CIRCABC leader authority for
   * the supplied node.
   *
   * <p>The user is considered to have the authority only when the node is a
   * CIRCABC node with a known service type and an owning group, and the user is
   * an administrator (leader) of that group for that service type.</p>
   *
   * @param nodeRef the node for which the authority is being evaluated
   * @param userName the user name to check
   * @return {@code true} if the user is a leader of the node's Interest Group
   *     for the resolved service type; {@code false} otherwise
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
      return false;
    }
    CircabcServiceType serviceType = circabcService.findServiceType(nodeRef);
    if (serviceType == CircabcServiceType.UNKNOWN) {
      LOGGER.info("Node {} has unknown service type", nodeRef);
      return false;
    }
    NodeRef group = circabcService.findGroup(nodeRef);
    if (group == null) {
      LOGGER.info("Node {} has no group", nodeRef);
      return false;
    }
    LOGGER.info(
      "Checking if user {} is admin for group {} and service type {} ",
      userName,
      group,
      serviceType
    );

    return circabcService.isAdmin(group, userName, serviceType);
  }

  /**
   * Returns the name of the authority granted by this dynamic authority.
   *
   * @return the fixed authority name {@code "ROLE_CIRCABC_LEADER"}
   */
  @Override
  public String getAuthority() {
    return "ROLE_CIRCABC_LEADER";
  }

  /**
   * Returns the set of permissions for which this dynamic authority is
   * required.
   *
   * <p>Returning {@code null} indicates that the authority is not tied to a
   * specific set of permissions and may apply generally.</p>
   *
   * @return {@code null}, meaning no specific permission restriction applies
   */
  @Override
  @SuppressWarnings("java:S1168")
  public Set<PermissionReference> requiredFor() {
    return null;
  }
}
