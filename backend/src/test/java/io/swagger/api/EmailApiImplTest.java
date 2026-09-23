package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.mail.MailPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.mail.MailWrapper;
import eu.europa.ec.digit.circabc.rest.service.user.LdapUserService;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.InvalidEmailException;
import io.swagger.model.*;
import io.swagger.util.CurrentUserPermissionCheckerService;
import jakarta.mail.MessagingException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class EmailApiImplTest {

  private EmailApiImpl emailApi;

  private MailService mailService;
  private AuthorityService authorityService;
  private AuthenticationService authenticationService;
  private PersonService personService;
  private NodeService nodeService;
  private MailPreferencesService mailPreferencesService;
  private GroupsApi groupsApi;
  private ProfilesApi profilesApi;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private CircabcApi circabcApi;
  private CircabcConfig circabcConfig;
  private ContentService contentService;
  private LdapUserService ldapUserService;
  private TemplateService templateService;

  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "test-id"
  );

  @Before
  public void setUp() throws Exception {
    // Initialize AuthenticationUtil
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    emailApi = new EmailApiImpl();

    mailService = mock(MailService.class);
    authorityService = mock(AuthorityService.class);
    authenticationService = mock(AuthenticationService.class);
    personService = mock(PersonService.class);
    nodeService = mock(NodeService.class);
    mailPreferencesService = mock(MailPreferencesService.class);
    groupsApi = mock(GroupsApi.class);
    profilesApi = mock(ProfilesApi.class);
    usersApi = mock(UsersApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    circabcApi = mock(CircabcApi.class);
    circabcConfig = mock(CircabcConfig.class);
    contentService = mock(ContentService.class);
    ldapUserService = mock(LdapUserService.class);
    templateService = mock(TemplateService.class);

    setField("mailService", mailService);
    setField("authorityService", authorityService);
    setField("authenticationService", authenticationService);
    setField("personService", personService);
    setField("nodeService", nodeService);
    setField("mailPreferencesService", mailPreferencesService);
    setField("groupsApi", groupsApi);
    setField("profilesApi", profilesApi);
    setField("usersApi", usersApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("circabcApi", circabcApi);
    setField("circabcConfig", circabcConfig);
    setField("contentService", contentService);
    setField("ldapUserService", ldapUserService);
    setField("templateService", templateService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = EmailApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(emailApi, value);
  }

  @Test(expected = InvalidEmailException.class)
  public void testGroupsIdEmailPost_whenEmptySubject_thenThrowsInvalidEmailException()
    throws Exception {
    EmailDefinition body = new EmailDefinition();
    body.setSubject("");
    body.setContent("some content");

    emailApi.groupsIdEmailPost("test-id", body);
  }

  @Test(expected = InvalidEmailException.class)
  public void testGroupsIdEmailPost_whenEmptyContent_thenThrowsInvalidEmailException()
    throws Exception {
    EmailDefinition body = new EmailDefinition();
    body.setSubject("some subject");
    body.setContent("");

    emailApi.groupsIdEmailPost("test-id", body);
  }

  @Test
  public void testGroupsIdEmailPost_whenValidInput_thenSendsEmails()
    throws Exception {
    String igId = "test-id";
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, igId);
    NodeRef categoryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id"
    );
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-ref"
    );
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-ref"
    );

    User user = new User();
    user.setUserId("user1");
    user.setEmail("user1@test.com");

    EmailDefinition body = new EmailDefinition();
    body.setSubject("Test Subject");
    body.setContent("Test Content");
    body.setUsers(Collections.singletonList(user));
    body.setAttachments(new ArrayList<>());

    when(authorityService.authorityExists("user1")).thenReturn(true);
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(personService.getPerson("testuser")).thenReturn(userRef);
    when(nodeService.getProperty(userRef, ContentModel.PROP_EMAIL)).thenReturn(
      "testuser@test.com"
    );
    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");

    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(categoryRef);
    when(nodeService.getPrimaryParent(igRef)).thenReturn(parentAssoc);

    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.buildDefaultModel(eq(igRef), isNull(), isNull())
    ).thenReturn(new HashMap<>());
    when(
      mailPreferencesService.getDefaultMailTemplate(
        igRef,
        MailTemplate.GROUP_MEMBERS_CONTACT
      )
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap())).thenReturn("Prepared Subject");
    when(mailWrapper.getBody(anyMap())).thenReturn("Prepared Body");

    when(circabcApi.getCompanyHomeNodeRef()).thenReturn(companyHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);

    emailApi.groupsIdEmailPost(igId, body);

    verify(mailService).send(
      eq("noreply@test.com"),
      eq("user1@test.com"),
      eq("testuser@test.com"),
      anyString(),
      anyString(),
      isNull()
    );
  }

  @Test
  public void testMailPost_whenValidEmail_thenSendsToAllUsers()
    throws Exception {
    User user1 = new User();
    user1.setEmail("user1@test.com");
    User user2 = new User();
    user2.setEmail("user2@test.com");

    EmailDefinition email = new EmailDefinition();
    email.setSubject("Subject");
    email.setContent("Content");
    email.setUsers(Arrays.asList(user1, user2));

    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");

    emailApi.mailPost(email);

    verify(mailService, times(2)).sendWithAttachment(
      eq("noreply@test.com"),
      anyString(),
      eq("noreply@test.com"),
      eq("Subject"),
      eq("Content"),
      eq(true),
      eq(true),
      isNull()
    );
  }

  @Test
  public void testMailPost_whenInvalidEmail_thenSkips() throws Exception {
    User user = new User();
    user.setEmail("not-an-email");

    EmailDefinition email = new EmailDefinition();
    email.setSubject("Subject");
    email.setContent("Content");
    email.setUsers(Collections.singletonList(user));

    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");

    emailApi.mailPost(email);

    verify(mailService, never()).sendWithAttachment(
      anyString(),
      anyString(),
      anyString(),
      anyString(),
      anyString(),
      anyBoolean(),
      anyBoolean(),
      anyList()
    );
  }

  @Test
  public void testApplyParams_whenParamsProvided_thenReplacesPlaceholders() {
    Map<String, String> params = new HashMap<>();
    params.put("{name}", "John");
    params.put("{role}", "Admin");

    String result = emailApi.applyParams(
      "Hello {name}, you are {role}.",
      params
    );

    assertEquals("Hello John, you are Admin.", result);
  }

  @Test
  public void testApplyParams_whenNullValue_thenSkipsReplacement() {
    Map<String, String> params = new HashMap<>();
    params.put("{name}", null);
    params.put("{role}", "Admin");

    String result = emailApi.applyParams(
      "Hello {name}, you are {role}.",
      params
    );

    assertEquals("Hello {name}, you are Admin.", result);
  }

  @Test
  public void testPrepareEmailForHelpdeskContact_whenGuestUser_thenUsesHelpdeskMail() {
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-ref"
    );

    when(authenticationService.getCurrentUserName()).thenReturn("guest");
    when(circabcApi.getCompanyHomeNodeRef()).thenReturn(companyHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(circabcConfig.getHelpDeskMail()).thenReturn("helpdesk@test.com");

    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.getDefaultMailTemplate(
        circabcRef,
        MailTemplate.HELPDESK_CONTACT
      )
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap())).thenReturn("Helpdesk Subject");
    when(mailWrapper.getBody(anyMap())).thenReturn("Helpdesk Body");

    EmailDefinition result = emailApi.prepareEmailForHelpdeskContact(
      "technical",
      "Test User",
      "user@test.com",
      "Help needed",
      "I need help"
    );

    assertNotNull(result);
    assertEquals("Helpdesk Subject", result.getSubject());
    assertEquals("Helpdesk Body", result.getContent());
    assertEquals(1, result.getUsers().size());
    assertEquals("helpdesk@test.com", result.getUsers().get(0).getEmail());
  }

  @Test
  public void testPrepareEmailForHelpdeskContact_whenInternalUser_thenUsesItHelpdesk() {
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-ref"
    );

    when(authenticationService.getCurrentUserName()).thenReturn("internaluser");

    CircabcUserDataBean userDataBean = mock(CircabcUserDataBean.class);
    when(userDataBean.getSourceOrganisation()).thenReturn("com");
    when(ldapUserService.getLDAPUserDataByUid("internaluser")).thenReturn(
      userDataBean
    );

    when(circabcApi.getCompanyHomeNodeRef()).thenReturn(companyHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(circabcConfig.getItHelpDeskMail()).thenReturn("it-helpdesk@test.com");

    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.getDefaultMailTemplate(
        circabcRef,
        MailTemplate.HELPDESK_CONTACT
      )
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap())).thenReturn("Subject");
    when(mailWrapper.getBody(anyMap())).thenReturn("Body");

    EmailDefinition result = emailApi.prepareEmailForHelpdeskContact(
      "technical",
      "Internal User",
      "internal@ec.europa.eu",
      "Help",
      "Content"
    );

    assertEquals("it-helpdesk@test.com", result.getUsers().get(0).getEmail());
  }

  @Test
  public void testPrepareConfirmationForHelpdeskContact_whenSmtTicketProvided_thenAppendsToSubject() {
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-ref"
    );

    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(circabcApi.getCompanyHomeNodeRef()).thenReturn(companyHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);

    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.getDefaultMailTemplate(
        circabcRef,
        MailTemplate.HELPDESK_CONTACT_CONFIRMATION
      )
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap())).thenReturn("Confirmation");
    when(mailWrapper.getBody(anyMap())).thenReturn("Body");

    EmailDefinition result = emailApi.prepareConfirmationForHelpdeskContact(
      "technical",
      "User",
      "user@test.com",
      "Subject",
      "Content",
      "SMT-12345"
    );

    assertEquals("Confirmation - SMT-12345", result.getSubject());
    assertEquals("user@test.com", result.getUsers().get(0).getEmail());
  }

  @Test
  public void testPrepareConfirmationForHelpdeskContact_whenNoSmtTicket_thenSubjectUnchanged() {
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-ref"
    );

    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(circabcApi.getCompanyHomeNodeRef()).thenReturn(companyHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);

    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.getDefaultMailTemplate(
        circabcRef,
        MailTemplate.HELPDESK_CONTACT_CONFIRMATION
      )
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap())).thenReturn("Confirmation");
    when(mailWrapper.getBody(anyMap())).thenReturn("Body");

    EmailDefinition result = emailApi.prepareConfirmationForHelpdeskContact(
      "technical",
      "User",
      "user@test.com",
      "Subject",
      "Content",
      null
    );

    assertEquals("Confirmation", result.getSubject());
  }

  @Test
  public void testGetUserMailTemplates_whenNoReadPermission_thenThrowsException()
    throws Exception {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef templatesRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "templates"
    );
    NodeRef mailsRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "mails"
    );
    NodeRef userTemplatesRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-templates"
    );
    NodeRef userIdRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-id-ref"
    );

    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(dicoRef);
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "templates"
      )
    ).thenReturn(templatesRef);
    when(
      nodeService.getChildByName(
        templatesRef,
        ContentModel.ASSOC_CONTAINS,
        "mails"
      )
    ).thenReturn(mailsRef);
    when(nodeService.exists(mailsRef)).thenReturn(true);
    when(
      nodeService.getChildByName(
        mailsRef,
        ContentModel.ASSOC_CONTAINS,
        "UserMailTemplates"
      )
    ).thenReturn(userTemplatesRef);
    when(
      nodeService.getChildByName(
        userTemplatesRef,
        ContentModel.ASSOC_CONTAINS,
        "testuser"
      )
    ).thenReturn(userIdRef);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        userIdRef.getId()
      )
    ).thenReturn(false);

    try {
      emailApi.getUserMailTemplates();
      fail("Expected SwaggerRuntimeException");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Write access denied"));
    }
  }

  @Test
  public void testDeleteUserMailTemplates_whenNodeDoesNotExist_thenSkips()
    throws Exception {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef templatesRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "templates"
    );
    NodeRef mailsRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "mails"
    );
    NodeRef userTemplatesRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-templates"
    );
    NodeRef userIdRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-id-ref"
    );

    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(dicoRef);
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "templates"
      )
    ).thenReturn(templatesRef);
    when(
      nodeService.getChildByName(
        templatesRef,
        ContentModel.ASSOC_CONTAINS,
        "mails"
      )
    ).thenReturn(mailsRef);
    when(nodeService.exists(mailsRef)).thenReturn(true);
    when(
      nodeService.getChildByName(
        mailsRef,
        ContentModel.ASSOC_CONTAINS,
        "UserMailTemplates"
      )
    ).thenReturn(userTemplatesRef);
    when(
      nodeService.getChildByName(
        userTemplatesRef,
        ContentModel.ASSOC_CONTAINS,
        "testuser"
      )
    ).thenReturn(userIdRef);
    when(
      currentUserPermissionCheckerService.hasAlfrescoWritePermission(
        userIdRef.getId()
      )
    ).thenReturn(true);

    NodeRef templateNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "template-1"
    );
    when(nodeService.exists(templateNodeRef)).thenReturn(false);

    emailApi.deleteUserMailTemplates("template-1");

    verify(nodeService, never()).removeChild(
      any(NodeRef.class),
      any(NodeRef.class)
    );
  }

  // --- prepareEmailForAdminContact ---

  @Test
  public void testPrepareEmailForAdminContact_whenValidEmails_thenReturnsDefinition() {
    NodeRef categRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id"
    );
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-ref"
    );

    AdminContactRequest body = new AdminContactRequest();
    body.setContent("Need help with category");

    when(mailPreferencesService.buildDefaultModel(null, null, null)).thenReturn(
      new java.util.HashMap<>()
    );
    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.getDefaultMailTemplate(
        categRef,
        MailTemplate.CATEGORY_ADMIN_CONTACT
      )
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap())).thenReturn("Admin Contact Subject");
    when(mailWrapper.getBody(anyMap())).thenReturn("Admin Contact Body");

    when(nodeService.getProperty(categRef, ContentModel.PROP_NAME)).thenReturn(
      "TestCategory"
    );
    when(nodeService.getProperty(categRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );
    when(circabcApi.getCompanyHomeNodeRef()).thenReturn(companyHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    User currentUser = new User();
    currentUser.setUserId("testuser");
    when(usersApi.usersUserIdGet("testuser")).thenReturn(currentUser);

    List<String> emails = java.util.Arrays.asList(
      "admin1@test.com",
      "admin2@test.com"
    );

    EmailDefinition result = emailApi.prepareEmailForAdminContact(
      categRef,
      body,
      emails
    );

    assertNotNull(result);
    assertEquals("Admin Contact Subject", result.getSubject());
    assertEquals("Admin Contact Body", result.getContent());
    assertEquals(2, result.getUsers().size());
  }

  @Test
  public void testPrepareEmailForAdminContact_whenInvalidEmail_thenSkipsIt() {
    NodeRef categRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id"
    );
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-ref"
    );

    AdminContactRequest body = new AdminContactRequest();
    body.setContent("Content");

    when(mailPreferencesService.buildDefaultModel(null, null, null)).thenReturn(
      new java.util.HashMap<>()
    );
    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.getDefaultMailTemplate(
        categRef,
        MailTemplate.CATEGORY_ADMIN_CONTACT
      )
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap())).thenReturn("Subject");
    when(mailWrapper.getBody(anyMap())).thenReturn("Body");

    when(nodeService.getProperty(categRef, ContentModel.PROP_NAME)).thenReturn(
      "Cat"
    );
    when(nodeService.getProperty(categRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );
    when(circabcApi.getCompanyHomeNodeRef()).thenReturn(companyHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    User currentUser = new User();
    when(usersApi.usersUserIdGet("testuser")).thenReturn(currentUser);

    List<String> emails = java.util.Arrays.asList(
      "not-an-email",
      "valid@test.com"
    );

    EmailDefinition result = emailApi.prepareEmailForAdminContact(
      categRef,
      body,
      emails
    );

    assertEquals(1, result.getUsers().size());
    assertEquals("valid@test.com", result.getUsers().get(0).getEmail());
  }

  // --- prepareEmailForGroupRequest ---

  @Test
  public void testPrepareEmailForGroupRequest_whenValid_thenReturnsDefinition() {
    NodeRef categRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id"
    );
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-ref"
    );

    GroupCreationRequest body = new GroupCreationRequest();
    body.setCategoryRef("cat-id");
    body.setProposedName("NewIG");
    I18nProperty title = new I18nProperty();
    title.put("en", "New IG Title");
    body.setProposedTitle(title);
    I18nProperty desc = new I18nProperty();
    desc.put("en", "New IG Description");
    body.setProposedDescription(desc);
    body.setJustification("We need this IG");
    User fromUser = new User();
    fromUser.setUserId("requester");
    body.setFrom(fromUser);
    body.setLeaders(java.util.Collections.singletonList(fromUser));

    when(mailPreferencesService.buildDefaultModel(null, null, null)).thenReturn(
      new java.util.HashMap<>()
    );
    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.getDefaultMailTemplate(
        categRef,
        MailTemplate.GROUP_REQUEST
      )
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap())).thenReturn("Group Request Subject");
    when(mailWrapper.getBody(anyMap())).thenReturn("Group Request Body");

    when(nodeService.getProperty(categRef, ContentModel.PROP_NAME)).thenReturn(
      "TestCat"
    );
    when(nodeService.getProperty(categRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );
    when(circabcApi.getCompanyHomeNodeRef()).thenReturn(companyHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);

    User toUser = new User();
    toUser.setEmail("admin@test.com");
    List<User> toUsers = java.util.Collections.singletonList(toUser);

    EmailDefinition result = emailApi.prepareEmailForGroupRequest(
      body,
      toUsers
    );

    assertNotNull(result);
    assertEquals("Group Request Subject", result.getSubject());
    assertEquals("Group Request Body", result.getContent());
    assertEquals(1, result.getUsers().size());
  }

  // --- prepareConfirmationForAdminContact ---

  @Test
  public void testPrepareConfirmationForAdminContact_whenCalled_thenReturnsDefinition() {
    NodeRef categRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id"
    );
    NodeRef companyHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "company-home"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-ref"
    );

    when(mailPreferencesService.buildDefaultModel(null, null, null)).thenReturn(
      new java.util.HashMap<>()
    );
    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.getDefaultMailTemplate(
        categRef,
        MailTemplate.CATEGORY_ADMIN_CONTACT_CONFIRMATION
      )
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap())).thenReturn("Confirmation Subject");
    when(mailWrapper.getBody(anyMap())).thenReturn("Confirmation Body");

    when(nodeService.getProperty(categRef, ContentModel.PROP_NAME)).thenReturn(
      "Cat"
    );
    when(nodeService.getProperty(categRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );
    when(circabcApi.getCompanyHomeNodeRef()).thenReturn(companyHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    User currentUser = new User();
    currentUser.setUserId("testuser");
    currentUser.setEmail("testuser@test.com");
    when(usersApi.usersUserIdGet("testuser")).thenReturn(currentUser);

    EmailDefinition result = emailApi.prepareConfirmationForAdminContact(
      categRef,
      "My content"
    );

    assertNotNull(result);
    assertEquals("Confirmation Subject", result.getSubject());
    assertEquals(1, result.getUsers().size());
    assertEquals("testuser@test.com", result.getUsers().get(0).getEmail());
  }
}
