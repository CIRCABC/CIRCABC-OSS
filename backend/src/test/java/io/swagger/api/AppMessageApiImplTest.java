package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.message.AppMessageDaoService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import io.swagger.model.*;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.SystemMessageModel;
import io.swagger.model.db.AppMessageDAO;
import io.swagger.model.db.DistributionEmailDAO;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class AppMessageApiImplTest {

  private AppMessageApiImpl appMessageApi;
  private AppMessageDaoService appMessageDaoService;
  private PersonService personService;
  private NodeService nodeService;
  private CircabcApi circabcApi;
  private NotificationService notificationService;

  @Before
  public void setUp() throws Exception {
    appMessageApi = new AppMessageApiImpl();
    appMessageDaoService = mock(AppMessageDaoService.class);
    personService = mock(PersonService.class);
    nodeService = mock(NodeService.class);
    circabcApi = mock(CircabcApi.class);
    notificationService = mock(NotificationService.class);

    setField(appMessageApi, "appMessageDaoService", appMessageDaoService);
    setField(appMessageApi, "personService", personService);
    setField(appMessageApi, "nodeService", nodeService);
    setField(appMessageApi, "circabcApi", circabcApi);
    setField(appMessageApi, "notificationService", notificationService);
  }

  private void setField(Object target, String fieldName, Object value)
    throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  public void testGetAppMessages_whenShowMessageTrue_thenReturned() {
    AppMessageDAO dao = createDAO(1, "Hello", "info", true, 5);
    when(appMessageDaoService.selectAppMessageTemplates(-1, -1)).thenReturn(
      List.of(dao)
    );

    List<AppMessage> result = appMessageApi.getAppMessages();

    assertEquals(1, result.size());
    assertEquals("Hello", result.get(0).getContent());
    assertEquals("info", result.get(0).getLevel());
    assertEquals(Integer.valueOf(5), result.get(0).getDisplayTime());
    assertTrue(result.get(0).getEnabled());
  }

  @Test
  public void testGetAppMessages_whenShowMessageFalse_thenFiltered() {
    AppMessageDAO dao = createDAO(1, "Hidden", "warn", false, 10);
    when(appMessageDaoService.selectAppMessageTemplates(-1, -1)).thenReturn(
      List.of(dao)
    );

    List<AppMessage> result = appMessageApi.getAppMessages();

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetAppMessages_whenDateClosureSet_thenConverted() {
    AppMessageDAO dao = createDAO(1, "Msg", "error", true, 3);
    Date closure = new Date();
    dao.setDateClosure(closure);
    when(appMessageDaoService.selectAppMessageTemplates(-1, -1)).thenReturn(
      List.of(dao)
    );

    List<AppMessage> result = appMessageApi.getAppMessages();

    assertNotNull(result.get(0).getDateClosure());
    assertEquals(closure.getTime(), result.get(0).getDateClosure().getMillis());
  }

  @Test
  public void testGetAppMessageTemplates_whenCalled_thenReturnsPaged() {
    AppMessageDAO dao1 = createDAO(1, "Msg1", "info", true, 5);
    AppMessageDAO dao2 = createDAO(2, "Msg2", "warn", false, 10);
    when(appMessageDaoService.selectAppMessageTemplates(0, 10)).thenReturn(
      List.of(dao1, dao2)
    );
    when(appMessageDaoService.countAppMessageTemplates()).thenReturn(2);

    PagedAppMessages result = appMessageApi.getAppMessageTemplates(0, 10);

    assertEquals(2, result.getData().size());
    assertEquals(Long.valueOf(2), result.getTotal());
  }

  @Test
  public void testAddAppMessageTemplate_whenDateClosureNull_thenPassesNull() {
    AppMessage template = new AppMessage();
    template.setContent("Test");
    template.setLevel("info");
    template.setDisplayTime(5);
    template.setEnabled(true);

    appMessageApi.addAppMessageTemplate(template);

    verify(appMessageDaoService).addAppMessageTemplate(
      "Test",
      null,
      "info",
      5,
      true
    );
  }

  @Test
  public void testAddAppMessageTemplate_whenDateClosureSet_thenConverts() {
    AppMessage template = new AppMessage();
    template.setContent("Test");
    template.setLevel("warn");
    template.setDisplayTime(10);
    template.setEnabled(false);
    DateTime dt = new DateTime(2026, 1, 1, 0, 0);
    template.setDateClosure(dt);

    appMessageApi.addAppMessageTemplate(template);

    verify(appMessageDaoService).addAppMessageTemplate(
      "Test",
      dt.toDate(),
      "warn",
      10,
      false
    );
  }

  @Test
  public void testUpdateAppMessageTemplate_whenCalled_thenDelegates() {
    AppMessage template = new AppMessage();
    template.setId(42);
    template.setContent("Updated");
    template.setLevel("error");
    template.setDisplayTime(15);
    template.setEnabled(true);

    appMessageApi.updateAppMessageTemplate(template);

    verify(appMessageDaoService).updateAppMessageTemplate(
      42,
      "Updated",
      null,
      "error",
      15,
      true
    );
  }

  @Test
  public void testDeleteAppMessageTemplate_whenCalled_thenDelegates() {
    appMessageApi.deleteAppMessageTemplate(7);

    verify(appMessageDaoService).deleteAppMessageTemplate(7);
  }

  @Test
  public void testGetAppMessageTemplate_whenCalled_thenConverts() {
    AppMessageDAO dao = createDAO(5, "Content", "info", true, 8);
    when(appMessageDaoService.getMessageTemplate(5)).thenReturn(dao);

    AppMessage result = appMessageApi.getAppMessageTemplate(5);

    assertEquals(Integer.valueOf(5), result.getId());
    assertEquals("Content", result.getContent());
  }

  @Test
  public void testGetDisplayOldMessage_whenPropertyNull_thenReturnsTrue() {
    NodeRef circabcRef = new NodeRef("workspace://SpacesStore/circabc-node");
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(
      nodeService.getProperty(
        circabcRef,
        CircabcModel.PROP_DISPLAY_OLD_APP_MESSAGE
      )
    ).thenReturn(null);

    DisplayConfiguration result = appMessageApi.getDisplayOldMessage();

    assertTrue(result.getDisplay());
  }

  @Test
  public void testGetDisplayOldMessage_whenPropertyFalse_thenReturnsFalse() {
    NodeRef circabcRef = new NodeRef("workspace://SpacesStore/circabc-node");
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(
      nodeService.getProperty(
        circabcRef,
        CircabcModel.PROP_DISPLAY_OLD_APP_MESSAGE
      )
    ).thenReturn("false");

    DisplayConfiguration result = appMessageApi.getDisplayOldMessage();

    assertFalse(result.getDisplay());
  }

  @Test
  public void testSetDisplayOldMessage_whenCalled_thenSetsProperty() {
    NodeRef circabcRef = new NodeRef("workspace://SpacesStore/circabc-node");
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);

    appMessageApi.setDisplayOldMessage(true);

    verify(nodeService).setProperty(
      circabcRef,
      CircabcModel.PROP_DISPLAY_OLD_APP_MESSAGE,
      true
    );
  }

  @Test
  public void testGetEnableOldMessage_whenNoMessageRef_thenReturnsFalse() {
    NodeRef circabcRef = new NodeRef("workspace://SpacesStore/circabc-node");
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(
      nodeService.getChildAssocs(
        eq(circabcRef),
        eq(ContentModel.ASSOC_CONTAINS),
        any(QName.class)
      )
    ).thenReturn(Collections.emptyList());

    EnableConfiguration result = appMessageApi.getEnableOldMessage();

    assertFalse(result.getEnable());
  }

  @Test
  public void testGetEnableOldMessage_whenMessageRefExists_thenReturnsValue() {
    NodeRef circabcRef = new NodeRef("workspace://SpacesStore/circabc-node");
    NodeRef messageRef = new NodeRef("workspace://SpacesStore/message-node");
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(messageRef);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(
      nodeService.getChildAssocs(
        eq(circabcRef),
        eq(ContentModel.ASSOC_CONTAINS),
        any(QName.class)
      )
    ).thenReturn(List.of(childAssoc));
    when(
      nodeService.getProperty(
        messageRef,
        SystemMessageModel.PROP_IS_SYSTEMMESSAGE_ENABLED
      )
    ).thenReturn(true);

    EnableConfiguration result = appMessageApi.getEnableOldMessage();

    assertTrue(result.getEnable());
  }

  @Test
  public void testSetEnableOldMessage_whenMessageRefExists_thenSetsProperty() {
    NodeRef circabcRef = new NodeRef("workspace://SpacesStore/circabc-node");
    NodeRef messageRef = new NodeRef("workspace://SpacesStore/message-node");
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(messageRef);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(
      nodeService.getChildAssocs(
        eq(circabcRef),
        eq(ContentModel.ASSOC_CONTAINS),
        any(QName.class)
      )
    ).thenReturn(List.of(childAssoc));

    appMessageApi.setEnableOldMessage(false);

    verify(nodeService).setProperty(
      messageRef,
      SystemMessageModel.PROP_IS_SYSTEMMESSAGE_ENABLED,
      false
    );
  }

  @Test
  public void testUdpateOldAppMessage_whenMessageRefExists_thenSetsContent() {
    NodeRef circabcRef = new NodeRef("workspace://SpacesStore/circabc-node");
    NodeRef messageRef = new NodeRef("workspace://SpacesStore/message-node");
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(messageRef);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(
      nodeService.getChildAssocs(
        eq(circabcRef),
        eq(ContentModel.ASSOC_CONTAINS),
        any(QName.class)
      )
    ).thenReturn(List.of(childAssoc));

    AppMessage template = new AppMessage();
    template.setContent("New content");

    appMessageApi.udpateOldAppMessage(template);

    verify(nodeService).setProperty(
      messageRef,
      SystemMessageModel.PROP_SYSTEMMESSAGE_TEXT,
      "New content"
    );
  }

  @Test
  public void testGetAppDistributionEmails_whenCalled_thenReturnsPaged() {
    DistributionEmailDAO email = new DistributionEmailDAO();
    email.setId(1);
    email.setEmailAddress("test@example.com");
    when(
      appMessageDaoService.selectDistributionEmails(0, 10, "test")
    ).thenReturn(List.of(email));
    when(appMessageDaoService.countDistributionEmails("test")).thenReturn(1L);

    PagedEmails result = appMessageApi.getAppDistributionEmails(0, 10, "test");

    assertEquals(1, result.getData().size());
    assertEquals(1L, result.getTotal());
  }

  @Test
  public void testAddAppDistributionPostEmails_whenNotSubscribed_thenInserts() {
    DistributionEmailDAO email = new DistributionEmailDAO();
    email.setEmailAddress("new@example.com");
    when(
      appMessageDaoService.hasDistributionEmail("new@example.com")
    ).thenReturn(0);

    appMessageApi.addAppDistributionPostEmails(List.of(email));

    verify(appMessageDaoService).insertEmail(email);
  }

  @Test
  public void testAddAppDistributionPostEmails_whenAlreadySubscribed_thenSkips() {
    DistributionEmailDAO email = new DistributionEmailDAO();
    email.setEmailAddress("existing@example.com");
    when(
      appMessageDaoService.hasDistributionEmail("existing@example.com")
    ).thenReturn(1);

    appMessageApi.addAppDistributionPostEmails(List.of(email));

    verify(appMessageDaoService, never()).insertEmail(email);
  }

  @Test
  public void testIsSubscribedDistributionEmail_whenNull_thenReturnsFalse() {
    assertFalse(appMessageApi.isSubscribedDistributionEmail(null));
  }

  @Test
  public void testIsSubscribedDistributionEmail_whenExists_thenReturnsTrue() {
    when(appMessageDaoService.hasDistributionEmail("a@b.com")).thenReturn(1);

    assertTrue(appMessageApi.isSubscribedDistributionEmail("a@b.com"));
  }

  @Test
  public void testRemoveAppDistributionPostEmails_whenIdNotNull_thenDeletes() {
    appMessageApi.removeAppDistributionPostEmails(5);

    verify(appMessageDaoService).deleteDistributionEmail(5);
  }

  @Test
  public void testRemoveAppDistributionPostEmails_whenIdNull_thenNoOp() {
    appMessageApi.removeAppDistributionPostEmails(null);

    verify(appMessageDaoService, never()).deleteDistributionEmail(anyInt());
  }

  @Test
  public void testGetSubscribedDistributionEmail_whenEmptyUserId_thenReturnsNull() {
    assertNull(appMessageApi.getSubscribedDistributionEmail(""));
  }

  @Test
  public void testGetSubscribedDistributionEmail_whenValidUser_thenLooksUpEmail() {
    NodeRef personRef = new NodeRef("workspace://SpacesStore/person-node");
    when(personService.getPerson("admin")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("admin@example.com");
    DistributionEmailDAO expected = new DistributionEmailDAO();
    expected.setEmailAddress("admin@example.com");
    when(
      appMessageDaoService.getDistributionEmail("admin@example.com")
    ).thenReturn(expected);

    DistributionEmailDAO result = appMessageApi.getSubscribedDistributionEmail(
      "admin"
    );

    assertEquals("admin@example.com", result.getEmailAddress());
  }

  @Test
  public void testGetSubscribedDistributionEmailById_whenCalled_thenDelegates() {
    DistributionEmailDAO expected = new DistributionEmailDAO();
    expected.setId(3);
    when(appMessageDaoService.getDistributionEmailById(3)).thenReturn(expected);

    DistributionEmailDAO result =
      appMessageApi.getSubscribedDistributionEmailById(3);

    assertEquals(Integer.valueOf(3), result.getId());
  }

  private AppMessageDAO createDAO(
    int id,
    String content,
    String level,
    boolean show,
    int displayTime
  ) {
    AppMessageDAO dao = new AppMessageDAO();
    dao.setId(id);
    dao.setMessageContent(content);
    dao.setMessageLevel(level);
    dao.setShowMessage(show);
    dao.setDisplayTime(displayTime);
    return dao;
  }
}
