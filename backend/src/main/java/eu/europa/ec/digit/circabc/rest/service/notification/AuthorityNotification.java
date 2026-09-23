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
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Represents the notification configuration of a single authority (a user or a group) on a node.
 *
 * <p>Instances of this interface are used to report back the notification status for an authority,
 * describing whether that authority is subscribed to notifications, which authority it refers to,
 * the kind of authority it is, and whether the setting is inherited from a parent node rather than
 * defined directly on the node itself.
 *
 * @author Yanick Pignot
 */
public interface AuthorityNotification {
  /**
   * Returns the notification status configured for this authority.
   *
   * @return the {@link NotificationStatus} enumeration value describing the authority's
   *     notification state
   */
  NotificationStatus getNotificationStatus();

  /**
   * Returns the identifier of the authority to which this notification applies.
   *
   * @return the authority name (for example a username or a group name)
   */
  String getAuthority();

  /**
   * Returns the type of the authority to which this notification applies.
   *
   * @return the {@link AuthorityType} indicating whether the authority is a user, group, or other
   *     authority type
   */
  AuthorityType getAuthorityType();

  /**
   * Indicates whether this notification setting is inherited from a parent node.
   *
   * @return {@code true} if the notification is inherited from a parent node, {@code false} if it
   *     is defined directly on the node
   */
  boolean getInherited();
}
