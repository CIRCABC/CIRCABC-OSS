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
 * Default implementation of {@link UserNotificationReport}.
 *
 * <p>This immutable value object aggregates the complete notification status report for a single
 * user at a given repository location. It combines the user-specific notification setting, the
 * setting inherited from the user's profile, the global notification status and the effective
 * (computed) status that determines whether the user will actually receive a notification.
 *
 * @author Yanick Pignot
 */
public class UserNotificationReportImpl implements UserNotificationReport {

  /** The repository node the notification report relates to. */
  private NodeRef location;

  /**
   * The effective global notification status; when no explicit value is supplied it falls back to
   * the computed notification status.
   */
  private boolean globalNotificationStatus;

  /** The authority (username) the report describes. */
  private String user;

  /** The name of the profile the user belongs to. */
  private String profile;

  /** The notification status inherited from the user's profile. */
  private NotificationStatus profileNotificationStatus;

  /** The notification status configured specifically for the user. */
  private NotificationStatus userNotificationStatus;

  /**
   * The computed notification status, i.e. the result of the service resolving whether the user
   * will actually receive a notification.
   */
  private boolean computedNotificationSatus;

  /**
   * Creates a notification report for a single user at a given location.
   *
   * <p>For an administrator the {@code globalNotificationStatus} may be {@code null}; in that case
   * the {@code willRecieve} value produced by the service computation is used instead.
   *
   * @param location the repository node the report relates to
   * @param user the authority (username) the report describes
   * @param profile the name of the profile the user belongs to
   * @param globalNotificationStatus the explicit global notification status, or {@code null} to
   *     fall back to the computed {@code willRecieve} value
   * @param userNotificationStatus the notification status configured specifically for the user
   * @param profileNotificationStatus the notification status inherited from the user's profile
   * @param willRecieve the computed status indicating whether the user will receive a notification
   */
  /*package*/ UserNotificationReportImpl(
    final NodeRef location,
    final String user,
    final String profile,
    final Boolean globalNotificationStatus,
    final NotificationStatus userNotificationStatus,
    final NotificationStatus profileNotificationStatus,
    final boolean willRecieve
  ) {
    this.location = location;
    this.userNotificationStatus = userNotificationStatus;
    this.profileNotificationStatus = profileNotificationStatus;

    this.user = user;
    this.profile = profile;
    // for admin, the global notification status can be null, keep the willReceive value that is the
    // result of the service computing.
    this.globalNotificationStatus = (globalNotificationStatus == null)
      ? willRecieve
      : globalNotificationStatus;
    this.computedNotificationSatus = willRecieve;
  }

  /**
   * Returns the authority (username) described by this report.
   *
   * @return the authority
   */
  public final String getUserAuthority() {
    return user;
  }

  /**
   * Indicates whether, according to the computed status, the user will receive a notification.
   *
   * @return {@code true} if the user will receive a notification, {@code false} otherwise
   */
  public final boolean isUserNotifiable() {
    return computedNotificationSatus;
  }

  /**
   * Returns the global notification status as an enumerated value.
   *
   * @return {@link GlobalNotificationStatus#ENABLED} when notifications are globally enabled,
   *     {@link GlobalNotificationStatus#DISABLED} otherwise
   */
  public final GlobalNotificationStatus getGlobalNotificationStatus() {
    if (globalNotificationStatus) {
      return GlobalNotificationStatus.ENABLED;
    } else {
      return GlobalNotificationStatus.DISABLED;
    }
  }

  /**
   * Returns the repository node the report relates to.
   *
   * @return the location
   */
  public final NodeRef getLocation() {
    return location;
  }

  /**
   * Returns the notification status configured specifically for the user.
   *
   * @return the user notification status
   */
  public final NotificationStatus getUserNotificationStatus() {
    return userNotificationStatus;
  }

  /**
   * Returns the notification status inherited from the user's profile.
   *
   * @return the profile notification status
   */
  public final NotificationStatus getProfileNotificationStatus() {
    return profileNotificationStatus;
  }

  /**
   * Returns the name of the profile the user belongs to.
   *
   * @return the user profile name
   */
  public String getUserProfile() {
    return profile;
  }

  /**
   * Computes a hash code based on the user, computed notification status, location and profile.
   *
   * @return the hash code for this report
   */
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((user == null) ? 0 : user.hashCode());
    result = PRIME * result + (computedNotificationSatus ? 1231 : 1237);
    result = PRIME * result + ((location == null) ? 0 : location.hashCode());
    result = PRIME * result + ((profile == null) ? 0 : profile.hashCode());
    return result;
  }

  /**
   * Compares this report to another for equality based on the user, computed notification status,
   * location and profile.
   *
   * @param obj the object to compare with
   * @return {@code true} if the given object is an equivalent report, {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final UserNotificationReportImpl other = (UserNotificationReportImpl) obj;
    if (user == null) {
      if (other.user != null) {
        return false;
      }
    } else if (!user.equals(other.user)) {
      return false;
    }
    if (computedNotificationSatus != other.computedNotificationSatus) {
      return false;
    }
    if (location == null) {
      if (other.location != null) {
        return false;
      }
    } else if (!location.equals(other.location)) {
      return false;
    }
    if (profile == null) {
      if (other.profile != null) {
        return false;
      }
    } else if (!profile.equals(other.profile)) {
      return false;
    }
    return true;
  }

  /**
   * Returns a human-readable description of the report, summarising whether the user will receive a
   * notification on the target node.
   *
   * @return a string representation of this report
   */
  @Override
  public String toString() {
    return (
      user +
      " (" +
      profile +
      " ) will " +
      ((!computedNotificationSatus) ? "NOT " : "") +
      " receive notifictation on node " +
      location
    );
  }
}
