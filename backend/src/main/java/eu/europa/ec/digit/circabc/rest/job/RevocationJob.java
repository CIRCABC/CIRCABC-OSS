package eu.europa.ec.digit.circabc.rest.job;

import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import io.swagger.api.GroupsApi;
import io.swagger.api.HistoryApi;
import io.swagger.api.UsersApi;
import io.swagger.model.InterestGroupProfile;
import io.swagger.model.UserRevocationRequest;
import io.swagger.util.Converter;
import jakarta.transaction.*;
import java.util.*;
import java.util.Map.Entry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled Quartz job that processes pending user revocation requests.
 *
 * <p>On each execution the job runs as the Alfresco system user and iterates over all revocation
 * requests currently in a "waiting" state (see {@link HistoryApi#getWaitingRevocations()}). For
 * every eligible request whose revocation date has passed and whose action is {@code "revoke"}, the
 * job removes the targeted users from all Interest Groups they belong to, cleans up and logs the
 * associated permissions, and updates the request state accordingly.
 *
 * <p>Each request is guarded by a distributed lock (keyed by the request id) to prevent concurrent
 * processing, and individual member removals are performed within non-propagating transactions.
 *
 * <p>The collaborating services and the {@code enabled} flag are not injected via Spring but read
 * from the Quartz {@link JobExecutionContext} job data map at execution time (see
 * {@link #prepareServices(JobExecutionContext)}). When {@code enabled} is {@code false} the job is a
 * no-op.
 */
public class RevocationJob implements Job {

  /** Action value marking a revocation request that should trigger user removal. */
  private static final String REVOKE = "revoke";
  /** Prefix used to build the lock key that guards processing of an individual revocation request. */
  private static final String REVOCATION_JOB = "revocationJob-";

  private static final Log logger = LogFactory.getLog(RevocationJob.class);

  /** API used to read waiting revocations and to persist job state and permission cleanup logs. */
  private HistoryApi historyApi;
  /** API used to remove members from Interest Groups. */
  private GroupsApi groupsApi;
  /** API used to look up a user's Interest Group memberships. */
  private UsersApi usersApi;
  /** Alfresco service used to check whether a given user still exists. */
  private PersonService personService;
  /** Alfresco service used to obtain non-propagating transactions for each member removal. */
  private TransactionService transactionService;
  /** Distributed lock service preventing concurrent processing of the same revocation request. */
  private LockService lockService;
  /** Whether the job is active; when {@code false} the job performs no work. */
  private boolean enabled;

  /**
   * Entry point invoked by the Quartz scheduler.
   *
   * <p>Loads the collaborating services from the job data map, and, if the job is enabled, runs as
   * the Alfresco system user to process every waiting revocation request. The security context is
   * always cleared afterwards.
   *
   * @param context the Quartz execution context providing the job data map with the required
   *     services and the {@code enabled} flag
   * @throws JobExecutionException if the job cannot be executed
   */
  @Override
  public void execute(JobExecutionContext context)
    throws JobExecutionException {
    prepareServices(context);
    if (!enabled) return;

    AuthenticationUtil.setRunAsUserSystem();
    try {
      for (UserRevocationRequest job : historyApi.getWaitingRevocations()) {
        processJob(job);
      }
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }

  private void processJob(UserRevocationRequest job) {
    if (!shouldProcessJob(job)) return;

    lockService.lock(REVOCATION_JOB + job.getId());
    Integer state = 0;
    Date jobStartedOn = new Date();

    try {
      Map<String, List<String>> groupsToClean = listGroupsFromMemberships(
        job.getUserIds()
      );
      for (Entry<
        String,
        List<String>
      > groupToCheck : groupsToClean.entrySet()) {
        uninviteGroupMembers(groupToCheck);
        processCleaningGroupMembers(groupToCheck);
        state = 1;
      }
      state = 2;
    } catch (Exception e) {
      logger.error(
        "Exception when processing revocation job " + job.getId(),
        e
      );
      state = -1;
    } finally {
      historyApi.updateRevocationJobState(
        job.getId(),
        jobStartedOn,
        new Date(),
        state
      );
      lockService.unlock(REVOCATION_JOB + job.getId());
    }
  }

  private boolean shouldProcessJob(UserRevocationRequest job) {
    return (
      job.getRevocationDate().isBeforeNow() &&
      !lockService.isLocked(REVOCATION_JOB + job.getId()) &&
      REVOKE.equals(job.getAction())
    );
  }

  private void prepareServices(JobExecutionContext context) {
    enabled = Boolean.valueOf(
      (String) context.getMergedJobDataMap().get("enabled")
    );
    historyApi = (HistoryApi) context.getMergedJobDataMap().get("historyApi");
    usersApi = (UsersApi) context.getMergedJobDataMap().get("usersApi");
    groupsApi = (GroupsApi) context.getMergedJobDataMap().get("groupsApi");
    personService = (PersonService) context
      .getMergedJobDataMap()
      .get("personService");
    transactionService = (TransactionService) context
      .getMergedJobDataMap()
      .get("transactionService");
    lockService = (LockService) context
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

  private void uninviteGroupMembers(Entry<String, List<String>> groupToCheck)
    throws SystemException, NotSupportedException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
    for (String userId : groupToCheck.getValue()) {
      if (userId != null) {
        UserTransaction trx =
          transactionService.getNonPropagatingUserTransaction(false);
        trx.begin();
        groupsApi.groupsIdMembersUserIdDelete(groupToCheck.getKey(), userId);
        trx.commit();
      }
    }
  }

  private Map<String, List<String>> listGroupsFromMemberships(
    List<String> userIds
  ) {
    Map<String, List<String>> groups = new HashMap<>();
    for (String userId : userIds) {
      if (personService.personExists(userId)) {
        addUserMemberships(userId, groups);
      }
    }
    return groups;
  }

  private void addUserMemberships(
    String userId,
    Map<String, List<String>> groups
  ) {
    for (InterestGroupProfile profile : usersApi.getUserMembership(userId)) {
      String groupId = profile.getInterestGroup().getId();
      groups.computeIfAbsent(groupId, k -> new ArrayList<>()).add(userId);
    }
  }
}
