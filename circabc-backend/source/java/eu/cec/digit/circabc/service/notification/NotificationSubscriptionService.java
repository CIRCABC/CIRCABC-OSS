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

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Set;

/**
 * Low level service that manage
 *
 * @author yanick pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface NotificationSubscriptionService {

    /**
     * Get the AuthorityNotification that is granted/denied to the given authority
     *
     * @param nodeRef   - the reference to the node
     * @param authority - the authority that match the permission
     * @return notification status
     * @throws InvalidNodeRefException error if the given node is invalid or not under an
     *                                 InterestGroup
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "authority"})
    AuthorityNotification getAuthorityNotificationStatus(
            final NodeRef nodeRef, final String authority) throws InvalidNodeRefException;

    /**
     * Get all the notifiable users. The process compute the notification status from the given node
     * to its interest group.
     *
     * @param nodeRef the from from which the status will be computed (can be any kind of node -
     *                space, content, topic, ....)
     * @return the set of users to notify
     * @throws InvalidNodeRefException error if the given node is invalid or not under an
     *                                 InterestGroup
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef"})
    Set<NotifiableUser> getNotifiableUsers(final NodeRef nodeRef) throws InvalidNodeRefException;

    /**
     * Get all the AuthorityNotification that are granted/denied to the current authentication for the
     * given node
     *
     * @param nodeRef - the reference to the node
     * @return the set of notification status
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef"})
    Set<AuthorityNotification> getNotifications(final NodeRef nodeRef);

    /**
     * Get the notification report of a given user authority for a given node
     *
     * @param nodeRef   - the reference to the node
     * @param authority - the authority that match the permission
     * @return the full report
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "userAuthority"})
    UserNotificationReport getUserNotificationReport(
            final NodeRef nodeRef, final String userAuthority);

    /**
     * Remove the notification settings of the given authority on the current node.
     *
     * @param nodeRef   where the notification status settings will removed
     * @param authority who losts the notification status setting
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "authority"})
    void removeNotification(final NodeRef nodeRef, final String authority);

    /**
     * Set a specific notification status on a node.
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "authority", "status"})
    void setNotificationStatus(
            final NodeRef nodeRef, final String authority, final NotificationStatus status);
}
