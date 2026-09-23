package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object representing the outcome of a guard/authorization check.
 *
 * <p>Instances of this class carry a single boolean flag indicating whether a
 * requested operation is authorized (granted) or not. It is typically serialized
 * to JSON as part of REST responses that expose the result of a permission or
 * access-control evaluation.</p>
 */
public class GuardAuthorization {

  /**
   * Whether the checked operation is authorized. {@code true} if access is
   * granted, {@code false} if denied, and {@code null} if the authorization
   * status has not been set.
   */
  private Boolean granted = null;

  /**
   * Returns whether the checked operation is authorized.
   *
   * @return {@code true} if access is granted, {@code false} if denied, or
   *     {@code null} if the status has not been set
   */
  public Boolean getGranted() {
    return granted;
  }

  /**
   * Sets whether the checked operation is authorized.
   *
   * @param granted {@code true} if access is granted, {@code false} if denied,
   *     or {@code null} to leave the status unset
   */
  public void setGranted(Boolean granted) {
    this.granted = granted;
  }

  /**
   * Indicates whether some other object is equal to this one. Two
   * {@code GuardAuthorization} instances are considered equal when their
   * {@code granted} flags are equal.
   *
   * @param o the reference object with which to compare
   * @return {@code true} if this object is the same as {@code o}, {@code false}
   *     otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GuardAuthorization guardAuthorization = (GuardAuthorization) o;
    return Objects.equals(this.granted, guardAuthorization.granted);
  }

  /**
   * Returns a hash code value for this object, derived from the
   * {@code granted} flag.
   *
   * @return a hash code value consistent with {@link #equals(java.lang.Object)}
   */
  @Override
  public int hashCode() {
    return Objects.hash(granted);
  }

  /**
   * Returns a human-readable string representation of this object, listing the
   * {@code granted} flag.
   *
   * @return a string representation of this {@code GuardAuthorization}
   */
  @Override
  public String toString() {
    return (
      "class GuardAuthorization {\n" +
      "    granted: " +
      toIndentedString(granted) +
      "\n" +
      "}"
    );
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
}
