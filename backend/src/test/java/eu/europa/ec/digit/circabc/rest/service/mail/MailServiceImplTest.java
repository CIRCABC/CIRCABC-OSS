package eu.europa.ec.digit.circabc.rest.service.mail;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSender;

public class MailServiceImplTest {

  private MailServiceImpl mailService;
  private JavaMailSender mailSender;
  private FileFolderService fileFolderService;
  private MailPreferencesService mailPreferencesService;
  private CircabcApi circabcApi;
  private ContentService contentService;
  private CircabcConfig circabcConfig;

  @Before
  public void setUp() throws Exception {
    mailService = new MailServiceImpl();

    mailSender = mock(JavaMailSender.class);
    fileFolderService = mock(FileFolderService.class);
    mailPreferencesService = mock(MailPreferencesService.class);
    circabcApi = mock(CircabcApi.class);
    contentService = mock(ContentService.class);
    circabcConfig = mock(CircabcConfig.class);

    setField("mailSender", mailSender);
    setField("fileFolderService", fileFolderService);
    setField("mailPreferencesService", mailPreferencesService);
    setField("circabcApi", circabcApi);
    setField("contentService", contentService);
    setField("circabcConfig", circabcConfig);

    mailService.setNoReply("noreply@circabc.eu");
    mailService.setSupport("support@circabc.eu");
    mailService.setDevTeam("devteam@circabc.eu");
    mailService.setHelpdeskAddress("helpdesk@circabc.eu");
    mailService.setLogoCid("logoCid");
    mailService.setHeaderLogoCid("headerLogoCid");
    mailService.setHeaderEULogoCid("headerEULogoCid");
    mailService.setHeaderBackgroundCid("headerBackgroundCid");
    mailService.setDisclamerText("Disclaimer {0} {1} {2}");
    mailService.setDisclamerHtml("Disclaimer {0} {1} {2}");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = MailServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(mailService, value);
  }

  @Test
  public void testSend_whenMailDisabled_thenReturnsTrue() throws Exception {
    when(circabcConfig.isMailEnabled()).thenReturn(false);

    boolean result = mailService.send(
      "from@circabc.eu",
      "to@circabc.eu",
      null,
      "Subject",
      "Body",
      false,
      false
    );

    assertTrue(result);
    verify(mailSender, never()).send(any(MimeMessage.class));
  }

