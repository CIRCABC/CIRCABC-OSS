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
package eu.cec.digit.circabc.service.notification;

import org.alfresco.service.cmr.security.AuthorityType;

/**
 * The interface used to support reporting back the notification status.
 *
 * @author Yanick Pignot
 */
public interface AuthorityNotification {

    /**
     * Get the Notification Status enumeration value
     */
    NotificationStatus getNotificationStatus();

    /**
     * Get the authority to which this notification applies.
     */
    String getAuthority();

    /**
     * Get the type of authority to which this notification applies.
     */
    AuthorityType getAuthorityType();

    /**
     * @return true if notification is inherited from parent node
     */
    boolean getInherited();
}
