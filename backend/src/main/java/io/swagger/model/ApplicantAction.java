package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object representing an action performed on a membership
 * applicant.
 *
 * <p>An instance describes what should happen to a user who applied for
 * membership in an Interest Group: the target {@link #username}, the
 * {@link #action} to apply (e.g. accept or reject the application) and an
 * optional {@link #message} to communicate to the applicant. It is typically
 * used as a request payload in the CIRCABC membership REST API.
 */
public class ApplicantAction {

  /** Username of the applicant the action applies to. */
  private String username = null;

  /** Action to perform on the applicant (e.g. accept or reject). */
  private String action = null;

  /** Optional message sent to the applicant along with the action. */
  private String message = null;

  /**
   * Get username
   *
   * @return username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the username of the applicant the action applies to.
   *
   * @param username the applicant's username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Get action
   *
   * @return action
   */
  public String getAction() {
    return action;
  }

  /**
   * Sets the action to perform on the applicant.
   *
   * @param action the action to apply (e.g. accept or reject)
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Get message
   *
   * @return message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the optional message sent to the applicant.
   *
   * @param message the message to communicate to the applicant
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Compares this object with another for equality based on the username,
   * action and message fields.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an {@code ApplicantAction}
   *         with equal field values, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicantAction applicantAction = (ApplicantAction) o;
    return (
      Objects.equals(this.username, applicantAction.username) &&
      Objects.equals(this.action, applicantAction.action) &&
      Objects.equals(this.message, applicantAction.message)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from
   * the username, action and message fields.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(username, action, message);
  }

  /**
   * Returns a human-readable representation of this object listing its field
   * values.
   *
   * @return a string representation of this {@code ApplicantAction}
   */
  @Override
  public String toString() {
    return (
      "class ApplicantAction {\n" +
      "    username: " +
      toIndentedString(username) +
      "\n" +
      "    action: " +
      toIndentedString(action) +
      "\n" +
      "    message: " +
      toIndentedString(message) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert to an indented string
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
