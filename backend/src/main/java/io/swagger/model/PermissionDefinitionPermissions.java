package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data model describing the permission assignments attached to a permission
 * definition.
 *
 * <p>It groups the permissions granted at two distinct levels:
 * <ul>
 *   <li>{@code profiles} &mdash; permissions assigned to membership profiles;</li>
 *   <li>{@code users} &mdash; permissions assigned directly to individual users.</li>
 * </ul>
 *
 * <p>This is a plain data transfer object used to serialize/deserialize the
 * JSON representation exchanged by the CIRCABC REST API.
 */
public class PermissionDefinitionPermissions {

  /** Permissions assigned to membership profiles. */
  private List<PermissionDefinitionPermissionsProfiles> profiles =
    new ArrayList<>();

  /** Permissions assigned directly to individual users. */
  private List<PermissionDefinitionPermissionsUsers> users = new ArrayList<>();

  /**
   * Returns the permissions assigned to membership profiles.
   *
   * @return the list of profile-level permission assignments
   */
  public List<PermissionDefinitionPermissionsProfiles> getProfiles() {
    return profiles;
  }

  /**
   * Sets the permissions assigned to membership profiles.
   *
   * @param profiles the list of profile-level permission assignments to set
   */
  public void setProfiles(
    List<PermissionDefinitionPermissionsProfiles> profiles
  ) {
    this.profiles = profiles;
  }

  /**
   * Returns the permissions assigned directly to individual users.
   *
   * @return the list of user-level permission assignments
   */
  public List<PermissionDefinitionPermissionsUsers> getUsers() {
    return users;
  }

  /**
   * Sets the permissions assigned directly to individual users.
   *
   * @param users the list of user-level permission assignments to set
   */
  public void setUsers(List<PermissionDefinitionPermissionsUsers> users) {
    this.users = users;
  }

  /**
   * Compares this object with another for equality based on the profile and
   * user permission assignments.
   *
   * @param o the object to compare with
   * @return {@code true} if the other object is a
   *         {@code PermissionDefinitionPermissions} with equal profiles and
   *         users, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PermissionDefinitionPermissions permissionDefinitionPermissions =
      (PermissionDefinitionPermissions) o;
    return (
      Objects.equals(this.profiles, permissionDefinitionPermissions.profiles) &&
      Objects.equals(this.users, permissionDefinitionPermissions.users)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)},
   * derived from the profile and user permission assignments.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(profiles, users);
  }

  /**
   * Returns a human-readable, multi-line string representation of this object.
   *
   * @return a string describing the profiles and users of this permission
   *         definition
   */
  @Override
  public String toString() {
    return (
      "class PermissionDefinitionPermissions {\n" +
      "    profiles: " +
      toIndentedString(profiles) +
      "\n" +
      "    users: " +
      toIndentedString(users) +
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
