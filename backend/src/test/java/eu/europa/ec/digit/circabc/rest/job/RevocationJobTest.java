package eu.europa.ec.digit.circabc.rest.job;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import io.swagger.api.GroupsApi;
import io.swagger.api.HistoryApi;
import io.swagger.api.UsersApi;
import io.swagger.model.InterestGroup;
import io.swagger.model.InterestGroupProfile;
import io.swagger.model.UserRevocationRequest;
import jakarta.transaction.UserTransaction;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class RevocationJobTest {

  private RevocationJob revocationJob;
  private HistoryApi historyApi;
  private GroupsApi groupsApi;
  private UsersApi usersApi;
  private PersonService personService;
  private TransactionService transactionService;
  private LockService lockService;
  private JobExecutionContext jobExecutionContext;
  private JobDataMap jobDataMap;

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
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    revocationJob = new RevocationJob();
    historyApi = mock(HistoryApi.class);
    groupsApi = mock(GroupsApi.class);
    usersApi = mock(UsersApi.class);
    personService = mock(PersonService.class);
    transactionService = mock(TransactionService.class);
    lockService = mock(LockService.class);
    jobExecutionContext = mock(JobExecutionContext.class);
    jobDataMap = mock(JobDataMap.class);

    when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
    when(jobDataMap.get("enabled")).thenReturn("true");
    when(jobDataMap.get("historyApi")).thenReturn(historyApi);
    when(jobDataMap.get("usersApi")).thenReturn(usersApi);
    when(jobDataMap.get("groupsApi")).thenReturn(groupsApi);
    when(jobDataMap.get("personService")).thenReturn(personService);
    when(jobDataMap.get("transactionService")).thenReturn(transactionService);
    when(jobDataMap.get("lockService")).thenReturn(lockService);
  }

  @Test
  public void testExecute_whenDisabled_thenDoesNothing() throws Exception {
    when(jobDataMap.get("enabled")).thenReturn("false");

    revocationJob.execute(jobExecutionContext);

    verifyNoInteractions(historyApi);
  }

  @Test
  public void testExecute_whenNoWaitingRevocations_thenDoesNothing()
    throws Exception {
    when(historyApi.getWaitingRevocations()).thenReturn(
      Collections.emptyList()
    );

    revocationJob.execute(jobExecutionContext);

    verify(historyApi).getWaitingRevocations();
    verifyNoMoreInteractions(groupsApi);
  }

  @Test
  public void testExecute_whenJobNotReadyToProcess_thenSkipsJob()
    throws Exception {
    UserRevocationRequest request = createRevocationRequest(
      1,
      DateTime.now().plusDays(1),
      "revoke"
    );
    when(historyApi.getWaitingRevocations()).thenReturn(List.of(request));

    revocationJob.execute(jobExecutionContext);

    verify(lockService, never()).lock(anyString());
  }

  @Test
  public void testExecute_whenJobIsLocked_thenSkipsJob() throws Exception {
    UserRevocationRequest request = createRevocationRequest(
      1,
      DateTime.now().minusDays(1),
      "revoke"
    );
    when(historyApi.getWaitingRevocations()).thenReturn(List.of(request));
    when(lockService.isLocked("revocationJob-1")).thenReturn(true);

    revocationJob.execute(jobExecutionContext);

    verify(lockService, never()).lock(anyString());
  }

  @Test
  public void testExecute_whenActionIsNotRevoke_thenSkipsJob()
    throws Exception {
    UserRevocationRequest request = createRevocationRequest(
      1,
      DateTime.now().minusDays(1),
      "other"
    );
    when(historyApi.getWaitingRevocations()).thenReturn(List.of(request));
    when(lockService.isLocked("revocationJob-1")).thenReturn(false);

    revocationJob.execute(jobExecutionContext);

    verify(lockService, never()).lock(anyString());
  }

  @Test
  public void testExecute_whenValidJob_thenProcessesRevocation()
    throws Exception {
    UserRevocationRequest request = createRevocationRequest(
      1,
      DateTime.now().minusDays(1),
      "revoke"
    );
    request.setUserIds(Arrays.asList("user1"));
    when(historyApi.getWaitingRevocations()).thenReturn(List.of(request));
    when(lockService.isLocked("revocationJob-1")).thenReturn(false);
    when(personService.personExists("user1")).thenReturn(true);

    InterestGroupProfile profile = new InterestGroupProfile();
    InterestGroup ig = new InterestGroup();
    ig.setId("group1");
    profile.setInterestGroup(ig);
    when(usersApi.getUserMembership("user1")).thenReturn(List.of(profile));

    UserTransaction trx = mock(UserTransaction.class);
    when(transactionService.getNonPropagatingUserTransaction(false)).thenReturn(
      trx
    );

    revocationJob.execute(jobExecutionContext);

    verify(lockService).lock("revocationJob-1");
    verify(groupsApi).groupsIdMembersUserIdDelete("group1", "user1");
    verify(trx).begin();
    verify(trx).commit();
    verify(historyApi).updateRevocationJobState(
      eq(1),
      any(Date.class),
      any(Date.class),
      eq(2)
    );
    verify(lockService).unlock("revocationJob-1");
  }

  @Test
  public void testExecute_whenUserDoesNotExist_thenSkipsUser()
    throws Exception {
    UserRevocationRequest request = createRevocationRequest(
      2,
      DateTime.now().minusDays(1),
      "revoke"
    );
    request.setUserIds(Arrays.asList("nonexistent"));
    when(historyApi.getWaitingRevocations()).thenReturn(List.of(request));
    when(lockService.isLocked("revocationJob-2")).thenReturn(false);
    when(personService.personExists("nonexistent")).thenReturn(false);

    revocationJob.execute(jobExecutionContext);

    verify(usersApi, never()).getUserMembership(anyString());
    verify(historyApi).updateRevocationJobState(
      eq(2),
      any(Date.class),
      any(Date.class),
      eq(2)
    );
  }

  @Test
  public void testExecute_whenExceptionOccurs_thenSetsStateToMinusOne()
    throws Exception {
    UserRevocationRequest request = createRevocationRequest(
      3,
      DateTime.now().minusDays(1),
      "revoke"
    );
    request.setUserIds(Arrays.asList("user1"));
    when(historyApi.getWaitingRevocations()).thenReturn(List.of(request));
    when(lockService.isLocked("revocationJob-3")).thenReturn(false);
    when(personService.personExists("user1")).thenReturn(true);

    when(usersApi.getUserMembership("user1")).thenThrow(
      new RuntimeException("error")
    );

    revocationJob.execute(jobExecutionContext);

    verify(historyApi).updateRevocationJobState(
      eq(3),
      any(Date.class),
      any(Date.class),
      eq(-1)
    );
    verify(lockService).unlock("revocationJob-3");
  }

  private UserRevocationRequest createRevocationRequest(
    Integer id,
    DateTime revocationDate,
    String action
  ) {
    UserRevocationRequest request = new UserRevocationRequest();
    request.setId(id);
    request.setRevocationDate(revocationDate);
    request.setAction(action);
    request.setUserIds(Collections.emptyList());
    return request;
  }
}
