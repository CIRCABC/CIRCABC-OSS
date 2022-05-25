package io.swagger.api;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.wai.dialog.content.edit.CreateContentBaseDialog.AttachementWrapper;
import io.swagger.exception.InvalidEmailException;
import io.swagger.exception.SwaggerRuntimeException;
import io.swagger.model.*;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.MessagingException;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author beaurpi
 */
public class EmailApiImpl implements EmailApi {

    public static final String ATTACHED_FILES = "attached_files";
    public static final String HTML = ".html";
    public static final String CATEGORY_NAME = "categoryName";
    public static final String CATEGORY_TITLE = "categoryTitle";
    public static final String FROM_USER = "fromUser";
    public static final String JUSTIFICATION = "justification";
    public static final String FAILED_TO_SEND_EMAIL_TO = "Failed to send email to ";
    public static final String CONTENT = "content";
    public static final String SUBJECT = "subject";
    public static final String GUEST = "guest";
    public static final String WRITE_ACCESS_DENIED = "Write access denied.";
    private static final String INTERNAL_COM = "com";
    private static final String INTERNAL_EXT = "ext";
    private static final Log logger = LogFactory.getLog(EmailApiImpl.class);
    private MailService mailService;
    private AuthorityService authorityService;
    private AuthenticationService authenticationService;
    private PersonService personService;
    private NodeService nodeService;
    private TemplateService templateService;
    private MailPreferencesService mailPreferencesService;
    private ManagementService managementService;
    private PermissionService permissionService;
    private ContentService contentService;
    private GroupsApi groupsApi;
    private ProfilesApi profilesApi;
    private UsersApi usersApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private LdapUserService ldapUserService;

