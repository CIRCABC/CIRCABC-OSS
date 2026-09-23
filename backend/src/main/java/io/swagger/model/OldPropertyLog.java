/**
 *
 */
package io.swagger.model;

import java.util.Date;
import java.util.Objects;

/**
 * Data model representing a logged change to a node property.
 *
 * <p>An {@code OldPropertyLog} captures the previous and new values of a property together with the
 * context in which the change occurred (the affected node, the group it belongs to and the user who
 * triggered the change). It is used to keep a history of property modifications so that a former
 * value can later be inspected or restored, with {@link #state} tracking the outcome of any restore
 * attempt.
 *
 * @author beaurpi
 */
public class OldPropertyLog {

  /** Identifier of the user who performed the property change. */
  private String userId;

  /** Identifier of the group (interest group) the affected node belongs to. */
  private String groupId;

  /** Identifier of the node whose property was changed. */
  private String nodeId;

  /** The previous value of the property, before the change. */
  private String oldValue;

  /** The new value of the property, after the change. */
  private String newValue;

  /** Type code of the changed property. */
  private Integer propertyType;

  /** Whether the change (or restore) was allowed. */
  private Boolean allowed;

  /** Outcome state of the log entry: -1 failed to restore, 0 new entry (no action done), 1 restored. */
  // -1 failed to restore, 0 new entry (no action done), 1 restored
  private Integer state;

  /** Timestamp at which the current {@link #state} was reached. */
  private Date stateDate;

  /**
   * Computes a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code derived from this log entry's fields
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      userId,
      groupId,
      nodeId,
      oldValue,
      newValue,
      allowed,
      state,
      stateDate
    );
  }

  /**
   * Returns a human-readable representation of this log entry.
   *
   * @return a string describing the field values of this log entry
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
      "    nodeId: " +
      toIndentedString(nodeId) +
      "\n" +
      "    oldValue: " +
      toIndentedString(oldValue) +
      "\n" +
      "    newValue: " +
      toIndentedString(newValue) +
      "    allowed: " +
      toIndentedString(allowed) +
      "\n" +
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

  /** @return the userId of the user who performed the change */
  public String getUserId() {
    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /** @return the nodeId */
  public String getNodeId() {
    return nodeId;
  }

  /** @param nodeId the nodeId to set */
  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  /** @return the oldValue */
  public String getOldValue() {
    return oldValue;
  }

  /** @param oldValue the oldValue to set */
  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  /** @return the newValue */
  public String getNewValue() {
    return newValue;
  }

  /** @param newValue the newValue to set */
  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  /** @return the propertyType */
  public Integer getPropertyType() {
    return propertyType;
  }

  /** @param propertyType the propertyType to set */
  public void setPropertyType(Integer propertyType) {
    this.propertyType = propertyType;
  }

  /** @return the allowed */
  public Boolean getAllowed() {
    return allowed;
  }

  /** @param allowed the allowed to set */
  public void setAllowed(Boolean allowed) {
    this.allowed = allowed;
  }

  /** @return the groupId */
  public String getGroupId() {
    return groupId;
  }

  /** @param groupId the groupId to set */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /** @return the state (-1 failed to restore, 0 new entry, 1 restored) */
  public Integer getState() {
    return state;
  }

  /** @param state the state to set (-1 failed to restore, 0 new entry, 1 restored) */
  public void setState(Integer state) {
    this.state = state;
  }

  /** @return the stateDate */
  public Date getStateDate() {
    return stateDate;
  }

  /** @param stateDate the stateDate to set */
  public void setStateDate(Date stateDate) {
    this.stateDate = stateDate;
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Compares this log entry to another object for equality based on all of its fields.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an {@code OldPropertyLog} with equal field values,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OldPropertyLog that = (OldPropertyLog) o;

    if (
      userId != null ? !userId.equals(that.userId) : that.userId != null
    ) return false;
    if (
      groupId != null ? !groupId.equals(that.groupId) : that.groupId != null
    ) return false;
    if (
      nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null
    ) return false;
    if (
      oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null
    ) return false;
    if (
      newValue != null ? !newValue.equals(that.newValue) : that.newValue != null
    ) return false;
    if (
      propertyType != null
        ? !propertyType.equals(that.propertyType)
        : that.propertyType != null
    ) return false;
    if (
      allowed != null ? !allowed.equals(that.allowed) : that.allowed != null
    ) return false;
    if (
      state != null ? !state.equals(that.state) : that.state != null
    ) return false;
    return stateDate != null
      ? stateDate.equals(that.stateDate)
      : that.stateDate == null;
  }
}
