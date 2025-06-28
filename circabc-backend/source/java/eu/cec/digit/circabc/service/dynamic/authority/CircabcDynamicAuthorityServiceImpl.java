package eu.cec.digit.circabc.service.dynamic.authority;

import eu.cec.digit.circabc.model.CircabcModel;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

public class CircabcDynamicAuthorityServiceImpl
  implements CircabcDynamicAuthorityService {

  private NodeService nodeService;

  private MultilingualContentService multilingualContentService;

  private CircabcDynamicAuthorityDAO circabcDAO;

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setMultilingualContentService(
    MultilingualContentService multilingualContentService
  ) {
    this.multilingualContentService = multilingualContentService;
  }

  public void setCircabcDAO(CircabcDynamicAuthorityDAO circabcDAO) {
    this.circabcDAO = circabcDAO;
  }

  @Override
  public NodeRef findGroup(NodeRef nodeRef) {
    return AuthenticationUtil.runAs(
      new RunAsWork<NodeRef>() {
        public NodeRef doWork() throws Exception {
          NodeRef result = null;
          NodeRef tempNodeRef = nodeRef;
          for (;;) {
            if (tempNodeRef == null) {
              break;
            }

            if (
              nodeService
                .getType(tempNodeRef)
                .equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)
            ) {
              tempNodeRef = multilingualContentService.getPivotTranslation(
                tempNodeRef
              );
            }

            if (
              nodeService.hasAspect(tempNodeRef, CircabcModel.ASPECT_IGROOT)
            ) {
              result = tempNodeRef;
              break;
            } else {
              tempNodeRef = nodeService
                .getPrimaryParent(tempNodeRef)
                .getParentRef();
            }
          }
          return result;
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
  }

  @Override
  public boolean isGroupMember(NodeRef group, String userName) {
    List<CircabcPermission> permissions = circabcDAO.getGroupPermission(
      group.toString(),
      userName
    );

    return permissions != null && !permissions.isEmpty();
  }

  @Override
  public boolean isAdmin(
    NodeRef group,
    String userName,
    CircabcServiceType serviceType
  ) {
    List<CircabcPermission> permissions = circabcDAO.getGroupPermission(
      group.toString(),
      userName
    );

    for (CircabcPermission permission : permissions) {
      if (
        serviceType == CircabcServiceType.LIBRARY &&
        permission.getLibraryPermission().equals("LibAdmin")
      ) {
        return true;
      }
      if (
        serviceType == CircabcServiceType.NEWSGROUP &&
        permission.getNewsGroupPermission().equals("NwsAdmin")
      ) {
        return true;
      }
      if (
        serviceType == CircabcServiceType.INFORMATION &&
        permission.getInformationPermission().equals("InfAdmin")
      ) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean isCircabcNode(NodeRef nodeRef) {
    return AuthenticationUtil.runAs(
      new RunAsWork<Boolean>() {
        public Boolean doWork() throws Exception {
          return nodeService.hasAspect(
            nodeRef,
            CircabcModel.ASPECT_CIRCABC_MANAGEMENT
          );
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
  }

  @Override
  public CircabcServiceType findServiceType(NodeRef nodeRef) {
    return AuthenticationUtil.runAs(
      new RunAsWork<CircabcServiceType>() {
        public CircabcServiceType doWork() throws Exception {
          CircabcServiceType result = CircabcServiceType.UNKNOWN;

          if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
            result = CircabcServiceType.LIBRARY;
          } else if (
            nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
          ) {
            result = CircabcServiceType.NEWSGROUP;
          } else if (
            nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
          ) {
            result = CircabcServiceType.INFORMATION;
          }
          return result;
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
  }
}
