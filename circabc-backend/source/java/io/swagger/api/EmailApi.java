package io.swagger.api;

import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import io.swagger.exception.InvalidEmailException;
import io.swagger.model.*;
import org.alfresco.service.cmr.repository.NodeRef;

import javax.mail.MessagingException;
import java.io.File;
import java.util.List;
import java.util.Map;

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
            MailTemplate mailTemplate);

    EmailDefinition prepareEmailForGroupRequest(GroupCreationRequest body, List<User> toUsers);

    void mailPost(EmailDefinition email);

    /**
     * send an email to the leaders of a group
     */
    void groupsIdLeadersEmailPost(String id, String content)
            throws InvalidEmailException, MessagingException;

    EmailDefinition prepareEmailForAdminContact(
            NodeRef categRef, AdminContactRequest body, List<String> emails);

    EmailDefinition prepareConfirmationForAdminContact(NodeRef categoryRef, String content);

    List<MailTemplateDefinition> getUserMailTemplates();

    String saveUserMailTemplate(
            String templateName, String templateSubject, String templateText, boolean overwrite);

    void deleteUserMailTemplates(String templateIds);

    EmailDefinition prepareEmailForHelpdeskContact(
            String reason, String name, String emailFrom, String subject, String content);

    EmailDefinition prepareConfirmationForHelpdeskContact(
            String reason, String name, String emailFrom, String subject, String content);

    void mailPost(EmailDefinition emailDefinition, List<File> attachementsFiles);

    EmailDefinition prepareRefusalGroupRequest(GroupCreationRequest intialRequest, String argument);

    EmailDefinition prepareAcceptationGroupRequest(
            GroupCreationRequest intialRequest, String argument);
}
