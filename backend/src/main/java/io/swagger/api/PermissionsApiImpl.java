package io.swagger.api;

import io.swagger.model.PermissionDefinition;
import io.swagger.model.PermissionDefinitionPermissionsProfiles;
import io.swagger.model.PermissionDefinitionPermissionsUsers;
import io.swagger.model.Profile;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.util.List;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link PermissionsApi}.
 *
 * <p>Provides the business logic for reading and mutating the access
 * permissions of a node (document, folder, topic, etc.) in the Alfresco
 * repository. Permissions are exposed as a {@link PermissionDefinition} that
 * distinguishes between per-user grants and per-profile (CIRCABC Interest
 * Group profile) grants, and that also carries the node's permission
 * inheritance flag.</p>
 *
 * <p>Alfresco authorities are mapped to CIRCABC concepts as follows:</p>
 * <ul>
 *   <li>{@link AuthorityType#USER} authorities become user permissions,
 *       resolved to a full user via {@link UsersApi}.</li>
 *   <li>{@link AuthorityType#GROUP}/{@link AuthorityType#EVERYONE} authorities
 *       (plus the {@link #GUEST} authority) become profile permissions,
 *       resolved against the Interest Group profiles provided by
 *       {@link ProfilesApi}.</li>
 * </ul>
 *
 * <p>Internal, technical permissions such as {@code NotificationStatus},
 * category-admin authorities and the guest authority are filtered out where
 * appropriate so that only meaningful, editable permissions are returned.</p>
 *
 * @author beaurpi
 */
public class PermissionsApiImpl implements PermissionsApi {

  /** Name of the special Alfresco authority representing guest/anonymous access. */
  public static final String GUEST = "guest";

  /**
   * Name of the internal permission used to track a user's notification
   * subscription state. It is filtered out of the returned permission
   * definitions because it is not an access-control permission.
   */
  private static final String NOTIFICATION_STATUS = "NotificationStatus";

  /** Alfresco service used to read, set and delete node permissions and inheritance. */
  @Autowired
  private PermissionService permissionService;

  /** API used to resolve the profiles defined in an Interest Group. */
  @Autowired
  private ProfilesApi profilesApi;

  /** API used to resolve an Alfresco user authority to a full user representation. */
  @Autowired
  private UsersApi usersApi;

  /** Helper providing repository navigation utilities, e.g. locating the current Interest Group. */
  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Retrieves the permission definition of a node.
   *
   * <p>Backs the {@code GET .../nodes/{id}/permissions} REST endpoint. It reads
   * every permission set on the node and splits it into user permissions and
   * profile permissions, while filtering out internal
   * ({@code NotificationStatus}), guest and category-admin entries. The
   * returned definition also reflects whether the node inherits permissions
   * from its parent.</p>
   *
   * <p>Profile permissions referring to a profile that no longer exists in the
   * Interest Group are silently skipped.</p>
   *
   * @param id the identifier of the node whose permissions are requested
   * @return the {@link PermissionDefinition} describing the node's user and
   *         profile permissions and its inheritance flag
   */
  @Override
  public PermissionDefinition getNodeIdPermissionsGet(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    NodeRef igRef = apiToolBox.getCurrentInterestGroup(nodeRef);
    List<Profile> groupProfiles = profilesApi.groupsIdProfilesGet(
      igRef.getId(),
      null,
      false
    );

    PermissionDefinition result = new PermissionDefinition();
    result.setInherited(permissionService.getInheritParentPermissions(nodeRef));

    Set<AccessPermission> perms = permissionService.getAllSetPermissions(
      nodeRef
    );
    for (AccessPermission perm : perms) {
      if (
        perm.getAuthorityType().equals(AuthorityType.USER) &&
        !perm.getPermission().equals(NOTIFICATION_STATUS) &&
        !perm.getAuthority().equals(GUEST)
      ) {
        result.getPermissions().getUsers().add(getUserPermission(perm));
      } else if (
        !perm.getAuthority().contains("CircaCategoryAdmin") &&
        (perm.getAuthority().equals(GUEST) ||
          ((perm.getAuthorityType().equals(AuthorityType.GROUP) ||
              perm.getAuthorityType().equals(AuthorityType.EVERYONE)) &&
            !perm.getPermission().equals(NOTIFICATION_STATUS)))
      ) {
        // necessary because some nodes can have permissions referring
        // non existing profile
        PermissionDefinitionPermissionsProfiles tmp = getProfilePermission(
          perm,
          groupProfiles
        );
        if (tmp.getProfile() != null) {
          result.getPermissions().getProfiles().add(tmp);
        }
      }
    }

    return result;
  }

  /**
   * Builds a user permission entry from an Alfresco access permission.
   *
   * @param perm the Alfresco access permission whose authority is a user
   * @return a {@link PermissionDefinitionPermissionsUsers} holding the resolved
   *         user, the granted permission and its inheritance flag
   */
  private PermissionDefinitionPermissionsUsers getUserPermission(
    AccessPermission perm
  ) {
    PermissionDefinitionPermissionsUsers result =
      new PermissionDefinitionPermissionsUsers();
    result.setUser(usersApi.usersUserIdGet(perm.getAuthority()));
    result.setPermission(perm.getPermission());
    result.setInherited(perm.isInherited());
    return result;
  }

  /**
   * Builds a profile permission entry from an Alfresco access permission.
   *
   * <p>The profile is resolved from the supplied Interest Group profiles; if no
   * matching profile is found the returned entry's profile will be
   * {@code null}, allowing the caller to skip stale permissions.</p>
   *
   * @param perm          the Alfresco access permission whose authority is a
   *                      group/profile authority
   * @param groupProfiles the profiles defined in the current Interest Group
   * @return a {@link PermissionDefinitionPermissionsProfiles} holding the
   *         resolved profile, the granted permission and its inheritance flag
   */
  private PermissionDefinitionPermissionsProfiles getProfilePermission(
    AccessPermission perm,
    List<Profile> groupProfiles
  ) {
    PermissionDefinitionPermissionsProfiles result =
      new PermissionDefinitionPermissionsProfiles();
    result.setProfile(getProfile(perm, groupProfiles));
    result.setPermission(perm.getPermission());
    result.setInherited(perm.isInherited());
    return result;
  }

  /**
   * Finds the Interest Group profile matching a permission's authority.
   *
   * @param perm          the access permission whose authority is compared
   *                      against each profile's group name
   * @param groupProfiles the profiles defined in the current Interest Group
   * @return the matching {@link Profile}, or {@code null} if none of the
   *         supplied profiles matches the permission's authority
   */
  private Profile getProfile(
    AccessPermission perm,
    List<Profile> groupProfiles
  ) {
    Profile result = null;
    for (Profile p : groupProfiles) {
      if (perm.getAuthority().equals(p.getGroupName())) {
        result = p;
      }
    }
    return result;
  }

  /**
   * Updates the permission definition of a node.
   *
   * <p>Backs the {@code PUT .../nodes/{id}/permissions} REST endpoint. The
   * behaviour depends on how the requested inheritance flag differs from the
   * node's current state:</p>
   * <ul>
   *   <li>If inheritance is being turned on (was off, now requested on), parent
   *       permission inheritance is enabled and no explicit permissions are
   *       changed.</li>
   *   <li>If inheritance is being turned off (was on, now requested off), parent
   *       permission inheritance is disabled.</li>
   *   <li>Otherwise, the profile and user permissions carried in {@code body}
   *       are applied to the node.</li>
   * </ul>
   *
   * @param id   the identifier of the node to update
   * @param body the desired permission definition, including the inheritance
   *             flag and the user/profile permissions to set
   * @return the refreshed {@link PermissionDefinition} of the node after the
   *         update
   */
  @Override
  public PermissionDefinition nodeIdPermissionsPut(
    String id,
    PermissionDefinition body
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    PermissionDefinition oldPerms = getNodeIdPermissionsGet(id);
    if (
      Boolean.FALSE.equals(oldPerms.getInherited()) &&
      Boolean.TRUE.equals(body.getInherited())
    ) {
      permissionService.setInheritParentPermissions(nodeRef, true);
      return getNodeIdPermissionsGet(id);
    } else if (
      Boolean.TRUE.equals(oldPerms.getInherited()) &&
      Boolean.FALSE.equals(body.getInherited())
    ) {
      permissionService.setInheritParentPermissions(nodeRef, false);
    } else {
      for (PermissionDefinitionPermissionsProfiles pdpp : body
        .getPermissions()
        .getProfiles()) {
        this.permissionService.setPermission(
          nodeRef,
          pdpp.getProfile().getGroupName(),
          pdpp.getPermission(),
          true
        );
      }

      for (PermissionDefinitionPermissionsUsers pdpu : body
        .getPermissions()
        .getUsers()) {
        this.permissionService.setPermission(
          nodeRef,
          pdpu.getUser().getUserId(),
          pdpu.getPermission(),
          true
        );
      }
    }
    return getNodeIdPermissionsGet(id);
  }

  /**
   * Deletes a single permission granted to an authority on a node.
   *
   * <p>Backs the delete-permission REST endpoint. The permission is only
   * removed when the node id, authority and permission are all provided
   * (authority and permission must be non-empty).</p>
   *
   * @param id         the identifier of the node
   * @param authority  the authority (user or group) whose permission is removed
   * @param permission the name of the permission to remove
   */
  @Override
  public void nodeIdPermissionsDelete(
    String id,
    String authority,
    String permission
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (id != null && !"".equals(authority) && !"".equals(permission)) {
      permissionService.deletePermission(nodeRef, authority, permission);
    }
  }

  /**
   * Clears all directly-set permissions granted to an authority on a node.
   *
   * <p>Backs the clear-permissions REST endpoint. Every permission set for the
   * given authority on the node is removed, except inherited permissions and
   * the internal {@code NotificationStatus} permission, which are preserved.
   * The operation is only performed when both the node id and a non-empty
   * authority are provided.</p>
   *
   * @param id        the identifier of the node
   * @param authority the authority (user or group) whose directly-set
   *                  permissions are cleared
   */
  @Override
  public void nodeIdPermissionsClear(String id, String authority) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (id != null && !"".equals(authority)) {
      Set<AccessPermission> allPerms = permissionService.getAllSetPermissions(
        nodeRef
      );
      for (AccessPermission perm : allPerms) {
        if (
          perm.getAuthority().equals(authority) &&
          !perm.isInherited() &&
          !perm.getPermission().equals(NOTIFICATION_STATUS)
        ) {
          permissionService.deletePermission(
            nodeRef,
            perm.getAuthority(),
            perm.getPermission()
          );
        }
      }
    }
  }
}
