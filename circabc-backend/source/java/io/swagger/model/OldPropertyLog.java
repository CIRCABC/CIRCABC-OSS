/**
 *
 */
package io.swagger.model;

import java.util.Date;
import java.util.Objects;

/** @author beaurpi */
public class OldPropertyLog {

    private String userId;
    private String groupId;
    private String nodeId;
    private String oldValue;
    private String newValue;
    private Integer propertyType;
    private Boolean allowed;
    // -1 failed to restore, 0 new entry (no action done), 1 restored
    private Integer state;
    private Date stateDate;

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId, nodeId, oldValue, newValue, allowed, state, stateDate);
    }

    @Override
    public String toString() {

        return "class OldMembership {\n"
                + "    userId: "
                + toIndentedString(userId)
                + "\n"
                + "    groupId: "
                + toIndentedString(groupId)
                + "\n"
                + "    nodeId: "
                + toIndentedString(nodeId)
                + "\n"
                + "    oldValue: "
                + toIndentedString(oldValue)
                + "\n"
                + "    newValue: "
                + toIndentedString(newValue)
                + "    allowed: "
                + toIndentedString(allowed)
                + "\n"
                + "\n"
                + "    state: "
                + toIndentedString(state)
                + "\n"
                + "    stateDate: "
                + toIndentedString(stateDate)
                + "\n"
                + "}";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return the nodeId */
    public String getNodeId() {
        return nodeId;
    }

    /** @param nodeId the nodeId to set */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /** @return the oldValue */
    public String getOldValue() {
        return oldValue;
    }

    /** @param oldValue the oldValue to set */
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /** @return the newValue */
    public String getNewValue() {
        return newValue;
    }

    /** @param newValue the newValue to set */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    /** @return the propertyType */
    public Integer getPropertyType() {
        return propertyType;
    }

    /** @param propertyType the propertyType to set */
    public void setPropertyType(Integer propertyType) {
        this.propertyType = propertyType;
    }

    /** @return the allowed */
    public Boolean getAllowed() {
        return allowed;
    }

    /** @param allowed the allowed to set */
    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OldPropertyLog that = (OldPropertyLog) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
        if (nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null) return false;
        if (oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null) return false;
        if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null) return false;
        if (propertyType != null ? !propertyType.equals(that.propertyType) : that.propertyType != null) return false;
        if (allowed != null ? !allowed.equals(that.allowed) : that.allowed != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        return stateDate != null ? stateDate.equals(that.stateDate) : that.stateDate == null;
    }
}
