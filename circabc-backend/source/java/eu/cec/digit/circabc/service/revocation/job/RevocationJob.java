/**
 *
 */
package eu.cec.digit.circabc.service.revocation.job;

import eu.cec.digit.circabc.service.lock.LockService;
import io.swagger.api.GroupsApi;
import io.swagger.api.HistoryApi;
import io.swagger.api.UsersApi;
import io.swagger.model.InterestGroupProfile;
import io.swagger.model.UserRevocationRequest;
import io.swagger.util.Converter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.transaction.*;
import java.util.*;
import java.util.Map.Entry;

/** @author beaurpi */
public class RevocationJob implements Job {

    private static final String REVOKE = "revoke";
    private static final String REVOCATION_JOB = "revocationJob-";
    private static final Log logger = LogFactory.getLog(RevocationJob.class);
    private HistoryApi historyApi;
    private GroupsApi groupsApi;
    private UsersApi usersApi;
    private NodeService nodeService;
    private PersonService personService;
    private TransactionService transactionService;
    private LockService lockService;

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        AuthenticationUtil.setRunAsUserSystem();
        prepareServices(arg0);
        List<UserRevocationRequest> jobs = historyApi.getWaitingRevocations();

        for (UserRevocationRequest job : jobs) {
            processJob(job);
        }

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    private void processJob(UserRevocationRequest job) {

        if (job.getRevocationDate().isBeforeNow()
                && !lockService.isLocked(REVOCATION_JOB + job.getId())
                && REVOKE.equals(job.getAction())) {
            lockService.lock(REVOCATION_JOB + job.getId());

            Integer state = 0; // -1 = error / 0 = waiting / 1 = not fully done / 2 fully finished
            Integer nbGroupChecked = 0;
            Date jobStartedOn = new Date();
            Map<String, List<String>> groupsToClean = new HashMap<>();
            groupsToClean = listGroupsFromMemberships(job.getUserIds());

            try {
                for (Entry<String, List<String>> groupToCheck : groupsToClean.entrySet()) {
                    uninviteGroupMembers(groupToCheck);
                    processCleaningGroupMembers(groupToCheck);
                    nbGroupChecked++;
                    state = 1;
                }
                state = 2;
            } catch (SecurityException e) {
                logger.error("Security Exception when processing revocation job" + job.getId(), e);
                state = -1;
            } catch (IllegalStateException e) {
                logger.error("IllegalStateException when processing revocation job" + job.getId(), e);
                state = -1;
            } catch (NotSupportedException e) {
                logger.error("NotSupportedException when processing revocation job" + job.getId(), e);
                state = -1;
            } catch (SystemException e) {
                logger.error("SystemException when processing revocation job" + job.getId(), e);
                state = -1;
            } catch (RollbackException e) {
                logger.error("RollbackException when processing revocation job" + job.getId(), e);
                state = -1;
            } catch (HeuristicMixedException e) {
                logger.error("HeuristicMixedException when processing revocation job" + job.getId(), e);
                state = -1;
            } catch (HeuristicRollbackException e) {
                logger.error("HeuristicRollbackException when processing revocation job" + job.getId(), e);
                state = -1;
            } catch (Exception e) {
                state = -1;
                logger.error("exception while revoking job" + job.getId(), e);
            } finally {
                Date jobFinishedOn = new Date();
                historyApi.updateRevocationJobState(job.getId(), jobStartedOn, jobFinishedOn, state);
                lockService.unlock(REVOCATION_JOB + job.getId());
            }
        }
    }

    private void prepareServices(JobExecutionContext arg0) {
        setHistoryApi((HistoryApi) arg0.getMergedJobDataMap().get("historyApi"));
        setUsersApi((UsersApi) arg0.getMergedJobDataMap().get("usersApi"));
        setGroupsApi((GroupsApi) arg0.getMergedJobDataMap().get("groupsApi"));
        setPersonService((PersonService) arg0.getMergedJobDataMap().get("personService"));
        setNodeService((NodeService) arg0.getMergedJobDataMap().get("nodeService"));
        setTransactionService(
                (TransactionService) arg0.getMergedJobDataMap().get("transactionService"));
        setLockService((LockService) arg0.getMergedJobDataMap().get("lockService"));
    }

    private void processCleaningGroupMembers(Entry<String, List<String>> groupToCheck) {
        NodeRef groupNodeRef = Converter.createNodeRefFromId(groupToCheck.getKey());
        historyApi.cleanAndLogPermissions(
                groupNodeRef, new HashSet<String>(groupToCheck.getValue()), true);
    }

    private void uninviteGroupMembers(Entry<String, List<String>> groupToCheck)
            throws NotSupportedException, SystemException, SecurityException, IllegalStateException,
            RollbackException, HeuristicMixedException, HeuristicRollbackException {
        for (String userId : groupToCheck.getValue()) {
            if (userId != null) {
                UserTransaction trx = transactionService.getNonPropagatingUserTransaction(false);
                trx.begin();
                groupsApi.groupsIdMembersUserIdDelete(groupToCheck.getKey(), userId);
                trx.commit();
            }
        }
    }

    private Map<String, List<String>> listGroupsFromMemberships(List<String> userIds) {
        Map<String, List<String>> groups = new HashMap<>();

        for (String userId : userIds) {
            if (personService.personExists(userId)) {
                List<InterestGroupProfile> profileMemberships = usersApi.getUserMembership(userId);
                for (InterestGroupProfile profile : profileMemberships) {
                    if (groups.containsKey(profile.getInterestGroup().getId())) {
                        List<String> users = groups.get(profile.getInterestGroup().getId());
                        users.add(userId);
                        groups.put(profile.getInterestGroup().getId(), users);
                    } else {
                        List<String> users = new ArrayList<String>();
                        users.add(userId);
                        groups.put(profile.getInterestGroup().getId(), users);
                    }
                }
            }
        }

        return groups;
    }

    /** @return the historyApi */
    public HistoryApi getHistoryApi() {
        return historyApi;
    }

    /** @param historyApi the historyApi to set */
    public void setHistoryApi(HistoryApi historyApi) {
        this.historyApi = historyApi;
    }

    /** @return the usersApi */
    public UsersApi getUsersApi() {
        return usersApi;
    }

    /** @param usersApi the usersApi to set */
    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    /** @return the groupsApi */
    public GroupsApi getGroupsApi() {
        return groupsApi;
    }

    /** @param groupsApi the groupsApi to set */
    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    /** @return the nodeService */
    public NodeService getNodeService() {
        return nodeService;
    }

    /** @param nodeService the nodeService to set */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /** @return the personService */
    public PersonService getPersonService() {
        return personService;
    }

    /** @param personService the personService to set */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /** @return the transactionService */
    public TransactionService getTransactionService() {
        return transactionService;
    }

    /** @param transactionService the transactionService to set */
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /** @return the lockService */
    public LockService getLockService() {
        return lockService;
    }

    /** @param lockService the lockService to set */
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }
}
