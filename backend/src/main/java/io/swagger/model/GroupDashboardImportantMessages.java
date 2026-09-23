package io.swagger.model;

import java.util.Objects;
import org.joda.time.LocalDate;

/**
 * Data transfer object representing a single important message displayed on an
 * Interest Group dashboard.
 *
 * <p>Each instance couples a {@link LocalDate} indicating when the message is
 * relevant with the textual message content, and is serialized as part of the
 * group dashboard REST response.
 */
public class GroupDashboardImportantMessages {

  /** The date associated with the important message. */
  private LocalDate date = null;

  /** The textual content of the important message. */
  private String message = null;

  /**
   * Returns the date associated with this important message.
   *
   * @return the message date
   */
  public LocalDate getDate() {
    return date;
  }

  /**
   * Sets the date associated with this important message.
   *
   * @param date the message date to set
   */
  public void setDate(LocalDate date) {
    this.date = date;
  }

  /**
   * Returns the textual content of this important message.
   *
   * @return the message text
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the textual content of this important message.
   *
   * @param message the message text to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Compares this object to another for equality based on the date and message
   * fields.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a
   *     {@code GroupDashboardImportantMessages} with equal date and message,
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
    GroupDashboardImportantMessages groupDashboardImportantMessages =
      (GroupDashboardImportantMessages) o;
    return (
      Objects.equals(this.date, groupDashboardImportantMessages.date) &&
      Objects.equals(this.message, groupDashboardImportantMessages.message)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from
   * the date and message fields.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(date, message);
  }

  /**
   * Returns a human-readable string representation of this object, listing its
   * date and message fields.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (
      "class GroupDashboardImportantMessages {\n" +
      "    date: " +
      toIndentedString(date) +
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
   * @return the indented string representation, or {@code "null"} if the object is {@code null}
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
