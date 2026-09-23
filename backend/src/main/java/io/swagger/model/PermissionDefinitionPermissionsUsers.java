package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object that associates a single {@link User} with the permission
 * granted to them within a permission definition.
 *
 * <p>Instances describe one entry in the list of user-level permissions returned
 * as part of a node's permission definition: the user the permission applies to,
 * the permission level itself, and whether that permission is inherited from a
 * parent node rather than set directly on the current node.
 */
public class PermissionDefinitionPermissionsUsers {

  /** The user to whom the permission is granted. */
  private User user = null;

  /** Whether the permission is inherited from a parent node ({@code true}) or set directly ({@code false}). */
  private Boolean inherited;

  /** The permission level granted to the user (e.g. an Alfresco permission name). */
  private String permission = null;

  /**
   * Returns the user to whom the permission is granted.
   *
   * @return the associated user
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets the user to whom the permission is granted.
   *
   * @param user the user to associate with this permission entry
   */
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Returns the permission level granted to the user.
   *
   * @return the permission level
   */
  public String getPermission() {
    return permission;
  }

  /**
   * Sets the permission level granted to the user.
   *
   * @param permission the permission level to set
   */
  public void setPermission(String permission) {
    this.permission = permission;
  }

  /**
   * Returns whether the permission is inherited from a parent node.
   *
   * @return {@code true} if the permission is inherited, {@code false} if set directly
   */
  public Boolean getInherited() {
    return inherited;
  }

  /**
   * Sets whether the permission is inherited from a parent node.
   *
   * @param inherited {@code true} if the permission is inherited, {@code false} if set directly
   */
  public void setInherited(Boolean inherited) {
    this.inherited = inherited;
  }

  /**
   * Compares this object with another for equality based on the associated user
   * and permission level.
   *
   * @param o the object to compare with
   * @return {@code true} if the other object is a {@code PermissionDefinitionPermissionsUsers}
   *     with an equal user and permission, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PermissionDefinitionPermissionsUsers permissionDefinitionPermissionsUsers =
      (PermissionDefinitionPermissionsUsers) o;
    return (
      Objects.equals(this.user, permissionDefinitionPermissionsUsers.user) &&
      Objects.equals(
        this.permission,
        permissionDefinitionPermissionsUsers.permission
      )
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, based on the
   * associated user and permission level.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(user, permission);
  }

  /**
   * Returns a human-readable string representation of this object.
   *
   * @return a string describing the user and permission fields
   */
  @Override
  public String toString() {
    return (
      "class PermissionDefinitionPermissionsUsers {\n" +
      "    user: " +
      toIndentedString(user) +
      "\n" +
      "    permission: " +
      toIndentedString(permission) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert to an indented string
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
