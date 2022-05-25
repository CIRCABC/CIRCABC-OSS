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
package eu.cec.digit.circabc.service.statistics.global;

import eu.cec.digit.circabc.service.lock.LockService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/** @author beaurpi */
public class GlobalStatisticsJobListener implements Job {

    static final Log logger = LogFactory.getLog(GlobalStatisticsJobListener.class);
    private static final String GLOBAL_STATISTICS_LOCK = "globalStatisticsRunningLock";
    private GlobalStatisticsService globalStatisticsService;
    private LockService circabcLockService;

    /** @return the globalStatisticsLock */
    public static String getGlobalStatisticsLock() {
        return GLOBAL_STATISTICS_LOCK;
    }

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap jobData = context.getJobDetail().getJobDataMap();

        String enabledOnHostname = (String) jobData.get("enabledOnHostname");

        if (enabledOnHostname != null && !enabledOnHostname.isEmpty()) {

            String hostName = null;

            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (final UnknownHostException e) {
            }

            if (hostName != null && !enabledOnHostname.equals(hostName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Skipping GlobalStatisticsJobListener. Run on host: "
                                    + enabledOnHostname
                                    + ", this host: "
                                    + hostName);
                }
                return;
            }
        }

        logger.debug("Running GlobalStatisticsJobListener");

        circabcLockService = (LockService) jobData.get("circabcLockService");
        globalStatisticsService = (GlobalStatisticsService) jobData.get("globalStatisticsService");

        Integer lockResult = lockJobFile();
        if (lockResult == 1) {

            try {

                AuthenticationUtil.setRunAsUserSystem();

                if (!globalStatisticsService.isReportSaveFolderExisting()) {
                    globalStatisticsService.prepareFolderRecipient();
                }

                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(new Date());

                if (gc.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                    globalStatisticsService.cleanAndZipPreviousReportFiles();
                }

                globalStatisticsService.saveStatsToExcel(
                        globalStatisticsService.getReportSaveFolder(),
                        globalStatisticsService.makeGlobalStats());

            } finally {
                AuthenticationUtil.clearCurrentSecurityContext();
                unlockJobFile();
            }
        }
    }

    /** @return the globalStatisticsService */
    public GlobalStatisticsService getGlobalStatisticsService() {
        return globalStatisticsService;
    }

    /** @param globalStatisticsService the globalStatisticsService to set */
    public void setGlobalStatisticsService(GlobalStatisticsService globalStatisticsService) {
        this.globalStatisticsService = globalStatisticsService;
    }

    /** @return the lockDaoService */
    public LockService getCircabcLockService() {
        return circabcLockService;
    }

    /** @param lockDaoService the lockDaoService to set */
    public void setCircabcLockService(LockService circabcLockService) {
        this.circabcLockService = circabcLockService;
    }

    public Integer lockJobFile() {

        Integer result = 0;

        if (!circabcLockService.isLocked(GLOBAL_STATISTICS_LOCK)) {
            circabcLockService.lock(GLOBAL_STATISTICS_LOCK);
            result = 1;
        }

        return result;
    }

    public Integer unlockJobFile() {

        Integer result = 0;

        if (circabcLockService.isLocked(GLOBAL_STATISTICS_LOCK)) {
            circabcLockService.unlock(GLOBAL_STATISTICS_LOCK);
            result = 1;
        }

        return result;
    }
}
