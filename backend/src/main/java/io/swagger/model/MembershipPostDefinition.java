package io.swagger.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object describing the payload used when creating (POST) one or
 * more memberships in an Interest Group.
 *
 * <p>It carries the list of user profiles to be added as members along with the
 * notification preferences and constraints that apply to the operation:
 * whether administrators and/or the affected users should be notified, an
 * optional free-text message to include in those notifications, and an optional
 * expiration date for the membership.
 *
 * <p>The fluent builder-style setter methods (e.g. {@link #adminNotifications(Boolean)})
 * return {@code this} to allow chained configuration of the definition.
 */
public class MembershipPostDefinition {

  /** Whether group administrators should be notified about the membership change. */
  private Boolean adminNotifications = null;

  /** Whether the affected users should be notified about the membership change. */
  private Boolean userNotifications = null;

  /** Optional free-text message to include in the notifications. */
  private String notifyText = null;

  /** Optional date on which the created memberships expire. */
  private Date expirationDate = null;

  /** The user profiles to be added as members. */
  private List<UserProfile> memberships = new ArrayList<>();

  /**
   * Fluent setter for the {@code adminNotifications} flag.
   *
   * @param adminNotifications whether group administrators should be notified
   * @return this instance, to allow method chaining
   */
  public MembershipPostDefinition adminNotifications(
    Boolean adminNotifications
  ) {
    this.adminNotifications = adminNotifications;
    return this;
  }

  /**
   * @return the expirationDate
   */
  public Date getExpirationDate() {
    return expirationDate;
  }

  /**
   * @param expirationDate the expirationDate to set
   */
  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  /**
   * Get adminNotifications
   *
   * @return adminNotifications
   */
  public Boolean getAdminNotifications() {
    return adminNotifications;
  }

  /**
   * Sets the {@code adminNotifications} flag.
   *
   * @param adminNotifications whether group administrators should be notified
   */
  public void setAdminNotifications(Boolean adminNotifications) {
    this.adminNotifications = adminNotifications;
  }

  /**
   * Fluent setter for the {@code userNotifications} flag.
   *
   * @param userNotifications whether the affected users should be notified
   * @return this instance, to allow method chaining
   */
  public MembershipPostDefinition userNotifications(Boolean userNotifications) {
    this.userNotifications = userNotifications;
    return this;
  }

  /**
   * Get userNotifications
   *
   * @return userNotifications
   */
  public Boolean getUserNotifications() {
    return userNotifications;
  }

  /**
   * Sets the {@code userNotifications} flag.
   *
   * @param userNotifications whether the affected users should be notified
   */
  public void setUserNotifications(Boolean userNotifications) {
    this.userNotifications = userNotifications;
  }

  /**
   * Fluent setter for the list of memberships.
   *
   * @param memberships the user profiles to be added as members
   * @return this instance, to allow method chaining
   */
  public MembershipPostDefinition memberships(List<UserProfile> memberships) {
    this.memberships = memberships;
    return this;
  }

  /**
   * Adds a single membership item to the list of memberships.
   *
   * @param membershipsItem the user profile to add as a member
   * @return this instance, to allow method chaining
   */
  public MembershipPostDefinition addMembershipsItem(
    UserProfile membershipsItem
  ) {
    this.memberships.add(membershipsItem);
    return this;
  }

  /**
   * Gets the optional free-text notification message.
   *
   * @return the notification text
   */
  public String getNotifyText() {
    return notifyText;
  }

  /**
   * Sets the optional free-text notification message.
   *
   * @param notifyText the notification text to set
   */
  public void setNotifyText(String notifyText) {
    this.notifyText = notifyText;
  }

  /**
   * Get memberships
   *
   * @return memberships
   */
  public List<UserProfile> getMemberships() {
    return memberships;
  }

  /**
   * Sets the list of memberships.
   *
   * @param memberships the user profiles to be added as members
   */
  public void setMemberships(List<UserProfile> memberships) {
    this.memberships = memberships;
  }

  /**
   * Compares this definition with another object for equality based on the
   * {@code adminNotifications}, {@code userNotifications} and
   * {@code memberships} fields.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equivalent
   *         {@code MembershipPostDefinition}, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MembershipPostDefinition membershipPostDefinition =
      (MembershipPostDefinition) o;
    return (
      Objects.equals(
        this.adminNotifications,
        membershipPostDefinition.adminNotifications
      ) &&
      Objects.equals(
        this.userNotifications,
        membershipPostDefinition.userNotifications
      ) &&
      Objects.equals(this.memberships, membershipPostDefinition.memberships)
    );
  }

  /**
   * Computes a hash code consistent with {@link #equals(java.lang.Object)},
   * based on the {@code adminNotifications}, {@code userNotifications} and
   * {@code memberships} fields.
   *
   * @return the hash code for this definition
   */
  @Override
  public int hashCode() {
    return Objects.hash(adminNotifications, userNotifications, memberships);
  }

  /**
   * Returns a human-readable, multi-line representation of this definition.
   *
   * @return a string representation of this definition
   */
  @Override
  public String toString() {
    return (
      "class MembershipPostDefinition {\n" +
      "    adminNotifications: " +
      toIndentedString(adminNotifications) +
      "\n" +
      "    userNotifications: " +
      toIndentedString(userNotifications) +
      "\n" +
      "    memberships: " +
      toIndentedString(memberships) +
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
