package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.joda.time.DateTime;

/**
 * Domain model that aggregates the dashboard activity of a single user for a specific day.
 *
 * <p>An entry contains the information related to all activities linked to the memberships of one
 * user, grouped by date. Each entry holds the day it refers to and the list of per-group activity
 * summaries ({@link SimpleGroupDashboardEntry}) that occurred on that day. A user dashboard is
 * typically represented as a collection of these entries, one per relevant date.
 */
public class UserDashboardEntry {

  /** The date to which all the grouped activities in this entry belong. */
  private DateTime date = null;

  /** The per-group activity summaries that occurred on {@link #date}. */
  private List<SimpleGroupDashboardEntry> groups = new ArrayList<>();

  /**
   * Get date
   *
   * @return date
   */
  public DateTime getDate() {
    return date;
  }

  /**
   * Sets the date to which all the grouped activities in this entry belong.
   *
   * @param date the date of the activities
   */
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

  /**
   * Sets the per-group activity summaries associated with this entry's date.
   *
   * @param groups the list of per-group dashboard entries
   */
  public void setGroups(List<SimpleGroupDashboardEntry> groups) {
    this.groups = groups;
  }

  /**
   * Compares this entry with another object for equality. Two entries are considered equal when
   * they share the same date and the same list of group activity summaries.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code UserDashboardEntry}, {@code false}
   *     otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserDashboardEntry userDashboardEntry = (UserDashboardEntry) o;
    return (
      Objects.equals(this.date, userDashboardEntry.date) &&
      Objects.equals(this.groups, userDashboardEntry.groups)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}, derived from the date
   * and the group activity summaries.
   *
   * @return the hash code for this entry
   */
  @Override
  public int hashCode() {
    return Objects.hash(date, groups);
  }

  /**
   * Returns a human-readable, multi-line string representation of this entry, listing its date and
   * groups.
   *
   * @return a string representation of this entry
   */
  @Override
  public String toString() {
    return (
      "class UserDashboardEntry {\n" +
      "    date: " +
      toIndentedString(date) +
      "\n" +
      "    groups: " +
      toIndentedString(groups) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to render
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
