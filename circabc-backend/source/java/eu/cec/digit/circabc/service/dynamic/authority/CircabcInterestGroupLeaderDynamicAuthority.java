package eu.cec.digit.circabc.service.dynamic.authority;

import java.util.Set;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;

public class CircabcInterestGroupLeaderDynamicAuthority
  implements DynamicAuthority {

  private CircabcDynamicAuthorityService circabcService;

  public void setCircabcService(CircabcDynamicAuthorityService circabcService) {
    this.circabcService = circabcService;
  }

  public void init() {
    if (circabcService == null) {
      throw new IllegalArgumentException("There must be a circabc service");
    }
  }

  @Override
  public boolean hasAuthority(NodeRef nodeRef, String userName) {
    if (!circabcService.isCircabcNode(nodeRef)) {
      return false;
    }
    CircabcServiceType serviceType = circabcService.findServiceType(nodeRef);
    if (serviceType == CircabcServiceType.UNKNOWN) {
      return false;
    }
    NodeRef group = circabcService.findGroup(nodeRef);
    if (group == null) {
      return false;
    }

    return circabcService.isAdmin(group, userName, serviceType);
  }

  @Override
  public String getAuthority() {
    return "ROLE_CIRCABC_LEADER";
  }

  @Override
  public Set<PermissionReference> requiredFor() {
    return null;
  }
}
