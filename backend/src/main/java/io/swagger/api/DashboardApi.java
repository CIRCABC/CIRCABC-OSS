package io.swagger.api;

import io.swagger.model.UserActionLog;
import io.swagger.model.UserNewsFeed;
import java.util.List;

/**
 * Business operations backing the user dashboard REST endpoints.
 *
 * <p>Implementations expose the activity data shown on a user's personal dashboard: the documents
 * the user has downloaded, the documents the user has uploaded, and a personalized news feed
 * aggregating recent activity relevant to the user.
 *
 * @author beaurpi
 */
public interface DashboardApi {
  /**
   * Retrieves the log of documents downloaded by the given user, used to populate the "downloads"
   * section of the user's dashboard.
   *
   * @param userId the identifier of the user whose download history is requested
   * @return the list of {@link UserActionLog} entries describing the user's downloads
   */
  List<UserActionLog> usersUserIdDashboardDownloadsGet(String userId);

  /**
   * Retrieves the log of documents uploaded by the given user, used to populate the "uploads"
   * section of the user's dashboard.
   *
   * @param userId the identifier of the user whose upload history is requested
   * @return the list of {@link UserActionLog} entries describing the user's uploads
   */
  List<UserActionLog> usersUserIdDashboardUploadsGet(String userId);

  /**
   * Retrieves the personalized news feed for the given user, aggregating recent activity relevant
   * to the user's dashboard.
   *
   * @param userId the identifier of the user whose news feed is requested
   * @param when an optional time reference constraining the news feed (e.g. the point in time from
   *     which activity should be reported)
   * @return the {@link UserNewsFeed} aggregating the user's relevant recent activity
   */
  UserNewsFeed usersUserIdDashboardNewsfeedGet(String userId, String when);
}
