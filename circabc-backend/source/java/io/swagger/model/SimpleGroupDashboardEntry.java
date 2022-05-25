package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * all the activities inside one group, simply sorted by date. it shall returns a filtered content
 * based on the user&#39;s permission the &#39;news&#39; should be sorted by date !
 */
public class SimpleGroupDashboardEntry {

    private String groupId = null;

    private List<EntryEvent> entries = new ArrayList<>();

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
     * Get entries
     *
     * @return entries
     */
    public List<EntryEvent> getEntries() {
        return entries;
    }

    public void setEntries(List<EntryEvent> entries) {
        this.entries = entries;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleGroupDashboardEntry simpleGroupDashboardEntry = (SimpleGroupDashboardEntry) o;
        return Objects.equals(this.groupId, simpleGroupDashboardEntry.groupId)
                && Objects.equals(this.entries, simpleGroupDashboardEntry.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, entries);
    }

    @Override
    public String toString() {

        return "class SimpleGroupDashboardEntry {\n"
                + "    groupId: "
                + toIndentedString(groupId)
                + "\n"
                + "    entries: "
                + toIndentedString(entries)
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
