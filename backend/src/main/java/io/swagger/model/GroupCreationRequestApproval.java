package io.swagger.model;

import java.util.Objects;

/**
 * Domain model (DTO) representing an approval decision made on a group creation
 * request.
 *
 * <p>An instance captures the outcome of a single reviewer's assessment of a
 * pending group creation request: which request it refers to ({@link #id}),
 * whether the reviewer agreed ({@link #agreement}) and an optional textual
 * justification for the decision ({@link #argument}). It is used to transfer
 * approval data between the REST layer and the service layer and is serialized
 * to/from JSON.
 */
public class GroupCreationRequestApproval {

  /** Identifier of the group creation request this approval applies to. */
  private long id;

  /**
   * Numeric flag indicating the reviewer's decision (e.g. approved or rejected).
   */
  private int agreement;

  /** Optional free-text justification supporting the approval decision. */
  private String argument;

  /**
   * Returns the identifier of the group creation request this approval applies
   * to.
   *
   * @return the group creation request id
   */
  public long getId() {
    return id;
  }

  /**
   * Sets the identifier of the group creation request this approval applies to.
   *
   * @param id the group creation request id
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Returns the numeric flag representing the reviewer's decision.
   *
   * @return the agreement value
   */
  public int getAgreement() {
    return agreement;
  }

  /**
   * Sets the numeric flag representing the reviewer's decision.
   *
   * @param agreement the agreement value
   */
  public void setAgreement(int agreement) {
    this.agreement = agreement;
  }

  /**
   * Returns the optional free-text justification for the decision.
   *
   * @return the justification argument, or {@code null} if none was provided
   */
  public String getArgument() {
    return argument;
  }

  /**
   * Sets the optional free-text justification for the decision.
   *
   * @param argument the justification argument
   */
  public void setArgument(String argument) {
    this.argument = argument;
  }

  /**
   * Compares this approval with another object for equality. Two instances are
   * equal when their {@code id}, {@code agreement} and {@code argument} fields
   * are all equal.
   *
   * @param o the object to compare against
   * @return {@code true} if the given object represents an equivalent approval,
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
    GroupCreationRequestApproval groupCreationRequestApproval =
      (GroupCreationRequestApproval) o;
    return (
      Objects.equals(this.id, groupCreationRequestApproval.id) &&
      Objects.equals(this.agreement, groupCreationRequestApproval.agreement) &&
      Objects.equals(this.argument, groupCreationRequestApproval.argument)
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
   * Returns a human-readable, multi-line representation of this approval, useful
   * for debugging and logging.
   *
   * @return a string describing this approval's field values
   */
  @Override
  public String toString() {
    return (
      "class GroupCreationRequestApproval {\n" +
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
