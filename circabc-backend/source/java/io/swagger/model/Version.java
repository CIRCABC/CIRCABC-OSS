package io.swagger.model;

import java.util.Objects;

/**
 * the description of a version of a content of any kind
 */
public class Version {

    private Node node = null;
    private String notes = "";
    private String versionLabel = null;

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
     * Get versionLabel
     *
     * @return versionLabel
     */
    public String getVersionLabel() {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version version = (Version) o;
        return Objects.equals(this.node, version.node)
                && Objects.equals(this.versionLabel, version.versionLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, versionLabel);
    }

    @Override
    public String toString() {

        return "class Version {\n"
                + "    node: "
                + toIndentedString(node)
                + "\n"
                + "    versionLabel: "
                + toIndentedString(versionLabel)
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
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
