package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.mail.MailPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.mail.MailWrapper;
import eu.europa.ec.digit.circabc.rest.service.user.LdapUserService;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.InvalidEmailException;
import io.swagger.exception.SwaggerRuntimeException;
import io.swagger.model.*;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.EmailUtil;
import jakarta.mail.MessagingException;
import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default implementation of {@link EmailApi}.
 *
 * <p>This service centralises all outgoing e-mail logic for CIRCABC. It is responsible for two
 * distinct concerns:
 *
 * <ul>
 *   <li><b>Sending e-mails</b> to Interest Group members and leaders, and to individual users,
 *       optionally with attachments and BCC handling (see the {@code groupsId*EmailPost} and
 *       {@code mailPost} methods).
 *   <li><b>Preparing e-mail content</b> ({@link EmailDefinition}) for a large set of CIRCABC
 *       workflows (group creation/deletion requests and their acceptance/refusal, category admin
 *       and helpdesk contact, confirmations, etc.). These {@code prepare*} methods resolve the
 *       appropriate {@link MailTemplate}, build the FreeMarker model, render subject and body and
 *       return the result without actually sending anything.
 * </ul>
 *
 * <p>It also manages per-user, reusable mail templates stored in the Alfresco repository under
 * {@code dictionary/CircaBC/templates/mails/UserMailTemplates/&lt;userId&gt;} (see
 * {@link #getUserMailTemplates()}, {@link #saveUserMailTemplate(String, String, String, boolean)}
 * and {@link #deleteUserMailTemplates(String)}).
 *
 * <p>Collaborating Alfresco services (mail, node, person, permission, content, template) and
 * CIRCABC APIs are injected via Spring {@link Autowired}. Rendering relies on
 * {@link MailPreferencesService} to obtain templates and build the default model, and on
 * {@link MailService} to perform the actual SMTP delivery.
 *
 * @author beaurpi
 */
public class EmailApiImpl implements EmailApi {

  /** Model key holding the list of {@link AttachementWrapper} attachments for a mail. */
  public static final String ATTACHED_FILES = "attached_files";
  /** File-name suffix used for HTML mail templates stored in the repository. */
  public static final String HTML = ".html";
  /** Model key for the (system) name of the category involved in a mail. */
  public static final String CATEGORY_NAME = "categoryName";
  /** Model key for the human-readable title of the category involved in a mail. */
  public static final String CATEGORY_TITLE = "categoryTitle";
  /** Model key for the user (or user node reference) the mail originates from. */
  public static final String FROM_USER = "fromUser";
  /** Model key for the justification/argument text supplied with a request. */
  public static final String JUSTIFICATION = "justification";
  /** Log message prefix used when an e-mail could not be delivered to a recipient. */
  public static final String FAILED_TO_SEND_EMAIL_TO =
    "Failed to send email to ";
  /** Model key for the free-text body/content of a mail. */
  public static final String CONTENT = "content";
  /** Model key for the subject of a mail. */
  public static final String SUBJECT = "subject";
  /** Alfresco user name of the anonymous guest user. */
  public static final String GUEST = "guest";
  /** Error message raised when the current user lacks write access to a template space. */
  public static final String WRITE_ACCESS_DENIED = "Write access denied.";
  /** Source-organisation code identifying internal Commission ("com") users. */
  private static final String INTERNAL_COM = "com";
  /** Source-organisation code identifying internal external ("ext") users. */
  private static final String INTERNAL_EXT = "ext";
  /** Model key for the reason/rejection message of a request. */
  private static final String REASON = "reason";
  /** Error message prefix used when a required mail template cannot be found. */
  private static final String EMAIL_TEMPLATE_DOES_NOT_EXIST =
    "Email template does not exists : ";
  /** Model key for the node reference of the user the mail originates from. */
  private static final String FROM_USER_REF = "fromUserRef";
  /** Fallback locale used when resolving multilingual titles/descriptions. */
  private static final String EN_US = "en-US";
  /** Model key for the Interest Group name. */
  private static final String IG_NAME = "igName";
  /** Logger for this class. */
  private static final Log logger = LogFactory.getLog(EmailApiImpl.class);

  @Autowired
  private MailService mailService;

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private PersonService personService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private TemplateService templateService;

  @Autowired
  private MailPreferencesService mailPreferencesService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private ContentService contentService;

  @Autowired
  private GroupsApi groupsApi;

  @Autowired
  private ProfilesApi profilesApi;

  @Autowired
  private UsersApi usersApi;

  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Autowired
  @Qualifier("ldapOrLuceneUserService")
  private LdapUserService ldapUserService;

  @Autowired
  private CircabcApi circabcApi;

  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Sends an ad-hoc e-mail to selected members of an Interest Group.
   *
   * <p>The subject and content must both be non-empty. The body is first rendered through the
   * {@link MailTemplate#GROUP_MEMBERS_CONTACT} template, then delivered to the resolved set of
   * recipients (explicit users plus every member matching one of the requested profiles).
   *
   * @param id the identifier of the Interest Group node
   * @param body the e-mail definition holding subject, content, target users and profiles
   * @throws InvalidEmailException if the subject or content is empty
   * @throws MessagingException if the mail infrastructure fails while sending
   */
  @Override
  public void groupsIdEmailPost(String id, EmailDefinition body)
    throws InvalidEmailException, MessagingException {
    if ("".equals(body.getSubject()) || "".equals(body.getContent())) {
      throw new InvalidEmailException("Empty subject or content for mail");
    }

    EmailDefinition realBody = prepareEmailForGroupMembersContact(
      Converter.createNodeRefFromId(id),
      body
    );

    Set<String> mails = collectRecipientEmails(id, body);
    sendEmailsToRecipients(mails, body.getSubject(), realBody.getContent());
  }

  private Set<String> collectRecipientEmails(String id, EmailDefinition body) {
    Set<String> mails = new HashSet<>();

    for (User u : body.getUsers()) {
      if (
        authorityService.authorityExists(u.getUserId()) &&
        EmailUtil.isValidEmailAddress(u.getEmail())
      ) {
        mails.add(u.getEmail().toLowerCase());
      }
    }

    if (body.getProfiles() != null && !body.getProfiles().isEmpty()) {
      collectProfileEmails(id, body.getProfiles(), mails);
    }

    return mails;
  }

  private void collectProfileEmails(
    String id,
    List<Profile> profiles,
    Set<String> mails
  ) {
    List<String> listOfGroups = new ArrayList<>();
    for (Profile p : profiles) {
      if (authorityService.authorityExists(p.getGroupName())) {
        listOfGroups.add(p.getGroupName());
      }
    }

    List<UserProfile> lUserProfiles = groupsApi.groupsIdMembersGet(
      id,
      null,
      null,
      null
    );
    for (UserProfile up : lUserProfiles) {
      if (
        listOfGroups.contains(up.getProfile().getGroupName()) &&
        EmailUtil.isValidEmailAddress(up.getUser().getEmail())
      ) {
        mails.add(up.getUser().getEmail().toLowerCase());
      }
    }
  }

  private void sendEmailsToRecipients(
    Set<String> mails,
    String subject,
    String content
  ) {
    String currentUser = authenticationService.getCurrentUserName();
    if ("".equals(currentUser)) {
      return;
    }

    NodeRef currentUserRef = personService.getPerson(currentUser);
    String from = nodeService
      .getProperty(currentUserRef, ContentModel.PROP_EMAIL)
      .toString();
    String fromDefault = mailService.getNoReplyEmailAddress();

    for (String email : mails) {
      try {
        mailService.send(fromDefault, email, from, subject, content, null);
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error("Failed to send email to:" + email, e);
        }
      }
    }
  }

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
  public void mailToUser(
    final NodeRef person,
    final NodeRef node,
    final String from,
    final Map<String, Object> extraModelParams,
    final Map<String, String> extraBodyParams,
    boolean isMailHtml,
    String usingTemplate,
    MailTemplate mailTemplate
  ) {
    final Map<QName, Serializable> personProperties = nodeService.getProperties(
      person
    );
    final String to = (String) personProperties.get(ContentModel.PROP_EMAIL);

    if (!EmailUtil.isValidEmailAddress(to)) {
      if (logger.isWarnEnabled()) {
        logger.warn("Invalid email address for person " + person + ": " + to);
      }
      return;
    }

    String noReply = mailService.getNoReplyEmailAddress();

    final NodeRef templatePerson = getTemplatePerson();
    final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      node,
      mailTemplate
    );
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      node,
      templatePerson,
      null
    );

    model.putAll(extraModelParams);
    String body = mail.getBody(model).replace("\n", "").replace("\r", "");
    String subject = mail.getSubject(model);

    String bodyToSend = buildEmailBody(
      usingTemplate,
      node,
      person,
      extraModelParams,
      extraBodyParams,
      body
    );

    try {
      mailService.send(
        noReply,
        to,
        from,
        applyParams(subject, extraBodyParams),
        bodyToSend,
        isMailHtml,
        false
      );
    } catch (final MessagingException e) {
      if (logger.isWarnEnabled()) {
        logger.warn(FAILED_TO_SEND_EMAIL_TO + to, e);
      }
    }
  }

  private String buildEmailBody(
    String usingTemplate,
    NodeRef node,
    NodeRef person,
    Map<String, Object> extraModelParams,
    Map<String, String> extraBodyParams,
    String body
  ) {
    if (usingTemplate != null) {
      final Map<String, Object> newModel =
        mailPreferencesService.buildDefaultModel(node, person, null);
      newModel.putAll(extraModelParams);
      final NodeRef templateRef = Converter.createNodeRefFromId(usingTemplate);
      return templateService.processTemplate(
        "freemarker",
        templateRef.toString(),
        newModel
      );
    }

    final String tempBody = applyParams(body, extraBodyParams);
    StringBuilder bodyToSend = new StringBuilder(
      tempBody.replace("\n", "").replace("\r", "")
    );
    appendAttachmentLinks(extraModelParams, bodyToSend);
    return bodyToSend.toString();
  }

  @SuppressWarnings("unchecked")
  private void appendAttachmentLinks(
    Map<String, Object> extraModelParams,
    StringBuilder bodyToSend
  ) {
    if (!extraModelParams.containsKey(ATTACHED_FILES)) {
      return;
    }
    List<AttachementWrapper> listW = (List<
      AttachementWrapper
    >) extraModelParams.get(ATTACHED_FILES);
    if (listW.isEmpty()) {
      return;
    }
    bodyToSend.append("<h3>Linked files and folders</h3>");
    bodyToSend.append("<ul>");
    for (AttachementWrapper aw : listW) {
      bodyToSend
        .append("<li>")
        .append(buildLinkFromAttachmentWrapper(aw))
        .append("</li>");
    }
    bodyToSend.append("</ul>");
  }

  private String buildLinkFromAttachmentWrapper(AttachementWrapper aw) {
    String name = (String) nodeService.getProperty(
      aw.getAttachRef(),
      ContentModel.PROP_NAME
    );
    String result = "<a href=\"";
    result +=
      circabcConfig.getWebRootUrl() +
      MessageFormat.format("/w/{0}/{1}", "browse", aw.getAttachRef().getId());

    result += "\">" + name + "</a>";

    return result;
  }

  /**
   * Performs literal placeholder substitution in a mail body.
   *
   * <p>Each entry of {@code extraBodyParams} whose value is non-null causes every occurrence of the
   * key to be replaced by the value in the returned string. Entries with a {@code null} value are
   * ignored.
   *
   * @param body the raw mail body containing placeholders
   * @param extraBodyParams the placeholder-to-value replacements to apply
   * @return the body with all applicable placeholders replaced
   */
  protected String applyParams(
    final String body,
    final Map<String, String> extraBodyParams
  ) {
    String bodyToUpdate = body;
    for (final Entry<String, String> entry : extraBodyParams.entrySet()) {
      if (entry.getValue() != null) {
        bodyToUpdate = bodyToUpdate.replace(entry.getKey(), entry.getValue());
      }
    }
    return bodyToUpdate;
  }

  /**
   * Resolves the person node used as the template author when building default mail models.
   *
   * @return the {@link NodeRef} of the configured template user
   */
  protected NodeRef getTemplatePerson() {
    final String templateUser = mailPreferencesService
      .getTemplateUserDetails()
      .getUserName();
    return personService.getPerson(templateUser);
  }

  /**
   * Resolves the person node of the fully authenticated current user.
   *
   * @return the {@link NodeRef} of the current user's person node
   */
  protected NodeRef getCurrentPerson() {
    final String currentUsername =
      AuthenticationUtil.getFullyAuthenticatedUser();
    return personService.getPerson(currentUsername);
  }

  /**
   * Prepares the notification e-mail sent to reviewers when a new Interest Group is requested.
   *
   * <p>Uses the {@link MailTemplate#GROUP_REQUEST} template of the target category and populates the
   * model with the requester, proposed group name/title/description, leaders and justification.
   *
   * @param body the group creation request holding the category, proposed metadata and requester
   * @param toUsers the recipients (typically the category administrators) of the notification
   * @return the rendered {@link EmailDefinition} (subject, content and recipients)
   */
  @Override
  public EmailDefinition prepareEmailForGroupRequest(
    GroupCreationRequest body,
    List<User> toUsers
  ) {
    EmailDefinition result = new EmailDefinition();
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      null,
      null,
      null
    );
    NodeRef categRef = Converter.createNodeRefFromId(body.getCategoryRef());
    final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      categRef,
      MailTemplate.GROUP_REQUEST
    );

    String categoryName = nodeService
      .getProperty(categRef, ContentModel.PROP_NAME)
      .toString();
    model.put(CATEGORY_NAME, categoryName);
    model.put("categRef", categRef);

    MLText categoryTitle = (MLText) nodeService.getProperty(
      categRef,
      ContentModel.PROP_TITLE
    );
    if (categoryTitle != null) {
      model.put(CATEGORY_TITLE, categoryTitle.getDefaultValue());
    } else {
      model.put(CATEGORY_TITLE, "");
    }

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );

    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());

    result.setSubject(mail.getSubject(model));

    model.put(FROM_USER, body.getFrom());
    model.put("futureLeaders", body.getLeaders());
    model.put("proposedName", body.getProposedName());
    model.put("proposedTitle", body.getProposedTitle().get("en"));
    model.put("proposedDescription", body.getProposedDescription().get("en"));
    model.put(JUSTIFICATION, body.getJustification());

    result.setUsers(toUsers);
    result.setContent(mail.getBody(model));
    return result;
  }

  /**
   * Sends the given e-mail to all of its recipients, using BCC by default.
   *
   * @param email the e-mail definition holding subject, content and target users
   */
  @Override
  public void mailPost(EmailDefinition email) {
    // Default value of useBcc is true
    mailPost(email, null, true);
  }

  /**
   * Sends the given e-mail to all of its recipients.
   *
   * @param email the e-mail definition holding subject, content and target users
   * @param useBcc {@code true} to place recipients in BCC, {@code false} otherwise
   */
  @Override
  public void mailPost(EmailDefinition email, boolean useBcc) {
    String noReply = mailService.getNoReplyEmailAddress();
    mailPost(email, null, noReply, useBcc);
  }

  /**
   * Sends the given e-mail with attachments to all of its recipients, using BCC by default.
   *
   * @param emailDefinition the e-mail definition holding subject, content and target users
   * @param attachement the files to attach to the message
   */
  @Override
  public void mailPost(
    EmailDefinition emailDefinition,
    List<File> attachement
  ) {
    // Default value of useBcc is true
    mailPost(emailDefinition, attachement, true);
  }

  /**
   * Sends the given e-mail with attachments to all of its recipients.
   *
   * @param emailDefinition the e-mail definition holding subject, content and target users
   * @param attachement the files to attach to the message
   * @param useBcc {@code true} to place recipients in BCC, {@code false} otherwise
   */
  @Override
  public void mailPost(
    EmailDefinition emailDefinition,
    List<File> attachement,
    boolean useBcc
  ) {
    String noReply = mailService.getNoReplyEmailAddress();
    mailPost(emailDefinition, attachement, noReply, useBcc);
  }

  /**
   * Internal helper that delivers an e-mail (optionally with attachments) to every valid recipient.
   *
   * <p>Invalid recipient addresses are skipped (with a warning) and individual send failures are
   * logged without aborting the remaining deliveries. The message is always sent as HTML with the
   * {@code noReply} address as sender.
   *
   * @param emailDefinition the e-mail definition holding subject, content and target users
   * @param attachement the files to attach, or {@code null} for none
   * @param noReply the no-reply address used as both sender and reply-to
   * @param useBcc {@code true} to place recipients in BCC, {@code false} otherwise
   */
  private void mailPost(
    EmailDefinition emailDefinition,
    List<File> attachement,
    String noReply,
    boolean useBcc
  ) {
    for (User toUser : emailDefinition.getUsers()) {
      String email = toUser.getEmail();
      if (!EmailUtil.isValidEmailAddress(email)) {
        if (logger.isWarnEnabled()) {
          logger.warn("Skipping invalid email address: " + email);
        }
        continue;
      }
      try {
        // Send the message
        mailService.sendWithAttachment(
          noReply,
          email,
          noReply,
          emailDefinition.getSubject(),
          emailDefinition.getContent(),
          true,
          useBcc,
          attachement
        );
      } catch (final MessagingException e) {
        if (logger.isWarnEnabled()) {
          logger.warn(FAILED_TO_SEND_EMAIL_TO + email, e);
        }
      }
    }
  }

  /**
   * Sends an e-mail from the current user to the leaders (full administrators) of an Interest Group.
   *
   * <p>A profile qualifies as a leader when it grants admin permissions across all services
   * (library, events, members, newsgroups and information). The message is rendered via the
   * {@link MailTemplate#GROUP_LEADERS_CONTACT} template and delivered to every distinct, valid
   * leader e-mail address.
   *
   * @param id the identifier of the Interest Group node
   * @param content the free-text message provided by the current user
   * @throws InvalidEmailException if the e-mail content cannot be prepared
   * @throws MessagingException if the mail infrastructure fails while sending
   */
  @Override
  public void groupsIdLeadersEmailPost(String id, String content)
    throws InvalidEmailException, MessagingException {
    EmailDefinition realBody = prepareEmailForGroupLeadersContact(
      Converter.createNodeRefFromId(id),
      content
    );

    Set<String> listOfEmails = new HashSet<>();

    List<Profile> profiles = profilesApi.groupsIdProfilesGet(id, "", true);
    for (Profile groupProfile : profiles) {
      Map<String, String> perms = groupProfile.getPermissions();
      if (
        perms.get("library").equals(LibraryPermissions.LIBADMIN.toString()) &&
        perms.get("events").equals(EventPermissions.EVEADMIN.toString()) &&
        perms.get("members").equals(DirectoryPermissions.DIRADMIN.toString()) &&
        perms
          .get("newsgroups")
          .equals(NewsGroupPermissions.NWSADMIN.toString()) &&
        perms
          .get("information")
          .equals(InformationPermissions.INFADMIN.toString())
      ) {
        List<String> profile = new ArrayList<>();
        profile.add(groupProfile.getGroupName());
        List<UserProfile> groupAdmins = groupsApi.groupsIdMembersGet(
          id,
          profile,
          null,
          null
        );

        for (UserProfile leader : groupAdmins) {
          String email = leader.getUser().getEmail();
          if (EmailUtil.isValidEmailAddress(email)) {
            listOfEmails.add(email);
          } else if (logger.isWarnEnabled()) {
            logger.warn("Skipping invalid leader email: " + email);
          }
        }
      }
    }

    String currentUser = authenticationService.getCurrentUserName();
    if (!"".equals(currentUser)) {
      NodeRef currentUserRef = personService.getPerson(currentUser);
      String from = nodeService
        .getProperty(currentUserRef, ContentModel.PROP_EMAIL)
        .toString();

      List<String> emailAddresses = new ArrayList<>(listOfEmails);

      String fromDefault = mailService.getNoReplyEmailAddress();

      mailService.send(
        fromDefault,
        emailAddresses,
        from,
        realBody.getSubject(),
        realBody.getContent(),
        new ArrayList<>()
      );
    }
  }

  /**
   * Prepares the e-mail sent to category administrators when a user contacts them.
   *
   * <p>Uses the {@link MailTemplate#CATEGORY_ADMIN_CONTACT} template of the given category and sets
   * the current user as sender and the supplied message as content. Only valid recipient addresses
   * are kept.
   *
   * @param categRef the node reference of the category whose administrators are contacted
   * @param body the admin contact request holding the message content
   * @param emails the candidate administrator e-mail addresses
   * @return the rendered {@link EmailDefinition} (subject, content and valid recipients)
   */
  @Override
  public EmailDefinition prepareEmailForAdminContact(
    NodeRef categRef,
    AdminContactRequest body,
    List<String> emails
  ) {
    EmailDefinition result = new EmailDefinition();
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      null,
      null,
      null
    );

    final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      categRef,
      MailTemplate.CATEGORY_ADMIN_CONTACT
    );

    String categoryName = nodeService
      .getProperty(categRef, ContentModel.PROP_NAME)
      .toString();
    model.put(CATEGORY_NAME, categoryName);

    MLText categoryTitle = (MLText) nodeService.getProperty(
      categRef,
      ContentModel.PROP_TITLE
    );
    if (categoryTitle != null) {
      model.put(CATEGORY_TITLE, categoryTitle.getDefaultValue());
    } else {
      model.put(CATEGORY_TITLE, categoryName);
    }

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );

    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());

    result.setSubject(mail.getSubject(model));

    String username = authenticationService.getCurrentUserName();
    User user = usersApi.usersUserIdGet(username);

    model.put(FROM_USER, user);
    model.put(CONTENT, body.getContent());

    List<User> toUsers = new ArrayList<>();
    for (String email : emails) {
      if (EmailUtil.isValidEmailAddress(email)) {
        User u = new User();
        u.setEmail(email);
        toUsers.add(u);
      } else if (logger.isWarnEnabled()) {
        logger.warn("Skipping invalid admin contact email: " + email);
      }
    }
    result.setUsers(toUsers);
    result.setContent(mail.getBody(model));
    return result;
  }

  /**
   * Prepares the confirmation e-mail sent back to a user after they contacted category admins.
   *
   * <p>Uses the {@link MailTemplate#CATEGORY_ADMIN_CONTACT_CONFIRMATION} template of the given
   * category, sets the current user both as sender and sole recipient and echoes the submitted
   * content.
   *
   * @param categoryRef the node reference of the contacted category
   * @param content the message content to echo back in the confirmation
   * @return the rendered {@link EmailDefinition} addressed to the current user
   */
  @Override
  public EmailDefinition prepareConfirmationForAdminContact(
    NodeRef categoryRef,
    String content
  ) {
    EmailDefinition result = new EmailDefinition();
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      null,
      null,
      null
    );

    final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      categoryRef,
      MailTemplate.CATEGORY_ADMIN_CONTACT_CONFIRMATION
    );

    String categoryName = nodeService
      .getProperty(categoryRef, ContentModel.PROP_NAME)
      .toString();
    model.put(CATEGORY_NAME, categoryName);

    MLText categoryTitle = (MLText) nodeService.getProperty(
      categoryRef,
      ContentModel.PROP_TITLE
    );
    if (categoryTitle != null) {
      model.put(CATEGORY_TITLE, categoryTitle.getDefaultValue());
    } else {
      model.put(CATEGORY_TITLE, categoryName);
    }

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );

    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());

    result.setSubject(mail.getSubject(model));

    String username = authenticationService.getCurrentUserName();
    User user = usersApi.usersUserIdGet(username);

    model.put(FROM_USER, user);
    model.put(CONTENT, content);

    List<User> toUsers = new ArrayList<>();
    toUsers.add(user);

    result.setUsers(toUsers);
    result.setContent(mail.getBody(model));
    return result;
  }

  /**
   * Returns the reusable mail templates owned by the current user.
   *
   * <p>Templates are read from the current user's space under
   * {@code UserMailTemplates/&lt;userId&gt;}. Children without content or a name are skipped.
   *
   * @return the list of the current user's {@link MailTemplateDefinition}s
   * @throws SwaggerRuntimeException if the current user lacks read permission on the space
   * @see io.swagger.api.EmailApi#getUserMailTemplates()
   */
  @Override
  public List<MailTemplateDefinition> getUserMailTemplates() {
    List<MailTemplateDefinition> templates = new ArrayList<>();

    NodeRef userIdMailTemplatesNodeRef =
      getOrCreateUserMailTemplatesSpaceNodeRef();

    if (
      !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        userIdMailTemplatesNodeRef.getId()
      )
    ) {
      throw new SwaggerRuntimeException(WRITE_ACCESS_DENIED);
    }

    List<ChildAssociationRef> childrenRef = nodeService.getChildAssocs(
      userIdMailTemplatesNodeRef
    );

    for (ChildAssociationRef childRef : childrenRef) {
      MailTemplateDefinition template = parseMailTemplate(
        childRef.getChildRef()
      );
      if (template != null) {
        templates.add(template);
      }
    }

    return templates;
  }

  private MailTemplateDefinition parseMailTemplate(NodeRef childNodeRef) {
    ContentReader reader = contentService.getReader(
      childNodeRef,
      ContentModel.PROP_CONTENT
    );
    if (reader == null) {
      logger.warn(
        childNodeRef.getId() +
          " has no content (reader == null). Will not be added."
      );
      return null;
    }

    String name = (String) nodeService.getProperty(
      childNodeRef,
      ContentModel.PROP_NAME
    );
    if (name == null || name.isEmpty()) {
      logger.warn(
        childNodeRef.getId() + " has an empty name. Will not be added."
      );
      return null;
    }

    String text = reader.getContentString();
    String subject = (String) nodeService.getProperty(
      childNodeRef,
      ContentModel.PROP_SUBJECT
    );

    return new MailTemplateDefinition(
      childNodeRef.getId(),
      name.endsWith(HTML) ? name.substring(0, name.length() - 5) : name,
      subject,
      text
    );
  }

  /**
   * Creates or updates a reusable mail template owned by the current user.
   *
   * <p>The template is stored as an HTML content node (with the {@code emailed} aspect) in the
   * current user's template space. When {@code overwrite} is {@code true} and a template with the
   * same name already exists, its subject and content are replaced; otherwise a new node is created.
   *
   * @param templateName the template name (used as the node name, with an {@code .html} suffix)
   * @param templateSubject the subject line stored on the template node
   * @param templateText the HTML body of the template
   * @param overwrite {@code true} to update an existing template with the same name
   * @return the identifier of the created or updated template node
   * @throws SwaggerRuntimeException if the current user lacks write permission on the space
   * @see io.swagger.api.EmailApi#saveUserMailTemplate(java.lang.String, java.lang.String,
   * java.lang.String, boolean)
   */
  @Override
  public String saveUserMailTemplate(
    String templateName,
    String templateSubject,
    String templateText,
    boolean overwrite
  ) {
    NodeRef userIdMailTemplatesNodeRef =
      getOrCreateUserMailTemplatesSpaceNodeRef();

    if (
      !this.currentUserPermissionCheckerService.hasAlfrescoWritePermission(
        userIdMailTemplatesNodeRef.getId()
      )
    ) {
      throw new SwaggerRuntimeException(WRITE_ACCESS_DENIED);
    }

    NodeRef contentRef = null;

    if (overwrite) {
      contentRef = nodeService.getChildByName(
        userIdMailTemplatesNodeRef,
        ContentModel.ASSOC_CONTAINS,
        templateName + HTML
      );
    }

    if (contentRef == null) {
      Map<QName, Serializable> properties = HashMap.newHashMap(1);
      properties.put(ContentModel.PROP_NAME, templateName + HTML);

      QName associationNameQName = QName.createQName(
        ContentModel.PROP_NAME.getNamespaceURI(),
        templateName + HTML
      );
      contentRef = nodeService
        .createNode(
          userIdMailTemplatesNodeRef,
          ContentModel.ASSOC_CONTAINS,
          associationNameQName,
          ContentModel.TYPE_CONTENT,
          properties
        )
        .getChildRef();
      properties = HashMap.newHashMap(1);
      nodeService.addAspect(
        contentRef,
        ContentModel.ASPECT_EMAILED,
        properties
      );
    }

    nodeService.setProperty(
      contentRef,
      ContentModel.PROP_SUBJECT,
      templateSubject
    );

    ContentWriter writer = contentService.getWriter(
      contentRef,
      ContentModel.PROP_CONTENT,
      true
    );
    writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
    writer.setEncoding("UTF-8");
    writer.putContent(templateText);

    return contentRef.getId();
  }

  /**
   * Deletes one or more reusable mail templates owned by the current user.
   *
   * <p>Template identifiers not found or already deleted are silently ignored.
   *
   * @param templateIds a comma-separated list of template node identifiers to remove
   * @throws SwaggerRuntimeException if the current user lacks write permission on the space
   * @see io.swagger.api.EmailApi#deleteUserMailTemplates(java.lang.String)
   */
  @Override
  public void deleteUserMailTemplates(String templateIds) {
    NodeRef userIdMailTemplatesNodeRef =
      getOrCreateUserMailTemplatesSpaceNodeRef();

    if (
      !this.currentUserPermissionCheckerService.hasAlfrescoWritePermission(
        userIdMailTemplatesNodeRef.getId()
      )
    ) {
      throw new SwaggerRuntimeException(WRITE_ACCESS_DENIED);
    }

    StringTokenizer tokenizer = new StringTokenizer(templateIds, ",");

    while (tokenizer.hasMoreTokens()) {
      String templateId = tokenizer.nextToken().trim();

      NodeRef nodeRef = Converter.createNodeRefFromId(templateId);

      if (!nodeService.exists(nodeRef)) {
        // if the item does not exist or has been deleted, continue
        continue;
      }

      nodeService.removeChild(userIdMailTemplatesNodeRef, nodeRef);
    }
  }

  private NodeRef getOrCreateUserMailTemplatesSpaceNodeRef() {
    return AuthenticationUtil.runAs(
      () -> {
        NodeRef dicoRef = circabcApi.getCircabcDictionaryNodeRef();
        NodeRef templatesNodeRef = nodeService.getChildByName(
          dicoRef,
          ContentModel.ASSOC_CONTAINS,
          "templates"
        );
        NodeRef mailsNodeRef = nodeService.getChildByName(
          templatesNodeRef,
          ContentModel.ASSOC_CONTAINS,
          "mails"
        );

        if (mailsNodeRef == null || !nodeService.exists(mailsNodeRef)) {
          logger.error(
            "The CIRCABC mail templates root could not be found: dictionary/CircaBC/templates/mails"
          );
          throw new SwaggerRuntimeException(
            "The CIRCABC mail templates root could not be found: dictionary/CircaBC/templates/mails"
          );
        }

        NodeRef userMailTemplatesNodeRef = nodeService.getChildByName(
          mailsNodeRef,
          ContentModel.ASSOC_CONTAINS,
          "UserMailTemplates"
        );
        if (userMailTemplatesNodeRef == null) {
          userMailTemplatesNodeRef = createNamedFolder(
            mailsNodeRef,
            "UserMailTemplates"
          );
        }

        String userId = AuthenticationUtil.getFullyAuthenticatedUser();

        NodeRef userIdMailTemplatesNodeRef = nodeService.getChildByName(
          userMailTemplatesNodeRef,
          ContentModel.ASSOC_CONTAINS,
          userId
        );
        if (userIdMailTemplatesNodeRef == null) {
          userIdMailTemplatesNodeRef = createNamedFolder(
            userMailTemplatesNodeRef,
            userId
          );
          permissionService.deletePermissions(userIdMailTemplatesNodeRef);
          permissionService.setInheritParentPermissions(
            userIdMailTemplatesNodeRef,
            false
          );
          permissionService.setPermission(
            userIdMailTemplatesNodeRef,
            userId,
            PermissionService.WRITE,
            true
          );
          permissionService.setPermission(
            userIdMailTemplatesNodeRef,
            userId,
            PermissionService.READ,
            true
          );
        }

        return userIdMailTemplatesNodeRef;
      },
      AuthenticationUtil.getAdminUserName()
    );
  }

  private NodeRef createNamedFolder(NodeRef parentRef, String name) {
    Map<QName, Serializable> properties = HashMap.newHashMap(1);
    properties.put(ContentModel.PROP_NAME, name);
    QName associationNameQName = QName.createQName(
      ContentModel.PROP_NAME.getNamespaceURI(),
      name
    );
    return nodeService
      .createNode(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        associationNameQName,
        ContentModel.TYPE_FOLDER,
        properties
      )
      .getChildRef();
  }

  private EmailDefinition prepareEmailForGroupMembersContact(
    NodeRef igRef,
    EmailDefinition body
  ) {
    EmailDefinition result = new EmailDefinition();
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      igRef,
      null,
      null
    );

    final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      igRef,
      MailTemplate.GROUP_MEMBERS_CONTACT
    );

    model.put("interestGroup", igRef);

    NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
    model.put("category", categoryRef);

    result.setSubject(mail.getSubject(model));

    String username = authenticationService.getCurrentUserName();
    NodeRef userRef = personService.getPerson(username);

    model.put(FROM_USER, userRef);

    model.put(SUBJECT, body.getSubject());

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );

    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());

    List<NodeRef> attachments = new ArrayList<>();
    for (String s : body.getAttachments()) {
      attachments.add(Converter.createNodeRefFromId(s));
    }

    model.put("message", body.getContent());

    if (!attachments.isEmpty()) {
      model.put("attachments", attachments);
    }

    result.setContent(mail.getBody(model));
    return result;
  }

  private EmailDefinition prepareEmailForGroupLeadersContact(
    NodeRef igRef,
    String message
  ) {
    EmailDefinition result = new EmailDefinition();
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      igRef,
      null,
      null
    );

    final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      igRef,
      MailTemplate.GROUP_LEADERS_CONTACT
    );

    model.put("interestGroup", igRef);

    NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
    model.put("category", categoryRef);

    String username = authenticationService.getCurrentUserName();
    NodeRef userRef = personService.getPerson(username);
    model.put(FROM_USER, userRef);

    model.put("message", message);

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );

    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());

    result.setContent(mail.getBody(model));
    result.setSubject(mail.getSubject(model));
    return result;
  }

  /**
   * Prepares the e-mail sent to the CIRCABC helpdesk when a user submits a contact request.
   *
   * <p>Rendered via the {@link MailTemplate#HELPDESK_CONTACT} template. The recipient is resolved
   * from the caller's origin: guests and users whose LDAP source organisation is internal
   * ({@code com}/{@code ext}) are routed to the IT helpdesk mailbox, all others to the general
   * helpdesk mailbox. When the caller is the guest user the operation runs as system while
   * preparing the mail and is restored afterwards.
   *
   * @param reason the contact reason (dots are converted to underscores for template lookup)
   * @param name the display name of the person submitting the request
   * @param emailFrom the reply-to address supplied by the requester
   * @param subject the subject of the request
   * @param content the message content of the request
   * @return the rendered {@link EmailDefinition} addressed to the appropriate helpdesk mailbox
   */
  @Override
  public EmailDefinition prepareEmailForHelpdeskContact(
    String reason,
    String name,
    String emailFrom,
    String subject,
    String content
  ) {
    String currentUser = authenticationService.getCurrentUserName();
    if (currentUser.equals(GUEST)) {
      AuthenticationUtil.setRunAsUserSystem();
    }

    EmailDefinition result = new EmailDefinition();
    Map<String, Object> model = new HashMap<>();

    model.put(REASON, reason.replace(".", "_"));
    model.put(SUBJECT, subject);
    model.put(CONTENT, content);
    model.put("name", name);
    model.put("emailFrom", emailFrom);

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );
    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());

    circabcConfig.addApplicationNameToModel(model);

    NodeRef cbcRef = circabcApi.getCircabcNodeRef();

    final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      cbcRef,
      MailTemplate.HELPDESK_CONTACT
    );

    result.setSubject(mail.getSubject(model));
    result.setContent(mail.getBody(model));

    User helpdesk = new User();

    if (GUEST.equals(currentUser)) {
      helpdesk.setEmail(circabcConfig.getHelpDeskMail());
    } else {
      CircabcUserDataBean userDataBean = ldapUserService.getLDAPUserDataByUid(
        currentUser
      );
      String org = userDataBean.getSourceOrganisation();

      if (
        INTERNAL_COM.equalsIgnoreCase(org) || INTERNAL_EXT.equalsIgnoreCase(org)
      ) {
        helpdesk.setEmail(circabcConfig.getItHelpDeskMail());
      } else {
        helpdesk.setEmail(circabcConfig.getHelpDeskMail());
      }
    }

    result.getUsers().add(helpdesk);

    if (currentUser.equals(GUEST)) {
      AuthenticationUtil.setRunAsUser(GUEST);
    }

    return result;
  }

  /**
   * Prepares the confirmation e-mail sent back to a user after they contacted the helpdesk.
   *
   * <p>Rendered via the {@link MailTemplate#HELPDESK_CONTACT_CONFIRMATION} template and addressed to
   * the requester's own address. When an SMT ticket reference is supplied it is appended to the
   * subject. When the caller is the guest user the operation runs as system while preparing the mail
   * and is restored afterwards.
   *
   * @param reason the contact reason (dots are converted to underscores for template lookup)
   * @param name the display name of the person who submitted the request
   * @param emailFrom the requester's address, used as the confirmation recipient
   * @param subject the subject of the original request
   * @param content the message content of the original request
   * @param smtTicket the optional SMT ticket reference to append to the subject, or {@code null}
   * @return the rendered {@link EmailDefinition} addressed to the requester
   */
  @Override
  public EmailDefinition prepareConfirmationForHelpdeskContact(
    String reason,
    String name,
    String emailFrom,
    String subject,
    String content,
    String smtTicket
  ) {
    String currentUser = authenticationService.getCurrentUserName();
    if (currentUser.equals(GUEST)) {
      AuthenticationUtil.setRunAsUserSystem();
    }

    EmailDefinition result = new EmailDefinition();
    Map<String, Object> model = new HashMap<>();

    model.put("name", name);
    model.put("emailFrom", emailFrom);
    model.put(CONTENT, content);
    model.put(REASON, reason.replace(".", "_"));
    model.put(SUBJECT, subject);

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );
    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());
    circabcConfig.addApplicationNameToModel(model);

    NodeRef cbcRef = circabcApi.getCircabcNodeRef();

    final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      cbcRef,
      MailTemplate.HELPDESK_CONTACT_CONFIRMATION
    );

    //add the SMT Ticket reference if any
    if (smtTicket != null) {
      result.setSubject(mail.getSubject(model) + " - " + smtTicket);
    } else {
      result.setSubject(mail.getSubject(model));
    }
    result.setContent(mail.getBody(model));

    User helpdesk = new User();
    helpdesk.setEmail(emailFrom);
    result.getUsers().add(helpdesk);

    if (currentUser.equals(GUEST)) {
      AuthenticationUtil.setRunAsUser(GUEST);
    }

    return result;
  }

  /**
   * Prepares the e-mail notifying the requester that their group creation request was refused.
   *
   * <p>Uses the default {@link MailTemplate#CATEGORY_GROUP_REQUEST_REFUSE} template of the request's
   * category and populates the model with the reviewer, requester and refusal justification.
   *
   * @param intialRequest the original group creation request
   * @param argument the refusal argument (justification)
   * @return the rendered {@link EmailDefinition} addressed to the requester
   * @throws IllegalStateException if the refusal mail template cannot be found
   */
  @Override
  public EmailDefinition prepareRefusalGroupRequest(
    GroupCreationRequest intialRequest,
    String argument
  ) {
    EmailDefinition result = new EmailDefinition();
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      null,
      null,
      null
    );
    NodeRef categRef = Converter.createNodeRefFromId(
      intialRequest.getCategoryRef()
    );

    final List<MailWrapper> mails = mailPreferencesService.getMailTemplates(
      categRef,
      MailTemplate.CATEGORY_GROUP_REQUEST_REFUSE
    );
    MailWrapper mail = null;

    for (MailWrapper mailTemplate : mails) {
      if (
        mailTemplate
          .getName()
          .equals(
            MailTemplate.CATEGORY_GROUP_REQUEST_REFUSE.getDefaultTemplateName()
          )
      ) {
        mail = mailTemplate;
      }
    }
    if (mail == null) {
      throw new IllegalStateException(
        EMAIL_TEMPLATE_DOES_NOT_EXIST +
          MailTemplate.CATEGORY_GROUP_REQUEST_REFUSE.getDefaultTemplateName()
      );
    }
    String categoryName = nodeService
      .getProperty(categRef, ContentModel.PROP_NAME)
      .toString();
    model.put(CATEGORY_NAME, categoryName);

    MLText categoryTitle = (MLText) nodeService.getProperty(
      categRef,
      ContentModel.PROP_TITLE
    );
    if (categoryTitle != null) {
      model.put(CATEGORY_TITLE, categoryTitle.getDefaultValue());
    } else {
      model.put(CATEGORY_TITLE, categoryName);
    }

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );

    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());

    result.setSubject(mail.getSubject(model));

    String username = authenticationService.getCurrentUserName();
    User user = usersApi.usersUserIdGet(username);

    NodeRef fromUserRef = personService.getPerson(
      intialRequest.getFrom().getUserId()
    );
    User fromUser = usersApi.usersUserIdGet(
      intialRequest.getFrom().getUserId()
    );

    model.put("reviewer", user);
    model.put(JUSTIFICATION, intialRequest.getArgument());
    model.put(FROM_USER, fromUser);
    model.put(FROM_USER_REF, fromUserRef);

    List<User> toUsers = new ArrayList<>();
    toUsers.add(fromUser);
    result.setUsers(toUsers);
    result.setContent(mail.getBody(model));
    return result;
  }

  /**
   * Prepares the e-mail notifying the requester that their group creation request was accepted.
   *
   * <p>Uses the default {@link MailTemplate#CATEGORY_GROUP_REQUEST_ACCEPT} template of the request's
   * category and populates the model with the reviewer, requester and justification.
   *
   * @param intialRequest the original group creation request
   * @param argument the acceptance argument (justification)
   * @return the rendered {@link EmailDefinition} addressed to the requester
   * @throws IllegalStateException if the acceptance mail template cannot be found
   */
  @Override
  public EmailDefinition prepareAcceptationGroupRequest(
    GroupCreationRequest intialRequest,
    String argument
  ) {
    EmailDefinition result = new EmailDefinition();
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      null,
      null,
      null
    );
    NodeRef categRef = Converter.createNodeRefFromId(
      intialRequest.getCategoryRef()
    );

    final List<MailWrapper> mails = mailPreferencesService.getMailTemplates(
      categRef,
      MailTemplate.CATEGORY_GROUP_REQUEST_ACCEPT
    );
    MailWrapper mail = null;

    for (MailWrapper mailTemplate : mails) {
      if (
        mailTemplate
          .getName()
          .equals(
            MailTemplate.CATEGORY_GROUP_REQUEST_ACCEPT.getDefaultTemplateName()
          )
      ) {
        mail = mailTemplate;
      }
    }

    if (mail == null) {
      throw new IllegalStateException(
        EMAIL_TEMPLATE_DOES_NOT_EXIST +
          MailTemplate.CATEGORY_GROUP_REQUEST_ACCEPT.getDefaultTemplateName()
      );
    }

    String categoryName = nodeService
      .getProperty(categRef, ContentModel.PROP_NAME)
      .toString();
    model.put(CATEGORY_NAME, categoryName);

    MLText categoryTitle = (MLText) nodeService.getProperty(
      categRef,
      ContentModel.PROP_TITLE
    );
    if (categoryTitle != null) {
      model.put(CATEGORY_TITLE, categoryTitle.getDefaultValue());
    } else {
      model.put(CATEGORY_TITLE, categoryName);
    }

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );

    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());

    result.setSubject(mail.getSubject(model));

    String username = authenticationService.getCurrentUserName();
    User user = usersApi.usersUserIdGet(username);

    NodeRef fromUserRef = personService.getPerson(
      intialRequest.getFrom().getUserId()
    );
    User fromUser = usersApi.usersUserIdGet(
      intialRequest.getFrom().getUserId()
    );

    model.put("reviewer", user);
    model.put(JUSTIFICATION, intialRequest.getArgument());
    model.put(FROM_USER, fromUser);
    model.put(FROM_USER_REF, fromUserRef);

    List<User> toUsers = new ArrayList<>();
    toUsers.add(fromUser);
    result.setUsers(toUsers);
    result.setContent(mail.getBody(model));
    return result;
  }

  /**
   * Prepares the e-mail notifying a single recipient about an Interest Group deletion request.
   *
   * <p>Uses the supplied {@link MailTemplate} of the request's category and populates the model with
   * the group metadata (name, title, description), the originating user and the justification.
   *
   * @param user the recipient of the notification
   * @param body the group deletion request holding the category and justification
   * @param mailTemplate the mail template to use for rendering
   * @param ig the Interest Group targeted by the deletion request
   * @param userFrom the user who initiated the deletion request
   * @return the rendered {@link EmailDefinition} addressed to {@code user}
   */
  @Override
  public EmailDefinition prepareEmailForGroupDeletionRequest(
    User user,
    GroupDeletionRequest body,
    MailTemplate mailTemplate,
    InterestGroup ig,
    User userFrom
  ) {
    EmailDefinition result = new EmailDefinition();

    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      null,
      null,
      null
    );
    NodeRef categRef = Converter.createNodeRefFromId(body.getCategoryRef());
    final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      categRef,
      mailTemplate
    );

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );

    String categoryName = nodeService
      .getProperty(categRef, ContentModel.PROP_NAME)
      .toString();

    model.put("categRef", categRef);
    model.put(CATEGORY_NAME, categoryName);
    model.put(IG_NAME, ig.getName());
    model.put(FROM_USER, userFrom);
    model.put(JUSTIFICATION, body.getJustification());
    model.put("name", ig.getName());

    String title = null;
    if (ig.getTitle() != null) {
      title = ig.getTitle().getDefaultValue();
      if (title == null) {
        title = ig.getTitle().get(EN_US);
      }
    }
    model.put("title", title != null ? title : "");
    String description = null;
    if (ig.getDescription() != null) {
      description = ig.getDescription().getDefaultValue();
      if (description == null) {
        description = ig.getDescription().get(EN_US);
      }
    }
    model.put("description", description != null ? description : "");

    String mailSubject = mail.getSubject(model);
    result.setSubject(mailSubject);

    String mailBody = null;
    try {
      logger.info("mailSubject: " + mailSubject);
      mailBody = mail.getBody(model);
    } catch (Exception e) {
      logger.error("Error getting mail body: " + e.getMessage(), e);
    }

    List<User> toUser = Collections.singletonList(user);
    result.setUsers(toUser);
    result.setContent(mailBody);
    return result;
  }

  /**
   * Prepares the group-deletion-request e-mail addressed to a leader, with category admins in copy.
   *
   * <p>Uses the supplied {@link MailTemplate} of the request's category. In addition to the group
   * metadata and justification, the model exposes the list of category administrator e-mail
   * addresses (model key {@code catAdminsEmails}) so the template can carbon-copy them.
   *
   * @param to the primary recipient (leader) of the notification
   * @param toCatAdmins the category administrators whose e-mails are added to the model
   * @param body the group deletion request holding the category and justification
   * @param mailTemplate the mail template to use for rendering
   * @param ig the Interest Group targeted by the deletion request
   * @param userFrom the user who initiated the deletion request
   * @return the rendered {@link EmailDefinition} addressed to {@code to}
   */
  @Override
  public EmailDefinition prepareEmailForGroupDeletionRequestLeaders(
    User to,
    List<User> toCatAdmins,
    GroupDeletionRequest body,
    MailTemplate mailTemplate,
    InterestGroup ig,
    User userFrom
  ) {
    EmailDefinition result = new EmailDefinition();

    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      null,
      null,
      null
    );
    NodeRef categRef = Converter.createNodeRefFromId(body.getCategoryRef());
    final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      categRef,
      mailTemplate
    );

    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );

    model.put(IG_NAME, ig.getName());
    model.put(FROM_USER, userFrom);
    model.put(JUSTIFICATION, body.getJustification());
    model.put("name", ig.getName());

    String categoryName = nodeService
      .getProperty(categRef, ContentModel.PROP_NAME)
      .toString();

    model.put(CATEGORY_NAME, categoryName);

    String title = null;
    if (ig.getTitle() != null) {
      title = ig.getTitle().getDefaultValue();
      if (title == null) {
        title = ig.getTitle().get(EN_US);
      }
    }

    model.put("title", title != null ? title : "");
    String description = null;
    if (ig.getDescription() != null) {
      description = ig.getDescription().getDefaultValue();
      if (description == null) {
        description = ig.getDescription().get(EN_US);
      }
    }
    model.put("description", description != null ? description : "");

    List<String> catAdminsEmails = toCatAdmins
      .stream()
      .map(User::getEmail)
      .toList();

    model.put("catAdminsEmails", catAdminsEmails);
    String mailSubject = mail.getSubject(model);
    result.setSubject(mailSubject);

    String mailBody = null;
    try {
      logger.info("mailSubject: " + mailSubject);
      mailBody = mail.getBody(model);
    } catch (Exception e) {
      logger.error("Error getting mail body: " + e.getMessage(), e);
    }
    List<User> toUser = Collections.singletonList(to);
    result.setUsers(toUser);
    result.setContent(mailBody);
    return result;
  }

  /**
   * Prepares the e-mail notifying the requester that a group deletion request was accepted.
   *
   * <p>Uses the default {@link MailTemplate#CATEGORY_GROUP_DELETE_REQUEST_ACCEPT} template of the
   * request's category and populates the model with the category name, Interest Group name and the
   * requester's node reference.
   *
   * @param user the recipient (original requester) of the notification
   * @param body the group deletion request holding the category reference
   * @param igName the name of the Interest Group that was deleted
   * @return the rendered {@link EmailDefinition} addressed to {@code user}
   * @throws IllegalStateException if the acceptance mail template cannot be found
   */
  @Override
  public EmailDefinition prepareAcceptationGroupDeleteRequest(
    User user,
    GroupDeletionRequest body,
    String igName
  ) {
    EmailDefinition result = new EmailDefinition();
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      null,
      null,
      null
    );
    NodeRef categRef = Converter.createNodeRefFromId(body.getCategoryRef());

    final List<MailWrapper> mails = mailPreferencesService.getMailTemplates(
      categRef,
      MailTemplate.CATEGORY_GROUP_DELETE_REQUEST_ACCEPT
    );
    MailWrapper mail = null;

    for (MailWrapper mailTemplate : mails) {
      if (
        mailTemplate
          .getName()
          .equals(
            MailTemplate.CATEGORY_GROUP_DELETE_REQUEST_ACCEPT.getDefaultTemplateName()
          )
      ) {
        mail = mailTemplate;
      }
    }
    if (mail == null) {
      throw new IllegalStateException(
        EMAIL_TEMPLATE_DOES_NOT_EXIST +
          MailTemplate.CATEGORY_GROUP_DELETE_REQUEST_ACCEPT.getDefaultTemplateName()
      );
    }
    String categoryName = nodeService
      .getProperty(categRef, ContentModel.PROP_NAME)
      .toString();
    model.put(CATEGORY_NAME, categoryName);
    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );
    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());
    result.setSubject(mail.getSubject(model));
    NodeRef fromUserRef = personService.getPerson(user.getUserId());
    model.put(IG_NAME, igName);
    model.put(FROM_USER_REF, fromUserRef);
    List<User> toUsers = Collections.singletonList(user);
    result.setUsers(toUsers);
    result.setContent(mail.getBody(model));
    return result;
  }

  /**
   * Prepares the e-mail notifying the requester that a group deletion request was refused.
   *
   * <p>Uses the default {@link MailTemplate#CATEGORY_GROUP_DELETE_REQUEST_REFUSE} template of the
   * request's category and populates the model with the category name, Interest Group name,
   * requester reference and the rejection message.
   *
   * @param body the group deletion request holding the category, requester and rejection message
   * @param igName the name of the Interest Group concerned by the request
   * @return the rendered {@link EmailDefinition} addressed to the requester
   * @throws IllegalStateException if the refusal mail template cannot be found
   */
  @Override
  public EmailDefinition prepareRefusalGroupDeleteRequest(
    GroupDeletionRequest body,
    String igName
  ) {
    EmailDefinition result = new EmailDefinition();
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      null,
      null,
      null
    );
    NodeRef categRef = Converter.createNodeRefFromId(body.getCategoryRef());
    final List<MailWrapper> mails = mailPreferencesService.getMailTemplates(
      categRef,
      MailTemplate.CATEGORY_GROUP_DELETE_REQUEST_REFUSE
    );
    MailWrapper mail = null;
    for (MailWrapper mailTemplate : mails) {
      if (
        mailTemplate
          .getName()
          .equals(
            MailTemplate.CATEGORY_GROUP_DELETE_REQUEST_REFUSE.getDefaultTemplateName()
          )
      ) {
        mail = mailTemplate;
      }
    }
    if (mail == null) {
      throw new IllegalStateException(
        EMAIL_TEMPLATE_DOES_NOT_EXIST +
          MailTemplate.CATEGORY_GROUP_DELETE_REQUEST_REFUSE.getDefaultTemplateName()
      );
    }
    String categoryName = nodeService
      .getProperty(categRef, ContentModel.PROP_NAME)
      .toString();
    model.put(CATEGORY_NAME, categoryName);
    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );

    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());

    result.setSubject(mail.getSubject(model));

    String userId =
      body.getFrom().getUserId() != null ? body.getFrom().getUserId() : "";
    NodeRef fromUserRef = personService.getPerson(userId);
    model.put(IG_NAME, igName);
    model.put(FROM_USER_REF, fromUserRef);
    model.put(REASON, body.getRejectedMessage());
    List<User> toUsers = Collections.singletonList(body.getFrom());
    result.setUsers(toUsers);
    result.setContent(mail.getBody(model));
    return result;
  }

  /**
   * Prepares the group-deletion-refusal e-mail addressed to an Interest Group leader.
   *
   * <p>Uses the default {@link MailTemplate#CATEGORY_GROUP_DELETE_REQUEST_REFUSE_LEADER} template of
   * the request's category and populates the model with the category name, Interest Group name, the
   * recipient's node reference and the rejection message.
   *
   * @param body the group deletion request holding the category and rejection message
   * @param igName the name of the Interest Group concerned by the request
   * @param user the leader who receives the refusal notification
   * @return the rendered {@link EmailDefinition} addressed to {@code user}
   * @throws IllegalStateException if the leader refusal mail template cannot be found
   */
  @Override
  public EmailDefinition prepareRefusalGroupDeleteRequestLeaders(
    GroupDeletionRequest body,
    String igName,
    User user
  ) {
    EmailDefinition result = new EmailDefinition();
    final Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      null,
      null,
      null
    );
    NodeRef categRef = Converter.createNodeRefFromId(body.getCategoryRef());
    final List<MailWrapper> mails = mailPreferencesService.getMailTemplates(
      categRef,
      MailTemplate.CATEGORY_GROUP_DELETE_REQUEST_REFUSE_LEADER
    );
    MailWrapper mail = null;
    for (MailWrapper mailTemplate : mails) {
      if (
        mailTemplate
          .getName()
          .equals(
            MailTemplate.CATEGORY_GROUP_DELETE_REQUEST_REFUSE_LEADER.getDefaultTemplateName()
          )
      ) {
        mail = mailTemplate;
      }
    }
    if (mail == null) {
      throw new IllegalStateException(
        EMAIL_TEMPLATE_DOES_NOT_EXIST +
          MailTemplate.CATEGORY_GROUP_DELETE_REQUEST_REFUSE_LEADER.getDefaultTemplateName()
      );
    }
    String categoryName = nodeService
      .getProperty(categRef, ContentModel.PROP_NAME)
      .toString();
    model.put(CATEGORY_NAME, categoryName);
    model.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );
    model.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());
    result.setSubject(mail.getSubject(model));
    String userId = user.getUserId() != null ? user.getUserId() : "";
    NodeRef toUserRef = personService.getPerson(userId);
    model.put(IG_NAME, igName);
    model.put("toUserRef", toUserRef);
    model.put(REASON, body.getRejectedMessage());
    List<User> toUsers = Collections.singletonList(user);
    result.setUsers(toUsers);
    result.setContent(mail.getBody(model));
    return result;
  }
}
