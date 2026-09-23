/**
 *
 */
package eu.europa.ec.digit.circabc.rest.job;

import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import io.swagger.api.HistoryApi;
import io.swagger.model.UserRevocationRequest;
import io.swagger.util.Converter;
import java.util.*;
import java.util.Map.Entry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz scheduled job that processes pending group-permission clean-up (user
 * revocation) requests.
 *
 * <p>On each execution the job runs as the Alfresco system user and asks the
 * {@link HistoryApi} for all {@link UserRevocationRequest}s that are waiting to
 * have their permissions cleaned. For every request whose revocation date has
 * passed, that is not already locked, whose action is {@code clean-permission}
 * and that references a group, the pending users are removed from the target
 * group and the operation is logged. A per-request lock (via
 * {@link LockService}) prevents concurrent processing of the same request, and
 * the final outcome (state, start and finish timestamps) is persisted back
 * through the {@link HistoryApi}.
 *
 * <p>The job is a no-op when it is disabled through its job data map.
 *
 * @author beaurpi
 */
public class CleanGroupPermissionJob implements Job {

  /** Action discriminator identifying revocation requests this job handles. */
  private static final String CLEAN_PERMISSION = "clean-permission";
  /** Prefix used to build the per-request lock key. */
  private static final String CLEAN_PERMISSION_JOB = "cleanPermissionJob-";

  private static final Log logger = LogFactory.getLog(
    CleanGroupPermissionJob.class
  );

  /** API used to read pending requests and clean/log group permissions. */
  private HistoryApi historyApi;
  /** Distributed lock service guarding concurrent processing of a request. */
  private LockService lockService;
  /** Flag controlling whether the job performs any work when triggered. */
  private Boolean enabled;

  /**
   * Entry point invoked by the Quartz scheduler.
   *
   * <p>Resolves the collaborating services from the job data map, and, when the
   * job is enabled, iterates over all waiting clean-permission requests and
   * processes each one while running as the Alfresco system user.
   *
   * @param context the Quartz execution context carrying the job data map with
   *     the {@code enabled} flag, {@code historyApi} and {@code lockService}
   *     entries
   * @throws JobExecutionException if the job fails in a way that Quartz should
   *     be notified of
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context)
    throws JobExecutionException {
    prepareServices(context);
    if (Boolean.FALSE.equals(enabled)) {
      return;
    }
    AuthenticationUtil.setRunAsUserSystem();
    List<UserRevocationRequest> jobs = historyApi.getWaitingCleanPermission();

    for (UserRevocationRequest job : jobs) {
      processJob(job);
    }

    AuthenticationUtil.clearCurrentSecurityContext();
  }

  private void processJob(UserRevocationRequest job) {
    if (
      job.getRevocationDate().isBeforeNow() &&
      !lockService.isLocked(CLEAN_PERMISSION_JOB + job.getId()) &&
      CLEAN_PERMISSION.equals(job.getAction()) &&
      job.getGroupId() != null
    ) {
      lockService.lock(CLEAN_PERMISSION_JOB + job.getId());

      Integer state = 0; // -1 = error / 0 = waiting / 1 = not fully done / 2 fully finished
      Date jobStartedOn = new Date();
      Map<String, List<String>> groupsToClean = new HashMap<>();
      groupsToClean.put(job.getGroupId(), job.getUserIds());

      try {
        for (Entry<
          String,
          List<String>
        > groupToCheck : groupsToClean.entrySet()) {
          processCleaningGroupMembers(groupToCheck);
          state = 1;
        }
        state = 2;
      } catch (SecurityException e) {
        logger.error(
          "Security Exception when processing clean permission job" +
            job.getId(),
          e
        );
        state = -1;
      } catch (IllegalStateException e) {
        logger.error(
          "IllegalStateException when processing clean permission job" +
            job.getId(),
          e
        );
        state = -1;
      } catch (Exception e) {
        state = -1;
        logger.error("exception while clean permission job" + job.getId(), e);
      } finally {
        Date jobFinishedOn = new Date();
        historyApi.updateRevocationJobState(
          job.getId(),
          jobStartedOn,
          jobFinishedOn,
          state
        );
        lockService.unlock(CLEAN_PERMISSION_JOB + job.getId());
      }
    }
  }

  private void prepareServices(JobExecutionContext context) {
    enabled = Boolean.valueOf(
      (String) context.getMergedJobDataMap().get("enabled")
    );
    this.historyApi = (HistoryApi) context
      .getMergedJobDataMap()
      .get("historyApi");
    this.lockService = (LockService) context
      .getMergedJobDataMap()
      .get("lockService");
  }

  private void processCleaningGroupMembers(
    Entry<String, List<String>> groupToCheck
  ) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(groupToCheck.getKey());
    historyApi.cleanAndLogPermissions(
      groupNodeRef,
      new HashSet<>(groupToCheck.getValue()),
      true
    );
  }
}
