package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import io.swagger.exception.InvalidEmailException;
import io.swagger.model.*;
import jakarta.mail.MessagingException;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business API for composing and sending CIRCABC email notifications.
 *
 * <p>This interface centralises all email-related operations of the REST layer. It
 * exposes two broad families of behaviour:
 *
 * <ul>
 *   <li><b>Sending</b> operations (e.g. {@code groupsIdEmailPost},
 *       {@code groupsIdLeadersEmailPost}, {@code mailPost}, {@code mailToUser}) that
 *       actually dispatch a message to one or more recipients, optionally with
 *       attachments and using either the BCC or the regular "To" field.</li>
 *   <li><b>Preparation</b> operations (the {@code prepare...} methods) that build a
 *       ready-to-send {@link EmailDefinition} — resolving the appropriate mail
 *       template, subject and body model — for a specific business scenario such as
 *       group creation/deletion requests, administrator/helpdesk contacts and their
 *       confirmation/acceptance/refusal replies. These methods do not send anything;
 *       the caller passes the returned definition to a {@code mailPost} method.</li>
 * </ul>
 *
 * <p>It also manages user-defined mail templates (list, save, delete). Implementations
 * are wired through the Spring context and injected into webscript endpoints.
 *
 * @author beaurpi
 */
public interface EmailApi {
  /**
   * Sends an email to all members of the given group.
   *
   * @param id   the identifier of the group whose members are the recipients
   * @param body the email definition (subject, content and options) to send
   * @throws InvalidEmailException if the email definition is invalid or a recipient
   *                               address cannot be resolved
   * @throws MessagingException    if the underlying mail transport fails to send
   */
  void groupsIdEmailPost(String id, EmailDefinition body)
    throws InvalidEmailException, MessagingException;

  /**
   * Send an email notification to the specified User authority
   *
   * @param person           Person node representing the user
   * @param node             Node they are invited too
   * @param from             From text message
   * @param extraModelParams Parameters added to the mail template model (used if usingTemplate !=
   *                         null)
   * @param extraBodyParams  Parameters added to the mail body defined in the text area (used if
   *                         usingTemplate == null)
   * @param isMailHtml       true if the mail is html format
   * @param usingTemplate    id of the template to be used, null if no custom template (use default)
   * @param mailTemplate     mail template definition
   */
  @SuppressWarnings("java:S107") // Parameters map to email composition fields
  void mailToUser(
    final NodeRef person,
    final NodeRef node,
    final String from,
    final Map<String, Object> extraModelParams,
    final Map<String, String> extraBodyParams,
    boolean isMailHtml,
    String usingTemplate,
    MailTemplate mailTemplate
  );

  /**
   * Builds the email definition used to notify the recipients of a new group
   * (interest group) creation request.
   *
   * <p>The subject and body are resolved from the {@code GROUP_REQUEST} mail template
   * configured on the request's category, and the model is populated with the request
   * details (proposed name/title/description, justification, requester, etc.).
   *
   * @param body    the group creation request holding the details to render
   * @param toUsers the users who should receive the notification
   * @return a ready-to-send {@link EmailDefinition} for the group creation request
   */
  EmailDefinition prepareEmailForGroupRequest(
    GroupCreationRequest body,
    List<User> toUsers
  );

  /**
   * Default implementaion of mailPost method with default useBcc parameter set to true
   * @param email the email to send
   **/
  void mailPost(EmailDefinition email);

  /**
   * Implementation of mailPost method where we can define if we use BCC or regular "To" field.
   * By default usBcc is true
   * @param email the email to send
   * @param useBcc do we use BCC or regular To parameter?
   */
  void mailPost(EmailDefinition email, boolean useBcc);

  /**
   * Sends the given email with the supplied file attachments, using BCC by default.
   *
   * @param emailDefinition   the email to send
   * @param attachementsFiles the files to attach to the message
   */
  void mailPost(EmailDefinition emailDefinition, List<File> attachementsFiles);

  /**
   * Sends the given email with the supplied file attachments, controlling whether the
   * recipients are placed in the BCC field or the regular "To" field.
   *
   * @param emailDefinition   the email to send
   * @param attachementsFiles the files to attach to the message
   * @param useBcc            {@code true} to use the BCC field, {@code false} to use the
   *                          regular "To" field
   */
  void mailPost(
    EmailDefinition emailDefinition,
    List<File> attachementsFiles,
    boolean useBcc
  );

  /**
   * Sends an email to the leaders of the given group.
   *
   * @param id      the identifier of the group whose leaders are the recipients
   * @param content the textual content of the message to send
   * @throws InvalidEmailException if the message or a recipient address is invalid
   * @throws MessagingException    if the underlying mail transport fails to send
   */
  void groupsIdLeadersEmailPost(String id, String content)
    throws InvalidEmailException, MessagingException;

  /**
   * Builds the email definition sent to administrators when a user contacts them
   * about a category.
   *
   * @param categRef the reference of the category the contact relates to
   * @param body     the administrator contact request holding subject and content
   * @param emails   the recipient email addresses (the administrators to contact)
   * @return a ready-to-send {@link EmailDefinition} for the administrator contact
   */
  EmailDefinition prepareEmailForAdminContact(
    NodeRef categRef,
    AdminContactRequest body,
    List<String> emails
  );

  /**
   * Builds the confirmation email returned to the user after an administrator
   * contact request has been submitted.
   *
   * @param categoryRef the reference of the category the contact related to
   * @param content     the content to include in the confirmation message
   * @return a ready-to-send confirmation {@link EmailDefinition}
   */
  EmailDefinition prepareConfirmationForAdminContact(
    NodeRef categoryRef,
    String content
  );

