/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.job;

import eu.europa.ec.digit.circabc.rest.service.CircabcServiceRegistry;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled Quartz job responsible for processing the CIRCABC REST access/audit logs.
 *
 * <p>When triggered, the job first checks the {@code enabled} flag from its merged job data map and
 * exits immediately if it is not set to {@code true}. Otherwise it runs under the Alfresco system
 * user, resolves its collaborators from the {@link CircabcServiceRegistry} and, provided the shared
 * {@code LOG_JOB} lock is not already held, delegates the actual log processing to
 * {@link LogService#processRestLog()}. A distributed {@link LockService} lock guarantees that only a
 * single instance processes the logs at a time, which is important in a clustered deployment.
 *
 * <p>The job is fault tolerant: any exception raised during execution is logged and swallowed so
 * that a failure does not prevent future scheduled runs, and the security context is always cleared
 * afterwards.
 */
public class LogJob implements Job {

  /** Name of the lock guarding concurrent executions of this job across the cluster. */
  private static final String LOG_JOB = "LOG_JOB";

  /** Logger used to report execution problems without interrupting the scheduler. */
  private static Log logger = LogFactory.getLog(
    eu.europa.ec.digit.circabc.rest.job.LogJob.class
  );

  /** Service providing the distributed lock used to serialise job executions. */
  private LockService lockService;

  /** Service that performs the actual processing of the REST logs. */
  private LogService logService;

  /**
   * Entry point invoked by the Quartz scheduler for each trigger.
   *
   * <p>Skips execution when the {@code enabled} job data flag is not {@code true}. When enabled, it
   * runs as the Alfresco system user, initialises its collaborators from the job data map and, if
   * the {@code LOG_JOB} lock is free, processes the REST logs. All exceptions are caught and logged,
   * and the security context is cleared in a {@code finally} block.
   *
   * @param context the Quartz execution context carrying the merged job data map (including the
   *     {@code enabled} flag and the {@code circabcServiceRegistry})
   * @throws JobExecutionException if the job cannot be executed (declared by the {@link Job}
   *     contract; not thrown directly by this implementation as errors are handled internally)
   */
  @Override
  public void execute(JobExecutionContext context)
    throws JobExecutionException {
    boolean enabled;
    try {
      enabled = Boolean.valueOf(
        (String) context.getMergedJobDataMap().get("enabled")
      );
      if (!enabled) {
        return;
      }
      AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
      initialize(context);
      boolean isLocked = false;
      if (!lockService.isLocked(LOG_JOB)) {
        processLogs(isLocked);
      }
    } catch (final Exception e) {
      logger.error("Can not run job LogJob", e);
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }

  /**
   * Acquires the {@code LOG_JOB} lock, processes the REST logs and releases the lock.
   *
   * <p>The lock is only released if it was successfully acquired. Any exception during processing is
   * logged and does not propagate.
   *
   * @param isLocked initial lock state; set to {@code true} once the lock has been acquired so that
   *     the {@code finally} block only releases a lock actually held
   */
  private void processLogs(boolean isLocked) {
    try {
      lockService.lock(LOG_JOB);
      isLocked = true;
      logService.processRestLog();
    } catch (Exception e) {
      logger.error("Exception when running the log job.", e);
    } finally {
      if (isLocked) {
        lockService.unlock(LOG_JOB);
      }
    }
  }

  /**
   * Resolves the job's collaborators from the {@link CircabcServiceRegistry} supplied in the job
   * data map, wiring up the {@link #lockService} and {@link #logService} fields.
   *
   * @param context the Quartz execution context whose job data map contains the
   *     {@code circabcServiceRegistry} entry
   */
  private void initialize(final JobExecutionContext context) {
    final JobDataMap jobData = context.getJobDetail().getJobDataMap();
    final Object circabcServiceRegistryObj = jobData.get(
      "circabcServiceRegistry"
    );

    CircabcServiceRegistry circabcServiceRegistry =
      (CircabcServiceRegistry) circabcServiceRegistryObj;
    lockService = circabcServiceRegistry.getLockService();
    logService = circabcServiceRegistry.getLogService();
  }
}
