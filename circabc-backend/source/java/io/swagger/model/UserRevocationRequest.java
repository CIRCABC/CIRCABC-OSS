/**
 *
 */
package io.swagger.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/** @author beaurpi */
public class UserRevocationRequest {

    private Integer id;
    private List<String> userIds = new ArrayList<>();
    private DateTime revocationDate;
    private String requester;
    private String action;
    private String groupId;
    private Integer requestState;
    private DateTime jobStarted;
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
