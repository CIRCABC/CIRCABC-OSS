package io.swagger.model;

import java.util.Objects;
import org.joda.time.DateTime;

/**
 * Domain model representing a single event entry within CIRCABC.
 *
 * <p>An {@code EntryEvent} captures a timestamped occurrence associated with a
 * repository {@link Node}, such as an activity or history record. It is a plain
 * data transfer object (DTO) serialized to JSON by the REST layer and carries:
 * when the event happened, its type, a free-form informational message, and the
 * node the event relates to.
 */
public class EntryEvent {

  /** Timestamp indicating when the event occurred. */
  private DateTime date = null;

  /** Category or classification of the event (e.g. the kind of action). */
  private String type = null;

  /** Free-form, human-readable description or additional detail about the event. */
  private String information = null;

  /** Repository node the event is associated with. */
  private Node node = null;

  /**
   * Returns the timestamp indicating when the event occurred.
   *
   * @return the event date, or {@code null} if not set
   */
  public DateTime getDate() {
    return date;
  }

  /**
   * Sets the timestamp indicating when the event occurred.
   *
   * @param date the event date to set
   */
  public void setDate(DateTime date) {
    this.date = date;
  }

  /**
   * Returns the category or classification of the event.
   *
   * @return the event type, or {@code null} if not set
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the category or classification of the event.
   *
   * @param type the event type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Returns the free-form informational message describing the event.
   *
   * @return the event information, or {@code null} if not set
   */
  public String getInformation() {
    return information;
  }

  /**
   * Sets the free-form informational message describing the event.
   *
   * @param information the event information to set
   */
  public void setInformation(String information) {
    this.information = information;
  }

  /**
   * Returns the repository node the event is associated with.
   *
   * @return the associated node, or {@code null} if not set
   */
  public Node getNode() {
    return node;
  }

  /**
   * Sets the repository node the event is associated with.
   *
   * @param node the associated node to set
   */
  public void setNode(Node node) {
    this.node = node;
  }

  /**
   * Compares this event with another object for equality.
   *
   * <p>Two {@code EntryEvent} instances are considered equal when their date,
   * type, information and node fields are all equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code EntryEvent},
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
    EntryEvent entryEvent = (EntryEvent) o;
    return (
      Objects.equals(this.date, entryEvent.date) &&
      Objects.equals(this.type, entryEvent.type) &&
      Objects.equals(this.information, entryEvent.information) &&
      Objects.equals(this.node, entryEvent.node)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)},
   * derived from the date, type, information and node fields.
   *
   * @return the hash code for this event
   */
  @Override
  public int hashCode() {
    return Objects.hash(date, type, information, node);
  }

  /**
   * Returns a human-readable, multi-line string representation of this event.
   *
   * @return a string describing this event and its field values
   */
  @Override
  public String toString() {
    return (
      "class EntryEvent {\n" +
      "    date: " +
      toIndentedString(date) +
      "\n" +
      "    type: " +
      toIndentedString(type) +
      "\n" +
      "    information: " +
      toIndentedString(information) +
      "\n" +
      "    node: " +
      toIndentedString(node) +
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
