package io.swagger.model;

import java.util.Date;
import java.util.Objects;

/**
 * Domain model representing the membership of a {@link User} inside an Interest
 * Group.
 *
 * <p>A {@code UserProfile} associates a user with the {@link Profile} (role) that
 * defines the permissions granted to that user within the group, along with an
 * optional expiration date after which the membership is no longer valid.
 *
 * <p>Equality and hash code are based solely on the {@code user} and
 * {@code profile}; the expiration date is intentionally excluded.
 */
public class UserProfile {

  /** The user who holds the membership. */
  private User user = null;

  /** The profile (role) granted to the user within the Interest Group. */
  private Profile profile = null;

  /**
   * The date on which the membership expires, or {@code null} if the membership
   * does not expire.
   */
  private Date expirationDate = null;

  /**
   * Get user
   *
   * @return user
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets the user who holds the membership.
   *
   * @param user the user to associate with this membership
   */
  public void setUser(User user) {
    this.user = user;
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
   * Get profile
   *
   * @return profile
   */
  public Profile getProfile() {
    return profile;
  }

  /**
   * Sets the profile (role) granted to the user within the Interest Group.
   *
   * @param profile the profile to assign to this membership
   */
  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  /**
   * Compares this membership with another object for equality.
   *
   * <p>Two {@code UserProfile} instances are considered equal when both their
   * {@code user} and {@code profile} are equal. The expiration date is not
   * taken into account.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code UserProfile} with the
   *     same user and profile, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserProfile userProfile = (UserProfile) o;
    return (
      Objects.equals(this.user, userProfile.user) &&
      Objects.equals(this.profile, userProfile.profile)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, computed from
   * the {@code user} and {@code profile}.
   *
   * @return the hash code for this membership
   */
  @Override
  public int hashCode() {
    return Objects.hash(user, profile);
  }

  /**
   * Returns a human-readable, multi-line string representation of this
   * membership, including the user and profile.
   *
   * @return a string representation of this {@code UserProfile}
   */
  @Override
  public String toString() {
    return (
      "class UserProfile {\n" +
      "    user: " +
      toIndentedString(user) +
      "\n" +
      "    profile: " +
      toIndentedString(profile) +
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
