package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * the representation of the dashboard of one CIRCABC user it contains the next events, the list of
 * activities in all its Interest group, his memberships and any other information needed ---
 * Description --- \&quot;entries\&quot; &#x3D; all the new content in all the groups the user is
 * member in \&quot;membershipRequests\&quot; &#x3D; all the request a user has submitted and
 * waiting on approval \&quot;incomingEvents\&quot; &#x3D; all the events in the user calendat that
 * are coming soon &gt;&gt; add any useful dashboard elements as a new properties
 */
public class UserDashboard {

    private String userId = null;

    private List<UserDashboardEntry> entries = new ArrayList<>();

    private List<Node> incomingEvents = new ArrayList<>();

    private List<Object> membershipRequests = new ArrayList<>();

    /**
     * Get userId
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get entries
     *
     * @return entries
     */
    public List<UserDashboardEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<UserDashboardEntry> entries) {
        this.entries = entries;
    }

    /**
     * Get incomingEvents
     *
     * @return incomingEvents
     */
    public List<Node> getIncomingEvents() {
        return incomingEvents;
    }

    public void setIncomingEvents(List<Node> incomingEvents) {
        this.incomingEvents = incomingEvents;
    }

    /**
     * Get membershipRequests
     *
     * @return membershipRequests
     */
    public List<Object> getMembershipRequests() {
        return membershipRequests;
    }

    public void setMembershipRequests(List<Object> membershipRequests) {
        this.membershipRequests = membershipRequests;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDashboard userDashboard = (UserDashboard) o;
        return Objects.equals(this.userId, userDashboard.userId)
                && Objects.equals(this.entries, userDashboard.entries)
                && Objects.equals(this.incomingEvents, userDashboard.incomingEvents)
                && Objects.equals(this.membershipRequests, userDashboard.membershipRequests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, entries, incomingEvents, membershipRequests);
    }

    @Override
    public String toString() {

        return "class UserDashboard {\n"
                + "    userId: "
                + toIndentedString(userId)
                + "\n"
                + "    entries: "
                + toIndentedString(entries)
                + "\n"
                + "    incomingEvents: "
                + toIndentedString(incomingEvents)
                + "\n"
                + "    membershipRequests: "
                + toIndentedString(membershipRequests)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }
}
