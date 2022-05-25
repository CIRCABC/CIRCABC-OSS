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
package eu.cec.digit.circabc.service.member;

import eu.cec.digit.circabc.service.history.MemberExpirationDAO;
import eu.cec.digit.circabc.service.lock.LockService;
import io.swagger.api.GroupsApi;
import io.swagger.api.HistoryApi;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

public class ExpiredMembersJob implements Job {

    private static final String EXPIRED_MEMBERS_JOB = "EXPIRED_MEMBERS_JOB";

    private static Log logger =
            LogFactory.getLog(eu.cec.digit.circabc.service.member.ExpiredMembersJob.class);

    private LockService lockService;
    private HistoryApi historyApi;
    private GroupsApi groupsApi;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        initialize(context);
        try {
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
            initialize(context);
            boolean isLocked = false;
            if (!lockService.isLocked(EXPIRED_MEMBERS_JOB)) {
                try {
                    lockService.lock(EXPIRED_MEMBERS_JOB);
                    isLocked = true;
                    List<MemberExpirationDAO> expiredUsers = historyApi.getExpiredUsers();
                    for (MemberExpirationDAO expiredUser : expiredUsers) {
                        try {
                            groupsApi.groupsIdMembersUserIdDelete(
                                    expiredUser.getGroupId(), expiredUser.getUserId());
                        } catch (Exception e) {
                            if (logger.isErrorEnabled()) {
                                logger.error(
                                        "error when uninviting user "
                                                + expiredUser.getUserId()
                                                + " from ig"
                                                + expiredUser.getGroupId(),
                                        e);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Exception when running the expired members job.", e);
                } finally {
                    if (isLocked) {
                        lockService.unlock(EXPIRED_MEMBERS_JOB);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Can not run job ExpiredMembersJob", e);
        } finally {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    /**
     * @return the groupsApi
     */
    public GroupsApi getGroupsApi() {
        return groupsApi;
    }

    /**
     * @param groupsApi the groupsApi to set
     */
    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    /**
     * @return the historyApi
     */
    public HistoryApi getHistoryApi() {
        return historyApi;
    }

    /**
     * @param historyApi the historyApi to set
     */
    public void setHistoryApi(HistoryApi historyApi) {
        this.historyApi = historyApi;
    }

    /**
     * @return the lockService
     */
    public LockService getLockService() {
        return lockService;
    }

    /**
     * @param lockService the lockService to set
     */
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    private void initialize(final JobExecutionContext context) {
        setHistoryApi((HistoryApi) context.getMergedJobDataMap().get("historyApi"));
        setGroupsApi((GroupsApi) context.getMergedJobDataMap().get("groupsApi"));
        setLockService((LockService) context.getMergedJobDataMap().get("lockService"));
    }
}
