package io.swagger.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * all the activities inside one group it shall returns a filtered content based on the user&#39;s
 * permission the &#39;news&#39; should be sorted by date !
 */
public class GroupDashboardEntry {

    private DateTime date = null;

    private String groupId = null;

    private List<EntryEvent> news = new ArrayList<>();

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
     * Get news
     *
     * @return news
     */
    public List<EntryEvent> getNews() {
        return news;
    }

    public void setNews(List<EntryEvent> news) {
        this.news = news;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupDashboardEntry groupDashboardEntry = (GroupDashboardEntry) o;
        return Objects.equals(this.date, groupDashboardEntry.date)
                && Objects.equals(this.groupId, groupDashboardEntry.groupId)
                && Objects.equals(this.news, groupDashboardEntry.news);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, groupId, news);
    }

    @Override
    public String toString() {

        return "class GroupDashboardEntry {\n"
                + "    date: "
                + toIndentedString(date)
                + "\n"
                + "    groupId: "
                + toIndentedString(groupId)
                + "\n"
                + "    news: "
                + toIndentedString(news)
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
