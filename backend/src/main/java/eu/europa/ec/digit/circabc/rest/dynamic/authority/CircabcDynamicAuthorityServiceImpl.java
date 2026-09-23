package eu.europa.ec.digit.circabc.rest.dynamic.authority;

import io.swagger.model.alfresco.CircabcModel;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link CircabcDynamicAuthorityService}.
 *
 * <p>Resolves CIRCABC-specific dynamic authorities by walking the Alfresco
 * repository hierarchy (via {@link NodeService}) and by querying persisted
 * CIRCABC permissions (via {@link CircabcDynamicAuthorityDAO}). Node
 * classification relies on the CIRCABC aspects declared in
 * {@link io.swagger.model.alfresco.CircabcModel}.
 *
 * <p>Because this service is invoked during ACS permission evaluation, the
 * repository traversals are executed as the system user through
 * {@link AuthenticationUtil#runAs} so that resolution is not blocked by the
 * permissions of the currently authenticated user.
 *
 * <p>Collaborating beans are injected by Spring via {@code @Autowired}.
 */
public class CircabcDynamicAuthorityServiceImpl
  implements CircabcDynamicAuthorityService
{

  /** Alfresco service used to inspect node types, aspects and hierarchy. */
  @Autowired
  private NodeService nodeService;

  /**
   * Service used to resolve the pivot (source-language) translation of a
   * multilingual container when walking the hierarchy.
   */
  @Autowired
  private MultilingualContentService multilingualContentService;

  /**
   * Data access object exposing persisted CIRCABC group permissions and
   * category-administrator relationships.
   */
  @Autowired
  private CircabcDynamicAuthorityDAO circabcDAO;

  /** Service used to render node paths in their namespace-prefixed form. */
  @Autowired
  private NamespaceService namespaceService;

  /**
   * {@inheritDoc}
   *
   * <p>Walks up the primary parent chain starting from {@code nodeRef} until a
   * node carrying the {@link io.swagger.model.alfresco.CircabcModel#ASPECT_IGROOT}
   * aspect (the Interest Group root) is found. Multilingual containers
   * encountered along the way are resolved to their pivot translation before
   * being inspected. The traversal runs as the system user.
   *
   * @param nodeRef the node whose governing Interest Group root is resolved
   * @return the {@link NodeRef} of the Interest Group root, or {@code null} if
   *     none is found along the ancestor chain
   */
  @Override
  public NodeRef findGroup(NodeRef nodeRef) {
    return AuthenticationUtil.runAs(
      new RunAsWork<NodeRef>() {
        public NodeRef doWork() throws Exception {
          NodeRef tempNodeRef = nodeRef;
          while (tempNodeRef != null) {
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
              return tempNodeRef;
            }
            tempNodeRef = nodeService
              .getPrimaryParent(tempNodeRef)
              .getParentRef();
          }
          return null;
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
  }

  /**
   * {@inheritDoc}
   *
   * <p>Membership is determined by looking up the persisted permissions of
   * {@code userName} on the given group; the user is considered a member when
   * at least one permission entry exists.
   *
   * @param group the CIRCABC group node
   * @param userName the user name to check
   * @return {@code true} if the user has at least one permission on the group,
   *     {@code false} otherwise
   */
  @Override
  public boolean isGroupMember(NodeRef group, String userName) {
    List<CircabcPermission> permissions = circabcDAO.getGroupPermission(
      group.toString(),
      userName
    );

    return permissions != null && !permissions.isEmpty();
  }

  /**
   * {@inheritDoc}
   *
   * <p>A user is treated as an administrator when they are a category
   * administrator of the group, or when one of their group permissions grants
   * the administrator role for the requested service area:
   * {@code LibAdmin} for {@link CircabcServiceType#LIBRARY},
   * {@code NwsAdmin}/{@code NwsModerate} for
   * {@link CircabcServiceType#NEWSGROUP}, and {@code InfAdmin} for
   * {@link CircabcServiceType#INFORMATION}.
   *
   * @param group the CIRCABC group node
   * @param userName the user name to check
   * @param serviceType the service area for which administrative rights are
   *     evaluated
   * @return {@code true} if the user administers the group for the given
   *     service, {@code false} otherwise
   */
  @Override
  public boolean isAdmin(
    NodeRef group,
    String userName,
    CircabcServiceType serviceType
  ) {
    if (circabcDAO.isCategoryAdmin(group.toString(), userName)) {
      return true;
    }
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
        (permission.getNewsGroupPermission().equals("NwsAdmin") ||
          permission.getNewsGroupPermission().equals("NwsModerate"))
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

  /**
   * {@inheritDoc}
   *
   * <p>Delegates directly to the DAO to determine whether {@code userName} is
   * an administrator of the category containing the given group.
   *
   * @param group the CIRCABC group node whose category is inspected
   * @param userName the user name to check
   * @return {@code true} if the user is a category administrator, {@code false}
   *     otherwise
   */
  @Override
  public boolean isCategoryAdmin(NodeRef group, String userName) {
    return circabcDAO.isCategoryAdmin(group.toString(), userName);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Considers the node part of CIRCABC when it carries the
   * {@link io.swagger.model.alfresco.CircabcModel#ASPECT_CIRCABC_MANAGEMENT}
   * aspect. As a fallback, it checks whether the node's namespace-prefixed path
   * is located under {@code /app:company_home/cm:CircaBC}. The evaluation runs
   * as the system user and returns {@code false} if the path cannot be
   * resolved.
   *
   * @param nodeRef the node to inspect
   * @return {@code true} if the node belongs to a CIRCABC structure,
   *     {@code false} otherwise
   */
  @Override
  public boolean isCircabcNode(NodeRef nodeRef) {
    return AuthenticationUtil.runAs(
      new RunAsWork<Boolean>() {
        public Boolean doWork() throws Exception {
          // First, keep existing aspect check
          if (
            nodeService.hasAspect(
              nodeRef,
              CircabcModel.ASPECT_CIRCABC_MANAGEMENT
            )
          ) {
            return true;
          }
          // Fallback: check if node is under /app:company_home/cm:CircaBC
          try {
            Path path = nodeService.getPath(nodeRef);
            String prefixPath = path.toPrefixString(namespaceService);
            return (
              prefixPath != null &&
              prefixPath.startsWith("/app:company_home/cm:CircaBC")
            );
          } catch (Exception e) {
            return false;
          }
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
  }

  /**
   * {@inheritDoc}
   *
   * <p>Classifies the node by inspecting its CIRCABC service aspects, in
   * order: {@link io.swagger.model.alfresco.CircabcModel#ASPECT_LIBRARY},
   * {@link io.swagger.model.alfresco.CircabcModel#ASPECT_NEWSGROUP} and
   * {@link io.swagger.model.alfresco.CircabcModel#ASPECT_INFORMATION}. The
   * inspection runs as the system user.
   *
   * @param nodeRef the node to classify
   * @return the matching {@link CircabcServiceType}, or
   *     {@link CircabcServiceType#UNKNOWN} if no service aspect is present
   */
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
