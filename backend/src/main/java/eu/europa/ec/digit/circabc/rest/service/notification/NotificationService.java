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

import eu.europa.ec.digit.circabc.rest.exception.NotificationException;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import io.swagger.model.AppMessage;
import io.swagger.model.NotifiableUser;
import io.swagger.model.UserProfile;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service contract for sending CIRCABC notifications about repository events to users.
 *
 * <p>Implementations are responsible for turning repository changes (content creation, new
 * editions, membership changes, bulk uploads, ad-hoc mailings, system-wide messages) into the
 * appropriate outbound notifications. Depending on the method and the recipients' preferences a
 * notification may be delivered by e-mail and/or surfaced as an in-application message. Recipients
 * are identified either as {@link NotifiableUser} instances (carrying per-user notification
 * preferences and locale) or as raw e-mail addresses.
 *
 * <p>Most operations are annotated with {@link Auditable} so that Alfresco records the invocation
 * together with the relevant arguments.
 *
 * @author filips
 * @author Yanick Pignot
 * @author beaurpi
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface NotificationService {
  /**
   * Notifies the given users about a change on the specified node using the default notification
   * behaviour.
   *
   * @param nodeRef the repository node the notification relates to
   * @param users the set of users to be notified, carrying their notification preferences
   * @throws NotificationException if the notification cannot be prepared or dispatched
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = { "nodeRef", "users" }
  )
  void notify(NodeRef nodeRef, Set<NotifiableUser> users)
    throws NotificationException;

  /**
   * Notifies the given users about a change on the specified node using a specific notification
   * type, an explicit message and an optional expiration date.
   *
   * @param nodeRef the repository node the notification relates to
   * @param users the set of users to be notified, carrying their notification preferences
   * @param notificationType the type of notification driving the content and delivery channel
   * @param notificationText the free-text message to include in the notification
   * @param expirationDate the date after which the notification is no longer relevant, or
   *     {@code null} if it never expires
   * @throws NotificationException if the notification cannot be prepared or dispatched
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "nodeRef", "users", "notificationType",
    }
  )
  void notify(
    NodeRef nodeRef,
    Set<NotifiableUser> users,
    NotificationType notificationType,
    String notificationText,
    Date expirationDate
  ) throws NotificationException;

  /**
   * Notifies the given users that a new edition (new version) of the specified node is available.
   *
   * @param nodeRef the repository node for which a new edition has been created
   * @param users the set of users to be notified, carrying their notification preferences
   * @throws NotificationException if the notification cannot be prepared or dispatched
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = { "nodeRef", "users" }
  )
  void notifyNewEdition(NodeRef nodeRef, Set<NotifiableUser> users)
    throws NotificationException;

  /**
   * Notifies the administrators of an interest group that a new member has joined.
   *
   * @param nodeRef the repository node (e.g. the group) the membership change relates to
   * @param admins the set of administrator users to be notified
   * @param newMember the profile of the newly added member
   * @throws NotificationException if the notification cannot be prepared or dispatched
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = { "nodeRef", "admins" }
  )
  void notifyNewMemberships(
    NodeRef nodeRef,
    Set<NotifiableUser> admins,
    UserProfile newMember
  ) throws NotificationException;

  /**
   * Notifies the administrators of an interest group that an existing membership has been updated.
   *
   * @param nodeRef the repository node (e.g. the group) the membership change relates to
   * @param admins the set of administrator users to be notified
   * @param newMember the profile of the member whose membership was updated
   * @throws NotificationException if the notification cannot be prepared or dispatched
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = { "nodeRef", "admins" }
  )
  void notifyUpdateMemberships(
    NodeRef nodeRef,
    Set<NotifiableUser> admins,
    UserProfile newMember
  ) throws NotificationException;

  /**
   * Sends an e-mail notification to a list of raw e-mail addresses using the given mail template.
   *
   * <p>This method only uses e-mails; there is no locale feature for translation, so the content is
   * produced in English only.
   *
   * @param nodeRef the repository node the notification relates to
   * @param mails the list of destination e-mail addresses
   * @param templateType the mail template used to render the message
   * @throws NotificationException if the notification cannot be prepared or dispatched
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "nodeRef", "mails", "templateType",
    }
  )
  void notify(NodeRef nodeRef, List<String> mails, MailTemplate templateType)
    throws NotificationException;

  /**
   * Notifies the given users about a bulk upload of several files under a common parent node.
   *
   * @param parentRef the parent node under which the files were uploaded
   * @param nodeRefs the nodes of the newly uploaded files
   * @param notifiableUsers the set of users to be notified, carrying their notification preferences
   * @param notifyDocBulk the mail template used to render the bulk-upload notification
   */
  void notifyNewFiles(
    NodeRef parentRef,
    List<NodeRef> nodeRefs,
    Set<NotifiableUser> notifiableUsers,
    MailTemplate notifyDocBulk
  );

  /**
   * Sends an e-mail notification to a list of raw e-mail addresses, attaching the given files.
   *
   * @param nodeRef the repository node the notification relates to
   * @param mails the list of destination e-mail addresses
   * @param mailTemplate the mail template used to render the message
   * @param files the files to attach to the notification e-mail
   */
  void notify(
    NodeRef nodeRef,
    List<String> mails,
    MailTemplate mailTemplate,
    List<File> files
  );

  /**
   * Notifies the given users about a change on the specified node using an explicit mail template.
   *
   * @param nodeRef the repository node the notification relates to
   * @param users the set of users to be notified, carrying their notification preferences
   * @param mailTemplate the mail template used to render the message
   */
  void notify(
    NodeRef nodeRef,
    Set<NotifiableUser> users,
    MailTemplate mailTemplate
  );

  /**
   * Sends a system-wide message (not tied to a specific repository node) to a list of e-mail
   * addresses.
   *
   * @param mailAddress the list of destination e-mail addresses
   * @param template the application message to be delivered
   */
  void notifySystemMessage(List<String> mailAddress, AppMessage template);
}
