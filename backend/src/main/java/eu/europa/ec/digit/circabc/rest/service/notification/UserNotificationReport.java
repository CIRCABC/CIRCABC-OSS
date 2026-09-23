/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.notification;

import io.swagger.model.NotificationStatus;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Read-only view that aggregates the complete notification status report for a single user at a
 * given repository location.
 *
 * <p>A notification report combines the different levels of notification configuration that apply
 * to a user for a specific node (location): the user's global preference, the notification status
 * inherited from the user's profile, and any notification status set explicitly for the user. From
 * these, it also derives whether the user should ultimately receive a notification.
 *
 * @author Yanick Pignot
 */
public interface UserNotificationReport {
  /**
   * Gets the repository location (node) this notification report relates to.
   *
   * @return the {@link NodeRef} of the location the report was computed for
   */
  NodeRef getLocation();

  /**
   * Gets the global notification status set as the user's preference.
   *
   * @return the {@link GlobalNotificationStatus} configured as the user's global preference
   */
  GlobalNotificationStatus getGlobalNotificationStatus();

  /**
   * Gets the notification status set for the user's profile at the current location.
   *
   * @return the {@link NotificationStatus} derived from the user's profile for this location
   */
  NotificationStatus getProfileNotificationStatus();

  /**
   * Gets the notification status set specifically for this user at the current location.
   *
   * @return the {@link NotificationStatus} set explicitly for the user at this location
   */
  NotificationStatus getUserNotificationStatus();

  /**
   * Indicates whether the combined notification statuses allow the user to receive a notification.
   *
   * @return {@code true} if the user is eligible to receive a notification, {@code false} otherwise
   */
  boolean isUserNotifiable();

  /**
   * Gets the user authority (identifier) this notification report applies to.
   *
   * @return the user authority the report applies to
   */
  String getUserAuthority();

  /**
   * Gets the user authority profile this notification report applies to.
   *
   * @return the profile associated with the user for this report
   */
  String getUserProfile();
}
