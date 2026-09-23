package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model (DTO) representing the complete notification configuration of a single node.
 *
 * <p>A node's notification settings are expressed through two collections: notification
 * settings that apply per profile and notification settings that apply per individual user.
 * This class aggregates both so a node's notification configuration can be transferred as a
 * single JSON payload by the REST API.
 */
public class NotificationDefinition {

  /** Notification settings defined at the profile level for the node. */
  private List<NotificationDefinitionProfiles> profiles = new ArrayList<>();

  /** Notification settings defined at the individual-user level for the node. */
  private List<NotificationDefinitionUsers> users = new ArrayList<>();

  /**
   * Returns the profile-level notification settings for the node.
   *
   * @return the list of profile notification settings; never {@code null}
   */
  public List<NotificationDefinitionProfiles> getProfiles() {
    return profiles;
  }

  /**
   * Sets the profile-level notification settings for the node.
   *
   * @param profiles the list of profile notification settings to apply
   */
  public void setProfiles(List<NotificationDefinitionProfiles> profiles) {
    this.profiles = profiles;
  }

  /**
   * Returns the user-level notification settings for the node.
   *
   * @return the list of user notification settings; never {@code null}
   */
  public List<NotificationDefinitionUsers> getUsers() {
    return users;
  }

  /**
   * Sets the user-level notification settings for the node.
   *
   * @param users the list of user notification settings to apply
   */
  public void setUsers(List<NotificationDefinitionUsers> users) {
    this.users = users;
  }

  /**
   * Compares this notification definition with another object for equality.
   *
   * <p>Two {@code NotificationDefinition} instances are equal when both their profile and user
   * notification settings are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code NotificationDefinition} with equal
   *     profile and user settings; {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotificationDefinition notificationDefinition = (NotificationDefinition) o;
    return (
      Objects.equals(this.profiles, notificationDefinition.profiles) &&
      Objects.equals(this.users, notificationDefinition.users)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from the profile and
   * user notification settings.
   *
   * @return the hash code for this notification definition
   */
  @Override
  public int hashCode() {
    return Objects.hash(profiles, users);
  }

  /**
   * Returns a human-readable, multi-line string representation of this notification definition,
   * listing its profile and user settings. Intended for debugging and logging.
   *
   * @return a formatted string describing this notification definition
   */
  @Override
  public String toString() {
    return (
      "class NotificationDefinition {\n" +
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
