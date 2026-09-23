/**
 *
 */
package io.swagger.model.db;

import java.util.Date;

/**
 * Data access object representing a single historical change to a user property.
 *
 * <p>Each instance captures one audited modification of a property associated with a user, such as
 * a change to a permission or profile-related attribute within an Interest Group. It records who
 * and what was affected ({@code userId}, {@code groupId}, {@code nodeId}), the type of property
 * that changed ({@code typeId}, {@code typeName}), the previous and new values ({@code oldValue},
 * {@code newValue}), whether the change was allowed, and the state of the change together with the
 * timestamp at which that state was reached.
 *
 * <p>This is a plain data container used to move user property history rows between the persistence
 * layer and the REST/service layers; it holds no business logic.
 *
 * @author beaurpi
 */
public class UserPropertyHistoryDAO {

  /** Identifier of the user whose property was changed. */
  private String userId;

  /** Identifier of the group (Interest Group) in which the change occurred. */
  private String groupId;

  /** Identifier of the repository node the property change relates to. */
  private String nodeId;

  /** Numeric identifier of the property type that was changed. */
  private Integer typeId;

  /** Human-readable name of the property type that was changed. */
  private String typeName;

  /** Value of the property before the change. */
  private String oldValue;

  /** Value of the property after the change. */
  private String newValue;

  /** Flag indicating whether the change was allowed. */
  private Boolean allowed;

  /** Numeric code representing the state of the change. */
  private Integer state;

  /** Timestamp at which the recorded state was reached. */
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
