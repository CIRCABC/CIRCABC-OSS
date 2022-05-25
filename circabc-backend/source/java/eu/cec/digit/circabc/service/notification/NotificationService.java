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

import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import io.swagger.model.AppMessage;
import io.swagger.model.UserProfile;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author filips
 * @author Yanick Pignot
 * @author beaurpi
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface NotificationService {

    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "users"})
    void notify(NodeRef nodeRef, Set<NotifiableUser> users) throws Exception;

    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "users", "notificationType"})
    void notify(NodeRef nodeRef, Set<NotifiableUser> users, NotificationType notificationType, String notificationText, Date expirationDate)
            throws Exception;

    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "users"})
    void notifyNewEdition(NodeRef nodeRef, Set<NotifiableUser> users) throws Exception;

    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "admins"})
    void notifyNewMemberships(NodeRef nodeRef, Set<NotifiableUser> admins, UserProfile newMember)
            throws Exception;

    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "admins"})
    void notifyUpdateMemberships(NodeRef nodeRef, Set<NotifiableUser> admins, UserProfile newMember)
            throws Exception;

    /**
     * * This method use only emails, there is not any locale feature for translation -> only in
     * english so
     *
     * @param nodeRef
     * @param mails
     * @param templateType
     * @throws Exception
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "mails", "templateType"})
    void notify(NodeRef nodeRef, List<String> mails, MailTemplate templateType) throws Exception;

    /**
     * * new method to notify the users of a multiple upload of files
     *
     * @param parentRef
     * @param nodeRefs
     * @param notifiableUsers
     * @param notifyDocBulk
     */
    void notifyNewFiles(
            NodeRef parentRef,
            List<NodeRef> nodeRefs,
            Set<NotifiableUser> notifiableUsers,
            MailTemplate notifyDocBulk);

    void notify(NodeRef nodeRef, List<String> mails, MailTemplate mailTemplate, List<File> files);

    void notifySystemMessage(List<String> mailAddress, AppMessage template);
}
