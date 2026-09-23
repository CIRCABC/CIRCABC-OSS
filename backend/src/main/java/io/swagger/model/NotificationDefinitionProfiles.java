package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object that binds a {@link Profile} to its notification
 * configuration for a given notification definition.
 *
 * <p>Each instance represents one profile's notification setting within an
 * Interest Group service, indicating which notification level applies to the
 * profile and whether that setting is inherited from a parent node rather than
 * defined directly on the current node.
 */
@jakarta.annotation.Generated(
  value = "io.swagger.codegen.languages.SpringCodegen",
  date = "2017-04-06T14:37:04.823+02:00"
)
public class NotificationDefinitionProfiles {

  /** The profile to which the notification configuration applies. */
  private Profile profile = null;

  /** The notification level/value configured for the profile. */
  private String notifications = null;

  /**
   * Whether the notification setting is inherited from a parent node
   * ({@code true}) or defined directly on this node ({@code false}).
   * Defaults to {@code true}.
   */
  private Boolean inherited = true;

  /**
   * Returns the profile associated with this notification configuration.
   *
   * @return the profile
   */
  public Profile getProfile() {
    return profile;
  }

  /**
   * Sets the profile associated with this notification configuration.
   *
   * @param profile the profile to set
   */
  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  /**
   * Returns the notification level configured for the profile.
   *
   * @return the notification value
   */
  public String getNotifications() {
    return notifications;
  }

  /**
   * Sets the notification level configured for the profile.
   *
   * @param notifications the notification value to set
   */
  public void setNotifications(String notifications) {
    this.notifications = notifications;
  }

  /**
   * Compares this object with another for equality based on the profile,
   * notifications and inherited fields.
   *
   * @param o the object to compare against
   * @return {@code true} if the given object is a
   *         {@code NotificationDefinitionProfiles} with equal field values,
   *         {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotificationDefinitionProfiles notificationDefinitionProfiles =
      (NotificationDefinitionProfiles) o;
    return (
      Objects.equals(this.profile, notificationDefinitionProfiles.profile) &&
      Objects.equals(
        this.notifications,
        notificationDefinitionProfiles.notifications
      ) &&
      Objects.equals(this.inherited, notificationDefinitionProfiles.inherited)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from
   * the profile, notifications and inherited fields.
   *
   * @return the hash code value for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(profile, notifications, inherited);
  }

  /**
   * Returns a human-readable representation of this object with its fields
   * listed on separate, indented lines.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NotificationDefinitionProfiles {\n");

    sb.append("    profile: ").append(toIndentedString(profile)).append("\n");
    sb
      .append("    notification: ")
      .append(toIndentedString(notifications))
      .append("\n");
    sb
      .append("    inherited: ")
      .append(toIndentedString(inherited))
      .append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
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
}
