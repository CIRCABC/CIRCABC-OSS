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
package eu.cec.digit.circabc.service.notification;

/** @author beaurpi */
public enum NotificationType {

    /*
     * maybe to use in future this classe to use for notification service and better notification management
     * purpose is to specify in the notify method the type of notification
     * following the type we use one template
     */

    NOTIFY_CONTENT_UPLOAD,
    NOTIFY_CONTENT_UPDATE,
    NOTIFY_USER_INVITATION,
    NOTIFY_USER_INVITATION_ADMINS,
    NOTIFY_USER_MEMBERSHIP_UPDATE
}