    @Override
    public void groupsIdEmailPost(String id, EmailDefinition body)
            throws InvalidEmailException, MessagingException {

        if ("".equals(body.getSubject()) || "".equals(body.getContent())) {
            throw new InvalidEmailException("Empty subject or content for mail");
        }

        EmailDefinition realBody =
                prepareEmailForGroupMembersContact(Converter.createNodeRefFromId(id), body);

        // to ensure unique email address so mail is sent only once
        Set<String> mails = new HashSet<>();

        for (User u : body.getUsers()) {
            if (authorityService.authorityExists(u.getUserId()) && !"".equals(u.getEmail())) {
                mails.add(u.getEmail().toLowerCase());
            }
        }

        if (body.getProfiles() != null && !body.getProfiles().isEmpty()) {
            List<String> listOfGroups = new ArrayList<>();
            for (Profile p : body.getProfiles()) {
                if (authorityService.authorityExists(p.getGroupName())) {
                    listOfGroups.add(p.getGroupName());
                }
            }

            List<UserProfile> lUserProfiles = groupsApi.groupsIdMembersGet(id, null, null, null);
            for (UserProfile up : lUserProfiles) {
                if (listOfGroups.contains(up.getProfile().getGroupName())) {
                    mails.add(up.getUser().getEmail().toLowerCase());
                }
            }
        }

        List<String> listOfEmails = new ArrayList<>(mails);

        String currentUser = authenticationService.getCurrentUserName();
        if (!"".equals(currentUser)) {

            NodeRef currentUserRef = personService.getPerson(currentUser);
            String from = nodeService.getProperty(currentUserRef, ContentModel.PROP_EMAIL).toString();

            String fromDefault = mailService.getNoReplyEmailAddress();

            for (String email : listOfEmails) {
                try {
                    mailService.send(
                            fromDefault, email, from, body.getSubject(), realBody.getContent(), null);
                } catch (Exception e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Failed to send email to:" + email, e);
                    }
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
            MailTemplate mailTemplate) {

        final Map<QName, Serializable> personProperties = getNodeService().getProperties(person);
        final String to = (String) personProperties.get(ContentModel.PROP_EMAIL);
        String noReply = getMailService().getNoReplyEmailAddress();

        // load default template
        final NodeRef templatePerson = getTemplatePerson();
        final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(node, mailTemplate);
        final Map<String, Object> model =
                mailPreferencesService.buildDefaultModel(node, templatePerson, null);

        model.putAll(extraModelParams);
        // remove template noise
        String body = mail.getBody(model).replace("\n", "").replace("\r", "");
        String subject = mail.getSubject(model);

        if (to != null && to.length() != 0) {

            StringBuilder bodyToSend;

            if (usingTemplate != null) {
                final Map<String, Object> newModel =
                        mailPreferencesService.buildDefaultModel(node, person, null);
                newModel.putAll(extraModelParams);
                final NodeRef templateRef = Converter.createNodeRefFromId(usingTemplate);
                bodyToSend =
                        new StringBuilder(
                                templateService.processTemplate("freemarker", templateRef.toString(), newModel));
            } else {
                final String tempBody = applyParams(body, extraBodyParams);
                bodyToSend = new StringBuilder(tempBody.replace("\n", "").replace("\r", ""));

                if (extraModelParams.containsKey(ATTACHED_FILES)) {

                    List<AttachementWrapper> listW =
                            (List<AttachementWrapper>) extraModelParams.get(ATTACHED_FILES);

                    if (!listW.isEmpty()) {

                        bodyToSend.append("<h3>Linked files and folders</h3>");
                        bodyToSend.append("<ul>");

                        for (AttachementWrapper aw : listW) {
                            bodyToSend.append("<li>").append(buildLinkFromAttachmentWrapper(aw)).append("</li>");
                        }
                        bodyToSend.append("</ul>");
                    }
                }
            }

            try {
                // Send the message
                getMailService()
                        .send(
                                noReply,
                                to,
                                from,
                                applyParams(subject, extraBodyParams),
                                bodyToSend.toString(),
                                isMailHtml,
                                false);
            } catch (final MessagingException e) {
                // the parameters should be false
                if (logger.isWarnEnabled()) {
                    logger.warn(FAILED_TO_SEND_EMAIL_TO + to, e);
                }
            }
        }
    }

    private String buildLinkFromAttachmentWrapper(AttachementWrapper aw) {

        Node n = new Node(aw.getAttachRef());
        String result = "<a href=\"";
        result += WebClientHelper.getGeneratedWaiFullUrl(n, ExtendedURLMode.HTTP_WAI_BROWSE);
        result += "\">" + aw.getName() + "</a>";

        return result;
    }

    protected String applyParams(final String body, final Map<String, String> extraBodyParams) {
        String bodyToUpdate = body;
        for (final Entry<String, String> entry : extraBodyParams.entrySet()) {
            if (entry.getValue() != null) {
                bodyToUpdate = bodyToUpdate.replace(entry.getKey(), entry.getValue());
            }
        }
        return bodyToUpdate;
    }

    protected NodeRef getTemplatePerson() {
        final String templateUser = mailPreferencesService.getTemplateUserDetails().getUserName();
        return getPersonService().getPerson(templateUser);
    }

    protected NodeRef getCurrentPerson() {
        final String currentUsername = AuthenticationUtil.getFullyAuthenticatedUser();
        return getPersonService().getPerson(currentUsername);
    }

    /**
     * @return the mailService
     */
    public MailService getMailService() {
        return mailService;
    }

    /**
     * @param mailService the mailService to set
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @param contentService the contentService to set
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @param currentUserPermissionCheckerService the currentUserPermissionCheckerService to set
     */
    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    /**
     * @return the authorityService
     */
    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * @return the personService
     */
    public PersonService getPersonService() {
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param templateService the templateService to set
     */
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * @param mailPreferencesService the mailPreferencesService to set
     */
    public void setMailPreferencesService(MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
    }

    /**
     * @param managementService the managementService to set
     */
    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @return the ldapUserService
     */
    public LdapUserService getLdapUserService() {
        return ldapUserService;
    }

    /**
     * @param ldapUserService the ldapUserService to set
     */
    public void setLdapUserService(LdapUserService ldapUserService) {
        this.ldapUserService = ldapUserService;
    }

    public GroupsApi getGroupsApi() {
        return groupsApi;
    }

    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    public ProfilesApi getProfilesApi() {
        return profilesApi;
    }

    public void setProfilesApi(ProfilesApi profilesApi) {
        this.profilesApi = profilesApi;
    }

    public UsersApi getUsersApi() {
        return usersApi;
    }

    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    @Override
    public EmailDefinition prepareEmailForGroupRequest(
            GroupCreationRequest body, List<User> toUsers) {

        EmailDefinition result = new EmailDefinition();
        final Map<String, Object> model = mailPreferencesService.buildDefaultModel(null, null, null);
        NodeRef categRef = Converter.createNodeRefFromId(body.getCategoryRef());
        final MailWrapper mail =
                mailPreferencesService.getDefaultMailTemplate(categRef, MailTemplate.GROUP_REQUEST);

        String categoryName = nodeService.getProperty(categRef, ContentModel.PROP_NAME).toString();
        model.put(CATEGORY_NAME, categoryName);
        model.put("categRef", categRef);

        MLText categoryTitle = (MLText) nodeService.getProperty(categRef, ContentModel.PROP_TITLE);
        if (categoryTitle != null) {
            model.put(CATEGORY_TITLE, categoryTitle.getDefaultValue());
        } else {
            model.put(CATEGORY_TITLE, "");
        }

        model.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());

        model.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());

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

    @Override
    public void mailPost(EmailDefinition email) {
        String noReply = getMailService().getNoReplyEmailAddress();
        mailPost(email, noReply);
    }

    private void mailPost(EmailDefinition email, String noReply) {

        for (User toUser : email.getUsers()) {
            try {
                // Send the message
                getMailService()
                        .send(
                                noReply,
                                toUser.getEmail(),
                                noReply,
                                email.getSubject(),
                                email.getContent(),
                                true,
                                true);
            } catch (final MessagingException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(FAILED_TO_SEND_EMAIL_TO + toUser.getEmail(), e);
                }
            }
        }
    }

    @Override
    public void groupsIdLeadersEmailPost(String id, String content)
            throws InvalidEmailException, MessagingException {

        EmailDefinition realBody =
                prepareEmailForGroupLeadersContact(Converter.createNodeRefFromId(id), content);

        Set<String> listOfEmails = new HashSet<>();

        List<Profile> profiles = profilesApi.groupsIdProfilesGet(id, "", true);
        for (Profile groupProfile : profiles) {
            Map<String, String> perms = groupProfile.getPermissions();
            if (perms.get("library").equals(LibraryPermissions.LIBADMIN.toString())
                    && perms.get("events").equals(EventPermissions.EVEADMIN.toString())
                    && perms.get("members").equals(DirectoryPermissions.DIRADMIN.toString())
                    && perms.get("newsgroups").equals(NewsGroupPermissions.NWSADMIN.toString())
                    && perms.get("information").equals(InformationPermissions.INFADMIN.toString())) {

                List<String> profile = new ArrayList<>();
                profile.add(groupProfile.getGroupName());
                List<UserProfile> groupAdmins = groupsApi.groupsIdMembersGet(id, profile, null, null);

                for (UserProfile leader : groupAdmins) {
                    listOfEmails.add(leader.getUser().getEmail());
                }
            }
        }

        String currentUser = authenticationService.getCurrentUserName();
        if (!"".equals(currentUser)) {

            NodeRef currentUserRef = personService.getPerson(currentUser);
            String from = nodeService.getProperty(currentUserRef, ContentModel.PROP_EMAIL).toString();

            List<String> emailAddresses = new ArrayList<>(listOfEmails);

            String fromDefault = mailService.getNoReplyEmailAddress();

            mailService.send(
                    fromDefault,
                    emailAddresses,
                    from,
                    realBody.getSubject(),
                    realBody.getContent(),
                    new ArrayList<NodeRef>());
        }
    }

    @Override
    public EmailDefinition prepareEmailForAdminContact(
            NodeRef categRef, AdminContactRequest body, List<String> emails) {

        EmailDefinition result = new EmailDefinition();
        final Map<String, Object> model = mailPreferencesService.buildDefaultModel(null, null, null);

        final MailWrapper mail =
                mailPreferencesService.getDefaultMailTemplate(
                        categRef, MailTemplate.CATEGORY_ADMIN_CONTACT);

        String categoryName = nodeService.getProperty(categRef, ContentModel.PROP_NAME).toString();
        model.put(CATEGORY_NAME, categoryName);

        MLText categoryTitle = (MLText) nodeService.getProperty(categRef, ContentModel.PROP_TITLE);
        if (categoryTitle != null) {
            model.put(CATEGORY_TITLE, categoryTitle.getDefaultValue());
        } else {
            model.put(CATEGORY_TITLE, categoryName);
        }

        model.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());

        model.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());

