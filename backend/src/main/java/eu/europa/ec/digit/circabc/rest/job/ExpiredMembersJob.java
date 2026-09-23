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

import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import io.swagger.api.GroupsApi;
import io.swagger.api.HistoryApi;
import io.swagger.model.db.MemberExpirationDAO;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz scheduled job that removes users whose membership in an Interest Group has expired.
 *
 * <p>On each execution the job queries the history layer for members whose membership expiration
 * date has passed and uninvites (removes) each of them from their respective group. The job runs
 * under the Alfresco system user and is guarded by a distributed {@link LockService} lock so that,
 * in a clustered deployment, only a single node processes the expired members at any given time.
 *
 * <p>The job is configured declaratively via Quartz and its collaborators are injected through the
 * job data map (see {@link #initialize(JobExecutionContext)}). If the {@code enabled} flag is not
 * set to {@code true}, the job returns immediately without doing any work.
 */
public class ExpiredMembersJob implements Job {

  /** Identifier used as the key for acquiring/releasing the distributed lock for this job. */
  private static final String EXPIRED_MEMBERS_JOB = "EXPIRED_MEMBERS_JOB";

  private static Log logger = LogFactory.getLog(ExpiredMembersJob.class);

  /** Distributed lock service ensuring only one node processes expired members at a time. */
  private LockService lockService;

  /** API used to look up the members whose membership has expired. */
  private HistoryApi historyApi;

  /** API used to remove (uninvite) expired members from their groups. */
  private GroupsApi groupsApi;

  /** Whether the job is enabled; when {@code false} the job performs no work. */
  private boolean enabled;

  /**
   * Entry point invoked by the Quartz scheduler.
   *
   * <p>Initializes the job's collaborators from the execution context, and, if the job is enabled,
   * processes the expired members while running as the Alfresco system user. Any exception raised
   * during processing is logged and swallowed so that the job does not fail the scheduler; the
   * security context is always cleared afterwards.
   *
   * @param context the Quartz execution context carrying the merged job data map with the injected
   *     collaborators and the {@code enabled} flag
   * @throws JobExecutionException if the job execution fails in a way that must be reported to the
   *     Quartz scheduler
   */
  @Override
  public void execute(JobExecutionContext context)
    throws JobExecutionException {
    initialize(context);
    if (!enabled) {
      return;
    }
    try {
      AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
      processExpiredMembers();
    } catch (final Exception e) {
      logger.error("Can not run job ExpiredMembersJob", e);
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }

  /**
   * Acquires the distributed lock and processes the expired members.
   *
   * <p>If the lock is already held (for example by another cluster node), the method returns
   * without doing anything. The lock is always released when processing completes, and any
   * exception raised while processing is logged rather than propagated.
   */
  private void processExpiredMembers() {
    if (lockService.isLocked(EXPIRED_MEMBERS_JOB)) {
      return;
    }

    boolean isLocked = false;
    try {
      lockService.lock(EXPIRED_MEMBERS_JOB);
      isLocked = true;
      removeExpiredUsers();
    } catch (Exception e) {
      logger.warn("Exception when running the expired members job.", e);
    } finally {
      if (isLocked) {
        lockService.unlock(EXPIRED_MEMBERS_JOB);
      }
    }
  }

  /**
   * Retrieves all members whose membership has expired and removes each one from its group.
   */
  private void removeExpiredUsers() {
    List<MemberExpirationDAO> expiredUsers = historyApi.getExpiredUsers();
    for (MemberExpirationDAO expiredUser : expiredUsers) {
      removeExpiredUser(expiredUser);
    }
  }

  /**
   * Removes (uninvites) a single expired member from the group in which their membership expired.
   *
   * <p>Any exception raised while removing the member is logged and swallowed so that processing of
   * the remaining expired members can continue.
   *
   * @param expiredUser the expired membership record identifying the group and user to remove
   */
  private void removeExpiredUser(MemberExpirationDAO expiredUser) {
    try {
      groupsApi.groupsIdMembersUserIdDelete(
        expiredUser.getGroupId(),
        expiredUser.getUserId()
      );
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "error when uninviting user " +
            expiredUser.getUserId() +
            " from ig" +
            expiredUser.getGroupId(),
          e
        );
      }
    }
  }

  /**
   * Initializes the job's collaborators and configuration from the Quartz job data map.
   *
   * @param context the Quartz execution context whose merged job data map supplies the
   *     {@code historyApi}, {@code groupsApi} and {@code lockService} collaborators as well as the
   *     {@code enabled} flag
   */
  private void initialize(final JobExecutionContext context) {
    this.historyApi = (HistoryApi) context
      .getMergedJobDataMap()
      .get("historyApi");
    this.groupsApi = (GroupsApi) context.getMergedJobDataMap().get("groupsApi");
    this.lockService = (LockService) context
      .getMergedJobDataMap()
      .get("lockService");
    this.enabled = Boolean.valueOf(
      (String) context.getMergedJobDataMap().get("enabled")
    );
  }
}
