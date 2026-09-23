package eu.europa.ec.digit.circabc.rest.job;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import io.swagger.api.HistoryApi;
import io.swagger.model.UserRevocationRequest;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CleanGroupPermissionJobTest {

  private CleanGroupPermissionJob job;
  private HistoryApi historyApi;
  private LockService lockService;
  private JobExecutionContext context;
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

    job = new CleanGroupPermissionJob();
    historyApi = mock(HistoryApi.class);
    lockService = mock(LockService.class);
    context = mock(JobExecutionContext.class);
    jobDataMap = mock(JobDataMap.class);

    when(context.getMergedJobDataMap()).thenReturn(jobDataMap);
    when(jobDataMap.get("historyApi")).thenReturn(historyApi);
    when(jobDataMap.get("lockService")).thenReturn(lockService);
  }

  @Test
  public void testExecute_whenDisabled_thenDoesNothing()
    throws JobExecutionException {
    when(jobDataMap.get("enabled")).thenReturn("false");

    job.execute(context);

    verifyNoInteractions(historyApi);
  }

  @Test
  public void testExecute_whenEnabledAndNoJobs_thenNoProcessing()
    throws JobExecutionException {
    when(jobDataMap.get("enabled")).thenReturn("true");
    when(historyApi.getWaitingCleanPermission()).thenReturn(
      Collections.emptyList()
    );

    job.execute(context);

    verify(historyApi).getWaitingCleanPermission();
    verify(historyApi, never()).updateRevocationJobState(
      anyInt(),
      any(Date.class),
      any(Date.class),
      anyInt()
    );
  }

  @Test
  public void testExecute_whenJobReadyToProcess_thenCleansPermissions()
    throws JobExecutionException {
    when(jobDataMap.get("enabled")).thenReturn("true");

    UserRevocationRequest request = new UserRevocationRequest();
    request.setId(1);
    request.setAction("clean-permission");
    request.setGroupId("test-group-id");
    request.setRevocationDate(DateTime.now().minusDays(1));
    request.setUserIds(Arrays.asList("user1", "user2"));

    when(historyApi.getWaitingCleanPermission()).thenReturn(
      Collections.singletonList(request)
    );
    when(lockService.isLocked("cleanPermissionJob-1")).thenReturn(false);

    job.execute(context);

    verify(lockService).lock("cleanPermissionJob-1");
    verify(historyApi).cleanAndLogPermissions(any(), anySet(), eq(true));
    verify(historyApi).updateRevocationJobState(
      eq(1),
      any(Date.class),
      any(Date.class),
      eq(2)
    );
    verify(lockService).unlock("cleanPermissionJob-1");
  }

  @Test
  public void testExecute_whenJobIsLocked_thenSkipsProcessing()
    throws JobExecutionException {
    when(jobDataMap.get("enabled")).thenReturn("true");

    UserRevocationRequest request = new UserRevocationRequest();
    request.setId(2);
    request.setAction("clean-permission");
    request.setGroupId("test-group-id");
    request.setRevocationDate(DateTime.now().minusDays(1));

    when(historyApi.getWaitingCleanPermission()).thenReturn(
      Collections.singletonList(request)
    );
    when(lockService.isLocked("cleanPermissionJob-2")).thenReturn(true);

    job.execute(context);

    verify(lockService, never()).lock(anyString());
    verify(historyApi, never()).cleanAndLogPermissions(
      any(),
      anySet(),
      anyBoolean()
    );
  }

  @Test
  public void testExecute_whenRevocationDateInFuture_thenSkipsProcessing()
    throws JobExecutionException {
    when(jobDataMap.get("enabled")).thenReturn("true");

    UserRevocationRequest request = new UserRevocationRequest();
    request.setId(3);
    request.setAction("clean-permission");
    request.setGroupId("test-group-id");
    request.setRevocationDate(DateTime.now().plusDays(1));

    when(historyApi.getWaitingCleanPermission()).thenReturn(
      Collections.singletonList(request)
    );

    job.execute(context);

    verify(lockService, never()).lock(anyString());
  }

  @Test
  public void testExecute_whenExceptionDuringClean_thenSetsErrorState()
    throws JobExecutionException {
    when(jobDataMap.get("enabled")).thenReturn("true");

    UserRevocationRequest request = new UserRevocationRequest();
    request.setId(4);
    request.setAction("clean-permission");
    request.setGroupId("test-group-id");
    request.setRevocationDate(DateTime.now().minusDays(1));
    request.setUserIds(Arrays.asList("user1"));

    when(historyApi.getWaitingCleanPermission()).thenReturn(
      Collections.singletonList(request)
    );
    when(lockService.isLocked("cleanPermissionJob-4")).thenReturn(false);
    doThrow(new RuntimeException("error"))
      .when(historyApi)
      .cleanAndLogPermissions(any(), anySet(), eq(true));

    job.execute(context);

    verify(historyApi).updateRevocationJobState(
      eq(4),
      any(Date.class),
      any(Date.class),
      eq(-1)
    );
    verify(lockService).unlock("cleanPermissionJob-4");
  }
}
