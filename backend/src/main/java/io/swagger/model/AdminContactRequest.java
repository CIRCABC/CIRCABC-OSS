/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object representing a request to contact a CIRCABC administrator.
 *
 * <p>Instances of this model carry the message content submitted by a user together
 * with a flag indicating whether the sender wants to receive a copy of the message.
 * It is typically deserialized from the JSON body of a REST request and used by the
 * service layer to build and dispatch the corresponding contact/notification e-mail.
 *
 * @author beaurpi
 */
public class AdminContactRequest {

  /** The textual body/message content of the contact request. */
  private String content = null;

  /**
   * Whether a copy of the message should also be sent back to the requester.
   * Defaults to {@code false}.
   */
  private Boolean sendCopy = false;

  /**
   * Returns the message content of the contact request.
   *
   * @return the message content, or {@code null} if none was set
   */
  public String getContent() {
    return content;
  }

  /**
   * Sets the message content of the contact request.
   *
   * @param content the message content to set
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Indicates whether a copy of the message should be sent back to the requester.
   *
   * @return {@code true} if a copy should be sent, {@code false} otherwise
   */
  public Boolean getSendCopy() {
    return sendCopy;
  }

  /**
   * Sets whether a copy of the message should be sent back to the requester.
   *
   * @param sendCopy {@code true} to request a copy, {@code false} otherwise
   */
  public void setSendCopy(Boolean sendCopy) {
    this.sendCopy = sendCopy;
  }

  /**
   * Compares this request to another object for equality.
   *
   * <p>Two {@code AdminContactRequest} instances are considered equal when both their
   * {@code content} and {@code sendCopy} values are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code AdminContactRequest},
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
    AdminContactRequest adminContactRequest = (AdminContactRequest) o;
    return (
      Objects.equals(this.content, adminContactRequest.content) &&
      Objects.equals(this.sendCopy, adminContactRequest.sendCopy)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from the
   * {@code content} and {@code sendCopy} fields.
   *
   * @return the hash code for this request
   */
  @Override
  public int hashCode() {
    return Objects.hash(content, sendCopy);
  }

  /**
   * Returns a human-readable string representation of this request, with each field
   * rendered on its own indented line.
   *
   * @return a string representation of this {@code AdminContactRequest}
   */
  @Override
  public String toString() {
    return (
      "class AdminContactRequest {\n" +
      "    content: " +
      Util.toIndentedString(content) +
      "\n" +
      "    sendCopy: " +
      Util.toIndentedString(sendCopy) +
      "\n}"
    );
  }
}
