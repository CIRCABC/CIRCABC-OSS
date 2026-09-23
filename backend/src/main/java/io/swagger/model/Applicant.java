package io.swagger.model;

import java.util.Objects;
import org.joda.time.DateTime;

/**
 * Domain model representing an application for membership to an Interest Group.
 *
 * <p>An {@code Applicant} captures who requested membership, when the request was
 * submitted, and the justification provided in support of the request. Instances are
 * typically serialized to JSON as part of the REST API responses that list pending
 * membership applications.
 */
public class Applicant {

  /** The date and time at which the membership application was submitted. */
  private DateTime submitted = null;

  /** The user who submitted the membership application. */
  private User user = null;

  /** The free-text justification provided by the applicant in support of the request. */
  private String justification = null;

  /**
   * Returns the date and time at which the membership application was submitted.
   *
   * @return the submission timestamp, or {@code null} if not set
   */
  public DateTime getSubmitted() {
    return submitted;
  }

  /**
   * Sets the date and time at which the membership application was submitted.
   *
   * @param submitted the submission timestamp to set
   */
  public void setSubmitted(DateTime submitted) {
    this.submitted = submitted;
  }

  /**
   * Returns the user who submitted the membership application.
   *
   * @return the applicant user, or {@code null} if not set
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets the user who submitted the membership application.
   *
   * @param user the applicant user to set
   */
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Returns the free-text justification provided by the applicant.
   *
   * @return the justification text, or {@code null} if not set
   */
  public String getJustification() {
    return justification;
  }

  /**
   * Sets the free-text justification provided by the applicant.
   *
   * @param justification the justification text to set
   */
  public void setJustification(String justification) {
    this.justification = justification;
  }

  /**
   * Compares this applicant with another object for equality. Two {@code Applicant}
   * instances are considered equal when their submission timestamp, user and
   * justification are all equal.
   *
   * @param o the object to compare with this instance
   * @return {@code true} if the given object represents an equivalent applicant,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Applicant applicant = (Applicant) o;
    return (
      Objects.equals(this.submitted, applicant.submitted) &&
      Objects.equals(this.user, applicant.user) &&
      Objects.equals(this.justification, applicant.justification)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from the
   * submission timestamp, user and justification.
   *
   * @return the hash code for this applicant
   */
  @Override
  public int hashCode() {
    return Objects.hash(submitted, user, justification);
  }

  /**
   * Returns a human-readable, multi-line string representation of this applicant,
   * primarily intended for debugging and logging.
   *
   * @return a string representation of this applicant
   */
  @Override
  public String toString() {
    return (
      "class Applicant {\n" +
      "    submitted: " +
      toIndentedString(submitted) +
      "\n" +
      "    user: " +
      toIndentedString(user) +
      "\n" +
      "    justification: " +
      toIndentedString(justification) +
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
