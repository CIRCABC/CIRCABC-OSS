package io.swagger.model;

import java.util.Objects;

/**
 * GroupLockRequest
 */
public class GroupLockRequest {

  private String message = null;

  private Boolean readOnly = false;

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

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupLockRequest groupLockRequest = (GroupLockRequest) o;
    return (
      Objects.equals(this.message, groupLockRequest.message) &&
      Objects.equals(this.readOnly, groupLockRequest.readOnly)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, readOnly);
  }

  @Override
  public String toString() {
    return (
      "class GroupLockRequest {\n" +
      "    message: " +
      toIndentedString(message) +
      "\n" +
      "    readOnly: " +
      toIndentedString(readOnly) +
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
