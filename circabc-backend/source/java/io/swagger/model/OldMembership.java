/**
 *
 */
package io.swagger.model;

import java.util.Date;
import java.util.Objects;

/** @author beaurpi */
public class OldMembership {

    private String userId;
    private String groupId;
    private String profileId;
    private String alfGroupName;
    // -1 failed to restore, 0 new entry (no action done), 1 restored
    private Integer state;
    private Date stateDate;

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId, profileId, alfGroupName, state, stateDate);
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
                + "    profileId: "
                + toIndentedString(profileId)
                + "\n"
                + "    alfGroupName: "
                + toIndentedString(alfGroupName)
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getAlfGroupName() {
        return alfGroupName;
    }

    public void setAlfGroupName(String alfGroupName) {
        this.alfGroupName = alfGroupName;
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
        OldMembership that = (OldMembership) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(profileId, that.profileId) &&
                Objects.equals(alfGroupName, that.alfGroupName) &&
                Objects.equals(state, that.state) &&
                Objects.equals(stateDate, that.stateDate);
    }
}
