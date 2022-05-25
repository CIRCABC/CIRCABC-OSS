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
package eu.cec.digit.circabc.repo.lock;


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

public class UnlockJob implements Job {

    private static boolean isInitialized = false;

    private static ServiceRegistry serviceRegistry;

    private static CircabcServiceRegistry circabcServiceRegistry;

    private static Log logger = LogFactory.getLog(eu.cec.digit.circabc.repo.lock.UnlockJob.class);

    private static LockService lockService;

    private static int maxJobLockTimeInHours;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
            initialize(context);

            try {
                lockService.unlockAll(maxJobLockTimeInHours);
            } catch (final Exception e) {
                logger.error("Exception when running the unlock job.", e);
            }

        } catch (final Exception e) {
            logger.error("Can not run job LockJob", e);
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
            maxJobLockTimeInHours = Integer.valueOf((String) jobData.get("maxJobLockTimeInHours"));

            isInitialized = true;
        }
    }
}
