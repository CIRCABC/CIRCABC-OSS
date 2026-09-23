package io.swagger.api;

import io.swagger.exception.NonExistingNodeException;
import io.swagger.model.GuardAuthorization;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.permissions.CategoryPermissions;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.Converter;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link GuardsApi}.
 *
 * <p>Each method resolves, for the currently authenticated user, whether a
 * given view, action or administration operation is permitted on a specific
 * node of the Alfresco repository. Authorization is computed by inspecting the
 * node's content-model aspects and the permissions set on it (via the Alfresco
 * {@link PermissionService}, {@link AuthenticationService} and
 * {@link AuthorityService}) and is returned as a {@link GuardAuthorization}
 * whose {@code granted} flag reflects the outcome. These results are typically
 * used by the frontend to decide whether UI features should be shown or
 * enabled.
 *
 * @author beaurpi
 */
public class GuardsApiImpl implements GuardsApi {

  /** Name of the "information" service, used by {@link #guardsGroupIdServiceNameGet}. */
  private static final String INFORMATION = "information";
  /** Name of the "members" service, used by {@link #guardsGroupIdServiceNameGet}. */
  private static final String MEMBERS = "members";
  /** Name of the "applicants" service, used by {@link #guardsGroupIdServiceNameGet}. */
  private static final String APPLICANTS = "applicants";
  /** Alfresco authority name of the guest (anonymous / non-authenticated) user. */
  private static final String GUEST = "guest";

  /** Alfresco service used to query and evaluate node permissions. */
  @Autowired
  private PermissionService permissionService;

  /** Alfresco service used to obtain the currently authenticated user. */
  @Autowired
  private AuthenticationService authenticationService;

  /** Alfresco service used to resolve the authorities (groups) of the current user. */
  @Autowired
  private AuthorityService authorityService;

  /** Alfresco service used to inspect node existence, aspects and child associations. */
  @Autowired
  private NodeService nodeService;

  /**
   * Determines whether the current user may view the given Interest Group.
   *
   * <p>Access is granted when the group is public (visible to guests), when it
   * is visible to registered users and the caller is authenticated (i.e. not
   * the guest user), or when the caller has explicit {@code Read} permission on
   * a non-registered group.
   *
   * @param id the identifier of the Interest Group node
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when the group may be viewed by the current user
   */
  @Override
  public GuardAuthorization guardsGroupIdGet(String id) {
    NodeRef igRef = Converter.createNodeRefFromId(id);

    GuardAuthorization result = new GuardAuthorization();
    result.setGranted(false);

    final Set<AccessPermission> allSetPermissions =
      permissionService.getAllSetPermissions(igRef);
    boolean isPublic = isAuthorityVisible(allSetPermissions, GUEST);
    boolean isRegistered = isAuthorityVisible(
      allSetPermissions,
      "GROUP_EVERYONE"
    );

    if (isPublic) {
      result.setGranted(true);
    } else if (
      isRegistered && !authenticationService.getCurrentUserName().equals(GUEST)
    ) {
      result.setGranted(true);
    } else if (!isRegistered) {
      String username = authenticationService.getCurrentUserName();

      if (username != null && !username.equals(GUEST)) {
        result.setGranted(
          permissionService
            .hasPermission(igRef, "Read")
            .equals(AccessStatus.ALLOWED)
        );
      }
    }

    return result;
  }

  /**
   * Checks whether the given authority is explicitly made visible (or hidden)
   * by the supplied set of permissions.
   *
   * <p>The set is scanned for an allowed entry that targets the given authority
   * and carries either a {@code Visibility} or {@code NoVisibility} permission.
   *
   * @param allSetPermissions the permissions currently set on a node
   * @param authority         the authority (user or group) to look for
   * @return {@code true} if an allowed {@code Visibility} permission is found
   *         for the authority; {@code false} if a {@code NoVisibility}
   *         permission is found or if no matching visibility entry exists
   */
  private boolean isAuthorityVisible(
    Set<AccessPermission> allSetPermissions,
    String authority
  ) {
    for (AccessPermission accessPermission : allSetPermissions) {
      if (
        accessPermission.getAuthority().equals(authority) &&
        accessPermission.getAccessStatus().equals(AccessStatus.ALLOWED)
      ) {
        if (accessPermission.getPermission().equals("NoVisibility")) {
          return false;
        } else if (accessPermission.getPermission().equals("Visibility")) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Determines whether the current user may access the service that owns the
   * given node.
   *
   * <p>The relevant access permission is chosen based on the node's aspect
   * (Library, Information, Newsgroup or Event).
   *
   * @param id the identifier of the node to check
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when access is allowed
   * @throws NonExistingNodeException if no node exists for the given
   *                                  identifier
   */
  @Override
  public GuardAuthorization guardsAccessIdGet(String id)
    throws NonExistingNodeException {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    if (!nodeService.exists(nodeRef)) {
      throw new NonExistingNodeException(
        "the node is not existing" + nodeRef.toString()
      );
    }

    GuardAuthorization result = new GuardAuthorization();
    result.setGranted(false);

    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
      if (
        permissionService
          .hasPermission(nodeRef, LibraryPermissions.LIBACCESS.toString())
          .equals(AccessStatus.ALLOWED)
      ) {
        result.setGranted(true);
      }
    } else if (
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ) {
      if (
        permissionService
          .hasPermission(nodeRef, InformationPermissions.INFACCESS.toString())
          .equals(AccessStatus.ALLOWED)
      ) {
        result.setGranted(true);
      }
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)) {
      if (
        permissionService
          .hasPermission(nodeRef, NewsGroupPermissions.NWSACCESS.toString())
          .equals(AccessStatus.ALLOWED)
      ) {
        result.setGranted(true);
      }
    } else if (
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT) &&
      permissionService
        .hasPermission(nodeRef, EventPermissions.EVEACCESS.toString())
        .equals(AccessStatus.ALLOWED)
    ) {
      result.setGranted(true);
    }

    return result;
  }

