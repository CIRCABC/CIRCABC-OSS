package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object summarising the activity news feed for a single user.
 *
 * <p>It aggregates, over a given time window ({@link WhenEnum}), the total counts
 * of new content activity across all Interest Groups the user belongs to, and
 * carries the per-Interest-Group breakdown in {@link #groupFeeds}. This model is
 * serialised to JSON and returned by the CIRCABC REST API.</p>
 */
public class UserNewsFeed {

  /** Time window the aggregated counts refer to (today, current week, previous week). */
  private WhenEnum when = null;

  /** Total number of newly uploaded content items within the time window. */
  private Long uploads = null;

  /** Total number of content updates within the time window. */
  private Long updates = null;

  /** Total number of comments posted within the time window. */
  private Long comments = null;

  /** Per-Interest-Group breakdown of the activity that makes up the aggregated totals. */
  private List<InterestGroupFeed> groupFeeds = new ArrayList<>();

  /**
   * Returns the time window that the aggregated counts refer to.
   *
   * @return the time window, or {@code null} if not set
   */
  public WhenEnum getWhen() {
    return when;
  }

  /**
   * Sets the time window that the aggregated counts refer to.
   *
   * @param when the time window to set
   */
  public void setWhen(WhenEnum when) {
    this.when = when;
  }

  /**
   * Returns the total number of newly uploaded content items within the time window.
   *
   * @return the upload count, or {@code null} if not set
   */
  public Long getUploads() {
    return uploads;
  }

  /**
   * Sets the total number of newly uploaded content items within the time window.
   *
   * @param uploads the upload count to set
   */
  public void setUploads(Long uploads) {
    this.uploads = uploads;
  }

  /**
   * Returns the total number of content updates within the time window.
   *
   * @return the update count, or {@code null} if not set
   */
  public Long getUpdates() {
    return updates;
  }

  /**
   * Sets the total number of content updates within the time window.
   *
   * @param updates the update count to set
   */
  public void setUpdates(Long updates) {
    this.updates = updates;
  }

  /**
   * Returns the total number of comments posted within the time window.
   *
   * @return the comment count, or {@code null} if not set
   */
  public Long getComments() {
    return comments;
  }

  /**
   * Sets the total number of comments posted within the time window.
   *
   * @param comments the comment count to set
   */
  public void setComments(Long comments) {
    this.comments = comments;
  }

  /**
   * Returns the per-Interest-Group breakdown of the activity feed.
   *
   * @return the list of per-Interest-Group feeds (never {@code null} by default)
   */
  public List<InterestGroupFeed> getGroupFeeds() {
    return groupFeeds;
  }

  /**
   * Sets the per-Interest-Group breakdown of the activity feed.
   *
   * @param groupFeeds the list of per-Interest-Group feeds to set
   */
  public void setGroupFeeds(List<InterestGroupFeed> groupFeeds) {
    this.groupFeeds = groupFeeds;
  }

  /**
   * Compares this news feed with another object for equality. Two instances are
   * equal when all their fields (time window, counts and group feeds) are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object represents an equal news feed
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserNewsFeed userNewsFeed = (UserNewsFeed) o;
    return (
      Objects.equals(this.when, userNewsFeed.when) &&
      Objects.equals(this.uploads, userNewsFeed.uploads) &&
      Objects.equals(this.updates, userNewsFeed.updates) &&
      Objects.equals(this.comments, userNewsFeed.comments) &&
      Objects.equals(this.groupFeeds, userNewsFeed.groupFeeds)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code derived from all fields
   */
  @Override
  public int hashCode() {
    return Objects.hash(when, uploads, updates, comments, groupFeeds);
  }

  /**
   * Returns a human-readable, multi-line representation of this news feed,
   * primarily intended for debugging and logging.
   *
   * @return a string describing all field values
   */
  @Override
  public String toString() {
    return (
      "class UserNewsFeed {\n" +
      "    when: " +
      toIndentedString(when) +
      "\n" +
      "    uploads: " +
      toIndentedString(uploads) +
      "\n" +
      "    updates: " +
      toIndentedString(updates) +
      "\n" +
      "    comments: " +
      toIndentedString(comments) +
      "\n" +
      "    groupFeeds: " +
      toIndentedString(groupFeeds) +
      "\n" +
      "}"
    );
  }

  /**
   * Converts the given object to its string form with each line indented by
   * four spaces, except the first line.
   *
   * @param o the object to render (may be {@code null})
   * @return the indented string representation
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Enumerates the supported time windows for aggregating the user news feed.
   *
   * <p>Each constant is backed by the JSON string value used on the wire.</p>
   */
  public enum WhenEnum {
    TODAY("today"),

    WEEK("week"),

    PREVIOUSWEEK("previousWeek");

    /** The JSON string representation of this time window. */
    private final String value;

    WhenEnum(String value) {
      this.value = value;
    }

    /**
     * Resolves the enum constant matching the given JSON string value.
     *
     * @param text the JSON string value to look up
     * @return the matching constant, or {@code null} if none matches
     */
    public static WhenEnum fromValue(String text) {
      for (WhenEnum b : WhenEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    /**
     * Returns the JSON string value of this time window.
     *
     * @return the wire representation of this constant
     */
    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }
}
