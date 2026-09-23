package eu.europa.ec.digit.circabc.rest.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.auto.upload.AutoUploadManagementService;
import eu.europa.ec.digit.circabc.rest.service.lock.DBLockServiceImpl;
import io.swagger.model.Configuration;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AutoUploadJobListenerTest {

  private AutoUploadJobListener listener;
  private JobExecutionContext jobContext;
  private JobDetail jobDetail;
  private JobDataMap jobDataMap;
  private JobDataMap mergedJobDataMap;
  private AutoUploadManagementService autoUploadManagementService;
  private DBLockServiceImpl circabcLockService;

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

    listener = new AutoUploadJobListener();
    jobContext = mock(JobExecutionContext.class);
    jobDetail = mock(JobDetail.class);
    jobDataMap = new JobDataMap();
    mergedJobDataMap = new JobDataMap();

    autoUploadManagementService = mock(AutoUploadManagementService.class);
    circabcLockService = mock(DBLockServiceImpl.class);

    jobDataMap.put("autoUploadManagementService", autoUploadManagementService);
    jobDataMap.put("circabcLockService", circabcLockService);

    when(jobContext.getJobDetail()).thenReturn(jobDetail);
    when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
    when(jobContext.getMergedJobDataMap()).thenReturn(mergedJobDataMap);
  }

  @Test
  public void testExecute_whenDisabled_thenReturnsImmediately()
    throws JobExecutionException, SQLException {
    mergedJobDataMap.put("enabled", "false");

    listener.execute(jobContext);

    verify(autoUploadManagementService, never()).listAllConfigurations();
  }

  @Test
  public void testExecute_whenEnabledAndNoConfigurations_thenDoesNothing()
    throws Exception {
    mergedJobDataMap.put("enabled", "true");
    when(autoUploadManagementService.listAllConfigurations()).thenReturn(
      Collections.emptyList()
    );

    listener.execute(jobContext);

    verify(autoUploadManagementService).listAllConfigurations();
  }

  @Test
  public void testExecute_whenEnabledAndConfigurationStatusNotActive_thenSkips()
    throws Exception {
    mergedJobDataMap.put("enabled", "true");

    Configuration conf = new Configuration();
    conf.setIdConfiguration(1L);
    conf.setStatus(0); // not active (active = 1)
    conf.setDateRestriction("0 0 * * * ?");

    when(autoUploadManagementService.listAllConfigurations()).thenReturn(
      Arrays.asList(conf)
    );
    when(autoUploadManagementService.lockJobFile(1L)).thenReturn(1);

    listener.execute(jobContext);

    verify(autoUploadManagementService, never()).documentExists(
      any(NodeRef.class)
    );
    verify(autoUploadManagementService).unlockJobFile(1L);
  }

  @Test
  public void testExecute_whenLockNotAcquired_thenSkipsProcessing()
    throws Exception {
    mergedJobDataMap.put("enabled", "true");

    Configuration conf = new Configuration();
    conf.setIdConfiguration(1L);
    conf.setStatus(1);
    conf.setDateRestriction("0 0 * * * ?");

    when(autoUploadManagementService.listAllConfigurations()).thenReturn(
      Arrays.asList(conf)
    );
    when(autoUploadManagementService.lockJobFile(1L)).thenReturn(0);

    listener.execute(jobContext);

    verify(autoUploadManagementService, never()).documentExists(
      any(NodeRef.class)
    );
    // lock was not acquired so unlockJobFile should NOT be called
    verify(autoUploadManagementService, never()).unlockJobFile(1L);
  }

  @Test
  public void testExecute_whenDocumentDoesNotExist_thenMarksConfigurationAsFailed()
    throws Exception {
    mergedJobDataMap.put("enabled", "true");

    Configuration conf = new Configuration();
    conf.setIdConfiguration(1L);
    conf.setStatus(1);
    conf.setFileNodeRef("workspace://SpacesStore/test-id");
    conf.setDateRestriction("* * * * * ?"); // always satisfied

    when(autoUploadManagementService.listAllConfigurations()).thenReturn(
      Arrays.asList(conf)
    );
    when(autoUploadManagementService.lockJobFile(1L)).thenReturn(1);
    when(
      autoUploadManagementService.documentExists(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id")
      )
    ).thenReturn(false);

    listener.execute(jobContext);

    verify(autoUploadManagementService).updateConfiguration(
      argThat(c -> c.getStatus() == -1)
    );
    verify(autoUploadManagementService).unlockJobFile(1L);
  }

  @Test
  public void testExecute_whenSQLExceptionOnListConfigurations_thenReturnsEmptyList()
    throws Exception {
    mergedJobDataMap.put("enabled", "true");

    when(autoUploadManagementService.listAllConfigurations()).thenThrow(
      new SQLException("DB error")
    );

    listener.execute(jobContext);

    verify(autoUploadManagementService, never()).lockJobFile(anyLong());
  }

  @Test
  public void testExecute_whenHostnameDoesNotMatch_thenSkips()
    throws Exception {
    mergedJobDataMap.put("enabled", "true");
    jobDataMap.put(
      "enabledOnHostname",
      "some-other-host-that-does-not-exist-xyz"
    );

    listener.execute(jobContext);

    verify(autoUploadManagementService, never()).listAllConfigurations();
  }

  @Test
  public void testExecute_whenInvalidCronExpression_thenSkipsConfiguration()
    throws Exception {
    mergedJobDataMap.put("enabled", "true");

    Configuration conf = new Configuration();
    conf.setIdConfiguration(1L);
    conf.setStatus(1);
    conf.setDateRestriction("INVALID_CRON");
    conf.setFileNodeRef("workspace://SpacesStore/test-id");

    when(autoUploadManagementService.listAllConfigurations()).thenReturn(
      Arrays.asList(conf)
    );
    when(autoUploadManagementService.lockJobFile(1L)).thenReturn(1);

    listener.execute(jobContext);

    verify(autoUploadManagementService, never()).documentExists(
      any(NodeRef.class)
    );
    verify(autoUploadManagementService).unlockJobFile(1L);
  }
}
