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
 * Default implementation of {@link AuthorityNotification}.
 *
 * <p>This immutable value object holds the notification configuration of a single authority (a user
 * or a group) on a node: the notification status, the authority identifier, the derived authority
 * type, and whether the setting is inherited from a parent node. The authority type is resolved
 * automatically from the supplied authority name via
 * {@link AuthorityType#getAuthorityType(String)}.
 *
 * @author Yanick Pignot
 */
public class AuthorityNotificationImpl implements AuthorityNotification {

  /** The notification status configured for the authority. */
  private NotificationStatus notificationStatus;

  /** The identifier of the authority (for example a username or a group name). */
  private String authority;

  /** The type of authority, derived from {@link #authority}. */
  private AuthorityType authorityType;

  /** Whether the notification setting is inherited from a parent node. */
  private boolean isInherited;

  /**
   * Creates a notification entry for an authority, explicitly indicating whether the setting is
   * inherited from a parent node. The authority type is derived from the given authority name.
   *
   * @param notificationStatus the notification status configured for the authority
   * @param authority the authority identifier (for example a username or a group name)
   * @param isInheritedFromParent {@code true} if the notification setting is inherited from a parent
   *     node, {@code false} if it is defined directly on the node
   */
  public AuthorityNotificationImpl(
    NotificationStatus notificationStatus,
    String authority,
    boolean isInheritedFromParent
  ) {
    super();
    this.notificationStatus = notificationStatus;
    this.authority = authority;
    this.authorityType = AuthorityType.getAuthorityType(authority);
    this.isInherited = isInheritedFromParent;
  }

  /**
   * Creates a notification entry for an authority that is defined directly on the node (that is, not
   * inherited from a parent). The authority type is derived from the given authority name.
   *
   * @param notificationStatus the notification status configured for the authority
   * @param authority the authority identifier (for example a username or a group name)
   */
  public AuthorityNotificationImpl(
    NotificationStatus notificationStatus,
    String authority
  ) {
    super();
    this.notificationStatus = notificationStatus;
    this.authority = authority;
    this.authorityType = AuthorityType.getAuthorityType(authority);
    this.isInherited = false;
  }

  /**
   * {@inheritDoc}
   *
   * @return the {@link NotificationStatus} configured for this authority
   */
  public NotificationStatus getNotificationStatus() {
    return notificationStatus;
  }

  /**
   * {@inheritDoc}
   *
   * @return the authority identifier (for example a username or a group name)
   */
  public String getAuthority() {
    return authority;
  }

  /**
   * {@inheritDoc}
   *
   * @return the {@link AuthorityType} derived from the authority identifier
   */
  public AuthorityType getAuthorityType() {
    return authorityType;
  }

  /**
   * Returns a human-readable representation combining the notification status, the authority
   * identifier and the authority type.
   *
   * @return a string describing this notification entry
   */
  @Override
  public String toString() {
    return (
      notificationStatus +
      " " +
      this.authority +
      " (" +
      this.authorityType +
      ")"
    );
  }

  /**
   * Compares this notification entry to another object for equality. Two
   * {@code AuthorityNotificationImpl} instances are considered equal when they share the same
   * notification status and refer to the same authority identifier.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an {@link AuthorityNotification} with the same
   *     notification status and authority, {@code false} otherwise
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AuthorityNotificationImpl)) {
      return false;
    }
    final AuthorityNotification other = (AuthorityNotification) o;
    return (
      this.getNotificationStatus() == other.getNotificationStatus() &&
      this.getAuthority().equals(other.getAuthority())
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, computed from the authority
   * identifier and the notification status.
   *
   * @return the hash code for this notification entry
   */
  @Override
  public int hashCode() {
    return (authority.hashCode() * 37) + notificationStatus.hashCode();
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code true} if the notification setting is inherited from a parent node, {@code false}
   *     if it is defined directly on the node
   */
  @Override
  public boolean getInherited() {
    return isInherited;
  }
}
