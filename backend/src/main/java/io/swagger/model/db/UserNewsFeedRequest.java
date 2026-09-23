/**
 *
 */
package io.swagger.model.db;

import java.util.List;

/**
 * Request payload used to query a user's activity news feed.
 *
 * <p>It carries the filtering criteria applied when retrieving news feed entries for a user: the
 * Interest Groups to include, the activity types to include, and the time window to consider.
 *
 * @author beaurpi
 */
public class UserNewsFeedRequest {

  /** Identifiers of the Interest Groups whose activities should be included in the feed. */
  private List<Long> igIds;

  /** Identifiers of the activity types to include when filtering the feed. */
  private List<Long> activityIds;

  /** Time window to filter the feed by; expected values are {@code today}, {@code week} or {@code previousWeek}. */
  private String when; // values should be today / week / previousWeek

  /** @return the igIds */
  public List<Long> getIgIds() {
    return igIds;
  }

  /** @param igIds the igIds to set */
  public void setIgIds(List<Long> igIds) {
    this.igIds = igIds;
  }

  /** @return the activityIds */
  public List<Long> getActivityIds() {
    return activityIds;
  }

  /** @param activityIds the activityIds to set */
  public void setActivityIds(List<Long> activityIds) {
    this.activityIds = activityIds;
  }

  /** @return the when */
  public String getWhen() {
    return when;
  }

  /** @param when the when to set */
  public void setWhen(String when) {
    this.when = when;
  }
}
