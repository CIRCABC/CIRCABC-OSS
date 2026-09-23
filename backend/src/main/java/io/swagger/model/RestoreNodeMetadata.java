package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object describing the parameters required to restore a node
 * from the Alfresco archive (trashcan) back into the live repository.
 *
 * <p>It carries the identifier of the archived node to restore and,
 * optionally, the identifier of the target folder into which the node should
 * be placed. When the target folder is omitted, the node is typically
 * restored to its original location.
 */
public class RestoreNodeMetadata {

  /** The node id of the archived (deleted) node that should be restored. */
  private String archiveNodeId = null;

  /**
   * The node id of the target space the node will be restored into; may be
   * left empty to restore to the original location.
   */
  private String targetFolderId = null;

  /**
   * the node Id of the archive to be restored
   *
   * @return archiveNodeId
   */
  public String getArchiveNodeId() {
    return archiveNodeId;
  }

  /**
   * Sets the node id of the archived node to be restored.
   *
   * @param archiveNodeId the id of the archived node
   */
  public void setArchiveNodeId(String archiveNodeId) {
    this.archiveNodeId = archiveNodeId;
  }

  /**
   * he node Id of the target space into which the node will be restored it can be left empty
   *
   * @return targetFolderId
   */
  public String getTargetFolderId() {
    return targetFolderId;
  }

  /**
   * Sets the node id of the target space into which the node will be restored.
   *
   * @param targetFolderId the id of the target folder; may be {@code null} or
   *     empty to restore to the original location
   */
  public void setTargetFolderId(String targetFolderId) {
    this.targetFolderId = targetFolderId;
  }

  /**
   * Compares this object with another for equality based on the archive node
   * id and target folder id.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code RestoreNodeMetadata}
   *     with equal field values, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RestoreNodeMetadata restoreNodeMetadata = (RestoreNodeMetadata) o;
    return (
      Objects.equals(this.archiveNodeId, restoreNodeMetadata.archiveNodeId) &&
      Objects.equals(this.targetFolderId, restoreNodeMetadata.targetFolderId)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code derived from the field values
   */
  @Override
  public int hashCode() {
    return Objects.hash(archiveNodeId, targetFolderId);
  }

  /**
   * Returns a human-readable representation of this object with its fields
   * listed on separate, indented lines.
   *
   * @return a string representation of this {@code RestoreNodeMetadata}
   */
  @Override
  public String toString() {
    return (
      "class RestoreNodeMetadata {\n" +
      "    archiveNodeId: " +
      toIndentedString(archiveNodeId) +
      "\n" +
      "    targetFolderId: " +
      toIndentedString(targetFolderId) +
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
