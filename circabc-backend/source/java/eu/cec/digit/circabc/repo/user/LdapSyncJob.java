package eu.cec.digit.circabc.repo.user;

import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.user.UserService;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.StopWatch;

import java.util.List;

public class LdapSyncJob implements Job {

    private static final String LDAP_SYNC = "LDAP_SYNC";

    private static final String ADMIN_USER = "admin";

    private static boolean isInitialized = false;

    private static ServiceRegistry serviceRegistry;

    private static CircabcServiceRegistry circabcServiceRegistry;

    private static Log logger = LogFactory.getLog(LdapSyncJob.class);

    private static UserService userService;

    private static LockService lockService;

    private PersonService personService;

    private NodeService nodeService;

    private List<String> excludedUsers;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            boolean isLocked = false;
            AuthenticationUtil.setRunAsUser(ADMIN_USER);
            initialize(context);
            if (!lockService.isLocked(LDAP_SYNC)) {
                try {
                    lockService.lock(LDAP_SYNC);
                    isLocked = true;
                    syncLdap();
                } finally {
                    if (isLocked) {
                        lockService.unlock(LDAP_SYNC);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Can not run job LdapSync", e);
        } finally {
            AuthenticationUtil.clearCurrentSecurityContext();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("finished...");
        }
    }

    private void initialize(final JobExecutionContext context) {
        if (!isInitialized) {

            AuthenticationUtil.setRunAsUser(ADMIN_USER);
            final JobDataMap jobData = context.getJobDetail().getJobDataMap();

            final Object serviceRegistryObj = jobData.get("serviceRegistry");
            serviceRegistry = (ServiceRegistry) serviceRegistryObj;
            personService = serviceRegistry.getPersonService();
            nodeService = serviceRegistry.getNodeService();
            final Object circabcServiceRegistryObj = jobData.get("circabcServiceRegistry");
            circabcServiceRegistry = (CircabcServiceRegistry) circabcServiceRegistryObj;
            userService = circabcServiceRegistry.getUserService();
            lockService = circabcServiceRegistry.getLockService();
            excludedUsers = (List<String>) jobData.get("excludedUsers");

            isInitialized = true;
        }
    }

    private void syncLdap() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get all users  ");
        List<PersonInfo> personInfos =
                personService
                        .getPeople(null, null, null, new PagingRequest(Integer.MAX_VALUE, null))
                        .getPage();
        stopWatch.stop();
        if (logger.isInfoEnabled()) {
            logger.info(stopWatch.prettyPrint());
            logger.info("Total number of users: " + personInfos.size());
        }
        for (PersonInfo personInfo : personInfos) {
            final String userName = personInfo.getUserName();
            if (excludedUsers.contains(userName) || isStorkUser(userName)) {
                continue;
            }
            final Boolean userExists = userService.userExists(userName);
            if (userExists != null && userExists == false) {
                if (userService.getAuthenticationEnabled(userName)) {
                    stopWatch.start("Disable user " + userName);
                    final NodeRef nodeRef = personInfo.getNodeRef();
                    nodeService.setProperty(nodeRef, UserModel.PROP_GLOBAL_NOTIFICATION, false);
                    userService.setAuthenticationEnabled(userName, false);
                    stopWatch.stop();
                    if (logger.isInfoEnabled()) {
                        logger.info("Disable user: " + userName);
                    }
                }
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info(stopWatch.prettyPrint());
        }
    }

    private boolean isStorkUser(String userName) {
        // check if user name contain two  slash chars : '/'
        // it should not start with '/'
        final int indexOf = userName.indexOf('/');
        final int lastIndexOf = userName.lastIndexOf('/');
        return indexOf > 0 && lastIndexOf > indexOf;
    }
}
