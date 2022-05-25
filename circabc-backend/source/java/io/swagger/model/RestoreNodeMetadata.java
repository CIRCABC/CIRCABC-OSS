package io.swagger.model;

import java.util.Objects;

/**
 * RestoreNodeMetadata
 */
public class RestoreNodeMetadata {

    private String archiveNodeId = null;

    private String targetFolderId = null;

    /**
     * the node Id of the archive to be restored
     *
     * @return archiveNodeId
     */
    public String getArchiveNodeId() {
        return archiveNodeId;
    }

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

    public void setTargetFolderId(String targetFolderId) {
        this.targetFolderId = targetFolderId;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RestoreNodeMetadata restoreNodeMetadata = (RestoreNodeMetadata) o;
        return Objects.equals(this.archiveNodeId, restoreNodeMetadata.archiveNodeId)
                && Objects.equals(this.targetFolderId, restoreNodeMetadata.targetFolderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(archiveNodeId, targetFolderId);
    }

    @Override
    public String toString() {

        return "class RestoreNodeMetadata {\n"
                + "    archiveNodeId: "
                + toIndentedString(archiveNodeId)
                + "\n"
                + "    targetFolderId: "
                + toIndentedString(targetFolderId)
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