        result.setSubject(mail.getSubject(model));

        String username = authenticationService.getCurrentUserName();
        User user = usersApi.usersUserIdGet(username);

        model.put(FROM_USER, user);
        model.put(CONTENT, body.getContent());

        List<User> toUsers = new ArrayList<>();
        for (String email : emails) {
            User u = new User();
            u.setEmail(email);
            toUsers.add(u);
        }
        result.setUsers(toUsers);
        result.setContent(mail.getBody(model));
        return result;
    }

    @Override
    public EmailDefinition prepareConfirmationForAdminContact(NodeRef categoryRef, String content) {
        EmailDefinition result = new EmailDefinition();
        final Map<String, Object> model = mailPreferencesService.buildDefaultModel(null, null, null);

        final MailWrapper mail =
                mailPreferencesService.getDefaultMailTemplate(
                        categoryRef, MailTemplate.CATEGORY_ADMIN_CONTACT_CONFIRMATION);

        String categoryName = nodeService.getProperty(categoryRef, ContentModel.PROP_NAME).toString();
        model.put(CATEGORY_NAME, categoryName);

        MLText categoryTitle = (MLText) nodeService.getProperty(categoryRef, ContentModel.PROP_TITLE);
        if (categoryTitle != null) {
            model.put(CATEGORY_TITLE, categoryTitle.getDefaultValue());
        } else {
            model.put(CATEGORY_TITLE, categoryName);
        }

        model.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());

        model.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());

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
     * @see io.swagger.api.EmailApi#getUserMailTemplates()
     */
    @Override
    public List<MailTemplateDefinition> getUserMailTemplates() {

        List<MailTemplateDefinition> templates = new ArrayList<>();

        NodeRef userIdMailTemplatesNodeRef = getOrCreateUserMailTemplatesSpaceNodeRef();

        if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
                userIdMailTemplatesNodeRef.getId())) {
            throw new SwaggerRuntimeException(WRITE_ACCESS_DENIED);
        }

        List<ChildAssociationRef> childrenRef = nodeService.getChildAssocs(userIdMailTemplatesNodeRef);

        for (ChildAssociationRef childRef : childrenRef) {

            NodeRef childNodeRef = childRef.getChildRef();

            ContentReader reader = contentService.getReader(childNodeRef, ContentModel.PROP_CONTENT);

            if (reader == null) {
                logger.warn(childNodeRef.getId() + " has no content (reader == null). Will not be added.");
                continue;
            }

            String text = reader.getContentString();

            String name = (String) nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME);

            if (name == null || name.length() == 0) {
                logger.warn(childNodeRef.getId() + " has an empty name. Will not be added.");
                continue;
            }

            String subject = (String) nodeService.getProperty(childNodeRef, ContentModel.PROP_SUBJECT);

            templates.add(
                    new MailTemplateDefinition(
                            childNodeRef.getId(),
                            name.endsWith(HTML) ? name.substring(0, name.length() - 5) : name,
                            subject,
                            text));
        }

        return templates;
    }

    /**
     * @see io.swagger.api.EmailApi#saveUserMailTemplate(java.lang.String, java.lang.String,
     * java.lang.String, boolean)
     */
    @Override
    public String saveUserMailTemplate(
            String templateName, String templateSubject, String templateText, boolean overwrite) {

        NodeRef userIdMailTemplatesNodeRef = getOrCreateUserMailTemplatesSpaceNodeRef();

        if (!this.currentUserPermissionCheckerService.hasAlfrescoWritePermission(
                userIdMailTemplatesNodeRef.getId())) {
            throw new SwaggerRuntimeException(WRITE_ACCESS_DENIED);
        }

        NodeRef contentRef = null;

        if (overwrite) {
            contentRef =
                    nodeService.getChildByName(
                            userIdMailTemplatesNodeRef, ContentModel.ASSOC_CONTAINS, templateName + HTML);
        }

        if (contentRef == null) {
            Map<QName, Serializable> properties = new HashMap<>(1);
            properties.put(ContentModel.PROP_NAME, templateName + HTML);

            QName associationNameQName =
                    QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), templateName + HTML);
            contentRef =
                    nodeService
                            .createNode(
                                    userIdMailTemplatesNodeRef,
                                    ContentModel.ASSOC_CONTAINS,
                                    associationNameQName,
                                    ContentModel.TYPE_CONTENT,
                                    properties)
                            .getChildRef();
            properties = new HashMap<>(1);
            nodeService.addAspect(contentRef, ContentModel.ASPECT_EMAILED, properties);
        }

        nodeService.setProperty(contentRef, ContentModel.PROP_SUBJECT, templateSubject);

        ContentWriter writer = contentService.getWriter(contentRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
        writer.setEncoding("UTF-8");
        writer.putContent(templateText);

        return contentRef.getId();
    }

    /**
     * @see io.swagger.api.EmailApi#deleteUserMailTemplates(java.lang.String)
     */
    @Override
    public void deleteUserMailTemplates(String templateIds) {

        NodeRef userIdMailTemplatesNodeRef = getOrCreateUserMailTemplatesSpaceNodeRef();

        if (!this.currentUserPermissionCheckerService.hasAlfrescoWritePermission(
                userIdMailTemplatesNodeRef.getId())) {
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
                new AuthenticationUtil.RunAsWork<NodeRef>() {

                    public NodeRef doWork() {

                        NodeRef dicoRef = managementService.getCircabcDictionaryNodeRef();
                        NodeRef templatesNodeRef =
                                nodeService.getChildByName(dicoRef, ContentModel.ASSOC_CONTAINS, "templates");
                        NodeRef mailsNodeRef =
                                nodeService.getChildByName(templatesNodeRef, ContentModel.ASSOC_CONTAINS, "mails");

                        if (mailsNodeRef == null || !nodeService.exists(mailsNodeRef)) {
                            logger.error(
                                    "The CIRCABC mail templates root could not be found: dictionary/CircaBC/templates/mails");
                            throw new SwaggerRuntimeException(
                                    "The CIRCABC mail templates root could not be found: dictionary/CircaBC/templates/mails");
                        }

                        NodeRef userMailTemplatesNodeRef =
                                nodeService.getChildByName(
                                        mailsNodeRef, ContentModel.ASSOC_CONTAINS, "UserMailTemplates");
                        if (userMailTemplatesNodeRef == null) {
                            userMailTemplatesNodeRef = createNamedFolder(mailsNodeRef, "UserMailTemplates");
                        }

                        String userId = AuthenticationUtil.getFullyAuthenticatedUser();

                        NodeRef userIdMailTemplatesNodeRef =
                                nodeService.getChildByName(
                                        userMailTemplatesNodeRef, ContentModel.ASSOC_CONTAINS, userId);
                        if (userIdMailTemplatesNodeRef == null) {
                            userIdMailTemplatesNodeRef = createNamedFolder(userMailTemplatesNodeRef, userId);
                            permissionService.deletePermissions(userIdMailTemplatesNodeRef);
                            permissionService.setInheritParentPermissions(userIdMailTemplatesNodeRef, false);
                            permissionService.setPermission(
                                    userIdMailTemplatesNodeRef, userId, PermissionService.WRITE, true);
                            permissionService.setPermission(
                                    userIdMailTemplatesNodeRef, userId, PermissionService.READ, true);
                        }

                        return userIdMailTemplatesNodeRef;
                    }
                },
                AuthenticationUtil.getAdminUserName());
    }

    private NodeRef createNamedFolder(NodeRef parentRef, String name) {

        Map<QName, Serializable> properties = new HashMap<>(1);
        properties.put(ContentModel.PROP_NAME, name);
        QName associationNameQName = QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), name);
        return nodeService
                .createNode(
                        parentRef,
                        ContentModel.ASSOC_CONTAINS,
                        associationNameQName,
                        ContentModel.TYPE_FOLDER,
                        properties)
                .getChildRef();
    }

    private EmailDefinition prepareEmailForGroupMembersContact(NodeRef igRef, EmailDefinition body) {

        EmailDefinition result = new EmailDefinition();
        final Map<String, Object> model = mailPreferencesService.buildDefaultModel(igRef, null, null);

        final MailWrapper mail =
                mailPreferencesService.getDefaultMailTemplate(igRef, MailTemplate.GROUP_MEMBERS_CONTACT);

        model.put("interestGroup", igRef);

        NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
        model.put("category", categoryRef);

        result.setSubject(mail.getSubject(model));

        String username = authenticationService.getCurrentUserName();
        NodeRef userRef = personService.getPerson(username);

        model.put(FROM_USER, userRef);

        model.put(SUBJECT, body.getSubject());

        model.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());

        model.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());

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

    private EmailDefinition prepareEmailForGroupLeadersContact(NodeRef igRef, String message) {

        EmailDefinition result = new EmailDefinition();
        final Map<String, Object> model = mailPreferencesService.buildDefaultModel(igRef, null, null);

        final MailWrapper mail =
                mailPreferencesService.getDefaultMailTemplate(igRef, MailTemplate.GROUP_LEADERS_CONTACT);

        model.put("interestGroup", igRef);

        NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
        model.put("category", categoryRef);

        String username = authenticationService.getCurrentUserName();
        NodeRef userRef = personService.getPerson(username);
        model.put(FROM_USER, userRef);

        model.put("message", message);

        model.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());

        model.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());

        result.setContent(mail.getBody(model));
        result.setSubject(mail.getSubject(model));
        return result;
    }

    @Override
    public EmailDefinition prepareEmailForHelpdeskContact(
            String reason, String name, String emailFrom, String subject, String content) {

        String currentUser = authenticationService.getCurrentUserName();
        if (currentUser.equals(GUEST)) {
            AuthenticationUtil.setRunAsUserSystem();
        }

        EmailDefinition result = new EmailDefinition();
        Map<String, Object> model = new HashMap<>();

        model.put("reason", reason.replace(".", "_"));
        model.put(SUBJECT, subject);
        model.put(CONTENT, content);
        model.put("name", name);
        model.put("emailFrom", emailFrom);

        model.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());
        model.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());
        CircabcConfiguration.addApplicationNameToModel(model);

        NodeRef cbcRef = managementService.getCircabcNodeRef();

        final MailWrapper mail =
                mailPreferencesService.getDefaultMailTemplate(cbcRef, MailTemplate.HELPDESK_CONTACT);

        result.setSubject(mail.getSubject(model));
        result.setContent(mail.getBody(model));

        User helpdesk = new User();

        if (GUEST.equals(currentUser)) {
            helpdesk.setEmail(CircabcConfiguration.getProperty("mail.from.circabc.helpdesk"));
        } else {
            CircabcUserDataBean userDataBean = ldapUserService.getLDAPUserDataByUid(currentUser);
            String org = userDataBean.getSourceOrganisation();

            if (INTERNAL_COM.equalsIgnoreCase(org) || INTERNAL_EXT.equalsIgnoreCase(org)) {
                helpdesk.setEmail(CircabcConfiguration.getProperty("mail.from.circabc.it.helpdesk"));
            } else {
                helpdesk.setEmail(CircabcConfiguration.getProperty("mail.from.circabc.helpdesk"));
            }
        }

        result.getUsers().add(helpdesk);

        if (currentUser.equals(GUEST)) {
            AuthenticationUtil.setRunAsUser(GUEST);
        }

        return result;
    }

    @Override
    public EmailDefinition prepareConfirmationForHelpdeskContact(
            String reason, String name, String emailFrom, String subject, String content) {

        String currentUser = authenticationService.getCurrentUserName();
        if (currentUser.equals(GUEST)) {
            AuthenticationUtil.setRunAsUserSystem();
        }

        EmailDefinition result = new EmailDefinition();
        Map<String, Object> model = new HashMap<>();

        model.put("name", name);
        model.put("emailFrom", emailFrom);
        model.put(CONTENT, content);
        model.put("reason", reason.replace(".", "_"));
        model.put(SUBJECT, subject);

        model.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());
        model.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());
        CircabcConfiguration.addApplicationNameToModel(model);

        NodeRef cbcRef = managementService.getCircabcNodeRef();

        final MailWrapper mail =
                mailPreferencesService.getDefaultMailTemplate(
                        cbcRef, MailTemplate.HELPDESK_CONTACT_CONFIRMATION);

        result.setSubject(mail.getSubject(model));
        result.setContent(mail.getBody(model));

        User helpdesk = new User();
        helpdesk.setEmail(emailFrom);
        result.getUsers().add(helpdesk);

        if (currentUser.equals(GUEST)) {
            AuthenticationUtil.setRunAsUser(GUEST);
        }

        return result;
    }

    @Override
    public void mailPost(EmailDefinition emailDefinition, List<File> attachement) {
        String noReply = getMailService().getNoReplyEmailAddress();

        for (User toUser : emailDefinition.getUsers()) {
            try {
                // Send the message
                getMailService()
                        .sendWithAttachment(
                                noReply,
                                toUser.getEmail(),
                                noReply,
                                emailDefinition.getSubject(),
                                emailDefinition.getContent(),
                                true,
                                true,
                                attachement);
            } catch (final MessagingException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(FAILED_TO_SEND_EMAIL_TO + toUser.getEmail(), e);
                }
            }
        }
    }

    @Override
    public EmailDefinition prepareRefusalGroupRequest(
            GroupCreationRequest intialRequest, String argument) {
        EmailDefinition result = new EmailDefinition();
        final Map<String, Object> model = mailPreferencesService.buildDefaultModel(null, null, null);
        NodeRef categRef = Converter.createNodeRefFromId(intialRequest.getCategoryRef());

        final List<MailWrapper> mails =
                mailPreferencesService.getMailTemplates(
                        categRef, MailTemplate.CATEGORY_GROUP_REQUEST_REFUSE);
        MailWrapper mail = null;

        for (MailWrapper mailTemplate : mails) {
            if (mailTemplate
                    .getName()
                    .equals(MailTemplate.CATEGORY_GROUP_REQUEST_REFUSE.getDefaultTemplateName())) {
                mail = mailTemplate;
            }
        }
        if (mail == null) {
            throw new IllegalStateException(
                    "Email template does not exists : "
                            + MailTemplate.CATEGORY_GROUP_REQUEST_REFUSE.getDefaultTemplateName());
        }
        String categoryName = nodeService.getProperty(categRef, ContentModel.PROP_NAME).toString();
        model.put(CATEGORY_NAME, categoryName);

        MLText categoryTitle = (MLText) nodeService.getProperty(categRef, ContentModel.PROP_TITLE);
        if (categoryTitle != null) {
            model.put(CATEGORY_TITLE, categoryTitle.getDefaultValue());
        } else {
            model.put(CATEGORY_TITLE, categoryName);
        }

        model.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());

        model.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());

        result.setSubject(mail.getSubject(model));

        String username = authenticationService.getCurrentUserName();
        User user = usersApi.usersUserIdGet(username);

        NodeRef fromUserRef = personService.getPerson(intialRequest.getFrom().getUserId());
        User fromUser = usersApi.usersUserIdGet(intialRequest.getFrom().getUserId());

        model.put("reviewer", user);
        model.put(JUSTIFICATION, intialRequest.getArgument());
        model.put(FROM_USER, fromUser);
        model.put("fromUserRef", fromUserRef);

        List<User> toUsers = new ArrayList<>();
        toUsers.add(fromUser);
        result.setUsers(toUsers);
        result.setContent(mail.getBody(model));
        return result;
    }

    @Override
    public EmailDefinition prepareAcceptationGroupRequest(
            GroupCreationRequest intialRequest, String argument) {
        EmailDefinition result = new EmailDefinition();
        final Map<String, Object> model = mailPreferencesService.buildDefaultModel(null, null, null);
        NodeRef categRef = Converter.createNodeRefFromId(intialRequest.getCategoryRef());

        final List<MailWrapper> mails =
                mailPreferencesService.getMailTemplates(
                        categRef, MailTemplate.CATEGORY_GROUP_REQUEST_ACCEPT);
        MailWrapper mail = null;

        for (MailWrapper mailTemplate : mails) {
            if (mailTemplate
                    .getName()
                    .equals(MailTemplate.CATEGORY_GROUP_REQUEST_ACCEPT.getDefaultTemplateName())) {
                mail = mailTemplate;
            }
        }

        if (mail == null) {
            throw new IllegalStateException(
                    "Email template does not exists : "
                            + MailTemplate.CATEGORY_GROUP_REQUEST_ACCEPT.getDefaultTemplateName());
        }

        String categoryName = nodeService.getProperty(categRef, ContentModel.PROP_NAME).toString();
        model.put(CATEGORY_NAME, categoryName);

        MLText categoryTitle = (MLText) nodeService.getProperty(categRef, ContentModel.PROP_TITLE);
        if (categoryTitle != null) {
            model.put(CATEGORY_TITLE, categoryTitle.getDefaultValue());
        } else {
            model.put(CATEGORY_TITLE, categoryName);
        }

        model.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());

        model.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());

        result.setSubject(mail.getSubject(model));

        String username = authenticationService.getCurrentUserName();
        User user = usersApi.usersUserIdGet(username);

        NodeRef fromUserRef = personService.getPerson(intialRequest.getFrom().getUserId());
        User fromUser = usersApi.usersUserIdGet(intialRequest.getFrom().getUserId());

        model.put("reviewer", user);
        model.put(JUSTIFICATION, intialRequest.getArgument());
        model.put(FROM_USER, fromUser);
        model.put("fromUserRef", fromUserRef);

        List<User> toUsers = new ArrayList<>();
        toUsers.add(fromUser);
        result.setUsers(toUsers);
        result.setContent(mail.getBody(model));
        return result;
    }
}
