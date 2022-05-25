package io.swagger.model;

import org.joda.time.DateTime;

import java.util.Objects;

public class EntryEvent {

    private DateTime date = null;

    private String type = null;

    private String information = null;

    private Node node = null;

    /**
     * Get date
     *
     * @return date
     */
    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    /**
     * Get type
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get information
     *
     * @return information
     */
    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    /**
     * Get on
     *
     * @return on
     */
    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntryEvent entryEvent = (EntryEvent) o;
        return Objects.equals(this.date, entryEvent.date)
                && Objects.equals(this.type, entryEvent.type)
                && Objects.equals(this.information, entryEvent.information)
                && Objects.equals(this.node, entryEvent.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, type, information, node);
    }

    @Override
    public String toString() {

        return "class EntryEvent {\n"
                + "    date: "
                + toIndentedString(date)
                + "\n"
                + "    type: "
                + toIndentedString(type)
                + "\n"
                + "    information: "
                + toIndentedString(information)
                + "\n"
                + "    node: "
                + toIndentedString(node)
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
