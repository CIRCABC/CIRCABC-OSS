package io.swagger.model;

import org.joda.time.DateTime;

import java.util.Objects;

public class AuditAction {

    private InterestGroup interestGroup = null;

    private Node node = null;

    private String event = null;

    private String info = null;

    private User who = null;

    private DateTime when = null;

    /**
     * Get interestGroup
     *
     * @return interestGroup
     */
    public InterestGroup getInterestGroup() {
        return interestGroup;
    }

    public void setInterestGroup(InterestGroup interestGroup) {
        this.interestGroup = interestGroup;
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
     * Get event
     *
     * @return event
     */
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * Get info
     *
     * @return info
     */
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Get who
     *
     * @return who
     */
    public User getWho() {
        return who;
    }

    public void setWho(User who) {
        this.who = who;
    }

    /**
     * Get when
     *
     * @return when
     */
    public DateTime getWhen() {
        return when;
    }

    public void setWhen(DateTime when) {
        this.when = when;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuditAction auditAction = (AuditAction) o;
        return Objects.equals(this.interestGroup, auditAction.interestGroup)
                && Objects.equals(this.node, auditAction.node)
                && Objects.equals(this.event, auditAction.event)
                && Objects.equals(this.info, auditAction.info)
                && Objects.equals(this.who, auditAction.who)
                && Objects.equals(this.when, auditAction.when);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interestGroup, node, event, info, who, when);
    }

    @Override
    public String toString() {

        return "class AuditAction {\n"
                + "    interestGroup: "
                + toIndentedString(interestGroup)
                + "\n"
                + "    node: "
                + toIndentedString(node)
                + "\n"
                + "    event: "
                + toIndentedString(event)
                + "\n"
                + "    info: "
                + toIndentedString(info)
                + "\n"
                + "    who: "
                + toIndentedString(who)
                + "\n"
                + "    when: "
                + toIndentedString(when)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }
}
