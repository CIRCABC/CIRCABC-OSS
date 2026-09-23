package io.swagger.api;

import io.swagger.model.PermissionDefinition;

/**
 * Business interface for managing the access permissions set on a repository node.
 *
 * <p>Implementations expose the operations backing the {@code /nodes/{id}/permissions}
 * REST resource: reading the permissions currently applied to a node, replacing them
 * (including toggling inheritance from the parent), removing a single permission entry
 * and clearing all locally set permissions for a given authority. Permissions are
 * resolved both for individual users and for interest-group profiles (groups), and
 * internal/technical entries such as the notification status permission or the
 * category administrator authority are filtered out.
 *
 * @author beaurpi
 * @see io.swagger.model.PermissionDefinition
 */
public interface PermissionsApi {
  /**
   * Retrieves the permissions currently set on the given node.
   *
   * <p>The returned definition reports whether the node inherits permissions from its
   * parent and lists the user and profile (group) permission entries applied to the
   * node. Entries for non-existing profiles, the guest-only technical entries, the
   * notification status permission and the category administrator authority are
   * excluded from the result.
   *
   * @param id the identifier of the node whose permissions are requested
   * @return the {@link PermissionDefinition} describing the node's inheritance flag and
   *         its user and profile permission entries
   */
  PermissionDefinition getNodeIdPermissionsGet(String id);

  /**
   * Replaces the permissions set on the given node with the supplied definition.
   *
   * <p>If the request only changes the inheritance flag, the node's parent-permission
   * inheritance is toggled accordingly. Otherwise the profile and user permission
   * entries carried by {@code body} are applied to the node.
   *
   * @param id   the identifier of the node whose permissions are being updated
   * @param body the {@link PermissionDefinition} describing the desired inheritance flag
   *             and the profile and user permission entries to apply
   * @return the updated {@link PermissionDefinition} as read back from the node
   */
  PermissionDefinition nodeIdPermissionsPut(
    String id,
    PermissionDefinition body
  );

  /**
   * Removes a single permission entry from the given node.
   *
   * @param id         the identifier of the node to update
   * @param authority  the authority (user or group) the permission is granted to
   * @param permission the name of the permission to remove for the authority
   */
  void nodeIdPermissionsDelete(String id, String authority, String permission);

  /**
   * Clears all locally set permissions for the given authority on the node.
   *
   * <p>Only permissions directly set on the node are removed; inherited permissions and
   * the notification status permission are left untouched.
   *
   * @param id        the identifier of the node to update
   * @param authority the authority (user or group) whose local permissions are cleared
   */
  void nodeIdPermissionsClear(String id, String authority);
}
