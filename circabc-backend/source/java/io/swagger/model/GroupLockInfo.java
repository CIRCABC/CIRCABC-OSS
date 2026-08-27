package io.swagger.model;

import java.util.Date;
import java.util.Objects;

/**
 * GroupLockInfo
 */
public class GroupLockInfo {

  private Boolean locked = null;

  private String message = null;

  private Boolean readOnly = null;

  private String lockedBy = null;

  private Date lockedDate = null;

  /**
   * Get locked
   *
   * @return locked
   */
  public Boolean getLocked() {
    return locked;
  }

  public void setLocked(Boolean locked) {
    this.locked = locked;
  }

  /**
   * Get message
   *
   * @return message
   */
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get readOnly
   *
   * @return readOnly
   */
  public Boolean getReadOnly() {
    return readOnly;
  }

  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }

  /**
   * Get lockedBy
   *
   * @return lockedBy
   */
  public String getLockedBy() {
    return lockedBy;
  }

  public void setLockedBy(String lockedBy) {
    this.lockedBy = lockedBy;
  }

  /**
   * Get lockedDate
   *
   * @return lockedDate
   */
  public Date getLockedDate() {
    return lockedDate;
  }

  public void setLockedDate(Date lockedDate) {
    this.lockedDate = lockedDate;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupLockInfo groupLockInfo = (GroupLockInfo) o;
    return (
      Objects.equals(this.locked, groupLockInfo.locked) &&
      Objects.equals(this.message, groupLockInfo.message) &&
      Objects.equals(this.readOnly, groupLockInfo.readOnly) &&
      Objects.equals(this.lockedBy, groupLockInfo.lockedBy) &&
      Objects.equals(this.lockedDate, groupLockInfo.lockedDate)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(locked, message, readOnly, lockedBy, lockedDate);
  }

  @Override
  public String toString() {
    return (
      "class GroupLockInfo {\n" +
      "    locked: " +
      toIndentedString(locked) +
      "\n" +
      "    message: " +
      toIndentedString(message) +
      "\n" +
      "    readOnly: " +
      toIndentedString(readOnly) +
      "\n" +
      "    lockedBy: " +
      toIndentedString(lockedBy) +
      "\n" +
      "    lockedDate: " +
      toIndentedString(lockedDate) +
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
