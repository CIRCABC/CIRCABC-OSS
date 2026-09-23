package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object describing a single audited user action within CIRCABC.
 *
 * <p>An instance captures who performed an action, what action was performed,
 * when it happened, and on which content and Interest Group it took place. It
 * is typically used to serialize audit/activity log entries in REST responses.
 */
public class UserActionLog {

  /** Timestamp at which the action occurred, represented as a string. */
  private String actionDate = null;

  /** Identifier or name of the action that was performed. */
  private String action = null;

  /** Content node on which the action was performed. */
  private Node node = null;

  /** Identifier of the Interest Group node associated with the action. */
  private String igNode = null;

  /** Username of the user who performed the action. */
  private String username = null;

  /**
   * Get actionDate
   *
   * @return actionDate
   */
  public String getActionDate() {
    return actionDate;
  }

  /**
   * Sets the timestamp at which the action occurred.
   *
   * @param actionDate the action date to set
   */
  public void setActionDate(String actionDate) {
    this.actionDate = actionDate;
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
   * Sets the action that was performed.
   *
   * @param action the action to set
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Get node
   *
   * @return node
   */
  public Node getNode() {
    return node;
  }

  /**
   * Sets the content node on which the action was performed.
   *
   * @param node the node to set
   */
  public void setNode(Node node) {
    this.node = node;
  }

  /**
   * @return the igNode
   */
  public String getIgNode() {
    return igNode;
  }

  /**
   * @param igNode the igNode to set
   */
  public void setIgNode(String igNode) {
    this.igNode = igNode;
  }

  /**
   * Compares this log entry with another object for equality based on the
   * action date, action, node and Interest Group node.
   *
   * @param o the object to compare with
   * @return {@code true} if the other object is a {@code UserActionLog} with
   *     equal action date, action, node and Interest Group node; {@code false}
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
    UserActionLog userActionLog = (UserActionLog) o;
    return (
      Objects.equals(this.actionDate, userActionLog.actionDate) &&
      Objects.equals(this.action, userActionLog.action) &&
      Objects.equals(this.node, userActionLog.node) &&
      Objects.equals(this.igNode, userActionLog.igNode)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, computed from
   * the action date, action, node and Interest Group node.
   *
   * @return the hash code for this log entry
   */
  @Override
  public int hashCode() {
    return Objects.hash(actionDate, action, node, igNode);
  }

  /**
   * Returns a human-readable string representation of this log entry.
   *
   * @return a string describing the action date, action, node and Interest
   *     Group node
   */
  @Override
  public String toString() {
    return (
      "class UserActionLog {\n" +
      "    actionDate: " +
      toIndentedString(actionDate) +
      "\n" +
      "    action: " +
      toIndentedString(action) +
      "\n" +
      "    node: " +
      toIndentedString(node) +
      "\n" +
      "    igNode: " +
      toIndentedString(igNode) +
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

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }
}
