/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/**
 * Domain model (DTO) describing whether a user account can be recovered and, if so, which profile
 * the recovery applies to.
 *
 * <p>This object is typically returned by user-recovery related REST endpoints to indicate if a
 * given user is eligible for recovery ({@link #recoverable}) together with the associated
 * {@link Profile} details.
 *
 * @author beaurpi
 */
public class UserRecoveryOption {

  /** Flag indicating whether the associated user account is recoverable. Defaults to {@code false}. */
  private Boolean recoverable = false;

  /** The user profile that the recovery option refers to. */
  private Profile profile;

  /**
   * Returns whether the associated user account is recoverable.
   *
   * @return {@code true} if the account can be recovered, {@code false} otherwise
   */
  public Boolean getRecoverable() {
    return recoverable;
  }

  /**
   * Sets whether the associated user account is recoverable.
   *
   * @param recoverable {@code true} if the account can be recovered, {@code false} otherwise
   */
  public void setRecoverable(Boolean recoverable) {
    this.recoverable = recoverable;
  }

  /**
   * Returns the user profile that this recovery option refers to.
   *
   * @return the associated {@link Profile}
   */
  public Profile getProfile() {
    return profile;
  }

  /**
   * Sets the user profile that this recovery option refers to.
   *
   * @param profile the {@link Profile} to associate with this recovery option
   */
  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  /**
   * Compares this object with another for equality based on the {@code recoverable} flag and the
   * associated {@code profile}.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code UserRecoveryOption} with equal fields
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserRecoveryOption userRecoveryOption = (UserRecoveryOption) o;
    return (
      Objects.equals(this.recoverable, userRecoveryOption.recoverable) &&
      Objects.equals(this.profile, userRecoveryOption.profile)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from the
   * {@code recoverable} flag and the associated {@code profile}.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(recoverable, profile);
  }

  /**
   * Returns a human-readable string representation of this object.
   *
   * @return a formatted string containing the {@code recoverable} flag and {@code profile}
   */
  @Override
  public String toString() {
    return (
      "class News {\n" +
      "    recoverable: " +
      toIndentedString(recoverable) +
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