  @Test
  public void testSend_whenMailEnabled_thenSendsEmail() throws Exception {
    when(circabcConfig.isMailEnabled()).thenReturn(true);
    MimeMessage mimeMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    NodeRef circabcRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRoot);
    when(mailPreferencesService.getDisclamerLogo(circabcRoot)).thenReturn(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "logo")
    );
    when(mailPreferencesService.getHeaderLogo(circabcRoot)).thenReturn(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "header-logo")
    );
    when(mailPreferencesService.getHeaderEULogo(circabcRoot)).thenReturn(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "eu-logo")
    );

    doNothing()
      .when(mailSender)
      .send(any(org.springframework.mail.javamail.MimeMessagePreparator.class));

    boolean result = mailService.send(
      "from@circabc.eu",
      "to@circabc.eu",
      null,
      "Subject",
      "Body",
      false,
      false
    );

    assertTrue(result);
    verify(mailSender).send(
      any(org.springframework.mail.javamail.MimeMessagePreparator.class)
    );
  }

  @Test(expected = MessagingException.class)
  public void testSend_whenInvalidFromAddress_thenThrowsException()
    throws Exception {
    when(circabcConfig.isMailEnabled()).thenReturn(true);

    mailService.send(
      "invalid",
      "to@circabc.eu",
      null,
      "Subject",
      "Body",
      false,
      false
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendList_whenNullToList_thenThrowsException()
    throws Exception {
    mailService.send(
      "from@circabc.eu",
      (List<String>) null,
      null,
      "Subject",
      "Body",
      false,
      false
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendList_whenEmptyToList_thenThrowsException()
    throws Exception {
    mailService.send(
      "from@circabc.eu",
      Collections.emptyList(),
      null,
      "Subject",
      "Body",
      false,
      false
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendList_whenInvalidEmailInList_thenThrowsException()
    throws Exception {
    mailService.send(
      "from@circabc.eu",
      Arrays.asList("invalid-email"),
      null,
      "Subject",
      "Body",
      false,
      false
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendNode_whenContentIsNull_thenThrowsException()
    throws Exception {
    mailService.sendNode(
      null,
      "from@circabc.eu",
      "to@circabc.eu",
      null,
      "Subject",
      "Body",
      false
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendNodeList_whenContentIsNull_thenThrowsException()
    throws Exception {
    mailService.sendNode(
      null,
      "from@circabc.eu",
      Arrays.asList("to@circabc.eu"),
      null,
      "Subject",
      "Body",
      false
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendNodeList_whenToListEmpty_thenThrowsException()
    throws Exception {
    NodeRef content = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "content-id"
    );
    mailService.sendNode(
      content,
      "from@circabc.eu",
      Collections.emptyList(),
      null,
      "Subject",
      "Body",
      false
    );
  }

  @Test
  public void testSend_whenMailSenderThrowsException_thenReturnsFalse()
    throws Exception {
    when(circabcConfig.isMailEnabled()).thenReturn(true);

    doThrow(new RuntimeException("SMTP error"))
      .when(mailSender)
      .send(any(org.springframework.mail.javamail.MimeMessagePreparator.class));

    boolean result = mailService.send(
      "from@circabc.eu",
      "to@circabc.eu",
      null,
      "Subject",
      "Body",
      false,
      false
    );

    assertFalse(result);
  }

  @Test
  public void testGetNoReplyEmailAddress() {
    assertEquals("noreply@circabc.eu", mailService.getNoReplyEmailAddress());
  }

  @Test
  public void testGetSupportEmailAddress() {
    assertEquals("support@circabc.eu", mailService.getSupportEmailAddress());
  }

  @Test
  public void testGetDevTeamEmailAddress() {
    assertEquals("devteam@circabc.eu", mailService.getDevTeamEmailAddress());
  }

  @Test
  public void testGetHelpdeskAddress() {
    assertEquals("helpdesk@circabc.eu", mailService.getHelpdeskAddress());
  }

  @Test
  public void testGetOrganizer_whenNotSet_thenUsesCircabcConfig() {
    when(circabcConfig.getEmailAddress()).thenReturn("organizer@circabc.eu");
    assertEquals("organizer@circabc.eu", mailService.getOrganizer());
  }

  @Test
  public void testGetIsListenerActive_whenNotSet_thenUsesCircabcConfig() {
    when(circabcConfig.isListenerActive()).thenReturn(true);
    assertTrue(mailService.getIsListenerActive());
  }

  @Test(expected = MessagingException.class)
  public void testSendWithAttachment_whenToIsNull_thenThrowsException()
    throws Exception {
    mailService.sendWithAttachment(
      "from@circabc.eu",
      null,
      null,
      "Subject",
      "Body",
      true,
      false,
      null
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendWithAttachment_whenToIsEmpty_thenThrowsException()
    throws Exception {
    mailService.sendWithAttachment(
      "from@circabc.eu",
      "",
      null,
      "Subject",
      "Body",
      true,
      false,
      null
    );
  }

  @Test
  public void testInit_setsApplicationNameAndFormatsDisclaimers() {
    when(circabcConfig.getApplicationName()).thenReturn("CIRCABC");
    when(circabcConfig.getWebRootUrl()).thenReturn("https://circabc.eu");

    mailService.init();

    assertEquals("CIRCABC", mailService.getApplicationName());
    assertNotNull(mailService.getDisclamerHtml());
    assertNotNull(mailService.getDisclamerText());
  }

  @Test
  public void testEnvironmentNameSettings() {
    mailService.setEnvironmentNameEnabled(true);
    mailService.setEnvironmentName("TEST");
    // Verify no exception thrown - these are simple setters
  }

  @Test
  public void testRedirectEmailSettings() {
    mailService.setRedirectEmailAddressEnabled(true);
    mailService.setRedirectEmailAddress("redirect@circabc.eu");
    // Verify no exception thrown - these are simple setters
  }

  @Test(expected = MessagingException.class)
  public void testSendWithAttachments_whenToListNull_thenThrowsException()
    throws Exception {
    mailService.send(
      "from@circabc.eu",
      (List<String>) null,
      null,
      "Subject",
      "Body",
      Collections.singletonList(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "att")
      )
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendSingleToWithAttachments_whenToNull_thenThrowsException()
    throws Exception {
    mailService.send(
      "from@circabc.eu",
      (String) null,
      null,
      "Subject",
      "Body",
      Collections.singletonList(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "att")
      )
    );
  }

  // --- send with list of recipients ---

  @Test
  public void testSendList_whenValidList_thenSendsToAll() throws Exception {
    when(circabcConfig.isMailEnabled()).thenReturn(true);
    MimeMessage mimeMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    List<String> to = Arrays.asList("user1@test.com", "user2@test.com");
    boolean result = mailService.send(
      "from@test.com",
      to,
      null,
      "Subject",
      "Body",
      null
    );

    verify(mailSender).send(
      any(org.springframework.mail.javamail.MimeMessagePreparator.class)
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendList_whenNullList_thenThrows() throws Exception {
    mailService.send(
      "from@test.com",
      (List<String>) null,
      null,
      "Subject",
      "Body",
      null
    );
  }

  // --- send with single to and attachments ---

  @Test
  public void testSendWithAttachments_whenValid_thenSends() throws Exception {
    when(circabcConfig.isMailEnabled()).thenReturn(true);
    MimeMessage mimeMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    NodeRef attachment = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "att-1"
    );
    boolean result = mailService.send(
      "from@test.com",
      "to@test.com",
      null,
      "Subject",
      "Body",
      Collections.singletonList(attachment)
    );

    verify(mailSender).send(
      any(org.springframework.mail.javamail.MimeMessagePreparator.class)
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendWithAttachments_whenNullTo_thenThrows() throws Exception {
    mailService.send(
      "from@test.com",
      (String) null,
      null,
      "Subject",
      "Body",
      Collections.emptyList()
    );
  }

  // --- sendNode with list ---

  @Test(expected = MessagingException.class)
  public void testSendNodeList_whenNullContent_thenThrows() throws Exception {
    mailService.sendNode(
      null,
      "from@test.com",
      Arrays.asList("to@test.com"),
      null,
      "Subject",
      "Body",
      true
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendNodeList_whenEmptyToList_thenThrows2() throws Exception {
    NodeRef content = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "content-1"
    );
    mailService.sendNode(
      content,
      "from@test.com",
      Collections.emptyList(),
      null,
      "Subject",
      "Body",
      true
    );
  }

  // --- getNoReplyEmailAddress ---

  @Test
  public void testGetNoReplyEmailAddress_returnsConfigured() {
    assertEquals("noreply@circabc.eu", mailService.getNoReplyEmailAddress());
  }

  @Test
  public void testGetSupportEmailAddress_returnsConfigured() {
    assertEquals("support@circabc.eu", mailService.getSupportEmailAddress());
  }

  @Test
  public void testGetDevTeamEmailAddress_returnsConfigured() {
    assertEquals("devteam@circabc.eu", mailService.getDevTeamEmailAddress());
  }

  @Test
  public void testGetHelpdeskAddress_returnsConfigured() {
    assertEquals("helpdesk@circabc.eu", mailService.getHelpdeskAddress());
  }
}
