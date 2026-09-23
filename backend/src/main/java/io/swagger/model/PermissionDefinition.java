package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object representing the complete permission configuration of a
 * single node.
 *
 * <p>It captures whether the node inherits permissions from its parent and the
 * concrete set of permissions granted on the node.
 */
public class PermissionDefinition {

  /** Whether the node inherits permissions from its parent node. */
  private Boolean inherited = false;

  /** The set of permissions defined directly on the node. */
  private PermissionDefinitionPermissions permissions =
    new PermissionDefinitionPermissions();

  /**
   * Returns whether the node inherits permissions from its parent node.
   *
   * @return {@code true} if permissions are inherited, {@code false} otherwise
   */
  public Boolean getInherited() {
    return inherited;
  }

  /**
   * Sets whether the node inherits permissions from its parent node.
   *
   * @param inherited {@code true} if permissions should be inherited
   */
  public void setInherited(Boolean inherited) {
    this.inherited = inherited;
  }

  /**
   * Returns the set of permissions defined directly on the node.
   *
   * @return the node's permissions
   */
  public PermissionDefinitionPermissions getPermissions() {
    return permissions;
  }

  /**
   * Sets the set of permissions defined directly on the node.
   *
   * @param permissions the node's permissions
   */
  public void setPermissions(PermissionDefinitionPermissions permissions) {
    this.permissions = permissions;
  }

  /**
   * Compares this permission definition to another object for equality.
   *
   * <p>Two instances are equal when both their {@code inherited} flag and their
   * {@code permissions} are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code PermissionDefinition}
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PermissionDefinition permissionDefinition = (PermissionDefinition) o;
    return (
      Objects.equals(this.inherited, permissionDefinition.inherited) &&
      Objects.equals(this.permissions, permissionDefinition.permissions)
    );
  }

  /**
   * Returns a hash code derived from the {@code inherited} flag and the
   * {@code permissions}.
   *
   * @return the hash code for this permission definition
   */
  @Override
  public int hashCode() {
    return Objects.hash(inherited, permissions);
  }

  /**
   * Returns a human-readable, multi-line string representation of this
   * permission definition.
   *
   * @return the string representation
   */
  @Override
  public String toString() {
    return (
      "class PermissionDefinition {\n" +
      "    inherited: " +
      toIndentedString(inherited) +
      "\n" +
      "    permissions: " +
      toIndentedString(permissions) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
