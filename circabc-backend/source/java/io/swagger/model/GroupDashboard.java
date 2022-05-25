package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * the representation of the dashboard of one CIRCABC Interest Group it contains the next events,
 * the list of activities in all its Interest group, and any other information needed ---
 * Description --- \&quot;entries\&quot; &#x3D; all the new content in all the groups the user is
 * member in \&quot;applicationRequests\&quot; &#x3D; all the applications for membership waiting on
 * approval \&quot;incomingEvents\&quot; &#x3D; all the events in the user calendat that are coming
 * soon \&quot;importantMessages\&quot; &#x3D; a communication chanel that IGLeaders can user
 * &gt;&gt; add any useful dashboard elements as a new properties
 */
public class GroupDashboard {

    private String groupId = null;

    private InterestGroup group = null;

    private List<GroupDashboardEntry> entries = new ArrayList<>();

    private List<Node> incomingEvents = new ArrayList<>();

    private List<Object> applicationRequests = new ArrayList<>();

    private List<GroupDashboardImportantMessages> importantMessages = new ArrayList<>();

    /**
     * Get groupId
     *
     * @return groupId
     */
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Get group
     *
     * @return group
     */
    public InterestGroup getGroup() {
        return group;
    }

    public void setGroup(InterestGroup group) {
        this.group = group;
    }

    /**
     * Get entries
     *
     * @return entries
     */
    public List<GroupDashboardEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<GroupDashboardEntry> entries) {
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
     * Get applicationRequests
     *
     * @return applicationRequests
     */
    public List<Object> getApplicationRequests() {
        return applicationRequests;
    }

    public void setApplicationRequests(List<Object> applicationRequests) {
        this.applicationRequests = applicationRequests;
    }

    /**
     * Get importantMessages
     *
     * @return importantMessages
     */
    public List<GroupDashboardImportantMessages> getImportantMessages() {
        return importantMessages;
    }

    public void setImportantMessages(List<GroupDashboardImportantMessages> importantMessages) {
        this.importantMessages = importantMessages;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupDashboard groupDashboard = (GroupDashboard) o;
        return Objects.equals(this.groupId, groupDashboard.groupId)
                && Objects.equals(this.group, groupDashboard.group)
                && Objects.equals(this.entries, groupDashboard.entries)
                && Objects.equals(this.incomingEvents, groupDashboard.incomingEvents)
                && Objects.equals(this.applicationRequests, groupDashboard.applicationRequests)
                && Objects.equals(this.importantMessages, groupDashboard.importantMessages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                groupId, group, entries, incomingEvents, applicationRequests, importantMessages);
    }

    @Override
    public String toString() {

        return "class GroupDashboard {\n"
                + "    groupId: "
                + toIndentedString(groupId)
                + "\n"
                + "    group: "
                + toIndentedString(group)
                + "\n"
                + "    entries: "
                + toIndentedString(entries)
                + "\n"
                + "    incomingEvents: "
                + toIndentedString(incomingEvents)
                + "\n"
                + "    applicationRequests: "
                + toIndentedString(applicationRequests)
                + "\n"
                + "    importantMessages: "
                + toIndentedString(importantMessages)
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
