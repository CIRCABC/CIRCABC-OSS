package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object representing a single profile-to-permission mapping within a
 * permission definition.
 *
 * <p>Each instance associates a {@link Profile} with the {@link #permission} granted to
 * it, and indicates via {@link #inherited} whether that permission is directly assigned
 * on the node or inherited from an ancestor. Collections of these entries describe the
 * effective permissions of the profiles configured on a CIRCABC node.
 */
@jakarta.annotation.Generated(
  value = "class io.swagger.codegen.languages.SpringCodegen",
  date = "2016-11-25T13:04:03.820Z"
)
public class PermissionDefinitionPermissionsProfiles {

  /** The profile to which the permission applies. */
  private Profile profile = null;
  /** Whether the permission is inherited from an ancestor node rather than set directly. */
  private Boolean inherited = null;
  /** The permission (access right) granted to the profile. */
  private String permission = null;

  /**
   * Get profile
   *
   * @return profile
   */
  public Profile getProfile() {
    return profile;
  }

  /**
   * Sets the profile to which the permission applies.
   *
   * @param profile the profile to associate with this entry
   */
  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  /**
   * Get permission
   *
   * @return permission
   */
  public String getPermission() {
    return permission;
  }

  /**
   * Sets the permission granted to the profile.
   *
   * @param permission the permission (access right) to set
   */
  public void setPermission(String permission) {
    this.permission = permission;
  }

  /**
   * @return the inherited
   */
  public Boolean getInherited() {
    return inherited;
  }

  /**
   * @param inherited the inherited to set
   */
  public void setInherited(Boolean inherited) {
    this.inherited = inherited;
  }

  /**
   * Compares this entry with another for equality based on the profile and permission.
   * The {@link #inherited} flag is intentionally excluded from the comparison.
   *
   * @param o the object to compare with
   * @return {@code true} if the other object is a
   *     {@code PermissionDefinitionPermissionsProfiles} with the same profile and
   *     permission, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PermissionDefinitionPermissionsProfiles permissionDefinitionPermissionsProfiles =
      (PermissionDefinitionPermissionsProfiles) o;
    return (
      Objects.equals(
        this.profile,
        permissionDefinitionPermissionsProfiles.profile
      ) &&
      Objects.equals(
        this.permission,
        permissionDefinitionPermissionsProfiles.permission
      )
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, computed from the
   * profile and permission.
   *
   * @return the hash code for this entry
   */
  @Override
  public int hashCode() {
    return Objects.hash(profile, permission);
  }

  /**
   * Returns a human-readable, multi-line representation of this entry, primarily for
   * debugging and logging.
   *
   * @return a string representation of this entry
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PermissionDefinitionPermissionsProfiles {\n");

    sb.append("    profile: ").append(toIndentedString(profile)).append("\n");
    sb
      .append("    permission: ")
      .append(toIndentedString(permission))
      .append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert; may be {@code null}
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
