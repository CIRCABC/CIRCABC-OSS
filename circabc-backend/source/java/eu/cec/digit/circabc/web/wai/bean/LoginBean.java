/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.web.wai.bean;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.app.context.UICircabcContextService;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.wai.dialog.signup.SelfRegistrationDialog;
import net.sf.acegisecurity.AuthenticationCredentialsNotFoundException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.LoginOutcomeBean;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author atadian
 */
public class LoginBean extends org.alfresco.web.bean.LoginBean {

    public static final String WAI_LOGIN_PAGE = "/jsp/extension/wai/login.jsp";
    public static final String BEAN_NAME = "LoginBean";
    private static final long serialVersionUID = 3966998512111743446L;
    /**
     * Logger for this class *
     */
    private static final Log logger = LogFactory.getLog(LoginBean.class);
    /**
     * I18N messages
     */
    private static final String MSG_ERROR_UNKNOWN_USER = "error_login_user";
    private static final String MSG_ERROR_ALREADY_REGISTRED_USER = "error_already_registred_user";
    private static final String MSG_ERROR_REGISTRATION_PARAMETERS = "error_registration_parameters";
    private static String WEB_ROOT_URL = CircabcConfiguration
            .getProperty(CircabcConfiguration.WEB_ROOT_URL);
    /**
     * The domain where the user wants to log in *
     */
    protected String domain = UserModel.ALFRESCO_USER_PREFIX;
    private boolean registrationProcess;
    private boolean badParameters;

    private transient UserService userService;
    private transient ProfileManagerServiceFactory profileManagerServiceFactory;
    private transient ManagementService managementService;
    private transient LogService logService;

    private LockService lockService = null;
    private CheckOutCheckInService checkOutCheckInService = null;

    private MutableAuthenticationService authenticationService = null;

    private SearchService searchService = null;

    @Override
    public String login() {
        String outcome;

        // Fall-back to alfresco domain
        this.domain = UserModel.ALFRESCO_USER_PREFIX;
        outcome = super.login();

        return outcome;
    }

