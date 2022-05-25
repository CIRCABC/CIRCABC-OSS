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
package eu.cec.digit.circabc.service.user;

import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.lock.LockService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Slobodan Filipovic
 */
public class LdapNullUsersJob implements Job {

    private static final String LDAP_NULL_USERS_JOB = "LdapNullUsersJob";
    private static final String ADMIN_USER = "admin";
    private static boolean isInitialized = false;
    private static CircabcServiceRegistry circabcServiceRegistry;
    private static Log logger = LogFactory.getLog(LdapNullUsersJob.class);
    private static UserService userService;
    private static LockService lockService;

    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            initialize(context);
            AuthenticationUtil.setRunAsUser(ADMIN_USER);
            if (!lockService.isLocked(LDAP_NULL_USERS_JOB)) {
                lockService.lock(LDAP_NULL_USERS_JOB);
                userService.updateMissingLastNamePersons();
            }
        } catch (final Exception e) {
            logger.error("Can not run job LdapNullUsersJob", e);
        } finally {
            AuthenticationUtil.clearCurrentSecurityContext();
            lockService.unlock(LDAP_NULL_USERS_JOB);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("finished...");
        }
    }

    private void initialize(final JobExecutionContext context) {
        if (!isInitialized) {

            try {
                AuthenticationUtil.setRunAsUser(ADMIN_USER);
                final JobDataMap jobData = context.getJobDetail().getJobDataMap();
                final Object circabcServiceRegistryObj = jobData.get("circabcServiceRegistry");
                circabcServiceRegistry = (CircabcServiceRegistry) circabcServiceRegistryObj;
                userService = circabcServiceRegistry.getUserService();
                lockService = circabcServiceRegistry.getLockService();
                isInitialized = true;
            } finally {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
        }
    }
}
