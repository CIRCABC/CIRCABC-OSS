/**
 *
 */
package eu.cec.digit.circabc.repo.app.model;

import java.util.Date;

/** @author beaurpi */
public class UserPropertyHistoryDAO {

    private String userId;
    private String groupId;
    private String nodeId;
    private Integer typeId;
    private String typeName;
    private String oldValue;
    private String newValue;
    private Boolean allowed;
    private Integer state;
    private Date stateDate;

    /** @return the userId */
    public String getUserId() {
        return userId;
    }

    /** @param userId the userId to set */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return the groupId */
    public String getGroupId() {
        return groupId;
    }

    /** @param groupId the groupId to set */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /** @return the nodeId */
    public String getNodeId() {
        return nodeId;
    }

    /** @param nodeId the nodeId to set */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /** @return the typeId */
    public Integer getTypeId() {
        return typeId;
    }

    /** @param typeId the typeId to set */
    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    /** @return the typeName */
    public String getTypeName() {
        return typeName;
    }

    /** @param typeName the typeName to set */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
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

    /** @return the allowed */
    public Boolean getAllowed() {
        return allowed;
    }

    /** @param allowed the allowed to set */
    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }

    /** @return the state */
    public Integer getState() {
        return state;
    }

    /** @param state the state to set */
    public void setState(Integer state) {
        this.state = state;
    }

    /** @return the stateDate */
    public Date getStateDate() {
        return stateDate;
    }

    /** @param stateDate the stateDate to set */
    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }
}