    /**
     * Action that perform the activation of an account and, if all is correct, log the user
     */
    public String activateAndLoginCirca() {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {
                // 	activate the user if we are in a registration process scope and the parameters are valid
                authenticationService.setAuthenticationEnabled(getUsername(), true);

                if (logger.isDebugEnabled()) {
                    logger.warn("The user " + getUsername()
                            + " is now correctly registred in circabc. Its account is set as active.");
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

        return loginCirca();
    }


    public String loginCirca() {
        final String user = super.getUsername();
        if (user.equals("")) {
            return null;
        }

        this.setUsername(super.getUsername());

        @SuppressWarnings("unchecked") final Map<Object, Object> session = FacesContext
                .getCurrentInstance().getExternalContext().getSessionMap();

        session.put(LoginOutcomeBean.PARAM_REDIRECT_URL, null);

        super.login();

        final LogRecord logRecord = new LogRecord();
        logRecord.setService("Directory");
        logRecord.setActivity("Login");
        logRecord.setUser(user);

//      if a redirect URL has been provided then use that
        // this allows servlets etc. to provide a URL to return too after a successful login
        final String redirectURL = (String) session.get(LoginOutcomeBean.PARAM_REDIRECT_URL);
        if (redirectURL != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Redirect URL found: " + redirectURL);
            }

            // remove redirect URL from session
            session.remove(LoginOutcomeBean.PARAM_REDIRECT_URL);

            logLoginStatus(logRecord, true);

            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(redirectURL);
                FacesContext.getCurrentInstance().responseComplete();
                return null;

            } catch (final IOException ioErr) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unable to redirect to url: " + redirectURL);
                }
            }
        }

        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getMessages().hasNext()) {
            // there is a message -> probably error;
            logLoginStatus(logRecord, false);
            return null;
        }
        UICircabcContextService.getInstance(FacesContext.getCurrentInstance()).circabcEntered();

        ((CircabcBrowseBean) this.browseBean).refreshBrowsing();

        // set language
        final UserPreferencesBean userPreferencesBean = getUserPreferencesBean();
        final String language = userPreferencesBean.getLanguage();
        userPreferencesBean.setLanguage(language);
        logLoginStatus(logRecord, true);
        return CircabcBrowseBean.WAI_BROWSE_OUTCOME;
    }

    /**
     * @param logRecord
     */
    private void logLoginStatus(final LogRecord logRecord, final Boolean status) {
        /*if login successful we check the root node
         * if not, we don't have permission to do it and it would throw one exception
         */
        if (status) {
            NodeRef circabcNodeRef = getManagementService().getCircabcNodeRef();
            logRecord.setIgID(
                    (Long) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NODE_DBID));
            logRecord
                    .setIgName((String) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NAME));
        }

        logRecord.setOK(status);
        getLogService().log(logRecord);
    }

    /**
     * Called in a JSP displayer, he returns always true and serve to init the activation with the url
     * send at the self registration process
     **/
    public boolean getInitActivation() {
        // DIGIT-CIRCABC-683 Workaround to avoid to display 4 times the same message du to
        // the call of this method 4 times.
        if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
            return false;
        }

        registrationProcess = false;
        badParameters = false;

        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        final String paramUserName = (String) params.get(SelfRegistrationDialog.USERNAME_URL_PARAM);
        final String activationID = (String) params
                .get(SelfRegistrationDialog.ACTIVATION_KEY_URL_PARAM);

        getAuthenticationService().authenticateAsGuest();

        // Check if the parameters are presents
        if (paramUserName != null && activationID != null) {
            // all self registred users are in the circa domain
            registrationProcess = true;

            // test if the person is created
            if (getPersonService().personExists(paramUserName)) {
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
                    public Object doWork() {
                        NodeRef userNodeRef = getPersonService().getPerson(paramUserName);

                        if (userNodeRef != null && userNodeRef.getId().equals(activationID)) {
                            if (getAuthenticationService().getAuthenticationEnabled(paramUserName)) {
                                // user already registred. Just inform the user.
                                Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(),
                                        MSG_ERROR_ALREADY_REGISTRED_USER));
                                badParameters = true;

                                if (logger.isDebugEnabled()) {
                                    logger.debug("A person wants to activate a second time its account. " +
                                            " Account: " + paramUserName +
                                            " Activation id: " + activationID);
                                }
                            }
                        } else {
                            // the activation key doesn't correspond to the user id
                            Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(),
                                    MSG_ERROR_REGISTRATION_PARAMETERS));
                            badParameters = true;

                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "A person wants to activate its aacount but the activation key is not correct. "
                                                +
                                                " Account: " + paramUserName +
                                                " Activation id: " + activationID);
                            }
                        }

                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());

            } else {
                // the person is not created
                Utils.addErrorMessage(Application
                        .getMessage(FacesContext.getCurrentInstance(), MSG_ERROR_REGISTRATION_PARAMETERS));
                badParameters = true;
            }

            if (logger.isWarnEnabled()) {
                logger.warn("A person wants to activate an inexisting account." +
                        " Check if the same error is not reproduced too many times." +
                        " Account: " + paramUserName +
                        " Activation id: " + activationID);
            }
        }

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String logout() {
        String userName;

        try {
            userName = authenticationService.getCurrentUserName();
        } catch (AuthenticationCredentialsNotFoundException e) {
            // Execute the parent method (Alfresco 4.2.4 change)
            return super.logout();
        }

        final FacesContext context = FacesContext.getCurrentInstance();

        // Unlock all current user locked nodes that do not have working
        // copies, if any
//        if (lockService == null) {
//        	lockService = Repository.getServiceRegistry(
//        				FacesContext.getCurrentInstance()).getLockService();
//        }
//        if (checkOutCheckInService == null) {
//        	checkOutCheckInService = Repository.getServiceRegistry(
//        				FacesContext.getCurrentInstance()).getCheckOutCheckInService();
//        }
//        
//        final List<NodeRef> lockedNodes = getLocks(userName, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
//		
//        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
//        	
//     		public Object doWork()
//     		{
//     	        for (NodeRef lockedNode : lockedNodes) {
//     	        	if (checkOutCheckInService.getWorkingCopy(lockedNode) == null) {
//     	        		lockService.unlock(lockedNode);
//     	        	}
//     	        }
//     	        
//     	        return null;
//     		}
//     	}, AuthenticationUtil.SYSTEM_USER_NAME);

        // Log logout activity
        final LogRecord logRecord = new LogRecord();
        logRecord.setService("Directory");
        logRecord.setActivity("Logout");

        boolean externalAuth = false;

        String redirectUrlAfterLogout = null;

        final Map<Object, Object> session = context.getExternalContext().getSessionMap();
        final User user = (User) session.get(AuthenticationHelper.AUTHENTICATION_USER);

        try {
            // Migration 3.1 -> 3.4.6 - 12/12/2011 - Privileged methods
            AuthenticationUtil.setRunAsUserSystem();

            redirectUrlAfterLogout = getUserService().getRedirectUrlAfterLogout();
            final NodeRef circabcNodeRef = getManagementService().getCircabcNodeRef();
            logRecord.setIgID(
                    (Long) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NODE_DBID));
            logRecord
                    .setIgName((String) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NAME));

            // need to capture this value before invalidating the session
            // Migration 3.1 -> 3.4.6 - 05/12/2011
            // Original method isAlfrescoAuth changed.
            externalAuth = "logout".equals(getLogoutOutcome());

            if (user != null) {
                logRecord.setUser(user.getUserName());
                // invalidate ticket and clear the Security context for this thread
                getAuthenticationService().invalidateTicket(user.getTicket());
                getAuthenticationService().clearCurrentSecurityContext();
            }
            // remove all objects from our session by hand
            // we do this as invalidating the Portal session would invalidate all other portlets!
            for (final Object key : session.keySet()) {
                session.remove(key);
            }

            // Perform log out actions
            Application.logOut(context);

            // set language to last used
            final String language = preferences.getLanguage();
            if (language != null && language.length() != 0) {
                Application.setLanguage(context, language);
            }

            logRecord.setOK(true);
            getLogService().log(logRecord);
        } catch (Exception exp) {
            if (logger.isErrorEnabled()) {
                logger.error("[EXC] Exception:" + exp);
            }
            Utils.addErrorMessage(exp.getMessage());
            logRecord.setOK(false);
        } finally {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
        if ((redirectUrlAfterLogout != null) && !redirectUrlAfterLogout.isEmpty() &&
                (user != null && !AuthenticationUtil.getAdminUserName().equals(user.getUserName()))
                && CircabcConfig.ENT) {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            try {
                externalContext
                        .redirect(redirectUrlAfterLogout + "?url=" + new URI(WEB_ROOT_URL).toASCIIString());
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Can not redirect", e);
                }
            } catch (URISyntaxException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Can not parse URI : " + WEB_ROOT_URL, e);
                }
            }
        }

        return externalAuth ? "logoutReal" : "relogin";
    }

    /**
     * Method getLocks was removed from the Alfresco 4.2 API, so its functionality was brought back
     * from Alfresco 3, since there was no equivalent provided
     */
    protected List<NodeRef> getLocks(String userName, StoreRef storeRef) {
        return getLocks(storeRef, "ASPECT:\"" +
                ContentModel.ASPECT_LOCKABLE.toString() +
                "\" +@\\{http\\://www.alfresco.org/model/content/1.0\\}" +
                ContentModel.PROP_LOCK_OWNER.getLocalName() + ":\"" +
                userName + "\"");
    }

    /**
     * Get the locks given a store and query string.
     *
     * @param storeRef the store reference
     * @param query    the query string
     * @return the locked nodes
     */
    private List<NodeRef> getLocks(StoreRef storeRef, String query) {

        List<NodeRef> result = new ArrayList<>();
        ResultSet resultSet = null;

        try {
            resultSet = this.searchService.query(storeRef,
                    SearchService.LANGUAGE_LUCENE, query);

            result = resultSet.getNodeRefs();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }

        return result;
    }

    @Override
    public String getUsername() {
        String userName = super.getUsername();
        if (userName != null) {
            int indexOf = userName.indexOf('@');
            if (indexOf > 0) {
                userName = userName.substring(0, indexOf);
            }
        }
        return userName;
    }

    /**
     * @return if the login page is not in a self registration process. This method should be called
     * ONLY after the call of the isRegistrationProcessWithTestingParams to init the values.
     */
    public boolean isRegistrationProcess() {
        return registrationProcess;
    }

    /**
     * @return if error were found in the parameters of the registration process
     */
    public boolean isBadParameters() {
        return badParameters;
    }

    /**
     * Login action handler for ECAS
     *
     * @return outcome view name
     */
