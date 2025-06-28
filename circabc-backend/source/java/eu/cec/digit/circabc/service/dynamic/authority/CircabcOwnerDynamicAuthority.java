package eu.cec.digit.circabc.service.dynamic.authority;

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

public class CircabcOwnerDynamicAuthority implements DynamicAuthority {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    CircabcOwnerDynamicAuthority.class
  );

  private OwnableService ownableService;
  private CircabcDynamicAuthorityService circabcService;

  public void setOwnableService(OwnableService ownableService) {
    this.ownableService = ownableService;
  }

  public void setCircabcService(CircabcDynamicAuthorityService circabcService) {
    this.circabcService = circabcService;
  }



  /**
   * Checks if the user has authority for a given node.
   *
   * @param  nodeRef    the reference to the node
   * @param  userName   the name of the user
   * @return            true if the user is owner of the node and still member of interest group, false otherwise
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
      if (circabcService.isGroupMember(group, userName)) {
        LOGGER.info("User {} is a member of group {}", userName, group);
        return isOwner(nodeRef, userName);
      } else {
        LOGGER.info("User {} is not a member of group {}", userName, group);
        return false;
      }
    }
  }

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

  @Override
  public String getAuthority() {
    return PermissionService.OWNER_AUTHORITY;
  }

  @Override
  public Set<PermissionReference> requiredFor() {
    return null;
  }
}
