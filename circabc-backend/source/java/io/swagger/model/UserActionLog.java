package io.swagger.model;

import java.util.Objects;

/**
 * UserActionLog
 */
public class UserActionLog {

    private String actionDate = null;

    private String action = null;

    private Node node = null;

    private String igNode = null;

    private String username = null;

    /**
     * Get actionDate
     *
     * @return actionDate
     */
    public String getActionDate() {
        return actionDate;
    }

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

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserActionLog userActionLog = (UserActionLog) o;
        return Objects.equals(this.actionDate, userActionLog.actionDate)
                && Objects.equals(this.action, userActionLog.action)
                && Objects.equals(this.node, userActionLog.node)
                && Objects.equals(this.igNode, userActionLog.igNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionDate, action, node, igNode);
    }

    @Override
    public String toString() {

        return "class UserActionLog {\n"
                + "    actionDate: "
                + toIndentedString(actionDate)
                + "\n"
                + "    action: "
                + toIndentedString(action)
                + "\n"
                + "    node: "
                + toIndentedString(node)
                + "\n"
                + "    igNode: "
                + toIndentedString(igNode)
                + "\n"
                + "}";
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
