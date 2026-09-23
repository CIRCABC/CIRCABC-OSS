package io.swagger.model;

import java.util.Objects;
import org.joda.time.DateTime;

/**
 * Data-transfer object representing a node that resides in the Alfresco archive
 * (trashcan / deleted items) store.
 *
 * <p>It extends {@link Node} with archive-specific metadata describing who
 * removed the node and when the removal occurred, so this information can be
 * exposed through the REST API when listing or inspecting deleted content.
 */
public class ArchiveNode extends Node {

  /** Identifier (typically the username) of the user who deleted the node. */
  private String deletedBy = null;

  /** Timestamp indicating when the node was moved to the archive store. */
  private DateTime deletedDate = null;

  /**
   * Get deletedBy
   *
   * @return deletedBy
   */
  public String getDeletedBy() {
    return deletedBy;
  }

  /**
   * Sets the identifier of the user who deleted the node.
   *
   * @param deletedBy the identifier (typically the username) of the deleting user
   */
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

  /**
   * Sets the timestamp at which the node was moved to the archive store.
   *
   * @param deletedDate the deletion timestamp
   */
  public void setDeletedDate(DateTime deletedDate) {
    this.deletedDate = deletedDate;
  }

  /**
   * Copies the common node attributes from the given {@link Node} into this
   * archive node, leaving the archive-specific fields ({@code deletedBy} and
   * {@code deletedDate}) untouched.
   *
   * <p>The copied attributes are the id, type, name, title, description,
   * properties, permissions, parent id and notifications.
   *
   * @param node the source node whose common attributes are merged into this instance
   */
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

  /**
   * Compares this archive node with another object for equality. Two archive
   * nodes are equal when all inherited {@link Node} attributes as well as the
   * {@code deletedBy} and {@code deletedDate} fields are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code ArchiveNode}, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArchiveNode archiveNode = (ArchiveNode) o;
    return (
      Objects.equals(this.getId(), archiveNode.getId()) &&
      Objects.equals(this.getType(), archiveNode.getType()) &&
      Objects.equals(this.getName(), archiveNode.getName()) &&
      Objects.equals(this.getTitle(), archiveNode.getTitle()) &&
      Objects.equals(this.getDescription(), archiveNode.getDescription()) &&
      Objects.equals(this.getProperties(), archiveNode.getProperties()) &&
      Objects.equals(this.getPermissions(), archiveNode.getPermissions()) &&
      Objects.equals(this.getParentId(), archiveNode.getParentId()) &&
      Objects.equals(this.getNotifications(), archiveNode.getNotifications()) &&
      Objects.equals(this.deletedBy, archiveNode.deletedBy) &&
      Objects.equals(this.deletedDate, archiveNode.deletedDate)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, computed from
   * the inherited {@link Node} attributes and the archive-specific fields.
   *
   * @return the hash code for this archive node
   */
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
      deletedDate
    );
  }

  /**
   * Returns a human-readable, multi-line representation of this archive node,
   * including all inherited attributes and the archive-specific fields.
   *
   * @return a string representation of this archive node
   */
  @Override
  public String toString() {
    return (
      "class ArchiveNode {\n" +
      "    id: " +
      toIndentedString(getId()) +
      "\n" +
      "    type: " +
      toIndentedString(getType()) +
      "\n" +
      "    name: " +
      toIndentedString(getName()) +
      "\n" +
      "    title: " +
      toIndentedString(getTitle()) +
      "\n" +
      "    description: " +
      toIndentedString(getDescription()) +
      "\n" +
      "    properties: " +
      toIndentedString(getProperties()) +
      "\n" +
      "    permissions: " +
      toIndentedString(getPermissions()) +
      "\n" +
      "    parentId: " +
      toIndentedString(getParentId()) +
      "\n" +
      "    notifications: " +
      toIndentedString(getNotifications()) +
      "\n" +
      "    deletedBy: " +
      toIndentedString(deletedBy) +
      "\n" +
      "    deletedDate: " +
      toIndentedString(deletedDate) +
      "\n" +
      "}"
    );
  }
}
