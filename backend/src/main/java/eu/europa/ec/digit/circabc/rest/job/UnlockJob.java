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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled Quartz job that releases stale locks held on repository nodes.
 *
 * <p>When triggered, the job reads its configuration from the Quartz {@link JobDataMap}, runs under
 * the Alfresco system user, and delegates to {@link LockService#unlockAll(int)} to unlock any node
 * whose lock has been held longer than the configured maximum duration. The job is a no-op unless
 * its {@code enabled} data map entry is set to {@code true}.
 *
 * <p>Expected job data map entries:
 *
 * <ul>
 *   <li>{@code enabled} - {@code String} ("true"/"false") toggling execution of the job.
 *   <li>{@code circabcServiceRegistry} - the {@link CircabcServiceRegistry} used to resolve the
 *       {@link LockService}.
 *   <li>{@code maxJobLockTimeInHours} - {@code String} holding the maximum lock age, in hours,
 *       beyond which locks are released.
 * </ul>
 */
public class UnlockJob implements Job {

  /** Logger for this job. */
  private static Log logger = LogFactory.getLog(UnlockJob.class);

  /** Service used to release node locks; resolved from the service registry at execution time. */
  private LockService lockService;

  /** Maximum age, in hours, a lock may be held before it is eligible to be released. */
  private int maxJobLockTimeInHours;

  /**
   * Entry point invoked by the Quartz scheduler.
   *
   * <p>Skips execution when the {@code enabled} data map entry is not {@code true}. Otherwise it
   * runs as the Alfresco system user, initializes its dependencies from the job context, and
   * performs the unlock operation. The security context is always cleared afterwards. Any exception
   * raised while running the job is caught and logged rather than propagated.
   *
   * @param context the Quartz execution context carrying the merged job data map
   * @throws JobExecutionException if the job cannot be executed (declared by the {@link Job}
   *     contract; not thrown directly by this implementation)
   */
  @Override
  public void execute(final JobExecutionContext context)
    throws JobExecutionException {
    boolean enabled;

    enabled = Boolean.parseBoolean(
      (String) context.getMergedJobDataMap().get("enabled")
    );
    if (!enabled) {
      return;
    }
    AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
    try {
      initialize(context);
      runUnlockJob();
    } catch (final Exception e) {
      logger.error("Can not run job UnockJob", e);
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }

  private void runUnlockJob() {
    try {
      lockService.unlockAll(maxJobLockTimeInHours);
    } catch (final Exception e) {
      logger.error("Exception when running the unlock job.", e);
    }
  }

  private void initialize(final JobExecutionContext context) {
    final JobDataMap jobData = context.getJobDetail().getJobDataMap();

    final Object circabcServiceRegistryObj = jobData.get(
      "circabcServiceRegistry"
    );
    CircabcServiceRegistry circabcServiceRegistry =
      (CircabcServiceRegistry) circabcServiceRegistryObj;
    lockService = circabcServiceRegistry.getLockService();
    maxJobLockTimeInHours = Integer.valueOf(
      (String) jobData.get("maxJobLockTimeInHours")
    );
  }
}
