/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.notification;

/**
 * Enumerates the categories of notifications that can be raised within the CIRCABC notification
 * service.
 *
 * <p>Each constant identifies a distinct kind of event (for example a content upload, a content
 * update or a membership change). The intended usage is to pass the appropriate value to the
 * notification service's notify operation so that the correct notification template and delivery
 * behaviour can be selected for the event being reported.
 *
 * @author beaurpi
 */
public enum NotificationType {
  /*
   * maybe to use in future this classe to use for notification service and better notification management
   * purpose is to specify in the notify method the type of notification
   * following the type we use one template
   */

  /** A new content item (e.g. a document) has been uploaded. */
  NOTIFY_CONTENT_UPLOAD,
  /** An existing content item has been updated. */
  NOTIFY_CONTENT_UPDATE,
  /** A user has been invited to join an interest group. */
  NOTIFY_USER_INVITATION,
  /** Administrators are notified about a user invitation. */
  NOTIFY_USER_INVITATION_ADMINS,
  /** A user's membership (e.g. profile or role) has been updated. */
  NOTIFY_USER_MEMBERSHIP_UPDATE,
}
