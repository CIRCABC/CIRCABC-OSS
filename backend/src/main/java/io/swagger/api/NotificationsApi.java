package io.swagger.api;

import io.swagger.model.NotificationDefinition;
import io.swagger.model.NotificationStatus;
import io.swagger.model.PagedNotificationConfigurations;
import io.swagger.model.PagedNotificationSubscribedUsers;
import io.swagger.model.PasteNotificationsState;

/**
 * Business operations for managing node-level notification settings and
 * subscriptions.
 *
 * <p>This interface defines the notification-related operations exposed by the
 * CIRCABC REST layer. Implementations encapsulate the logic for subscribing
 * and unsubscribing authorities (users or groups) to notifications on a given
 * repository node, reading and updating a node's notification definition,
 * controlling the propagation of notification settings during paste/copy
 * operations, and querying the notifiable users and notification
 * configurations of a node.
 *
 * <p>Throughout this interface the {@code id} parameter refers to the
 * identifier of the target Alfresco node the notification settings apply to.
 * The corresponding webscript endpoint classes in
 * {@code eu.europa.ec.digit.circabc.rest} delegate to an implementation of
 * this interface.
 */
public interface NotificationsApi {
  /**
   * Subscribes an authority to notifications on a node, or updates its
   * notification value.
   *
   * @param id the identifier of the target node
   * @param authority the authority (user or group) to configure notifications
   *     for
   * @param value the notification value/level to assign to the authority
   */
  void nodesIdNotificationsAuthorityPut(
    String id,
    String authority,
    String value
  );

  /**
   * Removes an authority's notification subscription from a node.
   *
   * @param id the identifier of the target node
   * @param authority the authority (user or group) whose notification
   *     subscription is removed
   */
  void nodesIdNotificationsAuthorityDelete(String id, String authority);

  /**
   * Retrieves the notification definition of a node.
   *
   * @param id the identifier of the target node
   * @return the notification definition currently configured on the node
   */
  NotificationDefinition nodesIdNotificationsGet(String id);

  /**
   * Creates or updates the notification definition of a node.
   *
   * @param id the identifier of the target node
   * @param body the notification definition to apply to the node
   * @return the resulting notification definition after the update
   */
  NotificationDefinition nodesIdNotificationsPost(
    String id,
    NotificationDefinition body
  );

  /**
   * Retrieves the paste notification state of a node, indicating how
   * notification settings are propagated on paste/copy operations.
   *
   * @param id the identifier of the target node
   * @return the current paste notification state of the node
   */
  PasteNotificationsState getPasteNotificationsState(String id);

  /**
   * Updates the paste notification state of a node, controlling how
   * notification settings are propagated on paste/copy operations.
   *
   * @param id the identifier of the target node
   * @param pasteEnable whether notification settings are propagated on paste
   * @param pasteAllEnable whether all notification settings are propagated on
   *     paste
   */
  void setPasteNotificationsState(
    String id,
    boolean pasteEnable,
    boolean pasteAllEnable
  );

  /**
   * Retrieves a paged list of notification configurations for a node.
   *
   * @param id the identifier of the target node
   * @param startItem the zero-based index of the first item to return
   * @param limit the maximum number of items to return
   * @param language the language used to localise the returned configurations
   * @return a paged collection of notification configurations
   */
  PagedNotificationConfigurations getNotifications(
    String id,
    int startItem,
    int limit,
    String language
  );

  /**
   * Retrieves a paged list of users who can be notified for a node.
   *
   * @param id the identifier of the target node
   * @param startItem the zero-based index of the first item to return
   * @param limit the maximum number of items to return
   * @return a paged collection of notifiable users
   */
  PagedNotificationSubscribedUsers getNotifiableUsers(
    String id,
    int startItem,
    int limit
  );

  /**
   * Removes the notification subscription of an authority on a node.
   *
   * @param id the identifier of the target node
   * @param authority the authority (user or group) whose notification is
   *     removed
   */
  void removeNotification(String id, String authority);

  /**
   * Sets the notification status of an authority on a node.
   *
   * @param id the identifier of the target node
   * @param authority the authority (user or group) whose notification status
   *     is updated
   * @param status the new notification status to apply
   */
  void setNotificationStatus(
    String id,
    String authority,
    NotificationStatus status
  );

  /**
   * Determines whether a user is subscribed for notifications on a node.
   *
   * @param id the identifier of the target node
   * @param userId the identifier of the user to check
   * @return {@code true} if the user is subscribed for notifications,
   *     {@code false} otherwise
   */
  boolean isUsersubscribedForNotification(String id, String userId);

  /**
   * Retrieves a paged, filtered list of users who can be notified for a node.
   *
   * @param id the identifier of the target node
   * @param startRecord the zero-based index of the first record to return
   * @param limit the maximum number of records to return
   * @param userName filter on the users' user name
   * @param firstName filter on the users' first name
   * @param lastName filter on the users' last name
   * @param email filter on the users' email address
   * @return a paged collection of notifiable users matching the given filters
   */
  PagedNotificationSubscribedUsers getNotifiableUsers(
    String id,
    int startRecord,
    int limit,
    String userName,
    String firstName,
    String lastName,
    String email
  );

  /**
   * Retrieves a paged, filtered list of notification configurations for a node.
   *
   * @param id the identifier of the target node
   * @param startRecord the zero-based index of the first record to return
   * @param limit the maximum number of records to return
   * @param language the language used to localise the returned configurations
   * @param queryType filter on the configuration/notification type
   * @param queryUserName filter on the subscribed user's user name
   * @param queryStatus filter on the notification status
   * @return a paged collection of notification configurations matching the
   *     given filters
   */
  PagedNotificationConfigurations getNotifications(
    String id,
    int startRecord,
    int limit,
    String language,
    String queryType,
    String queryUserName,
    String queryStatus
  );
}