  /**
   * Returns the injected Alfresco {@link PermissionService}.
   *
   * @return the permission service used to evaluate node permissions
   */
  public PermissionService getPermissionService() {
    return permissionService;
  }

  /**
   * Sets the Alfresco {@link PermissionService} to be used.
   *
   * @param permissionService the permission service to inject
   */
  public void setPermissionService(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  /**
   * Determines whether the current user may edit the given node.
   *
   * <p>Editing is granted when the user (or one of the groups it belongs to)
   * holds an editing-capable library permission on the node, or when the user
   * has {@code Write} permission on it.
   *
   * @param id the identifier of the node to check
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when the node may be edited
   * @throws NonExistingNodeException if no node exists for the given
   *                                  identifier
   */
  @Override
  public GuardAuthorization guardsEditionIdGet(String id)
    throws NonExistingNodeException {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    if (!nodeService.exists(nodeRef)) {
      throw new NonExistingNodeException(
        "the node is not existing" + nodeRef.toString()
      );
    }

    GuardAuthorization result = new GuardAuthorization();
    result.setGranted(false);
    String userName = AuthenticationUtil.getRunAsUser();
    Set<String> authorities = authorityService.getAuthorities();
    for (AccessPermission ac : permissionService.getAllSetPermissions(
      nodeRef
    )) {
      if (ac.getAuthorityType() == AuthorityType.USER) {
        if (
          ac.getAuthority().equals(userName) &&
          (ac
              .getPermission()
              .equals(LibraryPermissions.LIBMANAGEOWN.toString()) ||
            ac
              .getPermission()
              .equals(LibraryPermissions.LIBFULLEDIT.toString()) ||
            ac
              .getPermission()
              .equals(LibraryPermissions.LIBEDITONLY.toString()) ||
            (ac
                .getPermission()
                .equals(LibraryPermissions.LIBADMIN.toString()) &&
              ac.getAccessStatus().name().equals("ALLOWED")))
        ) {
          result.setGranted(true);
        }
      } else if (
        ac.getAuthorityType() == AuthorityType.GROUP &&
        authorities.contains(ac.getAuthority())
      ) {
        result.setGranted(true);
      }
    }

    if (
      permissionService
        .hasPermission(nodeRef, "Write")
        .equals(AccessStatus.ALLOWED)
    ) {
      result.setGranted(true);
    }

    return result;
  }

  /**
   * Determines whether the current user may access a specific service of an
   * Interest Group.
   *
   * <p>Supported service names are {@code members}, {@code applicants} and
   * {@code information}, each of which is evaluated against the corresponding
   * directory or content permissions.
   *
   * @param groupIp     the identifier of the Interest Group node
   * @param serviceName the name of the service to check ({@code members},
   *                    {@code applicants} or {@code information})
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when the service may be accessed
   */
  @Override
  public GuardAuthorization guardsGroupIdServiceNameGet(
    String groupIp,
    String serviceName
  ) {
    NodeRef igNodeRef = Converter.createNodeRefFromId(groupIp);

    GuardAuthorization result = new GuardAuthorization();
    result.setGranted(false);

    if (MEMBERS.equals(serviceName)) {
      for (DirectoryPermissions dirPermDef : DirectoryPermissions.values()) {
        if (
          !dirPermDef.equals(DirectoryPermissions.DIRNOACCESS) &&
          permissionService
            .hasPermission(igNodeRef, dirPermDef.toString())
            .equals(AccessStatus.ALLOWED)
        ) {
          result.setGranted(true);
          break;
        }
      }
    }

    if (
      APPLICANTS.equals(serviceName) &&
      (permissionService
          .hasPermission(
            igNodeRef,
            DirectoryPermissions.DIRMANAGEMEMBERS.toString()
          )
          .equals(AccessStatus.ALLOWED) ||
        permissionService
          .hasPermission(igNodeRef, DirectoryPermissions.DIRADMIN.toString())
          .equals(AccessStatus.ALLOWED))
    ) {
      result.setGranted(true);
    }

    if (INFORMATION.equals(serviceName)) {
      NodeRef infRef = nodeService.getChildByName(
        igNodeRef,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      );
      if (
        permissionService
          .hasPermission(infRef, "Consumer")
          .equals(AccessStatus.ALLOWED)
      ) {
        result.setGranted(true);
      }
    }

    return result;
  }

  /**
   * Determines whether the current user may administer the given node.
   *
   * <p>The required administration permission depends on the node's aspect:
   * an Interest Group root requires admin rights on all of its services
   * (Library, Information, Events, Newsgroups) and on the directory, while
   * Information, Library, Newsgroup, Event and Category nodes are each checked
   * against their own administration permission.
   *
   * @param nodeId the identifier of the node to check
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when the node may be administered
   */
  @Override
  public GuardAuthorization guardsAdministrationIdGet(String nodeId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);

    GuardAuthorization result = new GuardAuthorization();
    result.setGranted(false);

    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
      NodeRef infRef = nodeService.getChildByName(
        nodeRef,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      );
      NodeRef libRef = nodeService.getChildByName(
        nodeRef,
        ContentModel.ASSOC_CONTAINS,
        "Library"
      );
      NodeRef nwsRef = nodeService.getChildByName(
        nodeRef,
        ContentModel.ASSOC_CONTAINS,
        "Newsgroups"
      );
      NodeRef evtRef = nodeService.getChildByName(
        nodeRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      );

      if (
        permissionService
          .hasPermission(libRef, LibraryPermissions.LIBADMIN.toString())
          .equals(AccessStatus.ALLOWED) &&
        permissionService
          .hasPermission(infRef, InformationPermissions.INFADMIN.toString())
          .equals(AccessStatus.ALLOWED) &&
        permissionService
          .hasPermission(evtRef, EventPermissions.EVEADMIN.toString())
          .equals(AccessStatus.ALLOWED) &&
        permissionService
          .hasPermission(nwsRef, NewsGroupPermissions.NWSADMIN.toString())
          .equals(AccessStatus.ALLOWED) &&
        permissionService
          .hasPermission(nodeRef, DirectoryPermissions.DIRADMIN.toString())
          .equals(AccessStatus.ALLOWED)
      ) {
        result.setGranted(true);
      }
    } else if (
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ) {
      result.setGranted(
        permissionService
          .hasPermission(nodeRef, InformationPermissions.INFADMIN.toString())
          .equals(AccessStatus.ALLOWED)
      );
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
      result.setGranted(
        permissionService
          .hasPermission(nodeRef, LibraryPermissions.LIBADMIN.toString())
          .equals(AccessStatus.ALLOWED)
      );
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)) {
      result.setGranted(
        permissionService
          .hasPermission(nodeRef, NewsGroupPermissions.NWSADMIN.toString())
          .equals(AccessStatus.ALLOWED)
      );
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)) {
      result.setGranted(
        permissionService
          .hasPermission(nodeRef, EventPermissions.EVEADMIN.toString())
          .equals(AccessStatus.ALLOWED)
      );
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
      result.setGranted(
        permissionService
            .hasPermission(
              nodeRef,
              CategoryPermissions.CIRCACATEGORYADMIN.toString()
            )
            .equals(AccessStatus.ALLOWED) ||
          permissionService
            .hasPermission(
              nodeRef,
              CategoryPermissions.CIRCACATEGORYMANAGEMEMBERS.toString()
            )
            .equals(AccessStatus.ALLOWED)
      );
    }

    return result;
  }

  /**
   * Determines whether the current user may administer the members of the
   * given Interest Group.
   *
   * <p>Access is granted only for an Interest Group root node on which the
   * caller holds the directory administration permission.
   *
   * @param groupIp the identifier of the Interest Group node
   * @return a {@link GuardAuthorization} whose {@code granted} flag is
   *         {@code true} when member administration is allowed
   */
  @Override
  public GuardAuthorization guardsGroupIdMembersAdminGet(String groupIp) {
    NodeRef nodeRef = Converter.createNodeRefFromId(groupIp);

    GuardAuthorization result = new GuardAuthorization();
    result.setGranted(false);

    if (
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT) &&
      permissionService
        .hasPermission(nodeRef, DirectoryPermissions.DIRADMIN.toString())
        .equals(AccessStatus.ALLOWED)
    ) {
      result.setGranted(true);
    }

    return result;
  }
}
