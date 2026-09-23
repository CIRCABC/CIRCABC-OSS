/**
 *
 */
package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;

/**
 * Domain model representing a request to revoke (or otherwise act upon) the memberships of one or
 * more users within an Interest Group.
 *
 * <p>Such a request captures who is targeted ({@link #userIds}), in which group ({@link #groupId}),
 * the action to perform ({@link #action}), when it should take effect ({@link #revocationDate}) and
 * who initiated it ({@link #requester}). The remaining fields track the lifecycle of the associated
 * background job: its current state ({@link #requestState}) and the timestamps at which processing
 * started ({@link #jobStarted}) and completed ({@link #jobEnded}).
 *
 * @author beaurpi
 */
public class UserRevocationRequest {

  /** Unique identifier of the revocation request. */
  private Integer id;

  /** Identifiers of the users targeted by this revocation request. */
  private List<String> userIds = new ArrayList<>();

  /** Date at which the revocation should take effect. */
  private DateTime revocationDate;

  /** Identifier of the user who submitted the request. */
  private String requester;

  /** Action to be performed on the targeted users (e.g. the type of revocation). */
  private String action;

  /** Identifier of the Interest Group the request applies to. */
  private String groupId;

  /** Current lifecycle state of the request/associated job. */
  private Integer requestState;

  /** Timestamp at which processing of the request started. */
  private DateTime jobStarted;

  /** Timestamp at which processing of the request completed. */
  private DateTime jobEnded;

  /** @return the groupId */
  public String getGroupId() {
    return groupId;
  }

  /** @param groupId the groupId to set */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /** @return the id */
  public Integer getId() {
    return id;
  }

  /** @param id the id to set */
  public void setId(Integer id) {
    this.id = id;
  }

  /** @return the userIds */
  public List<String> getUserIds() {
    return userIds;
  }

  /** @param userIds the userIds to set */
  public void setUserIds(List<String> userIds) {
    this.userIds = userIds;
  }

  /** @return the scheduleDate */
  public DateTime getRevocationDate() {
    return revocationDate;
  }

  /** @param revocationDate the scheduleDate to set */
  public void setRevocationDate(DateTime revocationDate) {
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
  public DateTime getJobStarted() {
    return jobStarted;
  }

  /** @param jobStarted the jobStarted to set */
  public void setJobStarted(DateTime jobStarted) {
    this.jobStarted = jobStarted;
  }

  /** @return the jobEnded */
  public DateTime getJobEnded() {
    return jobEnded;
  }

  /** @param jobEnded the jobEnded to set */
  public void setJobEnded(DateTime jobEnded) {
    this.jobEnded = jobEnded;
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
