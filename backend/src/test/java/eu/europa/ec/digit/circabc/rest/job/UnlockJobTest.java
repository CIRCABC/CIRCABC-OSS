package eu.europa.ec.digit.circabc.rest.job;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.CircabcServiceRegistry;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import java.lang.reflect.Field;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class UnlockJobTest {

  private UnlockJob unlockJob;
  private JobExecutionContext context;
  private JobDataMap mergedJobDataMap;
  private JobDataMap jobDetailDataMap;
  private JobDetail jobDetail;
  private CircabcServiceRegistry circabcServiceRegistry;
  private LockService lockService;

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

    unlockJob = new UnlockJob();

    context = mock(JobExecutionContext.class);
    mergedJobDataMap = mock(JobDataMap.class);
    jobDetailDataMap = mock(JobDataMap.class);
    jobDetail = mock(JobDetail.class);
    circabcServiceRegistry = mock(CircabcServiceRegistry.class);
    lockService = mock(LockService.class);

    when(context.getMergedJobDataMap()).thenReturn(mergedJobDataMap);
    when(context.getJobDetail()).thenReturn(jobDetail);
    when(jobDetail.getJobDataMap()).thenReturn(jobDetailDataMap);
    when(jobDetailDataMap.get("circabcServiceRegistry")).thenReturn(
      circabcServiceRegistry
    );
    when(jobDetailDataMap.get("maxJobLockTimeInHours")).thenReturn("24");
    when(circabcServiceRegistry.getLockService()).thenReturn(lockService);
  }

  @Test
  public void testExecute_whenDisabled_thenDoesNothing()
    throws JobExecutionException {
    when(mergedJobDataMap.get("enabled")).thenReturn("false");

    unlockJob.execute(context);

    verifyNoInteractions(lockService);
  }

  @Test
  public void testExecute_whenEnabled_thenCallsUnlockAll()
    throws JobExecutionException {
    when(mergedJobDataMap.get("enabled")).thenReturn("true");

    unlockJob.execute(context);

    verify(lockService).unlockAll(24);
  }

  @Test
  public void testExecute_whenLockServiceThrows_thenNoExceptionPropagated()
    throws JobExecutionException {
    when(mergedJobDataMap.get("enabled")).thenReturn("true");
    doThrow(new RuntimeException("DB error"))
      .when(lockService)
      .unlockAll(anyInt());

    unlockJob.execute(context);
  }
}
