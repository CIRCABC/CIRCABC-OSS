package io.swagger.model;

import org.joda.time.DateTime;

import java.util.Objects;

/**
 * ArchiveNode
 */
public class ArchiveNode extends Node {

    private String deletedBy = null;

    private DateTime deletedDate = null;

    /**
     * Get deletedBy
     *
     * @return deletedBy
     */
    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    /**
     * Get deletedDate
     *
     * @return deletedDate
     */
    public DateTime getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(DateTime deletedDate) {
        this.deletedDate = deletedDate;
    }

    public void mergeNode(Node node) {
        this.setId(node.getId());
        this.setType(node.getType());
        this.setName(node.getName());
        this.setTitle(node.getTitle());
        this.setDescription(node.getDescription());
        this.setProperties(node.getProperties());
        this.setPermissions(node.getPermissions());
        this.setParentId(node.getParentId());
        this.setNotifications(node.getNotifications());
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArchiveNode archiveNode = (ArchiveNode) o;
        return Objects.equals(this.getId(), archiveNode.getId())
                && Objects.equals(this.getType(), archiveNode.getType())
                && Objects.equals(this.getName(), archiveNode.getName())
                && Objects.equals(this.getTitle(), archiveNode.getTitle())
                && Objects.equals(this.getDescription(), archiveNode.getDescription())
                && Objects.equals(this.getProperties(), archiveNode.getProperties())
                && Objects.equals(this.getPermissions(), archiveNode.getPermissions())
                && Objects.equals(this.getParentId(), archiveNode.getParentId())
                && Objects.equals(this.getNotifications(), archiveNode.getNotifications())
                && Objects.equals(this.deletedBy, archiveNode.deletedBy)
                && Objects.equals(this.deletedDate, archiveNode.deletedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getId(),
                getType(),
                getName(),
                getTitle(),
                getDescription(),
                getProperties(),
                getPermissions(),
                getParentId(),
                getNotifications(),
                deletedBy,
                deletedDate);
    }

    @Override
    public String toString() {

        return "class ArchiveNode {\n"
                + "    id: "
                + toIndentedString(getId())
                + "\n"
                + "    type: "
                + toIndentedString(getType())
                + "\n"
                + "    name: "
                + toIndentedString(getName())
                + "\n"
                + "    title: "
                + toIndentedString(getTitle())
                + "\n"
                + "    description: "
                + toIndentedString(getDescription())
                + "\n"
                + "    properties: "
                + toIndentedString(getProperties())
                + "\n"
                + "    permissions: "
                + toIndentedString(getPermissions())
                + "\n"
                + "    parentId: "
                + toIndentedString(getParentId())
                + "\n"
                + "    notifications: "
                + toIndentedString(getNotifications())
                + "\n"
                + "    deletedBy: "
                + toIndentedString(deletedBy)
                + "\n"
                + "    deletedDate: "
                + toIndentedString(deletedDate)
                + "\n"
                + "}";
    }
}
