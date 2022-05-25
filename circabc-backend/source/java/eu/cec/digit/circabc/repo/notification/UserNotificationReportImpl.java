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
package eu.cec.digit.circabc.repo.notification;

import eu.cec.digit.circabc.service.notification.GlobalNotificationStatus;
import eu.cec.digit.circabc.service.notification.NotificationStatus;
import eu.cec.digit.circabc.service.notification.UserNotificationReport;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The interface used to support reporting back the entire Notification Satus Report of an user.
 *
 * @author Yanick Pignot
 */
public class UserNotificationReportImpl implements UserNotificationReport {

    private NodeRef location;
    private boolean globalNotificationStatus;
    private String user;
    private String profile;

    private NotificationStatus profileNotificationStatus;
    private NotificationStatus userNotificationStatus;
    private boolean computedNotificationSatus;

    /*package*/ UserNotificationReportImpl(
            final NodeRef location,
            final String user,
            final String profile,
            final Boolean globalNotificationStatus,
            final NotificationStatus userNotificationStatus,
            final NotificationStatus profileNotificationStatus,
            final boolean willRecieve) {
        this.location = location;
        this.userNotificationStatus = userNotificationStatus;
        this.profileNotificationStatus = profileNotificationStatus;

        this.user = user;
        this.profile = profile;
        // for admin, the global notification status can be null, keep the willReceive value that is the
        // result of the service computing.
        this.globalNotificationStatus =
                (globalNotificationStatus == null) ? willRecieve : globalNotificationStatus;
        this.computedNotificationSatus = willRecieve;
    }

    /**
     * @return the authority
     */
    public final String getUserAuthority() {
        return user;
    }

    /**
     * @return if the user will receive a notification
     */
    public final boolean isUserNotifiable() {
        return computedNotificationSatus;
    }

    /**
     * @return the globalNotificationSatus
     */
    public final GlobalNotificationStatus getGlobalNotificationStatus() {
        if (globalNotificationStatus) {
            return GlobalNotificationStatus.ENABLED;
        } else {
            return GlobalNotificationStatus.DISABLED;
        }
    }

    /**
     * @return the location
     */
    public final NodeRef getLocation() {
        return location;
    }

    /**
     * @return the locationNotificationSatus
     */
    public final NotificationStatus getUserNotificationStatus() {
        return userNotificationStatus;
    }

    /**
     * @return the profileNotificationSatus
     */
    public final NotificationStatus getProfileNotificationStatus() {
        return profileNotificationStatus;
    }

    public String getUserProfile() {
        return profile;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
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

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return user
                + " ("
                + profile
                + " ) will "
                + ((computedNotificationSatus == false) ? "NOT " : "")
                + " receive notifictation on node "
                + location;
    }
}
