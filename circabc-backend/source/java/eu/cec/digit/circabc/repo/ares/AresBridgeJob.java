package eu.cec.digit.circabc.repo.ares;

import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.lock.LockService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AresBridgeJob implements Job {
    private static final String ARES_JOB = "ARES_JOB";

    private static boolean isInitialized = false;

    private static ServiceRegistry serviceRegistry;

    private static CircabcServiceRegistry circabcServiceRegistry;

    private static Log logger = LogFactory.getLog(eu.cec.digit.circabc.repo.ares.AresBridgeJob.class);

    private static LockService lockService;
    private static AresBridgeServiceImpl aresBridgeService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
            initialize(context);
            boolean isLocked = false;
            if (!lockService.isLocked(ARES_JOB)) {
                try {
                    lockService.lock(ARES_JOB);
                    isLocked = true;
                    aresBridgeService.process();
                } catch (Exception e) {
                    logger.error("Exception when running the log job.", e);
                } finally {
                    if (isLocked) {
                        lockService.unlock(ARES_JOB);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Can not run job LogJob", e);
        } finally {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    private void initialize(final JobExecutionContext context) {
        if (!isInitialized) {
            final JobDataMap jobData = context.getJobDetail().getJobDataMap();
            final Object serviceRegistryObj = jobData.get("serviceRegistry");
            serviceRegistry = (ServiceRegistry) serviceRegistryObj;

            final Object circabcServiceRegistryObj = jobData.get("circabcServiceRegistry");
            circabcServiceRegistry = (CircabcServiceRegistry) circabcServiceRegistryObj;
            lockService = circabcServiceRegistry.getLockService();
            aresBridgeService = circabcServiceRegistry.getAresBridgeService();
            isInitialized = true;
        }
    }
}
