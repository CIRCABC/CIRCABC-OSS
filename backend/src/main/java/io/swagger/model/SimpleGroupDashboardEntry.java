package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model representing the dashboard activity feed for a single Interest Group.
 *
 * <p>It groups all the activities that occurred inside one group into a flat list of {@link
 * EntryEvent} items, sorted by date. The content is expected to be filtered according to the
 * requesting user's permissions so that only activities the user is allowed to see are exposed.
 * Used to render the "news" section of a group dashboard.
 */
public class SimpleGroupDashboardEntry {

  /** Identifier of the Interest Group these dashboard activities belong to. */
  private String groupId = null;

  /** Activity events for the group, expected to be sorted by date. */
  private List<EntryEvent> entries = new ArrayList<>();

  /**
   * Returns the identifier of the Interest Group.
   *
   * @return the group id
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Sets the identifier of the Interest Group.
   *
   * @param groupId the group id to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Returns the list of activity events for the group.
   *
   * @return the activity entries, expected to be sorted by date
   */
  public List<EntryEvent> getEntries() {
    return entries;
  }

  /**
   * Sets the list of activity events for the group.
   *
   * @param entries the activity entries to set
   */
  public void setEntries(List<EntryEvent> entries) {
    this.entries = entries;
  }

  /**
   * Compares this dashboard entry with another object for equality.
   *
   * <p>Two {@code SimpleGroupDashboardEntry} instances are equal when they share the same group id
   * and the same list of entries.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code SimpleGroupDashboardEntry}, {@code
   *     false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SimpleGroupDashboardEntry simpleGroupDashboardEntry =
      (SimpleGroupDashboardEntry) o;
    return (
      Objects.equals(this.groupId, simpleGroupDashboardEntry.groupId) &&
      Objects.equals(this.entries, simpleGroupDashboardEntry.entries)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, based on the group id and entries.
   *
   * @return the hash code for this dashboard entry
   */
  @Override
  public int hashCode() {
    return Objects.hash(groupId, entries);
  }

  /**
   * Returns a human-readable representation of this dashboard entry, with its fields indented.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (
      "class SimpleGroupDashboardEntry {\n" +
      "    groupId: " +
      toIndentedString(groupId) +
      "\n" +
      "    entries: " +
      toIndentedString(entries) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
