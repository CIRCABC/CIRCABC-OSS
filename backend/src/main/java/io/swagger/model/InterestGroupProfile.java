package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object representing the membership of a user within an Interest Group.
 *
 * <p>An instance pairs a {@link Profile} (the user's role/profile) with the {@link InterestGroup}
 * the membership applies to, and is serialized to JSON as part of the CIRCABC REST API responses.
 */
public class InterestGroupProfile {

  /** The user profile (role) associated with this Interest Group membership. */
  private Profile profile = null;

  /** The Interest Group that this membership refers to. */
  private InterestGroup interestGroup = null;

  /**
   * Returns the user profile associated with this Interest Group membership.
   *
   * @return the {@link Profile}, or {@code null} if none has been set
   */
  public Profile getProfile() {
    return profile;
  }

  /**
   * Sets the user profile associated with this Interest Group membership.
   *
   * @param profile the {@link Profile} to associate with this membership
   */
  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  /**
   * Returns the Interest Group that this membership refers to.
   *
   * @return the {@link InterestGroup}, or {@code null} if none has been set
   */
  public InterestGroup getInterestGroup() {
    return interestGroup;
  }

  /**
   * Sets the Interest Group that this membership refers to.
   *
   * @param interestGroup the {@link InterestGroup} to associate with this membership
   */
  public void setInterestGroup(InterestGroup interestGroup) {
    this.interestGroup = interestGroup;
  }

  /**
   * Indicates whether another object is "equal to" this membership. Two
   * {@code InterestGroupProfile} instances are equal when both their profile and
   * interest group are equal.
   *
   * @param o the reference object with which to compare
   * @return {@code true} if this object is the same as {@code o}; {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InterestGroupProfile interestGroupProfile = (InterestGroupProfile) o;
    return (
      Objects.equals(this.profile, interestGroupProfile.profile) &&
      Objects.equals(this.interestGroup, interestGroupProfile.interestGroup)
    );
  }

  /**
   * Returns a hash code value consistent with {@link #equals(Object)}, derived
   * from the profile and interest group.
   *
   * @return the hash code for this membership
   */
  @Override
  public int hashCode() {
    return Objects.hash(profile, interestGroup);
  }

  /**
   * Returns a human-readable string representation of this membership, listing
   * its profile and interest group.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (
      "class InterestGroupProfile {\n" +
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
   *
   * @param o the object to convert to an indented string
   * @return the indented string representation of {@code o}
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
