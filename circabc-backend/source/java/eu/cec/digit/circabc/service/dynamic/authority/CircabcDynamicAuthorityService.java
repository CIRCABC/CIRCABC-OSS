package eu.cec.digit.circabc.service.dynamic.authority;

import org.alfresco.service.cmr.repository.NodeRef;

enum CircabcServiceType {
  LIBRARY,
  NEWSGROUP,
  INFORMATION,
  UNKNOWN,
}

public interface CircabcDynamicAuthorityService {
  boolean isCircabcNode(NodeRef nodeRef);
  NodeRef findGroup(NodeRef nodeRef);
  CircabcServiceType findServiceType(NodeRef nodeRef);
  boolean isGroupMember(NodeRef group, String userName);
  boolean isAdmin(
    NodeRef group,
    String userName,
    CircabcServiceType serviceType
  );
}
