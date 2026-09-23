package eu.europa.ec.digit.circabc.rest.service.mail;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import jakarta.mail.MessagingException;
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
import org.springframework.mail.javamail.MimeMessagePreparator;

public class MailServiceTest {

  private MailService mailService;
  private JavaMailSender mailSender;
  private CircabcConfig circabcConfig;

  @Before
  public void setUp() throws Exception {
    MailServiceImpl impl = new MailServiceImpl();

    mailSender = mock(JavaMailSender.class);
    circabcConfig = mock(CircabcConfig.class);
    CircabcApi circabcApi = mock(CircabcApi.class);
    MailPreferencesService mailPreferencesService = mock(
      MailPreferencesService.class
    );
    ContentService contentService = mock(ContentService.class);
    FileFolderService fileFolderService = mock(FileFolderService.class);

    setField(impl, "mailSender", mailSender);
    setField(impl, "circabcConfig", circabcConfig);
    setField(impl, "circabcApi", circabcApi);
    setField(impl, "mailPreferencesService", mailPreferencesService);
    setField(impl, "contentService", contentService);
    setField(impl, "fileFolderService", fileFolderService);

    impl.setNoReply("noreply@circabc.eu");
    impl.setSupport("support@circabc.eu");
    impl.setDevTeam("devteam@circabc.eu");
    impl.setHelpdeskAddress("helpdesk@circabc.eu");
    impl.setLogoCid("logoCid");
    impl.setHeaderLogoCid("headerLogoCid");
    impl.setHeaderEULogoCid("headerEULogoCid");
    impl.setDisclamerText("Disclaimer {0} {1} {2}");
    impl.setDisclamerHtml("Disclaimer {0} {1} {2}");

    mailService = impl;
  }

  private void setField(Object target, String fieldName, Object value)
    throws Exception {
    Field field = MailServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
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
  public void testSend_whenMailDisabled_returnsTrue() throws Exception {
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
    verify(mailSender, never()).send(any(MimeMessagePreparator.class));
  }

  @Test(expected = MessagingException.class)
  public void testSendList_whenNullTo_throwsException() throws Exception {
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
  public void testSendList_whenEmptyTo_throwsException() throws Exception {
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
  public void testSendNode_whenContentNull_throwsException() throws Exception {
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
  public void testSendNodeList_whenToEmpty_throwsException() throws Exception {
    NodeRef content = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "id"
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

  @Test(expected = MessagingException.class)
  public void testSendWithAttachment_whenToNull_throwsException()
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
  public void testSendWithNodeAttachments_whenToListNull_throwsException()
    throws Exception {
    NodeRef att = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "att");
    mailService.send(
      "from@circabc.eu",
      (List<String>) null,
      null,
      "Subject",
      "Body",
      Collections.singletonList(att)
    );
  }

  @Test(expected = MessagingException.class)
  public void testSendWithNodeAttachmentsSingleTo_whenToNull_throwsException()
    throws Exception {
    NodeRef att = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "att");
    mailService.send(
      "from@circabc.eu",
      (String) null,
      null,
      "Subject",
      "Body",
      Collections.singletonList(att)
    );
  }
}
