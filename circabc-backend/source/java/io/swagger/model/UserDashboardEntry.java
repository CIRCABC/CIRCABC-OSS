package io.swagger.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * contains the information related to all activities linked to the memberships of one user it is
 * grouped by date
 */
public class UserDashboardEntry {

    private DateTime date = null;

    private List<SimpleGroupDashboardEntry> groups = new ArrayList<>();

    /**
     * Get date
     *
     * @return date
     */
    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    /**
     * Get groups
     *
     * @return groups
     */
    public List<SimpleGroupDashboardEntry> getGroups() {
        return groups;
    }

    public void setGroups(List<SimpleGroupDashboardEntry> groups) {
        this.groups = groups;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDashboardEntry userDashboardEntry = (UserDashboardEntry) o;
        return Objects.equals(this.date, userDashboardEntry.date)
                && Objects.equals(this.groups, userDashboardEntry.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, groups);
    }

    @Override
    public String toString() {

        return "class UserDashboardEntry {\n"
                + "    date: "
                + toIndentedString(date)
                + "\n"
                + "    groups: "
                + toIndentedString(groups)
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
