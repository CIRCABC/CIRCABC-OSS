package io.swagger.api;

import io.swagger.model.UserActionLog;
import io.swagger.model.UserNewsFeed;

import java.util.List;

/**
 * @author beaurpi
 */
public interface DashboardApi {

    List<UserActionLog> usersUserIdDashboardDownloadsGet(String userId);

    List<UserActionLog> usersUserIdDashboardUploadsGet(String userId);

    UserNewsFeed usersUserIdDashboardNewsfeedGet(String userId, String when);
}
