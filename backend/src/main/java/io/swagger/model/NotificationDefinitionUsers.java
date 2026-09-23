package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object that associates a single {@link User} with a notification
 * configuration for a given CIRCABC node or service.
 *
 * <p>It captures which notification setting applies to the user (see
 * {@link #getNotifications()}) and whether that setting is explicitly defined on the
 * user or inherited from a parent (see {@link #getInherited()}). Instances are
 * typically serialized to JSON as part of a node's notification definition.
 */
@jakarta.annotation.Generated(
  value = "io.swagger.codegen.languages.SpringCodegen",
  date = "2017-04-06T14:37:04.823+02:00"
)
public class NotificationDefinitionUsers {

  /** The user this notification definition entry refers to. */
  private User user = null;

  /** The notification setting applied to the user (e.g. the notification level or type). */
  private String notifications = null;

  /**
   * Whether the notification setting is inherited from a parent rather than set
   * directly on the user. Defaults to {@code true}.
   */
  private Boolean inherited = true;

  /**
   * Get user
   *
   * @return user
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets the user this notification definition entry refers to.
   *
   * @param user the user to associate with this notification entry
   */
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Get notification
   *
   * @return notification
   */
  public String getNotifications() {
    return notifications;
  }

  /**
   * Sets the notification setting applied to the user.
   *
   * @param notifications the notification setting to apply
   */
  public void setNotifications(String notifications) {
    this.notifications = notifications;
  }

  /**
   * Compares this object with another for equality based on the user,
   * notification setting and inherited flag.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equivalent
   *         {@code NotificationDefinitionUsers}, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotificationDefinitionUsers notificationDefinitionUsers =
      (NotificationDefinitionUsers) o;
    return (
      Objects.equals(this.user, notificationDefinitionUsers.user) &&
      Objects.equals(
        this.notifications,
        notificationDefinitionUsers.notifications
      ) &&
      Objects.equals(this.inherited, notificationDefinitionUsers.inherited)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(user, notifications, inherited);
  }

  /**
   * Returns a human-readable representation of this object with its fields
   * indented for readability.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NotificationDefinitionUsers {\n");

    sb.append("    user: ").append(toIndentedString(user)).append("\n");
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
   *
   * @param o the object to convert
   * @return the indented string representation of the object
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
