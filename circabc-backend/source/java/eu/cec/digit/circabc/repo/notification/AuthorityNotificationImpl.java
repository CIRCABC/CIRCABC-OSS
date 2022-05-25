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

import eu.cec.digit.circabc.service.notification.AuthorityNotification;
import eu.cec.digit.circabc.service.notification.NotificationStatus;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * The interface used to support reporting back the notification status.
 *
 * @author Yanick Pignot
 */
public class AuthorityNotificationImpl implements AuthorityNotification {

    private NotificationStatus notificationStatus;
    private String authority;
    private AuthorityType authorityType;
    private boolean isInherited;

    /**
     * @param notificationStatus
     * @param authority
     * @param isInheritedFromParent
     */
    public AuthorityNotificationImpl(
            NotificationStatus notificationStatus, String authority, boolean isInheritedFromParent) {
        super();
        this.notificationStatus = notificationStatus;
        this.authority = authority;
        this.authorityType = AuthorityType.getAuthorityType(authority);
        this.isInherited = isInheritedFromParent;
    }

    public AuthorityNotificationImpl(NotificationStatus notificationStatus, String authority) {
        super();
        this.notificationStatus = notificationStatus;
        this.authority = authority;
        this.authorityType = AuthorityType.getAuthorityType(authority);
        this.isInherited = false;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public String getAuthority() {
        return authority;
    }

    public AuthorityType getAuthorityType() {
        return authorityType;
    }

    @Override
    public String toString() {
        return notificationStatus + " " + this.authority + " (" + this.authorityType + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthorityNotificationImpl)) {
            return false;
        }
        final AuthorityNotification other = (AuthorityNotification) o;
        return this.getNotificationStatus() == other.getNotificationStatus()
                && this.getAuthority().equals(other.getAuthority());
    }

    @Override
    public int hashCode() {
        return (authority.hashCode() * 37) + notificationStatus.hashCode();
    }

    @Override
    public boolean getInherited() {
        return isInherited;
    }
}
