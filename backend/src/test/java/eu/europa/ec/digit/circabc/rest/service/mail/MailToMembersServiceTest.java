package eu.europa.ec.digit.circabc.rest.service.mail;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import jakarta.mail.MessagingException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class MailToMembersServiceTest {

  private MailToMembersService mailToMembersService;
  private NodeRef testNodeRef;

  @Before
  public void setUp() {
    mailToMembersService = mock(MailToMembersService.class);
    testNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
  }

  @Test
  public void testSendToAllMembers_whenValidInput_thenReturnsTrue()
    throws MessagingException {
    when(
      mailToMembersService.sendToAllMembers(
        testNodeRef,
        "from@test.com",
        "to@test.com",
        "Subject",
        "Body",
        false
      )
    ).thenReturn(true);

    boolean result = mailToMembersService.sendToAllMembers(
      testNodeRef,
      "from@test.com",
      "to@test.com",
      "Subject",
      "Body",
      false
    );

    assertTrue(result);
    verify(mailToMembersService).sendToAllMembers(
      testNodeRef,
      "from@test.com",
      "to@test.com",
      "Subject",
      "Body",
      false
    );
  }

  @Test
  public void testSendToAllMembers_whenHtmlTrue_thenReturnsTrue()
    throws MessagingException {
    when(
      mailToMembersService.sendToAllMembers(
        testNodeRef,
        "from@test.com",
        "to@test.com",
        "Subject",
        "<b>Body</b>",
        true
      )
    ).thenReturn(true);

    boolean result = mailToMembersService.sendToAllMembers(
      testNodeRef,
      "from@test.com",
      "to@test.com",
      "Subject",
      "<b>Body</b>",
      true
    );

    assertTrue(result);
  }

  @Test
  public void testSendToAllMembers_whenSendFails_thenReturnsFalse()
    throws MessagingException {
    when(
      mailToMembersService.sendToAllMembers(
        any(NodeRef.class),
        anyString(),
        anyString(),
        anyString(),
        anyString(),
        anyBoolean()
      )
    ).thenReturn(false);

    boolean result = mailToMembersService.sendToAllMembers(
      testNodeRef,
      "from@test.com",
      "to@test.com",
      "Subject",
      "Body",
      false
    );

    assertFalse(result);
  }

  @Test(expected = MessagingException.class)
  public void testSendToAllMembers_whenMessagingError_thenThrowsException()
    throws MessagingException {
    when(
      mailToMembersService.sendToAllMembers(
        any(NodeRef.class),
        anyString(),
        anyString(),
        anyString(),
        anyString(),
        anyBoolean()
      )
    ).thenThrow(new MessagingException("SMTP error"));

    mailToMembersService.sendToAllMembers(
      testNodeRef,
      "from@test.com",
      "to@test.com",
      "Subject",
      "Body",
      false
    );
  }

  @Test
  public void testSendToAllMembers_whenNullNodeRef_thenReturnsFalse()
    throws MessagingException {
    when(
      mailToMembersService.sendToAllMembers(
        isNull(),
        anyString(),
        anyString(),
        anyString(),
        anyString(),
        anyBoolean()
      )
    ).thenReturn(false);

    boolean result = mailToMembersService.sendToAllMembers(
      null,
      "from@test.com",
      "to@test.com",
      "Subject",
      "Body",
      false
    );

    assertFalse(result);
  }
}
