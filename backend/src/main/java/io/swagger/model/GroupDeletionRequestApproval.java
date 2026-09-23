package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object representing a single approval decision on a group
 * deletion request.
 *
 * <p>An instance captures the outcome of one reviewer's vote on whether an
 * Interest Group should be deleted: which approval record it belongs to, the
 * agreement value expressing the decision, and an optional free-text argument
 * justifying that decision.
 */
public class GroupDeletionRequestApproval {

  /** Unique identifier of this approval record. */
  private long id;

  /**
   * Numeric code expressing the reviewer's decision on the deletion request
   * (for example approve versus reject).
   */
  private int agreement;

  /** Optional free-text justification supporting the agreement decision. */
  private String argument;

  /**
   * Returns the unique identifier of this approval record.
   *
   * @return the approval identifier
   */
  public long getId() {
    return id;
  }

  /**
   * Sets the unique identifier of this approval record.
   *
   * @param id the approval identifier to set
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Returns the numeric code expressing the reviewer's decision.
   *
   * @return the agreement value
   */
  public int getAgreement() {
    return agreement;
  }

  /**
   * Sets the numeric code expressing the reviewer's decision.
   *
   * @param agreement the agreement value to set
   */
  public void setAgreement(int agreement) {
    this.agreement = agreement;
  }

  /**
   * Returns the free-text justification supporting the decision.
   *
   * @return the argument, or {@code null} if none was provided
   */
  public String getArgument() {
    return argument;
  }

  /**
   * Sets the free-text justification supporting the decision.
   *
   * @param argument the argument to set
   */
  public void setArgument(String argument) {
    this.argument = argument;
  }

  /**
   * Compares this approval to another object for equality based on the
   * {@code id}, {@code agreement} and {@code argument} fields.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal
   *     {@code GroupDeletionRequestApproval}, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupDeletionRequestApproval groupDeletionRequestApproval =
      (GroupDeletionRequestApproval) o;
    return (
      Objects.equals(this.id, groupDeletionRequestApproval.id) &&
      Objects.equals(this.agreement, groupDeletionRequestApproval.agreement) &&
      Objects.equals(this.argument, groupDeletionRequestApproval.argument)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from
   * the {@code id}, {@code agreement} and {@code argument} fields.
   *
   * @return the hash code for this approval
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, agreement, argument);
  }

  /**
   * Returns a human-readable, multi-line string representation of this
   * approval.
   *
   * @return a string describing the field values
   */
  @Override
  public String toString() {
    return (
      "class GroupDeletionRequestApproval {\n" +
      "    id: " +
      toIndentedString(id) +
      "    agreement: " +
      toIndentedString(agreement) +
      "\n" +
      "    argument: " +
      toIndentedString(argument) +
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
