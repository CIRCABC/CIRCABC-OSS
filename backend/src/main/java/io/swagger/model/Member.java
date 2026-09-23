package io.swagger.model;

import java.util.Objects;

/**
 * Domain model representing a member relationship within CIRCABC.
 *
 * <p>A {@code Member} associates a {@link User} with a {@link Profile} in the
 * context of a specific {@link InterestGroup}. It captures who the user is,
 * which membership profile (role/permission set) they hold, and the interest
 * group the membership applies to. This is a plain data-transfer object (DTO)
 * that is serialized to and from the JSON REST API.</p>
 */
public class Member {

  /** The user that this membership belongs to. */
  private User user = null;

  /** The membership profile (role/permission set) held by the user. */
  private Profile profile = null;

  /** The interest group within which this membership applies. */
  private InterestGroup interestGroup = null;

  /**
   * Get user
   *
   * @return user
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets the user that this membership belongs to.
   *
   * @param user the user to associate with this membership
   */
  public void setUser(User user) {
    this.user = user;
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
   * Sets the membership profile (role/permission set) held by the user.
   *
   * @param profile the profile to associate with this membership
   */
  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  /**
   * Get interestGroup
   *
   * @return interestGroup
   */
  public InterestGroup getInterestGroup() {
    return interestGroup;
  }

  /**
   * Sets the interest group within which this membership applies.
   *
   * @param interestGroup the interest group to associate with this membership
   */
  public void setInterestGroup(InterestGroup interestGroup) {
    this.interestGroup = interestGroup;
  }

  /**
   * Compares this member with another object for equality. Two members are
   * equal when their user, profile and interest group are all equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code Member} with equal
   *     user, profile and interest group; {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Member member = (Member) o;
    return (
      Objects.equals(this.user, member.user) &&
      Objects.equals(this.profile, member.profile) &&
      Objects.equals(this.interestGroup, member.interestGroup)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from
   * the user, profile and interest group.
   *
   * @return the hash code for this member
   */
  @Override
  public int hashCode() {
    return Objects.hash(user, profile, interestGroup);
  }

  /**
   * Returns a human-readable, multi-line string representation of this member,
   * including its user, profile and interest group.
   *
   * @return a string representation of this member
   */
  @Override
  public String toString() {
    return (
      "class Member {\n" +
      "    user: " +
      toIndentedString(user) +
      "\n" +
      "    profile: " +
      toIndentedString(profile) +
      "\n" +
      "    interestGroup: " +
      toIndentedString(interestGroup) +
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
