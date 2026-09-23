package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.joda.time.DateTime;

/**
 * Data transfer object representing the dashboard entry for a single Interest Group.
 *
 * <p>It aggregates all the activities inside one group. The content is expected to be filtered
 * according to the requesting user's permissions, and the {@code news} entries should be sorted by
 * date.
 */
public class GroupDashboardEntry {

  /** Reference date/time of this dashboard entry. */
  private DateTime date = null;

  /** Identifier of the Interest Group this dashboard entry belongs to. */
  private String groupId = null;

  /** Activity/news items for the group, expected to be sorted by date. */
  private List<EntryEvent> news = new ArrayList<>();

  /**
   * Returns the reference date/time of this dashboard entry.
   *
   * @return the entry date
   */
  public DateTime getDate() {
    return date;
  }

  /**
   * Sets the reference date/time of this dashboard entry.
   *
   * @param date the entry date to set
   */
  public void setDate(DateTime date) {
    this.date = date;
  }

  /**
   * Returns the identifier of the Interest Group this dashboard entry belongs to.
   *
   * @return the group identifier
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Sets the identifier of the Interest Group this dashboard entry belongs to.
   *
   * @param groupId the group identifier to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Returns the activity/news items for the group.
   *
   * @return the list of news events, expected to be sorted by date
   */
  public List<EntryEvent> getNews() {
    return news;
  }

  /**
   * Sets the activity/news items for the group.
   *
   * @param news the list of news events to set
   */
  public void setNews(List<EntryEvent> news) {
    this.news = news;
  }

  /**
   * Compares this dashboard entry with another object for equality based on date, group identifier
   * and news list.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code GroupDashboardEntry} with equal fields
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupDashboardEntry groupDashboardEntry = (GroupDashboardEntry) o;
    return (
      Objects.equals(this.date, groupDashboardEntry.date) &&
      Objects.equals(this.groupId, groupDashboardEntry.groupId) &&
      Objects.equals(this.news, groupDashboardEntry.news)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code derived from date, group identifier and news list
   */
  @Override
  public int hashCode() {
    return Objects.hash(date, groupId, news);
  }

  /**
   * Returns a human-readable, multi-line string representation of this dashboard entry.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (
      "class GroupDashboardEntry {\n" +
      "    date: " +
      toIndentedString(date) +
      "\n" +
      "    groupId: " +
      toIndentedString(groupId) +
      "\n" +
      "    news: " +
      toIndentedString(news) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert
   * @return the indented string representation, or {@code "null"} if the object is {@code null}
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
