package io.swagger.model.db;

import java.util.Date;

/**
 * Data access object representing the expiration record of a member's assignment
 * within a CIRCABC Interest Group.
 *
 * <p>Each instance links a user (via {@code userId}) to a group ({@code groupId})
 * and a membership profile ({@code profileId}), together with the underlying
 * Alfresco authority group ({@code alfrescoGroup}) and the {@code expirationDate}
 * at which the membership is scheduled to expire. It is a plain data holder used
 * to transport member expiration information between the persistence layer and
 * the services that process membership expirations.
 */
public class MemberExpirationDAO {

  /** Identifier of the Interest Group the membership belongs to. */
  private String groupId;
  /** Identifier of the membership profile assigned to the user within the group. */
  private String profileId;
  /** Identifier of the user whose membership is subject to expiration. */
  private String userId;
  /** Name of the underlying Alfresco authority group backing the membership. */
  private String alfrescoGroup;
  /** Date on which the membership is scheduled to expire. */
  private Date expirationDate;

  /**
   * @return the groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * @return the expirationDate
   */
  public Date getExpirationDate() {
    return expirationDate;
  }

  /**
   * @param expirationDate the expirationDate to set
   */
  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  /**
   * @return the alfrescoGroup
   */
  public String getAlfrescoGroup() {
    return alfrescoGroup;
  }

  /**
   * @param alfrescoGroup the alfrescoGroup to set
   */
  public void setAlfrescoGroup(String alfrescoGroup) {
    this.alfrescoGroup = alfrescoGroup;
  }

  /**
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId the userId to set
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @return the profileId
   */
  public String getProfileId() {
    return profileId;
  }

  /**
   * @param profileId the profileId to set
   */
  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }
}
