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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The interface used to support reporting back the entire Notification Satus Report of an user.
 *
 * @author Yanick Pignot
 */
public interface UserNotificationReport {

    NodeRef getLocation();

    /**
     * Get the Global Notification Status enumeration value setted as the user preferences
     */
    GlobalNotificationStatus getGlobalNotificationStatus();

    /**
     * Get the Notification Status enumeration setted for the user's profile for the current location
     */
    NotificationStatus getProfileNotificationStatus();

    /**
     * Get the Notification Status enumeration setted for the specific user for the current location
     */
    NotificationStatus getUserNotificationStatus();

    /**
     * Return true if the Notification Statuses allow the user to receive a Notification
     */
    boolean isUserNotifiable();

    /**
     * Get the user authority to which this notification applies.
     */
    String getUserAuthority();

    /**
     * Get the user authority profile to which this notification applies.
     */
    String getUserProfile();
}
