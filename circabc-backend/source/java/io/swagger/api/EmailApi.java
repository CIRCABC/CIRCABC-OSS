package io.swagger.api;

import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import io.swagger.exception.InvalidEmailException;
import io.swagger.model.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author beaurpi
 */
public interface EmailApi {
  /**
   * send an email to the members of a group
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

  void mailPost(EmailDefinition emailDefinition, List<File> attachementsFiles);

  void mailPost(
    EmailDefinition emailDefinition,
    List<File> attachementsFiles,
    boolean useBcc
  );

  void groupDeletionMailPost(EmailDefinition email, String leaderEmail);

  /**
   * send an email to the leaders of a group
   */
  void groupsIdLeadersEmailPost(String id, String content)
    throws InvalidEmailException, MessagingException;

  EmailDefinition prepareEmailForAdminContact(
    NodeRef categRef,
    AdminContactRequest body,
    List<String> emails
  );

  EmailDefinition prepareConfirmationForAdminContact(
    NodeRef categoryRef,
    String content
  );

  List<MailTemplateDefinition> getUserMailTemplates();

  String saveUserMailTemplate(
    String templateName,
    String templateSubject,
    String templateText,
    boolean overwrite
  );

  void deleteUserMailTemplates(String templateIds);

  EmailDefinition prepareEmailForHelpdeskContact(
    String reason,
    String name,
    String emailFrom,
    String subject,
    String content
  );

  EmailDefinition prepareConfirmationForHelpdeskContact(
    String reason,
    String name,
    String emailFrom,
    String subject,
    String content,
    String smtTicket
  );

  EmailDefinition prepareRefusalGroupRequest(
    GroupCreationRequest intialRequest,
    String argument
  );

  EmailDefinition prepareAcceptationGroupRequest(
    GroupCreationRequest intialRequest,
    String argument
  );

  EmailDefinition prepareEmailForGroupDeletionRequest(
    User user,
    GroupDeletionRequest body,
    MailTemplate groupDeleteRequest,
    InterestGroup interestGroup,
    User userFrom
  );

  EmailDefinition prepareEmailForGroupDeletionRequestLeaders(
    User to,
    List<User> toCatAdmins,
    GroupDeletionRequest body,
    MailTemplate groupDeleteRequest,
    InterestGroup ig,
    User userFrom
  );

  EmailDefinition prepareRefusalGroupDeleteRequest(
    GroupDeletionRequest body,
    String igName
  );

  EmailDefinition prepareRefusalGroupDeleteRequestLeaders(
    GroupDeletionRequest body,
    String igName,
    User user
  );

  EmailDefinition prepareAcceptationGroupDeleteRequest(
    User userTo,
    GroupDeletionRequest body,
    String igName
  );
}
