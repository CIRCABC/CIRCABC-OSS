/**
 *
 */
package eu.cec.digit.circabc.service.revocation.job;

import eu.cec.digit.circabc.service.lock.LockService;
import io.swagger.api.GroupsApi;
import io.swagger.api.HistoryApi;
import io.swagger.api.UsersApi;
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

import java.util.*;
import java.util.Map.Entry;

/** @author beaurpi */
public class CleanGroupPermissionJob implements Job {

    private static final String CLEAN_PERMISSION = "clean-permission";
    private static final String CLEAN_PERMISSION_JOB = "cleanPermissionJob-";
    private static final Log logger = LogFactory.getLog(CleanGroupPermissionJob.class);
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
        List<UserRevocationRequest> jobs = historyApi.getWaitingCleanPermission();

        for (UserRevocationRequest job : jobs) {
            processJob(job);
        }

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    private void processJob(UserRevocationRequest job) {

        if (job.getRevocationDate().isBeforeNow()
                && !lockService.isLocked(CLEAN_PERMISSION_JOB + job.getId())
                && CLEAN_PERMISSION.equals(job.getAction())
                && job.getGroupId() != null) {
            lockService.lock(CLEAN_PERMISSION_JOB + job.getId());

            Integer state = 0; // -1 = error / 0 = waiting / 1 = not fully done / 2 fully finished
            Integer nbGroupChecked = 0;
            Date jobStartedOn = new Date();
            Map<String, List<String>> groupsToClean = new HashMap<>();
            groupsToClean.put(job.getGroupId(), job.getUserIds());

            try {
                for (Entry<String, List<String>> groupToCheck : groupsToClean.entrySet()) {
                    processCleaningGroupMembers(groupToCheck);
                    nbGroupChecked++;
                    state = 1;
                }
                state = 2;
            } catch (SecurityException e) {
                logger.error("Security Exception when processing clean permission job" + job.getId(), e);
                state = -1;
            } catch (IllegalStateException e) {
                logger.error("IllegalStateException when processing clean permission job" + job.getId(), e);
                state = -1;
            } catch (Exception e) {
                state = -1;
                logger.error("exception while clean permission job" + job.getId(), e);
            } finally {
                Date jobFinishedOn = new Date();
                historyApi.updateRevocationJobState(job.getId(), jobStartedOn, jobFinishedOn, state);
                lockService.unlock(CLEAN_PERMISSION_JOB + job.getId());
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
