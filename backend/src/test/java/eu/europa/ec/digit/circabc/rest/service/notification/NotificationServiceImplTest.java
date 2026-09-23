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
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class NotificationServiceImplTest {

  private NotificationServiceImpl service;
  private NodeService nodeService;
  private DictionaryService dictionaryService;
  private MailService mailService;
  private MailPreferencesService mailPreferencesService;
  private LogService logService;
  private ApiToolBox apiToolBox;
  private CircabcApi circabcApi;
  private ProfilesApi profilesApi;
  private AuthenticationService authenticationService;
  private PersonService personService;

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
    dictionaryService = mock(DictionaryService.class);
    mailService = mock(MailService.class);
    mailPreferencesService = mock(MailPreferencesService.class);
    logService = mock(LogService.class);
    apiToolBox = mock(ApiToolBox.class);
    circabcApi = mock(CircabcApi.class);
    profilesApi = mock(ProfilesApi.class);
    authenticationService = mock(AuthenticationService.class);
    personService = mock(PersonService.class);

    setField("nodeService", nodeService);
    setField("dictionaryService", dictionaryService);
    setField("mailService", mailService);
    setField("mailPreferencesService", mailPreferencesService);
    setField("logService", logService);
    setField("apiToolBox", apiToolBox);
    setField("circabcApi", circabcApi);
    setField("profilesApi", profilesApi);
    setField("authenticationService", authenticationService);
    setField("personService", personService);

    testNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node-id"
    );
    igNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-node-id"
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NotificationServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testNotify_whenUsersNull_thenReturnsWithoutAction()
    throws NotificationException {
    service.notify(testNodeRef, (Set<NotifiableUser>) null);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotify_whenUsersEmpty_thenReturnsWithoutAction()
    throws NotificationException {
    service.notify(testNodeRef, new HashSet<>());
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotify_whenNodeRefNull_thenReturnsWithoutAction()
    throws NotificationException {
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    service.notify(null, users);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotify_whenNodeDoesNotExist_thenReturnsWithoutAction()
    throws NotificationException {
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    when(nodeService.exists(testNodeRef)).thenReturn(false);

    service.notify(testNodeRef, users);

    verify(nodeService).exists(testNodeRef);
    verify(nodeService, never()).getType(any());
  }

  @Test
  public void testNotify_whenNodeHasHiddenAspect_thenReturnsWithoutAction()
    throws NotificationException {
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    when(nodeService.exists(testNodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(testNodeRef, ContentModel.ASPECT_HIDDEN)
    ).thenReturn(true);

    service.notify(testNodeRef, users);

    verify(nodeService, never()).getType(any());
  }

  @Test
  public void testNotify_whenContentNode_thenSendsNotification()
    throws Exception {
    NotifiableUser user = mock(NotifiableUser.class);
    when(user.getEmailAddress()).thenReturn("user@test.com");
    when(user.getNotificationLanguage()).thenReturn(Locale.ENGLISH);
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(user.getPerson()).thenReturn(personRef);

    Set<NotifiableUser> users = new LinkedHashSet<>();
    users.add(user);

    when(nodeService.exists(testNodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(testNodeRef, ContentModel.ASPECT_HIDDEN)
    ).thenReturn(false);
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(apiToolBox.getCurrentInterestGroup(testNodeRef)).thenReturn(igNodeRef);
    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn(null);
    when(nodeService.getProperty(igNodeRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NAME)
    ).thenReturn("testfile.txt");
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("Test File");
    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(200L);
    when(nodeService.getProperty(igNodeRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(nodeService.getPath(testNodeRef)).thenReturn(new Path());

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
    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");
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
  public void testNotifyNewEdition_whenUsersNull_thenReturnsWithoutAction()
    throws NotificationException {
    service.notifyNewEdition(testNodeRef, null);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotifyNewEdition_whenNodeRefNull_thenReturnsWithoutAction()
    throws NotificationException {
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    service.notifyNewEdition(null, users);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotifyNewEdition_whenNodeNotMultilingual_thenReturnsWithoutAction()
    throws NotificationException {
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    when(nodeService.exists(testNodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(
        testNodeRef,
        ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
      )
    ).thenReturn(false);

    service.notifyNewEdition(testNodeRef, users);

    verify(nodeService, never()).getType(any());
  }

  @Test
  public void testNotify_withMailList_whenMailsNull_thenReturnsWithoutAction()
    throws NotificationException {
    service.notify(testNodeRef, (List<String>) null, MailTemplate.NOTIFY_DOC);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotify_withMailList_whenMailsEmpty_thenReturnsWithoutAction()
    throws NotificationException {
    service.notify(testNodeRef, new ArrayList<>(), MailTemplate.NOTIFY_DOC);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testNotify_withMailList_whenNodeRefNull_thenReturnsWithoutAction()
    throws NotificationException {
    service.notify(null, List.of("a@b.com"), MailTemplate.NOTIFY_DOC);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testGetBestTitle_whenTitleIsString_thenReturnsString() {
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NAME)
    ).thenReturn("filename.txt");
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("My Title");

    String result = service.getBestTitle(testNodeRef);

    assertEquals("My Title", result);
  }

  @Test
  public void testGetBestTitle_whenTitleIsNull_thenThrowsNPE() {
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NAME)
    ).thenReturn("filename.txt");
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn(null);

    try {
      service.getBestTitle(testNodeRef);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // switch on null Serializable throws NPE in Java 21
    }
  }

  @Test
  public void testGetBestTitle_whenTitleIsMLText_thenReturnsDefaultValue() {
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NAME)
    ).thenReturn("filename.txt");
    MLText mlText = new MLText();
    mlText.put(Locale.ENGLISH, "English Title");
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn(mlText);

    String result = service.getBestTitle(testNodeRef);

    assertEquals("English Title", result);
  }

  @Test
  public void testGetMailFrom_delegatesToMailService() {
    when(mailService.getNoReplyEmailAddress()).thenReturn(
      "noreply@ec.europa.eu"
    );

    String result = service.getMailFrom();

    assertEquals("noreply@ec.europa.eu", result);
  }

  @Test
  public void testGetCircabcNodeRef_cachesResult() {
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-id"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);

    NodeRef first = service.getCircabcNodeRef();
    NodeRef second = service.getCircabcNodeRef();

    assertSame(first, second);
    verify(circabcApi, times(1)).getCircabcNodeRef();
  }

  // --- notify with mail list and existing node ---

  @Test
  public void testNotify_withMailList_whenNodeExists_thenSendsNotification()
    throws Exception {
    List<String> mails = List.of("user@test.com");

    when(nodeService.exists(testNodeRef)).thenReturn(true);
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        org.alfresco.model.ForumModel.TYPE_POST
      )
    ).thenReturn(false);
    when(apiToolBox.getCurrentInterestGroup(testNodeRef)).thenReturn(igNodeRef);
    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("IG Title");
    when(nodeService.getProperty(igNodeRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NAME)
    ).thenReturn("file.txt");
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("File Title");
    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(200L);
    when(nodeService.getPath(testNodeRef)).thenReturn(new Path());

    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(
      nodeService.getProperty(circabcRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1L);
    when(
      nodeService.getProperty(circabcRef, ContentModel.PROP_NAME)
    ).thenReturn("CIRCABC");

    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(mailWrapper.getSubject(anyMap(), any(Locale.class))).thenReturn(
      "Subject"
    );
    when(mailWrapper.getBody(anyMap(), any(Locale.class))).thenReturn("Body");
    when(
      mailPreferencesService.buildDefaultModel(
        eq(testNodeRef),
        isNull(),
        isNull()
      )
    ).thenReturn(new HashMap<>());
    when(
      mailPreferencesService.getDefaultMailTemplate(
        testNodeRef,
        MailTemplate.NOTIFY_DOC
      )
    ).thenReturn(mailWrapper);
    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");
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

    service.notify(testNodeRef, mails, MailTemplate.NOTIFY_DOC);

    verify(mailService).send(
      eq("noreply@test.com"),
      eq("user@test.com"),
      isNull(),
      anyString(),
      eq("Body"),
      eq(true),
      eq(false)
    );
  }

  // --- notify for post type ---

  @Test
  public void testNotify_whenPostType_thenCallsNotifyForPost()
    throws Exception {
    NotifiableUser user = mock(NotifiableUser.class);
    when(user.getEmailAddress()).thenReturn("user@test.com");
    when(user.getNotificationLanguage()).thenReturn(Locale.ENGLISH);
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person"
    );
    when(user.getPerson()).thenReturn(personRef);

    Set<NotifiableUser> users = new LinkedHashSet<>();
    users.add(user);

    when(nodeService.exists(testNodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(testNodeRef, ContentModel.ASPECT_HIDDEN)
    ).thenReturn(false);
    when(nodeService.getType(testNodeRef)).thenReturn(
      org.alfresco.model.ForumModel.TYPE_POST
    );
    when(apiToolBox.getCurrentInterestGroup(testNodeRef)).thenReturn(igNodeRef);
    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("IG");
    when(nodeService.getProperty(igNodeRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NAME)
    ).thenReturn("post");
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("Post Title");
    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(200L);
    when(nodeService.getPath(testNodeRef)).thenReturn(new Path());

    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(
      nodeService.getProperty(circabcRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1L);
    when(
      nodeService.getProperty(circabcRef, ContentModel.PROP_NAME)
    ).thenReturn("CIRCABC");

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
        MailTemplate.NOTIFY_POST
      )
    ).thenReturn(mailWrapper);
    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");
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

  // --- notifyNewEdition when multilingual ---

  @Test
  public void testNotifyNewEdition_whenMultilingual_thenSendsNotification()
    throws Exception {
    NotifiableUser user = mock(NotifiableUser.class);
    when(user.getEmailAddress()).thenReturn("user@test.com");
    when(user.getNotificationLanguage()).thenReturn(Locale.ENGLISH);
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person"
    );
    when(user.getPerson()).thenReturn(personRef);

    Set<NotifiableUser> users = new LinkedHashSet<>();
    users.add(user);

    when(nodeService.exists(testNodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(
        testNodeRef,
        ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
      )
    ).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(testNodeRef)).thenReturn(igNodeRef);
    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("IG");
    when(nodeService.getProperty(igNodeRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_NAME)
    ).thenReturn("doc.txt");
    when(
      nodeService.getProperty(testNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("Doc");
    when(
      nodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(200L);
    when(nodeService.getPath(testNodeRef)).thenReturn(new Path());

    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(
      nodeService.getProperty(circabcRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1L);
    when(
      nodeService.getProperty(circabcRef, ContentModel.PROP_NAME)
    ).thenReturn("CIRCABC");

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
        MailTemplate.ADD_NEW_TRANSLATION_EDITION
      )
    ).thenReturn(mailWrapper);
    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");
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

    service.notifyNewEdition(testNodeRef, users);

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

  // --- notify with Set<NotifiableUser> ---

  @Test
  public void testNotify_whenUsersNull_thenNoAction() throws Exception {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-1"
    );
    service.notify(nodeRef, (Set<NotifiableUser>) null);
    verifyNoInteractions(mailService);
  }

  @Test
  public void testNotify_whenUsersEmpty_thenNoAction() throws Exception {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-1"
    );
    service.notify(nodeRef, Collections.emptySet());
    verifyNoInteractions(mailService);
  }

  @Test
  public void testNotify_whenNodeRefNull_thenNoAction() throws Exception {
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    service.notify(null, users);
    verifyNoInteractions(mailService);
  }

  @Test
  public void testNotify_whenNodeDoesNotExist_thenNoAction() throws Exception {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "gone"
    );
    when(nodeService.exists(nodeRef)).thenReturn(false);
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    service.notify(nodeRef, users);
    verifyNoInteractions(mailService);
  }

  @Test
  public void testNotify_whenNodeIsHidden_thenNoAction() throws Exception {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "hidden"
    );
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN)).thenReturn(
      true
    );
    Set<NotifiableUser> users = new HashSet<>();
    users.add(mock(NotifiableUser.class));
    service.notify(nodeRef, users);
    verifyNoInteractions(mailService);
  }
}
