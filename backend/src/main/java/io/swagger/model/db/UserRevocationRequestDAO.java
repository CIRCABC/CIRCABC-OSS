/**
 *
 */
package io.swagger.model.db;

import java.util.Date;

/**
 * Data access object representing a single user revocation request persisted in the database.
 *
 * <p>A revocation request captures the intent to remove (or otherwise act upon) one or more users
 * from a given group, scheduled to be processed by a background job. Instances of this class map
 * directly to the underlying persistence rows: they hold the request metadata (who requested it and
 * when the action should occur), the current processing state, and the timestamps recording when
 * the asynchronous job started and finished. This class is a plain data holder and contains no
 * business logic.
 *
 * @author beaurpi
 */
public class UserRevocationRequestDAO {

  /** Unique identifier (primary key) of the revocation request. */
  private Integer id;

  /** Identifiers of the users targeted by this revocation request, stored as an encoded string. */
  private String userIds;

  /** Date on which the revocation is scheduled to take effect. */
  private Date revocationDate;

  /** Identifier of the user who submitted the revocation request. */
  private String requester;

  /** Current processing state of the request, encoded as an integer status code. */
  private Integer requestState;

  /** Timestamp recording when the asynchronous processing job started; {@code null} if not yet started. */
  private Date jobStarted;

  /** Timestamp recording when the asynchronous processing job ended; {@code null} if not yet finished. */
  private Date jobEnded;

  /** Identifier of the group from which the users are to be revoked. */
  private String groupId;

  /** Action to be performed for this request (e.g. the type of revocation operation). */
  private String action;

  /** @return the id */
  public Integer getId() {
    return id;
  }

  /** @param id the id to set */
  public void setId(Integer id) {
    this.id = id;
  }

  /** @return the userIds */
  public String getUserIds() {
    return userIds;
  }

  /** @param userIds the userIds to set */
  public void setUserIds(String userIds) {
    this.userIds = userIds;
  }

  /** @return the revocationDate */
  public Date getRevocationDate() {
    return revocationDate;
  }

  /** @param revocationDate the revocationDate to set */
  public void setRevocationDate(Date revocationDate) {
    this.revocationDate = revocationDate;
  }

  /** @return the requester */
  public String getRequester() {
    return requester;
  }

  /** @param requester the requester to set */
  public void setRequester(String requester) {
    this.requester = requester;
  }

  /** @return the requestState */
  public Integer getRequestState() {
    return requestState;
  }

  /** @param requestState the requestState to set */
  public void setRequestState(Integer requestState) {
    this.requestState = requestState;
  }

  /** @return the jobStarted */
  public Date getJobStarted() {
    return jobStarted;
  }

  /** @param jobStarted the jobStarted to set */
  public void setJobStarted(Date jobStarted) {
    this.jobStarted = jobStarted;
  }

  /** @return the jobEnded */
  public Date getJobEnded() {
    return jobEnded;
  }

  /** @param jobEnded the jobEnded to set */
  public void setJobEnded(Date jobEnded) {
    this.jobEnded = jobEnded;
  }

  /** @return the groupId */
  public String getGroupId() {
    return groupId;
  }

  /** @param groupId the groupId to set */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /** @return the action */
  public String getAction() {
    return action;
  }

  /** @param action the action to set */
  public void setAction(String action) {
    this.action = action;
  }
}
