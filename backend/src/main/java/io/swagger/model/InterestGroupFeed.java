package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object representing the activity feed of an Interest Group.
 *
 * <p>An {@code InterestGroupFeed} bundles the identifying information of an
 * Interest Group (its id, name and localized title) together with the
 * chronological list of {@link UserActionLog} entries that make up the group's
 * activity feed. It is serialized to JSON as part of the CIRCABC REST API
 * responses.
 */
public class InterestGroupFeed {

  /** Unique identifier (Alfresco node reference) of the Interest Group. */
  private String id = null;

  /** Machine/short name of the Interest Group. */
  private String name = null;

  /** Localized (i18n) display title of the Interest Group. */
  private I18nProperty title = null;

  /** Chronological list of user action log entries composing the feed. */
  private List<UserActionLog> feed = new ArrayList<>();

  /**
   * Get id
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the Interest Group.
   *
   * @param id the Interest Group identifier to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get name
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the Interest Group.
   *
   * @param name the Interest Group name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get title
   *
   * @return title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * Sets the localized display title of the Interest Group.
   *
   * @param title the i18n title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Get feed
   *
   * @return feed
   */
  public List<UserActionLog> getFeed() {
    return feed;
  }

  /**
   * Sets the list of user action log entries composing the feed.
   *
   * @param feed the feed entries to set
   */
  public void setFeed(List<UserActionLog> feed) {
    this.feed = feed;
  }

  /**
   * Compares this feed with another object for equality based on all fields
   * (id, name, title and feed entries).
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an {@code InterestGroupFeed}
   *     with equal field values, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InterestGroupFeed interestGroupFeed = (InterestGroupFeed) o;
    return (
      Objects.equals(this.id, interestGroupFeed.id) &&
      Objects.equals(this.name, interestGroupFeed.name) &&
      Objects.equals(this.title, interestGroupFeed.title) &&
      Objects.equals(this.feed, interestGroupFeed.feed)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from
   * all fields.
   *
   * @return the hash code for this feed
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, title, feed);
  }

  /**
   * Returns a human-readable, multi-line string representation of this feed.
   *
   * @return a string describing all field values
   */
  @Override
  public String toString() {
    return (
      "class InterestGroupFeed {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    name: " +
      toIndentedString(name) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    feed: " +
      toIndentedString(feed) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert
   * @return the indented string representation of {@code o}
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
