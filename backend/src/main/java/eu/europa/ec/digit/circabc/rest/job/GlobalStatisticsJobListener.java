/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.europa.ec.digit.circabc.rest.job;

import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.statistic.global.GlobalStatisticsService;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz {@link Job} that generates the global (platform-wide) usage statistics report for CIRCABC.
 *
 * <p>When triggered by the scheduler, the job checks whether it is enabled and whether it is allowed
 * to run on the current host, then acquires a cluster-wide lock (via {@link LockService}) to ensure a
 * single execution across the cluster. While holding the lock it runs as the system user and delegates
 * to {@link GlobalStatisticsService} to prepare the report destination folder, optionally archive the
 * previous week's report files (on Mondays), compute the global statistics and export them to an Excel
 * file. The lock is always released when the execution finishes.
 *
 * <p>Job configuration is provided through the Quartz {@link JobDataMap}, which supplies the
 * {@code enabled} flag, the optional {@code enabledOnHostname} restriction, and the collaborating
 * {@code circabcLockService} and {@code globalStatisticsService} beans.
 *
 * @author beaurpi
 */
public class GlobalStatisticsJobListener implements Job {

  /** Logger for this job. */
  static final Log logger = LogFactory.getLog(
    GlobalStatisticsJobListener.class
  );

  /** Name of the cluster-wide lock used to guarantee a single concurrent execution of the job. */
  private static final String GLOBAL_STATISTICS_LOCK =
    "globalStatisticsRunningLock";

  /** Service that computes and exports the global statistics report. */
  private GlobalStatisticsService globalStatisticsService;

  /** Service used to acquire and release the cluster-wide execution lock. */
  private LockService circabcLockService;

  /**
   * Returns the name of the lock used to serialize executions of this job.
   *
   * @return the global statistics lock name
   */
  public static String getGlobalStatisticsLock() {
    return GLOBAL_STATISTICS_LOCK;
  }

  /**
   * Executes the global statistics job.
   *
   * <p>The job returns immediately if it is disabled (via the {@code enabled} job data entry) or if it
   * is restricted to a different host (via the {@code enabledOnHostname} job data entry). Otherwise it
   * resolves its collaborating services from the job data map, attempts to acquire the global statistics
   * lock and, if successful, runs as the system user to prepare the report folder, archive the previous
   * week's report files on Mondays, and generate and export the global statistics to Excel. The security
   * context is cleared and the lock released in a {@code finally} block.
   *
   * @param context the Quartz execution context providing the job detail and merged job data map
   * @throws JobExecutionException if the job execution fails
   */
  public void execute(JobExecutionContext context)
    throws JobExecutionException {
    JobDataMap jobData = context.getJobDetail().getJobDataMap();
    boolean enabled = Boolean.parseBoolean(
      (String) context.getMergedJobDataMap().get("enabled")
    );
    if (!enabled) {
      return;
    }

    String enabledOnHostname = (String) jobData.get("enabledOnHostname");
    if (!shouldRunOnCurrentHost(enabledOnHostname)) {
      return;
    }

    circabcLockService = (LockService) jobData.get("circabcLockService");
    globalStatisticsService = (GlobalStatisticsService) jobData.get(
      "globalStatisticsService"
    );

    Integer lockResult = lockJobFile();

    if (lockResult == 1) {
      logger.debug("Running GlobalStatisticsJobListener");
      long start = System.currentTimeMillis();

      try {
        AuthenticationUtil.setRunAsUserSystem();

        if (
          Boolean.FALSE.equals(
            globalStatisticsService.isReportSaveFolderExisting()
          )
        ) {
          globalStatisticsService.prepareFolderRecipient();
        }

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());

        if (gc.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
          globalStatisticsService.cleanAndZipPreviousReportFiles();
        }

        globalStatisticsService.saveStatsToExcel(
          globalStatisticsService.getReportSaveFolder(),
          globalStatisticsService.makeGlobalStats()
        );

        long end = System.currentTimeMillis();
        logger.debug(
          ("End of GlobalStatisticsJobListenerJob. Processing took " +
            (end - start) +
            " ms")
        );
      } finally {
        AuthenticationUtil.clearCurrentSecurityContext();
        unlockJobFile();
      }
    }
  }

  private boolean shouldRunOnCurrentHost(String enabledOnHostname) {
    if (enabledOnHostname == null || enabledOnHostname.isEmpty()) {
      return true;
    }
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      return enabledOnHostname.equals(hostName);
    } catch (final UnknownHostException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Unable to determine hostname", e);
      }
      return true;
    }
  }

  /**
   * Returns the service used to compute and export the global statistics.
   *
   * @return the global statistics service
   */
  public GlobalStatisticsService getGlobalStatisticsService() {
    return globalStatisticsService;
  }

  /**
   * Sets the service used to compute and export the global statistics.
   *
   * @param globalStatisticsService the global statistics service to set
   */
  public void setGlobalStatisticsService(
    GlobalStatisticsService globalStatisticsService
  ) {
    this.globalStatisticsService = globalStatisticsService;
  }

  /**
   * Returns the service used to acquire and release the execution lock.
   *
   * @return the CIRCABC lock service
   */
  public LockService getCircabcLockService() {
    return circabcLockService;
  }

  /**
   * Sets the service used to acquire and release the execution lock.
   *
   * @param circabcLockService the CIRCABC lock service to set
   */
  public void setCircabcLockService(LockService circabcLockService) {
    this.circabcLockService = circabcLockService;
  }

  /**
   * Attempts to acquire the global statistics lock so that only one execution runs at a time.
   *
   * <p>If the lock is not already held, it tries to acquire it. Any {@link IllegalStateException} raised
   * while acquiring the lock is caught and logged.
   *
   * @return {@code 1} if the lock was successfully acquired, {@code 0} otherwise
   */
  public Integer lockJobFile() {
    Integer result = 0;

    if (!circabcLockService.isLocked(GLOBAL_STATISTICS_LOCK)) {
      try {
        circabcLockService.lock(GLOBAL_STATISTICS_LOCK);
        result = 1;
      } catch (IllegalStateException e) {
        logger.warn(
          "Exception when trying to get a lock for the execution of Global Statistics job.",
          e
        );
      }
    }
    return result;
  }

  /**
   * Releases the global statistics lock if it is currently held.
   *
   * @return {@code 1} if the lock was held and released, {@code 0} otherwise
   */
  public Integer unlockJobFile() {
    Integer result = 0;

    if (circabcLockService.isLocked(GLOBAL_STATISTICS_LOCK)) {
      circabcLockService.unlock(GLOBAL_STATISTICS_LOCK);
      result = 1;
    }

    return result;
  }
}
