package eu.europa.ec.digit.circabc.rest.service.iam;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import io.swagger.model.alfresco.CircabcModel;
import jakarta.mail.MessagingException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.task.TaskExecutor;

public class SynchronizationServiceImplTest {

  private SynchronizationServiceImpl service;
  private NodeService nodeService;
  private MailService mailService;
  private TaskExecutor taskExecutor;
  private IamWSClient iamWSClient;
  private EcordaDaoServiceImpl ecordaDaoServiceImpl;

  @Before
  public void setUp() throws Exception {
    service = new SynchronizationServiceImpl();
    nodeService = mock(NodeService.class);
    mailService = mock(MailService.class);
    taskExecutor = mock(TaskExecutor.class);
    iamWSClient = mock(IamWSClient.class);
    ecordaDaoServiceImpl = mock(EcordaDaoServiceImpl.class);

    setField("nodeService", nodeService);
    setField("mailService", mailService);
    setField("taskExecutor", taskExecutor);
    setField("iamWSClient", iamWSClient);
    setField("ecordaDaoServiceImpl", ecordaDaoServiceImpl);
    setField("emailErrorAdress", "error@test.com");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SynchronizationServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testGrantThemeRole_whenCalled_thenSubmitsTaskToExecutor() {
    service.grantThemeRole("user1", "theme1", "profile1");
    verify(taskExecutor).execute(any(Runnable.class));
  }

  @Test
  public void testRevokeThemeRole_whenCalled_thenSubmitsTaskToExecutor() {
    service.revokeThemeRole("user1", "theme1", "profile1");
    verify(taskExecutor).execute(any(Runnable.class));
  }

  @Test
  public void testGrantThemeRole_whenExecuted_thenCallsIamClient() {
    doAnswer(invocation -> {
      ((Runnable) invocation.getArgument(0)).run();
      return null;
    })
      .when(taskExecutor)
      .execute(any(Runnable.class));

    service.grantThemeRole("user1", "theme1", "profile1");

    verify(iamWSClient).grantThemeRole("user1", "theme1", "profile1");
  }

  @Test
  public void testRevokeThemeRole_whenExecuted_thenCallsIamClient() {
    doAnswer(invocation -> {
      ((Runnable) invocation.getArgument(0)).run();
      return null;
    })
      .when(taskExecutor)
      .execute(any(Runnable.class));

    service.revokeThemeRole("user1", "theme1", "profile1");

    verify(iamWSClient).revokeThemeRole("user1", "theme1", "profile1");
  }

  @Test
  public void testGetEcordaThemeIds_whenValidIgNode_thenReturnsThemeIds() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );
    when(ecordaDaoServiceImpl.getEcordaThemaID(igRef.toString())).thenReturn(
      Arrays.asList("theme1", "theme2")
    );

    List<String> result = service.getEcordaThemeIds(igRef);

    assertEquals(Arrays.asList("theme1", "theme2"), result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetEcordaThemeIds_whenNotIgNode_thenThrowsException() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "not-ig"
    );
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      false
    );

    service.getEcordaThemeIds(nodeRef);
  }

  @Test
  public void testGetEcordaThemeIds_whenDaoReturnsNull_thenReturnsEmptyList() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );
    when(ecordaDaoServiceImpl.getEcordaThemaID(igRef.toString())).thenReturn(
      null
    );

    List<String> result = service.getEcordaThemeIds(igRef);

    assertEquals(Collections.emptyList(), result);
  }

  @Test
  public void testGetEcordaThemeIds_whenDaoThrowsException_thenReturnsEmptyList() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );
    when(ecordaDaoServiceImpl.getEcordaThemaID(igRef.toString())).thenThrow(
      new RuntimeException("DB error")
    );

    List<String> result = service.getEcordaThemeIds(igRef);

    assertEquals(Collections.emptyList(), result);
  }

  @Test
  public void testGrantThemeRoles_whenMultipleUsers_thenSubmitsTaskForEach() {
    Set<String> users = new HashSet<>(Arrays.asList("user1", "user2", "user3"));

    service.grantThemeRoles(users, "theme1", "profile1");

    verify(taskExecutor, times(3)).execute(any(Runnable.class));
  }

  @Test
  public void testSendEmail_whenCalled_thenSendsViaMailService()
    throws MessagingException {
    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");

    service.sendEmail(
      "user1",
      "theme1",
      "role1",
      "grantThemeRole",
      new RuntimeException("test error")
    );

    verify(mailService).send(
      eq("noreply@test.com"),
      eq("error@test.com"),
      isNull(),
      contains("grantThemeRole"),
      contains("test error"),
      eq(false),
      eq(false)
    );
  }

  @Test
  public void testSendEmail_whenMailServiceThrows_thenDoesNotPropagate()
    throws MessagingException {
    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");
    when(
      mailService.send(
        anyString(),
        anyString(),
        isNull(),
        anyString(),
        anyString(),
        eq(false),
        eq(false)
      )
    ).thenThrow(new MessagingException("SMTP error"));

    // Should not throw
    service.sendEmail(
      "user1",
      "theme1",
      "role1",
      "grantThemeRole",
      new RuntimeException("test error")
    );
  }
}
