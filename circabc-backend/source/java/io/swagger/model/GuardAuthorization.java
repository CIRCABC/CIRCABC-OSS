package io.swagger.model;

import java.util.Objects;

/**
 * GuardAuthorization
 */
public class GuardAuthorization {

  private Boolean granted = null;
  private Boolean readOnly = null;
  private Boolean locked = null;

  /**
   * Get granted
   *
   * @return granted
   */
  public Boolean getGranted() {
    return granted;
  }

  public void setGranted(Boolean granted) {
    this.granted = granted;
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
   * Get locked — true when access was denied specifically because the IG is locked.
   * The user would normally have access (public/registered/member) but the lock prevents it.
   *
   * @return locked
   */
  public Boolean getLocked() {
    return locked;
  }

  public void setLocked(Boolean locked) {
    this.locked = locked;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GuardAuthorization guardAuthorization = (GuardAuthorization) o;
    return (
      Objects.equals(this.granted, guardAuthorization.granted) &&
      Objects.equals(this.readOnly, guardAuthorization.readOnly) &&
      Objects.equals(this.locked, guardAuthorization.locked)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(granted, readOnly, locked);
  }

  @Override
  public String toString() {
    return (
      "class GuardAuthorization {\n" +
      "    granted: " +
      toIndentedString(granted) +
      "\n" +
      "    readOnly: " +
      toIndentedString(readOnly) +
      "\n" +
      "    locked: " +
      toIndentedString(locked) +
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
