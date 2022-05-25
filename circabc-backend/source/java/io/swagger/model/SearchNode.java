package io.swagger.model;

import java.util.Objects;

/**
 * SearchNode
 */
@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.SpringCodegen",
        date = "2017-03-22T15:13:11.258+01:00")
public class SearchNode extends Node {

    private String resultType = null;

    private String targetNode = null;

    /**
     * Get targetNode
     *
     * @return targetNode
     */
    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode) {
        this.targetNode = targetNode;
    }

    /**
     * @return the resultType
     */
    public String getResultType() {
        return resultType;
    }

    /**
     * @param resultType the resultType to set
     */
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchNode searchNode = (SearchNode) o;
        return Objects.equals(this.getId(), searchNode.getId())
                && Objects.equals(this.getType(), searchNode.getType())
                && Objects.equals(this.getName(), searchNode.getName())
                && Objects.equals(this.getTitle(), searchNode.getTitle())
                && Objects.equals(this.getDescription(), searchNode.getDescription())
                && Objects.equals(this.getProperties(), searchNode.getProperties())
                && Objects.equals(this.getPermissions(), searchNode.getPermissions())
                && Objects.equals(this.getParentId(), searchNode.getParentId())
                && Objects.equals(this.getService(), searchNode.getService())
                && Objects.equals(this.targetNode, searchNode.targetNode)
                && Objects.equals(this.resultType, searchNode.resultType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), resultType, targetNode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SearchNode {\n");

        sb.append("    id: ").append(toIndentedString(getId())).append("\n");
        sb.append("    type: ").append(toIndentedString(getType())).append("\n");
        sb.append("    name: ").append(toIndentedString(getName())).append("\n");
        sb.append("    title: ").append(toIndentedString(getTitle())).append("\n");
        sb.append("    description: ").append(toIndentedString(getDescription())).append("\n");
        sb.append("    properties: ").append(toIndentedString(getProperties())).append("\n");
        sb.append("    permissions: ").append(toIndentedString(getPermissions())).append("\n");
        sb.append("    parentId: ").append(toIndentedString(getParentId())).append("\n");
        sb.append("    service: ").append(toIndentedString(getService())).append("\n");
        sb.append("    targetNode: ").append(toIndentedString(targetNode)).append("\n");
        sb.append("    resultType: ").append(toIndentedString(resultType)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
