package eu.europa.ec.digit.circabc.rest.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.CircabcServiceRegistry;
import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.config.CircabcConfig;
import io.swagger.model.MeetingRequestStatus;
import io.swagger.model.UpdateMode;
import jakarta.transaction.UserTransaction;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class MeetingResponseListenerTest {

  private MeetingResponseListener listener;
  private JobExecutionContext jobContext;
  private JobDataMap mergedJobDataMap;
  private JobDataMap jobDetailDataMap;
  private ServiceRegistry serviceRegistry;
  private CircabcServiceRegistry circabcServiceRegistry;
  private EventService eventService;
  private UserService userService;
  private LockService lockService;
  private CircabcConfig circabcConfig;
  private TransactionService transactionService;
  private UserTransaction userTransaction;

  @Before
  public void setUp() throws Exception {
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
    AuthenticationUtil.setFullyAuthenticatedUser("admin");

    listener = new MeetingResponseListener();

    jobContext = mock(JobExecutionContext.class);
    mergedJobDataMap = mock(JobDataMap.class);
    jobDetailDataMap = mock(JobDataMap.class);
    JobDetail jobDetail = mock(JobDetail.class);

    serviceRegistry = mock(ServiceRegistry.class);
    circabcServiceRegistry = mock(CircabcServiceRegistry.class);
    eventService = mock(EventService.class);
    userService = mock(UserService.class);
    lockService = mock(LockService.class);
    circabcConfig = mock(CircabcConfig.class);
    transactionService = mock(TransactionService.class);
    userTransaction = mock(UserTransaction.class);

    when(jobContext.getMergedJobDataMap()).thenReturn(mergedJobDataMap);
    when(jobContext.getJobDetail()).thenReturn(jobDetail);
    when(jobDetail.getJobDataMap()).thenReturn(jobDetailDataMap);
    when(jobDetailDataMap.get("serviceRegistry")).thenReturn(serviceRegistry);
    when(jobDetailDataMap.get("circabcServiceRegistry")).thenReturn(
      circabcServiceRegistry
    );
    when(circabcServiceRegistry.getNonSecureEventService()).thenReturn(
      eventService
    );
    when(circabcServiceRegistry.getUserService()).thenReturn(userService);
    when(circabcServiceRegistry.getLockService()).thenReturn(lockService);
    when(circabcServiceRegistry.getCircabcConfig()).thenReturn(circabcConfig);

    when(circabcConfig.getEmailProtocol()).thenReturn("pop3");
    when(circabcConfig.getEmailServer()).thenReturn("localhost");
    when(circabcConfig.getEmailBox()).thenReturn("INBOX");
    when(circabcConfig.getEmailUserName()).thenReturn("user");
    when(circabcConfig.getEmailPassword()).thenReturn("pass");
    when(circabcConfig.getEmailServerPort()).thenReturn(110);
    when(circabcConfig.isEmailListenerActive()).thenReturn(true);
    when(circabcConfig.isEmailUseTls()).thenReturn(false);

    when(serviceRegistry.getTransactionService()).thenReturn(
      transactionService
    );
    when(transactionService.getUserTransaction()).thenReturn(userTransaction);

    setField("eventService", eventService);
    setField("serviceRegistry", serviceRegistry);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = MeetingResponseListener.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(listener, value);
  }

  @Test
  public void testExecute_whenDisabled_thenReturnsEarly() throws Exception {
    when(mergedJobDataMap.get("enabled")).thenReturn("false");
    when(lockService.isLocked(anyString())).thenReturn(false);

    listener.execute(jobContext);

    verify(lockService, never()).isLocked(anyString());
  }

  @Test
  public void testExecute_whenLocked_thenSkipsProcessing() throws Exception {
    when(mergedJobDataMap.get("enabled")).thenReturn("true");
    when(lockService.isLocked("MeetingResponseListener")).thenReturn(true);

    listener.execute(jobContext);

    verify(lockService, never()).lock(anyString());
  }

  @Test
  public void testExecute_whenEnabled_thenAcquiresAndReleasesLock()
    throws Exception {
    when(mergedJobDataMap.get("enabled")).thenReturn("true");
    when(lockService.isLocked("MeetingResponseListener")).thenReturn(false);
    when(circabcConfig.isEmailListenerActive()).thenReturn(false);

    listener.execute(jobContext);

    verify(lockService).lock("MeetingResponseListener");
    verify(lockService).unlock("MeetingResponseListener");
  }

  @Test
  public void testGetMeetingRequestStatus_accepted() throws Exception {
    Method method = MeetingResponseListener.class.getDeclaredMethod(
      "getMeetingRequestStatus",
      String.class
    );
    method.setAccessible(true);

    String body =
      "BEGIN:VCALENDAR\r\nMETHOD:REPLY\r\nBEGIN:VEVENT\r\nATTENDEE;PARTSTAT=ACCEPTED\r\nEND:VEVENT\r\nEND:VCALENDAR";
    MeetingRequestStatus result = (MeetingRequestStatus) method.invoke(
      listener,
      body
    );

    assertEquals(MeetingRequestStatus.Accepted, result);
  }

  @Test
  public void testGetMeetingRequestStatus_declined() throws Exception {
    Method method = MeetingResponseListener.class.getDeclaredMethod(
      "getMeetingRequestStatus",
      String.class
    );
    method.setAccessible(true);

    String body =
      "BEGIN:VCALENDAR\r\nMETHOD:REPLY\r\nBEGIN:VEVENT\r\nATTENDEE;PARTSTAT=DECLINED\r\nEND:VEVENT\r\nEND:VCALENDAR";
    MeetingRequestStatus result = (MeetingRequestStatus) method.invoke(
      listener,
      body
    );

    assertEquals(MeetingRequestStatus.Rejected, result);
  }

  @Test
  public void testGetMeetingRequestStatus_tentative() throws Exception {
    Method method = MeetingResponseListener.class.getDeclaredMethod(
      "getMeetingRequestStatus",
      String.class
    );
    method.setAccessible(true);

    String body =
      "BEGIN:VCALENDAR\r\nMETHOD:REPLY\r\nBEGIN:VEVENT\r\nATTENDEE;PARTSTAT=TENTATIVE\r\nEND:VEVENT\r\nEND:VCALENDAR";
    MeetingRequestStatus result = (MeetingRequestStatus) method.invoke(
      listener,
      body
    );

    assertEquals(MeetingRequestStatus.Pending, result);
  }

  @Test
  public void testGetMeetingRequestStatus_unknown_returnsNull()
    throws Exception {
    Method method = MeetingResponseListener.class.getDeclaredMethod(
      "getMeetingRequestStatus",
      String.class
    );
    method.setAccessible(true);

    String body =
      "BEGIN:VCALENDAR\r\nMETHOD:REPLY\r\nBEGIN:VEVENT\r\nATTENDEE;PARTSTAT=UNKNOWN\r\nEND:VEVENT\r\nEND:VCALENDAR";
    MeetingRequestStatus result = (MeetingRequestStatus) method.invoke(
      listener,
      body
    );

    assertNull(result);
  }

  @Test
  public void testGetMeetingNodeRef_extractsUidFromBody() throws Exception {
    Method method = MeetingResponseListener.class.getDeclaredMethod(
      "getMeetingNodeRef",
      String.class
    );
    method.setAccessible(true);

    String uid = "abc-123-def-456";
    String body =
      "BEGIN:VCALENDAR\r\nBEGIN:VEVENT\r\nUID:" + uid + "\r\nEND:VEVENT\r\n";
    NodeRef result = (NodeRef) method.invoke(listener, body);

    NodeRef expected = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      uid
    );
    assertEquals(expected, result);
  }

  @Test
  public void testIsCalendarResponse_validResponse() throws Exception {
    Method method = MeetingResponseListener.class.getDeclaredMethod(
      "isCalendarResponse",
      String.class
    );
    method.setAccessible(true);

    String body = "BEGIN:VCALENDAR\r\nMETHOD:REPLY\r\nBEGIN:VEVENT\r\n";
    boolean result = (boolean) method.invoke(listener, body);

    assertTrue(result);
  }

  @Test
  public void testIsCalendarResponse_missingMethodReply() throws Exception {
    Method method = MeetingResponseListener.class.getDeclaredMethod(
      "isCalendarResponse",
      String.class
    );
    method.setAccessible(true);

    String body = "BEGIN:VCALENDAR\r\nBEGIN:VEVENT\r\n";
    boolean result = (boolean) method.invoke(listener, body);

    assertFalse(result);
  }

  @Test
  public void testIsCalendarResponse_missingVEvent() throws Exception {
    Method method = MeetingResponseListener.class.getDeclaredMethod(
      "isCalendarResponse",
      String.class
    );
    method.setAccessible(true);

    String body = "BEGIN:VCALENDAR\r\nMETHOD:REPLY\r\n";
    boolean result = (boolean) method.invoke(listener, body);

    assertFalse(result);
  }

  @Test
  public void testCheckEmail_whenInactive_thenReturnsEarly() throws Exception {
    when(mergedJobDataMap.get("enabled")).thenReturn("true");
    when(lockService.isLocked("MeetingResponseListener")).thenReturn(false);
    when(circabcConfig.isEmailListenerActive()).thenReturn(false);

    listener.execute(jobContext);

    verify(lockService).lock("MeetingResponseListener");
    verify(lockService).unlock("MeetingResponseListener");
  }

  @Test
  public void testUpdateMeetingRequestStatus_commitsTransaction()
    throws Exception {
    Method method = MeetingResponseListener.class.getDeclaredMethod(
      "updateMeetingRequestStatus",
      NodeRef.class,
      MeetingRequestStatus.class,
      UpdateMode.class,
      String.class,
      TransactionService.class
    );
    method.setAccessible(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );

    method.invoke(
      listener,
      nodeRef,
      MeetingRequestStatus.Accepted,
      UpdateMode.AllOccurences,
      "testuser",
      transactionService
    );

    verify(userTransaction).begin();
    verify(eventService).setMeetingRequestStatus(
      nodeRef,
      "testuser",
      MeetingRequestStatus.Accepted,
      UpdateMode.AllOccurences
    );
    verify(userTransaction).commit();
  }

  @Test
  public void testUpdateMeetingRequestStatus_rollsBackOnException()
    throws Exception {
    Method method = MeetingResponseListener.class.getDeclaredMethod(
      "updateMeetingRequestStatus",
      NodeRef.class,
      MeetingRequestStatus.class,
      UpdateMode.class,
      String.class,
      TransactionService.class
    );
    method.setAccessible(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    doThrow(new RuntimeException("fail"))
      .when(eventService)
      .setMeetingRequestStatus(any(), anyString(), any(), any());

    method.invoke(
      listener,
      nodeRef,
      MeetingRequestStatus.Accepted,
      UpdateMode.AllOccurences,
      "testuser",
      transactionService
    );

    verify(userTransaction).begin();
    verify(userTransaction).rollback();
    verify(userTransaction, never()).commit();
  }
}
