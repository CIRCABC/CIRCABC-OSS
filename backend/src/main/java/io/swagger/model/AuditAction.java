package io.swagger.model;

import java.util.Objects;
import org.joda.time.DateTime;

/**
 * Domain model (DTO) representing a single audit trail entry within CIRCABC.
 *
 * <p>An {@code AuditAction} captures a recorded event that occurred in the system, describing what
 * happened, who triggered it, when it happened and the context (interest group and node) in which
 * it took place. Instances are typically serialized to JSON and returned by the REST API when audit
 * information is requested.
 */
public class AuditAction {

  /** The interest group in whose context the audited action took place. */
  private InterestGroup interestGroup = null;

  /** The repository node (document, folder, topic, etc.) the action was performed on. */
  private Node node = null;

  /** The type of event that was audited (e.g. creation, update, deletion). */
  private String event = null;

  /** Additional free-text information describing the audited action. */
  private String info = null;

  /** The user who performed the audited action. */
  private User who = null;

  /** The date and time at which the audited action occurred. */
  private DateTime when = null;

  /**
   * Returns the interest group in whose context the audited action took place.
   *
   * @return the associated interest group, or {@code null} if not set
   */
  public InterestGroup getInterestGroup() {
    return interestGroup;
  }

  /**
   * Sets the interest group in whose context the audited action took place.
   *
   * @param interestGroup the interest group to associate with this audit entry
   */
  public void setInterestGroup(InterestGroup interestGroup) {
    this.interestGroup = interestGroup;
  }

  /**
   * Returns the repository node the action was performed on.
   *
   * @return the associated node, or {@code null} if not set
   */
  public Node getNode() {
    return node;
  }

  /**
   * Sets the repository node the action was performed on.
   *
   * @param node the node to associate with this audit entry
   */
  public void setNode(Node node) {
    this.node = node;
  }

  /**
   * Returns the type of event that was audited.
   *
   * @return the event identifier, or {@code null} if not set
   */
  public String getEvent() {
    return event;
  }

  /**
   * Sets the type of event that was audited.
   *
   * @param event the event identifier to set
   */
  public void setEvent(String event) {
    this.event = event;
  }

  /**
   * Returns the additional free-text information describing the audited action.
   *
   * @return the descriptive information, or {@code null} if not set
   */
  public String getInfo() {
    return info;
  }

  /**
   * Sets the additional free-text information describing the audited action.
   *
   * @param info the descriptive information to set
   */
  public void setInfo(String info) {
    this.info = info;
  }

  /**
   * Returns the user who performed the audited action.
   *
   * @return the acting user, or {@code null} if not set
   */
  public User getWho() {
    return who;
  }

  /**
   * Sets the user who performed the audited action.
   *
   * @param who the acting user to set
   */
  public void setWho(User who) {
    this.who = who;
  }

  /**
   * Returns the date and time at which the audited action occurred.
   *
   * @return the timestamp of the action, or {@code null} if not set
   */
  public DateTime getWhen() {
    return when;
  }

  /**
   * Sets the date and time at which the audited action occurred.
   *
   * @param when the timestamp to set
   */
  public void setWhen(DateTime when) {
    this.when = when;
  }

  /**
   * Compares this audit action with another object for equality. Two {@code AuditAction} instances
   * are considered equal when all of their fields (interest group, node, event, info, who and when)
   * are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code AuditAction}, {@code false}
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
    AuditAction auditAction = (AuditAction) o;
    return (
      Objects.equals(this.interestGroup, auditAction.interestGroup) &&
      Objects.equals(this.node, auditAction.node) &&
      Objects.equals(this.event, auditAction.event) &&
      Objects.equals(this.info, auditAction.info) &&
      Objects.equals(this.who, auditAction.who) &&
      Objects.equals(this.when, auditAction.when)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from all fields of this
   * audit action.
   *
   * @return the computed hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(interestGroup, node, event, info, who, when);
  }

  /**
   * Returns a human-readable, multi-line string representation of this audit action, listing each
   * field and its value.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (
      "class AuditAction {\n" +
      "    interestGroup: " +
      toIndentedString(interestGroup) +
      "\n" +
      "    node: " +
      toIndentedString(node) +
      "\n" +
      "    event: " +
      toIndentedString(event) +
      "\n" +
      "    info: " +
      toIndentedString(info) +
      "\n" +
      "    who: " +
      toIndentedString(who) +
      "\n" +
      "    when: " +
      toIndentedString(when) +
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
