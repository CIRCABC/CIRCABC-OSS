package eu.europa.ec.digit.circabc.rest.job;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import io.swagger.api.GroupsApi;
import io.swagger.api.HistoryApi;
import io.swagger.model.db.MemberExpirationDAO;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class ExpiredMembersJobTest {

  private ExpiredMembersJob job;
  private JobExecutionContext context;
  private JobDataMap jobDataMap;
  private LockService lockService;
  private HistoryApi historyApi;
  private GroupsApi groupsApi;

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

    job = new ExpiredMembersJob();
    context = mock(JobExecutionContext.class);
    jobDataMap = mock(JobDataMap.class);
    lockService = mock(LockService.class);
    historyApi = mock(HistoryApi.class);
    groupsApi = mock(GroupsApi.class);

    when(context.getMergedJobDataMap()).thenReturn(jobDataMap);
    when(jobDataMap.get("lockService")).thenReturn(lockService);
    when(jobDataMap.get("historyApi")).thenReturn(historyApi);
    when(jobDataMap.get("groupsApi")).thenReturn(groupsApi);
    when(jobDataMap.get("enabled")).thenReturn("true");
  }

  @Test
  public void testExecute_whenDisabled_thenDoesNothing() throws Exception {
    when(jobDataMap.get("enabled")).thenReturn("false");

    job.execute(context);

    verifyNoInteractions(lockService);
    verifyNoInteractions(historyApi);
  }

  @Test
  public void testExecute_whenAlreadyLocked_thenSkipsProcessing()
    throws Exception {
    when(lockService.isLocked("EXPIRED_MEMBERS_JOB")).thenReturn(true);

    job.execute(context);

    verify(lockService, never()).lock(anyString());
    verifyNoInteractions(historyApi);
  }

  @Test
  public void testExecute_whenNoExpiredUsers_thenLocksAndUnlocks()
    throws Exception {
    when(lockService.isLocked("EXPIRED_MEMBERS_JOB")).thenReturn(false);
    when(historyApi.getExpiredUsers()).thenReturn(Collections.emptyList());

    job.execute(context);

    verify(lockService).lock("EXPIRED_MEMBERS_JOB");
    verify(historyApi).getExpiredUsers();
    verify(lockService).unlock("EXPIRED_MEMBERS_JOB");
  }

  @Test
  public void testExecute_whenExpiredUsersExist_thenRemovesThem()
    throws Exception {
    when(lockService.isLocked("EXPIRED_MEMBERS_JOB")).thenReturn(false);

    MemberExpirationDAO user1 = new MemberExpirationDAO();
    user1.setGroupId("group1");
    user1.setUserId("user1");

    MemberExpirationDAO user2 = new MemberExpirationDAO();
    user2.setGroupId("group2");
    user2.setUserId("user2");

    when(historyApi.getExpiredUsers()).thenReturn(Arrays.asList(user1, user2));

    job.execute(context);

    verify(groupsApi).groupsIdMembersUserIdDelete("group1", "user1");
    verify(groupsApi).groupsIdMembersUserIdDelete("group2", "user2");
    verify(lockService).unlock("EXPIRED_MEMBERS_JOB");
  }

  @Test
  public void testExecute_whenDeleteThrows_thenContinuesWithOthers()
    throws Exception {
    when(lockService.isLocked("EXPIRED_MEMBERS_JOB")).thenReturn(false);

    MemberExpirationDAO user1 = new MemberExpirationDAO();
    user1.setGroupId("group1");
    user1.setUserId("user1");

    MemberExpirationDAO user2 = new MemberExpirationDAO();
    user2.setGroupId("group2");
    user2.setUserId("user2");

    when(historyApi.getExpiredUsers()).thenReturn(Arrays.asList(user1, user2));
    doThrow(new RuntimeException("fail"))
      .when(groupsApi)
      .groupsIdMembersUserIdDelete("group1", "user1");

    job.execute(context);

    verify(groupsApi).groupsIdMembersUserIdDelete("group2", "user2");
    verify(lockService).unlock("EXPIRED_MEMBERS_JOB");
  }
}
