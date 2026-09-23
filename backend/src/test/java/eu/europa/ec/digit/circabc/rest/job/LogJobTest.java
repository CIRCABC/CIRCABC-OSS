package eu.europa.ec.digit.circabc.rest.job;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.CircabcServiceRegistry;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import java.lang.reflect.Field;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class LogJobTest {

  private LogJob logJob;
  private JobExecutionContext context;
  private JobDataMap mergedJobDataMap;
  private JobDataMap jobDetailDataMap;
  private JobDetail jobDetail;
  private CircabcServiceRegistry circabcServiceRegistry;
  private LockService lockService;
  private LogService logService;

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

    logJob = new LogJob();

    context = mock(JobExecutionContext.class);
    mergedJobDataMap = mock(JobDataMap.class);
    jobDetailDataMap = mock(JobDataMap.class);
    jobDetail = mock(JobDetail.class);
    circabcServiceRegistry = mock(CircabcServiceRegistry.class);
    lockService = mock(LockService.class);
    logService = mock(LogService.class);

    when(context.getMergedJobDataMap()).thenReturn(mergedJobDataMap);
    when(context.getJobDetail()).thenReturn(jobDetail);
    when(jobDetail.getJobDataMap()).thenReturn(jobDetailDataMap);
    when(jobDetailDataMap.get("circabcServiceRegistry")).thenReturn(
      circabcServiceRegistry
    );
    when(circabcServiceRegistry.getLockService()).thenReturn(lockService);
    when(circabcServiceRegistry.getLogService()).thenReturn(logService);
  }

  @Test
  public void testExecute_whenDisabled_thenDoesNothing() throws Exception {
    when(mergedJobDataMap.get("enabled")).thenReturn("false");

    logJob.execute(context);

    verifyNoInteractions(lockService);
    verifyNoInteractions(logService);
  }

  @Test
  public void testExecute_whenEnabledAndNotLocked_thenProcessesLogs()
    throws Exception {
    when(mergedJobDataMap.get("enabled")).thenReturn("true");
    when(lockService.isLocked("LOG_JOB")).thenReturn(false);

    logJob.execute(context);

    verify(lockService).lock("LOG_JOB");
    verify(logService).processRestLog();
    verify(lockService).unlock("LOG_JOB");
  }

  @Test
  public void testExecute_whenEnabledAndAlreadyLocked_thenSkipsProcessing()
    throws Exception {
    when(mergedJobDataMap.get("enabled")).thenReturn("true");
    when(lockService.isLocked("LOG_JOB")).thenReturn(true);

    logJob.execute(context);

    verify(lockService, never()).lock(anyString());
    verifyNoInteractions(logService);
  }

  @Test
  public void testExecute_whenProcessRestLogThrows_thenUnlocksAndDoesNotPropagate()
    throws Exception {
    when(mergedJobDataMap.get("enabled")).thenReturn("true");
    when(lockService.isLocked("LOG_JOB")).thenReturn(false);
    doThrow(new RuntimeException("db error")).when(logService).processRestLog();

    logJob.execute(context);

    verify(lockService).lock("LOG_JOB");
    verify(lockService).unlock("LOG_JOB");
  }

  @Test
  public void testExecute_whenEnabledNull_thenDoesNotProcess()
    throws Exception {
    when(mergedJobDataMap.get("enabled")).thenReturn(null);

    logJob.execute(context);

    verifyNoInteractions(lockService);
  }
}