/*
    @SuppressWarnings("unchecked")
    public String ecasLogin()
    {
        String outcome = null;
        final LogRecord logRecord = new LogRecord();


        logger.info("[1] Loggin using ecas  ");
        final Principal userPrincipal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        logger.info("[2] userName:" + userPrincipal);
        if (userPrincipal == null )
        {
            Utils.addErrorMessage("userPrincipal is null ");
            return outcome;
        }
        
               

        final String userPrincipalName = userPrincipal.getName();
        if (userPrincipalName == null || userPrincipalName.equals(""))
        {
            Utils.addErrorMessage("userPrincipal is null ");
            return outcome;
        }

        String userName = "";

        userName = userPrincipalName  ;

        outcome = "success";
        final FacesContext fc = FacesContext.getCurrentInstance();

        try
        {
            logRecord.setService("Directory");
            logRecord.setActivity("Login");
            logRecord.setUser(userName);
            this.setUsername(userName);
            
        	if (logger.isInfoEnabled())
        	{
        		logger.info("[3]Getting External context:");
        	}
        	
            final Map<Object, Object> session = fc.getExternalContext().getSessionMap();
            
            try
            {
	            AuthenticationUtil.setRunAsUserSystem();
	            if (!getPersonService().personExists(userName))
	            {
	            	
	            	final CircabcUserDataBean user = new CircabcUserDataBean();
	            	user.setUserName(userName);

	            	final CircabcUserDataBean ldapUserDetail = getUserService().getLDAPUserDataByUid(userPrincipalName);
	            	user.copyLdapProperties(ldapUserDetail);
	            	if (ldapUserDetail == null )
	            	{
		            	if (userPrincipal instanceof DetailedUser )
		                {
		                	DetailedUser detailedUser = (DetailedUser) userPrincipal;
		                	user.copyDetailedUserProperties(detailedUser);
		                }
	            	}
	            	//user.setHomeSpaceNodeRef(managementService.getGuestHomeNodeRef());
	            	getUserService().createUser(user, true);
	            }
            } finally {
            	// Authenticate via the authentication service, then save the
                // details of user in an object
                // in the session - this is used by the servlet filter etc. on each
                // page to check for login
                AuthenticationUtil.setRunAsUser(userName);	
            }            
            
            // Migration 3.1 -> 3.4.6 - 12/12/2011
            try {
            	AuthenticationUtil.setRunAsUserSystem();
            	
            	final NodeRef circabcNodeRef = getManagementService().getCircabcNodeRef();
            	logRecord.setIgID((Long) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NODE_DBID));
            	logRecord.setIgName((String) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NAME));
            } 
            finally {
            	// Authenticate via the authentication service, then save the
                // details of user in an object
                // in the session - this is used by the servlet filter etc. on each
                // page to check for login
                AuthenticationUtil.setRunAsUser(userName);	
            }   
            
            // remove the session invalidated flag (used to remove last username
            // cookie by AuthenticationFilter)
            session.remove(AuthenticationHelper.SESSION_INVALIDATED);

            // setup User object and Home space ID
            if (logger.isInfoEnabled())
        	{
            	logger.info("[4]Ready to Create User:" + userName);
        	}
            final String ticket = getUserService().getCurrentTicket(userName);
            if (logger.isInfoEnabled())
        	{
            	logger.info("[5]Ticket:" + ticket + " for user: " + userName);
        	}

            final NodeRef nodeRef = getPersonService().getPerson(userName);
            if (logger.isInfoEnabled())
        	{
            	logger.info("[6]nodeRef:" + nodeRef + " for user: " + userName);
        	}

            final User user = new User(userName, ticket, nodeRef);

            if (logger.isInfoEnabled())
        	{
            	logger.info("[7]User Created:" + user);
        	}
            final NodeRef homeSpaceRef = (NodeRef) getNodeService().getProperty(getPersonService().getPerson(userName), ContentModel.PROP_HOMEFOLDER);

            if (logger.isInfoEnabled())
        	{
            	logger.info("[8] HomeSpace:" + homeSpaceRef);
        	}

            // check that the home space node exists - else user cannot login
            if (getNodeService().exists(homeSpaceRef) == false)
            {
                throw new InvalidNodeRefException(homeSpaceRef);
            }
            if (logger.isInfoEnabled())
        	{
            	logger.info("[9] Setting HomeSpaceId");
        	}
            user.setHomeSpaceId(homeSpaceRef.getId());

            // put the User object in the Session - the authentication servlet
            // will then allow
            // the app to continue without redirecting to the login page
            if (logger.isInfoEnabled())
        	{
            	logger.info("[10] Setting Extra session Data");
        	}
            session.put(AuthenticationHelper.AUTHENTICATION_USER, user);
//          if a redirect URL has been provided then use that
            // this allows servlets etc. to provide a URL to return too after a successful login
            final String redirectURL = (String)session.get(LoginOutcomeBean.PARAM_REDIRECT_URL);
            if (redirectURL != null)
            {
               if (logger.isDebugEnabled())
               {
                  logger.debug("Redirect URL found: " + redirectURL);
               }

               // remove redirect URL from session
               session.remove(LoginOutcomeBean.PARAM_REDIRECT_URL);

               try
               {
                  fc.getExternalContext().redirect(redirectURL);
                  fc.responseComplete();
                  return null;

               }
               catch (final IOException ioErr)
               {
            	   if (logger.isWarnEnabled())
            	   {
            		   logger.warn("Unable to redirect to url: " + redirectURL);
            	   }
               }
            }


            logRecord.setOK(true);
            getLogService().log(logRecord);
            
            // Migration 3.1 -> 3.4.6 - 12/12/2011
            try {
            	AuthenticationUtil.setRunAsUserSystem();
            	
                // set language 
                final UserPreferencesBean userPreferencesBean = getUserPreferencesBean();
                final String language = userPreferencesBean.getLanguage();
                userPreferencesBean.setLanguage(language);
                
                UICircabcContextService.getInstance(FacesContext.getCurrentInstance()).circabcEntered();
                ((CircabcBrowseBean) this.browseBean).refreshBrowsing();
            } 
            finally {
            	// Authenticate via the authentication service, then save the
                // details of user in an object
                // in the session - this is used by the servlet filter etc. on each
                // page to check for login
                AuthenticationUtil.setRunAsUser(userName);	
            }
            
            // By pass redirection after login
            return CircabcBrowseBean.WAI_BROWSE_OUTCOME;

        } catch (final AuthenticationException aerr)
        {
        	outcome = null;
            // the user probably doesn't exist
        	if (logger.isErrorEnabled())
        	{
        		logger.error("[EXC] AuthenticationException:" + aerr);
        	}
            Utils.addErrorMessage(Application.getMessage(fc, MSG_ERROR_UNKNOWN_USER));
            logRecord.setOK(false);

        } catch (final InvalidNodeRefException refErr)
        {
        	outcome = null;
        	if (logger.isErrorEnabled())
        	{
        		logger.error("[EXC] InvalidNodeRefException:" + refErr);
        	}
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(fc, Repository.ERROR_NOHOME), refErr.getNodeRef().getId()));
            logRecord.setOK(false);
        }
        catch(final NoSuchPersonException ex)
        {
        	outcome = null;
            // the user probably doesn't exist
        	if (logger.isErrorEnabled())
        	{
        		logger.error("[EXC] AuthenticationException:" + ex);
        	}
            Utils.addErrorMessage(Application.getMessage(fc, MSG_ERROR_UNKNOWN_USER));
            logRecord.setOK(false);
        }
        catch (final Exception exp)
        {
        	outcome = null;
        	if (logger.isErrorEnabled())
        	{
        		logger.error("[EXC] Exception:" + exp);
        	}
            Utils.addErrorMessage(exp.getMessage());
            logRecord.setOK(false);
            outcome = "login";
        }
        logRecord.setOK(false);
        getLogService().log(logRecord);
        return outcome;
    }
*/

    /**
     * @return the selected domain (circa or Alfresco)
     */
    public String getDomainsel() {
        return this.domain;
    }

    /**
     * set the selected domain
     */
    public void setDomainsel(final String sel) {
        this.domain = sel;
    }


    /**
     * To avoid cross validation problem (need domain to validate pattern), this validation will not
     * be done. Password will be validate on service side
     */
    @Override
    public void validatePassword(final FacesContext context, final UIComponent component,
                                 final Object value) throws ValidatorException {

    }

    /**
     * @return the UserService
     */
    public UserService getUserService() {

        if (userService == null) {
            userService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getUserService();
        }
        return userService;
    }

    /**
     * @param userService the UserService to set
     */
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    /**
     * @param authenticationService the authenticationService to set
     */
    public void setAuthenticationService(
            MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * @return the profileManagerServiceFactory
     */
    public ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        if (profileManagerServiceFactory == null) {
            profileManagerServiceFactory = (ProfileManagerServiceFactory) ApplicationContextHelper
                    .getApplicationContext().getBean("ProfileManagerServiceFactory");
        }

        return profileManagerServiceFactory;
    }

    /**
     * @param profileManagerServiceFactory the profileManagerServiceFactory to set
     */
    public void setProfileManagerServiceFactory(
            final ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    /**
     * @return the managementService
     */
    protected final ManagementService getManagementService() {
        if (managementService == null) {
            managementService = (ManagementService) ApplicationContextHelper.getApplicationContext()
                    .getBean("ManagementService");
        }
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @return the logService
     */
    protected final LogService getLogService() {
        if (logService == null) {
            logService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getLogService();
        }
        return logService;
    }

    /**
     * @param logService the logService to set
     */
    public final void setLogService(final LogService logService) {
        this.logService = logService;
    }

    /**
     * @param searchService the searchService to set
     */
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public String getLoginTextAdapted() {
        String result = "";

        if (CircabcConfig.ENT || CircabcConfig.ECHA) {
            result = WebClientHelper
                    .translate("login_page_text", CircabcConfiguration.getApplicationName());
        }

        return result;
    }

    public String getLoginTitleAdapted() {
        String result;

        if (CircabcConfig.ENT || CircabcConfig.ECHA) {
            result = WebClientHelper.translate("login_title", CircabcConfiguration.getApplicationName());
        } else {
            result = WebClientHelper.translate("login_title", "CIRCABC");
        }

        return result;
    }

    public Boolean getIsEcha() {
        return CircabcConfig.ECHA;
    }
}