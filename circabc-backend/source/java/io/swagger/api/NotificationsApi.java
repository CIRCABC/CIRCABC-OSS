package io.swagger.api;

import eu.cec.digit.circabc.service.notification.NotificationStatus;
import io.swagger.model.NotificationDefinition;
import io.swagger.model.PagedNotificationConfigurations;
import io.swagger.model.PagedNotificationSubscribedUsers;
import io.swagger.model.PasteNotificationsState;

public interface NotificationsApi {
  void nodesIdNotificationsAuthorityPut(
    String id,
    String authority,
    String value
  );

  void nodesIdNotificationsAuthorityDelete(String id, String authority);

  NotificationDefinition nodesIdNotificationsGet(String id);

  NotificationDefinition nodesIdNotificationsPost(
    String id,
    NotificationDefinition body
  );

  PasteNotificationsState getPasteNotificationsState(String id);

  void setPasteNotificationsState(
    String id,
    boolean pasteEnable,
    boolean pasteAllEnable
  );

  PagedNotificationConfigurations getNotifications(
    String id,
    int startItem,
    int limit,
    String language
  );

  PagedNotificationConfigurations getNotifications(
    String id,
    int startItem,
    int limit,
    String language,
    String queryType,
    String queryUserName,
    String queryStatus
  );

  PagedNotificationSubscribedUsers getNotifiableUsers(
    String id,
    int startItem,
    int limit
  );

  PagedNotificationSubscribedUsers getNotifiableUsers(
    String id,
    int startItem,
    int limit,
    String userName,
    String firstName,
    String lastName,
    String email
  );

  void removeNotification(String id, String authority);

  void setNotificationStatus(
    String id,
    String authority,
    NotificationStatus status
  );

  boolean isUsersubscribedForNotification(String id, String userId);
}
