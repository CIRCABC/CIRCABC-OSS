/**
 *
 */
package io.swagger.model;

import java.util.Date;
import java.util.Objects;

/**
 * Domain model representing a historical (former) membership of a user within an Interest Group.
 *
 * <p>An {@code OldMembership} captures the association between a user and a group as it existed
 * previously, together with the profile that was assigned, the underlying Alfresco group name and
 * the outcome of an attempt to restore that membership. It is used when former memberships are
 * listed or replayed (for example when re-adding users to a group).
 *
 * @author beaurpi
 */
public class OldMembership {

  /** Identifier of the user this former membership belongs to. */
  private String userId;
  /** Identifier of the Interest Group the membership relates to. */
  private String groupId;
  /** Identifier of the profile (role) the user held within the group. */
  private String profileId;
  /** Name of the underlying Alfresco authority (group) backing this membership. */
  private String alfGroupName;
  /**
   * Outcome of a restore attempt for this membership:
   * {@code -1} failed to restore, {@code 0} new entry (no action done), {@code 1} restored.
   */
  // -1 failed to restore, 0 new entry (no action done), 1 restored
  private Integer state;
  /** Date associated with the current {@link #state} (e.g. when the restore was attempted). */
  private Date stateDate;

  /**
   * Computes a hash code consistent with {@link #equals(Object)}, based on all fields.
   *
   * @return the hash code for this membership
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      userId,
      groupId,
      profileId,
      alfGroupName,
      state,
      stateDate
    );
  }

  /**
   * Returns a human-readable, multi-line string representation of this membership.
   *
   * @return a string listing all field values
   */
  @Override
  public String toString() {
    return (
      "class OldMembership {\n" +
      "    userId: " +
      toIndentedString(userId) +
      "\n" +
      "    groupId: " +
      toIndentedString(groupId) +
      "\n" +
      "    profileId: " +
      toIndentedString(profileId) +
      "\n" +
      "    alfGroupName: " +
      toIndentedString(alfGroupName) +
      "\n" +
      "    state: " +
      toIndentedString(state) +
      "\n" +
      "    stateDate: " +
      toIndentedString(stateDate) +
      "\n" +
      "}"
    );
  }

  /**
   * Returns the identifier of the user this membership belongs to.
   *
   * @return the user identifier
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the identifier of the user this membership belongs to.
   *
   * @param userId the user identifier to set
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Returns the identifier of the Interest Group the membership relates to.
   *
   * @return the group identifier
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Sets the identifier of the Interest Group the membership relates to.
   *
   * @param groupId the group identifier to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Returns the identifier of the profile (role) held within the group.
   *
   * @return the profile identifier
   */
  public String getProfileId() {
    return profileId;
  }

  /**
   * Sets the identifier of the profile (role) held within the group.
   *
   * @param profileId the profile identifier to set
   */
  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }

  /**
   * Returns the name of the underlying Alfresco authority (group) backing this membership.
   *
   * @return the Alfresco group name
   */
  public String getAlfGroupName() {
    return alfGroupName;
  }

  /**
   * Sets the name of the underlying Alfresco authority (group) backing this membership.
   *
   * @param alfGroupName the Alfresco group name to set
   */
  public void setAlfGroupName(String alfGroupName) {
    this.alfGroupName = alfGroupName;
  }

  /**
   * Returns the restore outcome state:
   * {@code -1} failed to restore, {@code 0} new entry (no action done), {@code 1} restored.
   *
   * @return the state value
   */
  public Integer getState() {
    return state;
  }

  /**
   * Sets the restore outcome state:
   * {@code -1} failed to restore, {@code 0} new entry (no action done), {@code 1} restored.
   *
   * @param state the state value to set
   */
  public void setState(Integer state) {
    this.state = state;
  }

  /**
   * Returns the date associated with the current {@link #state}.
   *
   * @return the state date
   */
  public Date getStateDate() {
    return stateDate;
  }

  /**
   * Sets the date associated with the current {@link #state}.
   *
   * @param stateDate the state date to set
   */
  public void setStateDate(Date stateDate) {
    this.stateDate = stateDate;
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert; may be {@code null}
   * @return the indented string representation of {@code o}
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Compares this membership with another object for equality based on all fields.
   *
   * @param o the object to compare with
   * @return {@code true} if the other object is an {@code OldMembership} with equal field values,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OldMembership that = (OldMembership) o;
    return (
      Objects.equals(userId, that.userId) &&
      Objects.equals(groupId, that.groupId) &&
      Objects.equals(profileId, that.profileId) &&
      Objects.equals(alfGroupName, that.alfGroupName) &&
      Objects.equals(state, that.state) &&
      Objects.equals(stateDate, that.stateDate)
    );
  }
}