  /**
   * Returns the mail templates defined by the current user.
   *
   * @return the list of the current user's mail template definitions
   */
  List<MailTemplateDefinition> getUserMailTemplates();

  /**
   * Saves a mail template for the current user.
   *
   * @param templateName    the name of the template
   * @param templateSubject the subject line stored in the template
   * @param templateText    the body text stored in the template
   * @param overwrite       {@code true} to overwrite an existing template with the same
   *                        name, {@code false} to fail/skip if it already exists
   * @return the identifier of the saved template
   */
  String saveUserMailTemplate(
    String templateName,
    String templateSubject,
    String templateText,
    boolean overwrite
  );

  /**
   * Deletes one or more of the current user's mail templates.
   *
   * @param templateIds the identifier(s) of the template(s) to delete
   */
  void deleteUserMailTemplates(String templateIds);

  /**
   * Builds the email definition sent to the helpdesk when a user submits a contact
   * request.
   *
   * @param reason    the reason/category selected for the contact
   * @param name      the name of the person making the contact
   * @param emailFrom the sender's email address
   * @param subject   the subject of the message
   * @param content   the body content of the message
   * @return a ready-to-send {@link EmailDefinition} for the helpdesk contact
   */
  EmailDefinition prepareEmailForHelpdeskContact(
    String reason,
    String name,
    String emailFrom,
    String subject,
    String content
  );

  /**
   * Builds the confirmation email returned to the user after a helpdesk contact
   * request has been submitted.
   *
   * @param reason    the reason/category selected for the contact
   * @param name      the name of the person making the contact
   * @param emailFrom the sender's email address
   * @param subject   the subject of the message
   * @param content   the body content of the message
   * @param smtTicket the support ticket reference associated with the request
   * @return a ready-to-send confirmation {@link EmailDefinition}
   */
  EmailDefinition prepareConfirmationForHelpdeskContact(
    String reason,
    String name,
    String emailFrom,
    String subject,
    String content,
    String smtTicket
  );

  /**
   * Builds the email definition notifying the requester that their group creation
   * request has been refused.
   *
   * @param intialRequest the original group creation request being refused
   * @param argument      the justification/comment explaining the refusal
   * @return a ready-to-send refusal {@link EmailDefinition}
   */
  EmailDefinition prepareRefusalGroupRequest(
    GroupCreationRequest intialRequest,
    String argument
  );

  /**
   * Builds the email definition notifying the requester that their group creation
   * request has been accepted.
   *
   * @param intialRequest the original group creation request being accepted
   * @param argument      the justification/comment accompanying the acceptance
   * @return a ready-to-send acceptance {@link EmailDefinition}
   */
  EmailDefinition prepareAcceptationGroupRequest(
    GroupCreationRequest intialRequest,
    String argument
  );

  /**
   * Builds the email definition notifying a user of a group deletion request.
   *
   * @param user              the recipient of the notification
   * @param body              the group deletion request details
   * @param groupDeleteRequest the mail template to use for the notification
   * @param interestGroup     the interest group targeted by the deletion request
   * @param userFrom          the user who initiated the deletion request
   * @return a ready-to-send {@link EmailDefinition} for the group deletion request
   */
  EmailDefinition prepareEmailForGroupDeletionRequest(
    User user,
    GroupDeletionRequest body,
    MailTemplate groupDeleteRequest,
    InterestGroup interestGroup,
    User userFrom
  );

  /**
   * Builds the email definition notifying the leaders (and category administrators)
   * of a group deletion request.
   *
   * @param to                the primary leader recipient of the notification
   * @param toCatAdmins       the category administrators to also notify
   * @param body              the group deletion request details
   * @param groupDeleteRequest the mail template to use for the notification
   * @param ig                the interest group targeted by the deletion request
   * @param userFrom          the user who initiated the deletion request
   * @return a ready-to-send {@link EmailDefinition} for the leaders' notification
   */
  EmailDefinition prepareEmailForGroupDeletionRequestLeaders(
    User to,
    List<User> toCatAdmins,
    GroupDeletionRequest body,
    MailTemplate groupDeleteRequest,
    InterestGroup ig,
    User userFrom
  );

  /**
   * Builds the email definition notifying a user that a group deletion request has
   * been accepted.
   *
   * @param userTo the recipient of the notification
   * @param body   the group deletion request details
   * @param igName the name of the interest group concerned
   * @return a ready-to-send acceptance {@link EmailDefinition}
   */
  EmailDefinition prepareAcceptationGroupDeleteRequest(
    User userTo,
    GroupDeletionRequest body,
    String igName
  );

  /**
   * Builds the email definition notifying that a group deletion request has been
   * refused.
   *
   * @param body   the group deletion request details
   * @param igName the name of the interest group concerned
   * @return a ready-to-send refusal {@link EmailDefinition}
   */
  EmailDefinition prepareRefusalGroupDeleteRequest(
    GroupDeletionRequest body,
    String igName
  );

  /**
   * Builds the email definition notifying the leaders that a group deletion request
   * has been refused.
   *
   * @param body   the group deletion request details
   * @param igName the name of the interest group concerned
   * @param user   the leader recipient of the notification
   * @return a ready-to-send refusal {@link EmailDefinition} for the leaders
   */
  EmailDefinition prepareRefusalGroupDeleteRequestLeaders(
    GroupDeletionRequest body,
    String igName,
    User user
  );
}
