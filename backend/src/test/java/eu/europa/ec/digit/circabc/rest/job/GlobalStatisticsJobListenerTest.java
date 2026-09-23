package eu.europa.ec.digit.circabc.rest.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.statistic.global.GlobalStatisticsService;
import java.lang.reflect.Field;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class GlobalStatisticsJobListenerTest {

  private GlobalStatisticsJobListener listener;
  private LockService lockService;
  private GlobalStatisticsService globalStatisticsService;
  private JobExecutionContext jobContext;
  private JobDataMap jobDataMap;
  private JobDataMap mergedJobDataMap;

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

    listener = new GlobalStatisticsJobListener();
    lockService = mock(LockService.class);
    globalStatisticsService = mock(GlobalStatisticsService.class);

    listener.setCircabcLockService(lockService);
    listener.setGlobalStatisticsService(globalStatisticsService);

    jobContext = mock(JobExecutionContext.class);
    jobDataMap = new JobDataMap();
    mergedJobDataMap = new JobDataMap();

    JobDetail jobDetail = mock(JobDetail.class);
    when(jobContext.getJobDetail()).thenReturn(jobDetail);
    when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
    when(jobContext.getMergedJobDataMap()).thenReturn(mergedJobDataMap);
  }

  @Test
  public void testExecute_whenDisabled_thenDoesNothing() throws Exception {
    mergedJobDataMap.put("enabled", "false");

    listener.execute(jobContext);

    verifyNoInteractions(globalStatisticsService);
  }

  @Test
  public void testExecute_whenEnabledAndLockAcquired_thenRunsStatistics()
    throws Exception {
    mergedJobDataMap.put("enabled", "true");
    jobDataMap.put("circabcLockService", lockService);
    jobDataMap.put("globalStatisticsService", globalStatisticsService);

    when(lockService.isLocked("globalStatisticsRunningLock"))
      .thenReturn(false)
      .thenReturn(true);
    when(globalStatisticsService.isReportSaveFolderExisting()).thenReturn(true);

    listener.execute(jobContext);

    verify(globalStatisticsService).makeGlobalStats();
    verify(lockService).unlock("globalStatisticsRunningLock");
  }

  @Test
  public void testExecute_whenLockNotAcquired_thenSkips() throws Exception {
    mergedJobDataMap.put("enabled", "true");
    jobDataMap.put("circabcLockService", lockService);
    jobDataMap.put("globalStatisticsService", globalStatisticsService);

    when(lockService.isLocked("globalStatisticsRunningLock")).thenReturn(true);

    listener.execute(jobContext);

    verify(globalStatisticsService, never()).makeGlobalStats();
  }

  @Test
  public void testExecute_whenFolderNotExisting_thenPreparesFolder()
    throws Exception {
    mergedJobDataMap.put("enabled", "true");
    jobDataMap.put("circabcLockService", lockService);
    jobDataMap.put("globalStatisticsService", globalStatisticsService);

    when(lockService.isLocked("globalStatisticsRunningLock"))
      .thenReturn(false)
      .thenReturn(true);
    when(globalStatisticsService.isReportSaveFolderExisting()).thenReturn(
      false
    );

    listener.execute(jobContext);

    verify(globalStatisticsService).prepareFolderRecipient();
  }

  @Test
  public void testLockJobFile_whenNotLocked_thenReturnsOne() {
    when(lockService.isLocked("globalStatisticsRunningLock")).thenReturn(false);

    Integer result = listener.lockJobFile();

    assertEquals(Integer.valueOf(1), result);
    verify(lockService).lock("globalStatisticsRunningLock");
  }

  @Test
  public void testLockJobFile_whenAlreadyLocked_thenReturnsZero() {
    when(lockService.isLocked("globalStatisticsRunningLock")).thenReturn(true);

    Integer result = listener.lockJobFile();

    assertEquals(Integer.valueOf(0), result);
    verify(lockService, never()).lock(anyString());
  }

  @Test
  public void testLockJobFile_whenLockThrowsException_thenReturnsZero() {
    when(lockService.isLocked("globalStatisticsRunningLock")).thenReturn(false);
    doThrow(new IllegalStateException("already locked"))
      .when(lockService)
      .lock("globalStatisticsRunningLock");

    Integer result = listener.lockJobFile();

    assertEquals(Integer.valueOf(0), result);
  }

  @Test
  public void testUnlockJobFile_whenLocked_thenReturnsOne() {
    when(lockService.isLocked("globalStatisticsRunningLock")).thenReturn(true);

    Integer result = listener.unlockJobFile();

    assertEquals(Integer.valueOf(1), result);
    verify(lockService).unlock("globalStatisticsRunningLock");
  }

  @Test
  public void testUnlockJobFile_whenNotLocked_thenReturnsZero() {
    when(lockService.isLocked("globalStatisticsRunningLock")).thenReturn(false);

    Integer result = listener.unlockJobFile();

    assertEquals(Integer.valueOf(0), result);
    verify(lockService, never()).unlock(anyString());
  }

  @Test
  public void testGetGlobalStatisticsLock_returnsConstant() {
    assertEquals(
      "globalStatisticsRunningLock",
      GlobalStatisticsJobListener.getGlobalStatisticsLock()
    );
  }
}
