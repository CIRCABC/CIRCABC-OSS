package eu.europa.ec.digit.circabc.rest.service.notification;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.exception.NotificationException;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.mail.MailWrapper;
import io.swagger.api.CircabcApi;
import io.swagger.api.ProfilesApi;
import io.swagger.model.NotifiableUser;
import io.swagger.model.UserProfile;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class NotificationServiceTest {

  private NotificationServiceImpl service;
  private NodeService nodeService;
  private MailService mailService;
  private MailPreferencesService mailPreferencesService;
  private DictionaryService dictionaryService;
  private LogService logService;
  private ApiToolBox apiToolBox;
  private AuthenticationService authenticationService;
  private PersonService personService;
  private ProfilesApi profilesApi;
  private CircabcApi circabcApi;

  private NodeRef testNodeRef;
  private NodeRef igNodeRef;

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

    service = new NotificationServiceImpl();

    nodeService = mock(NodeService.class);
    mailService = mock(MailService.class);
    mailPreferencesService = mock(MailPreferencesService.class);
    dictionaryService = mock(DictionaryService.class);
    logService = mock(LogService.class);
    apiToolBox = mock(ApiToolBox.class);
    authenticationService = mock(AuthenticationService.class);
    personService = mock(PersonService.class);
    profilesApi = mock(ProfilesApi.class);
    circabcApi = mock(CircabcApi.class);

    setField("nodeService", nodeService);
    setField("mailService", mailService);
    setField("mailPreferencesService", mailPreferencesService);
    setField("dictionaryService", dictionaryService);
    setField("logService", logService);
    setField("apiToolBox", apiToolBox);
    setField("authenticationService", authenticationService);
    setField("personService", personService);
    setField("profilesApi", profilesApi);
    setField("circabcApi", circabcApi);

    testNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node-id"
    );
    igNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id");

    when(apiToolBox.getCurrentInterestGroup(testNodeRef)).thenReturn(igNodeRef);
    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(nodeService.getProperty(igNodeRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("TestIG");
    when(nodeService.getPath(testNodeRef)).thenReturn(new Path());
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1L);
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NAME)
    ).thenReturn("testDoc");
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("testDoc");
    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NotificationServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testNotify_whenUsersNull_thenReturnsEarly()
    throws NotificationException {
    service.notify(testNodeRef, (Set<NotifiableUser>) null);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotify_whenUsersEmpty_thenReturnsEarly()
    throws NotificationException {
    service.notify(testNodeRef, Collections.emptySet());
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotify_whenNodeRefNull_thenReturnsEarly()
    throws NotificationException {
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    service.notify(null, users);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotify_whenNodeDoesNotExist_thenReturnsEarly()
    throws NotificationException {
    when(nodeService.exists(testNodeRef)).thenReturn(false);
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    service.notify(testNodeRef, users);
    verify(nodeService).exists(testNodeRef);
    verify(nodeService, never()).getType(any());
  }

  @Test
  public void testNotify_whenNodeIsHidden_thenReturnsEarly()
    throws NotificationException {
    when(nodeService.exists(testNodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(testNodeRef, ContentModel.ASPECT_HIDDEN)
    ).thenReturn(true);
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    service.notify(testNodeRef, users);
    verify(nodeService, never()).getType(any());
  }

  @Test
  public void testNotify_whenContentNode_thenSendsNotification()
    throws Exception {
    when(nodeService.exists(testNodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(testNodeRef, ContentModel.ASPECT_HIDDEN)
    ).thenReturn(false);
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        ForumModel.TYPE_POST
      )
    ).thenReturn(false);

    NotifiableUser user = mock(NotifiableUser.class);
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(user.getEmailAddress()).thenReturn("user@test.com");
    when(user.getNotificationLanguage()).thenReturn(Locale.ENGLISH);
    when(user.getPerson()).thenReturn(personRef);

    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(mailWrapper.getSubject(anyMap(), any(Locale.class))).thenReturn(
      "Subject"
    );
    when(mailWrapper.getBody(anyMap(), any(Locale.class))).thenReturn("Body");
    when(
      mailPreferencesService.buildDefaultModel(
        eq(testNodeRef),
        eq(personRef),
        isNull()
      )
    ).thenReturn(new HashMap<>());
    when(
      mailPreferencesService.getDefaultMailTemplate(
        testNodeRef,
        MailTemplate.NOTIFY_DOC
      )
    ).thenReturn(mailWrapper);
    when(
      mailService.send(
        anyString(),
        anyString(),
        isNull(),
        anyString(),
        anyString(),
        eq(true),
        eq(false)
      )
    ).thenReturn(true);

    Set<NotifiableUser> users = new LinkedHashSet<>();
    users.add(user);
    service.notify(testNodeRef, users);

    verify(mailService).send(
      eq("noreply@test.com"),
      eq("user@test.com"),
      isNull(),
      contains("Subject"),
      eq("Body"),
      eq(true),
      eq(false)
    );
  }

  @Test
  public void testNotifyNewEdition_whenUsersNull_thenReturnsEarly()
    throws NotificationException {
    service.notifyNewEdition(testNodeRef, null);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotifyNewEdition_whenNodeRefNull_thenReturnsEarly()
    throws NotificationException {
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    service.notifyNewEdition(null, users);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotifyNewEdition_whenNodeNotMultilingual_thenDoesNotSend()
    throws Exception {
    when(nodeService.exists(testNodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(
        testNodeRef,
        ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
      )
    ).thenReturn(false);

    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    service.notifyNewEdition(testNodeRef, users);

    verify(mailService, never()).send(
      any(),
      anyString(),
      any(),
      any(),
      any(),
      anyBoolean(),
      anyBoolean()
    );
  }

  @Test
  public void testNotifyWithMailList_whenMailsNull_thenReturnsEarly()
    throws NotificationException {
    service.notify(testNodeRef, (List<String>) null, MailTemplate.NOTIFY_DOC);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotifyWithMailList_whenMailsEmpty_thenReturnsEarly()
    throws NotificationException {
    service.notify(
      testNodeRef,
      Collections.emptyList(),
      MailTemplate.NOTIFY_DOC
    );
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotifyWithMailList_whenNodeRefNull_thenReturnsEarly()
    throws NotificationException {
    service.notify(null, List.of("a@b.com"), MailTemplate.NOTIFY_DOC);
    verifyNoInteractions(nodeService);
  }

  @Test(expected = NotificationException.class)
  public void testNotify_whenMailServiceThrows_thenThrowsNotificationException()
    throws Exception {
    when(nodeService.exists(testNodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(testNodeRef, ContentModel.ASPECT_HIDDEN)
    ).thenReturn(false);
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        ForumModel.TYPE_POST
      )
    ).thenReturn(false);

    NotifiableUser user = mock(NotifiableUser.class);
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(user.getEmailAddress()).thenReturn("user@test.com");
    when(user.getNotificationLanguage()).thenReturn(Locale.ENGLISH);
    when(user.getPerson()).thenReturn(personRef);

    when(
      mailPreferencesService.buildDefaultModel(any(), any(), any())
    ).thenThrow(new RuntimeException("Unexpected error"));

    Set<NotifiableUser> users = new LinkedHashSet<>();
    users.add(user);
    service.notify(testNodeRef, users);
  }

  @Test
  public void testGetBestTitle_whenTitleIsString_thenReturnsTitle()
    throws Exception {
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NAME)
    ).thenReturn("filename.txt");
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("My Title");

    java.lang.reflect.Method method =
      NotificationServiceImpl.class.getDeclaredMethod(
        "getBestTitle",
        NodeRef.class
      );
    method.setAccessible(true);
    String result = (String) method.invoke(service, testNodeRef);

    assertEquals("My Title", result);
  }

  @Test(expected = java.lang.reflect.InvocationTargetException.class)
  public void testGetBestTitle_whenTitleIsNull_thenThrowsNPE()
    throws Exception {
    NodeRef node = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "null-title-node"
    );
    when(nodeService.getProperty(node, ContentModel.PROP_NAME)).thenReturn(
      "filename.txt"
    );
    when(nodeService.getProperty(node, ContentModel.PROP_TITLE)).thenReturn(
      null
    );

    java.lang.reflect.Method method =
      NotificationServiceImpl.class.getDeclaredMethod(
        "getBestTitle",
        NodeRef.class
      );
    method.setAccessible(true);
    method.invoke(service, node);
  }

  @Test
  public void testNotifyNewMemberships_whenMessagingFails_thenThrowsNotificationException()
    throws Exception {
    NotifiableUser admin = mock(NotifiableUser.class);
    when(admin.getEmailAddress()).thenReturn("admin@test.com");
    when(admin.getNotificationLanguage()).thenReturn(Locale.ENGLISH);
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(admin.getPerson()).thenReturn(personRef);

    UserProfile newMember = mock(UserProfile.class);
    io.swagger.model.User memberUser = mock(io.swagger.model.User.class);
    when(newMember.getUser()).thenReturn(memberUser);
    when(memberUser.getUserId()).thenReturn("newuser");
    io.swagger.model.Profile profile = mock(io.swagger.model.Profile.class);
    when(newMember.getProfile()).thenReturn(profile);
    when(profile.getName()).thenReturn("ACCESS");
    when(profile.getTitle()).thenReturn(null);

    when(personService.getPerson("newuser")).thenReturn(personRef);
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(personService.getPerson("testuser")).thenReturn(personRef);

    when(
      mailPreferencesService.buildDefaultModel(any(), any(), any())
    ).thenReturn(new HashMap<>());
    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.getDefaultMailTemplate(any(), any())
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap(), any(Locale.class))).thenReturn(
      "Subject"
    );
    when(mailWrapper.getBody(anyMap(), any(Locale.class))).thenReturn("Body");
    when(
      mailService.send(
        anyString(),
        anyString(),
        any(),
        anyString(),
        anyString(),
        anyBoolean(),
        anyBoolean()
      )
    ).thenThrow(new jakarta.mail.MessagingException("SMTP error"));

    Set<NotifiableUser> admins = new LinkedHashSet<>();
    admins.add(admin);

    try {
      service.notifyNewMemberships(testNodeRef, admins, newMember);
      fail("Expected NotificationException");
    } catch (NotificationException e) {
      assertTrue(e.getMessage().contains("membership"));
    }
  }

  @Test
  public void testNotifySystemMessage_sendsToAllAddresses() throws Exception {
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-id"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(authenticationService.getCurrentUserName()).thenReturn("admin");
    NodeRef adminRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "admin-ref"
    );
    when(personService.getPerson("admin")).thenReturn(adminRef);
    when(
      mailPreferencesService.buildDefaultModel(
        eq(circabcRef),
        eq(adminRef),
        isNull()
      )
    ).thenReturn(new HashMap<>());

    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(
      mailPreferencesService.getDefaultMailTemplate(
        circabcRef,
        MailTemplate.NOTIFY_SYSTEM_MESSAGE
      )
    ).thenReturn(mailWrapper);
    when(mailWrapper.getSubject(anyMap(), any(Locale.class))).thenReturn(
      "System"
    );
    when(mailWrapper.getBody(anyMap(), any(Locale.class))).thenReturn(
      "Message body"
    );

    io.swagger.config.CircabcConfig config = mock(
      io.swagger.config.CircabcConfig.class
    );
    when(config.getApplicationName()).thenReturn("CIRCABC");
    setField("circabcConfig", config);

    io.swagger.model.AppMessage appMessage = mock(
      io.swagger.model.AppMessage.class
    );
    when(appMessage.getContent()).thenReturn("System is down");

    List<String> addresses = List.of("a@test.com", "b@test.com");
    service.notifySystemMessage(addresses, appMessage);

    verify(mailService).send(
      eq("noreply@test.com"),
      eq(addresses),
      isNull(),
      contains("System"),
      eq("Message body"),
      eq(true),
      eq(true)
    );
  }
}
