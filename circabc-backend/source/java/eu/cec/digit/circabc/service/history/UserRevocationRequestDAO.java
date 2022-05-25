/**
 *
 */
package eu.cec.digit.circabc.service.history;

import java.util.Date;

/** @author beaurpi */
public class UserRevocationRequestDAO {

    private Integer id;
    private String userIds;
    private Date revocationDate;
    private String requester;
    private Integer requestState;
    private Date jobStarted;
    private Date jobEnded;
    private String groupId;
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
