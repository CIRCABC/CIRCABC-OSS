/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.dialog;

import eu.cec.digit.circabc.business.api.BusinessRegistry;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.app.SecurityService;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.mail.MailToMembersService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.bean.override.CircabcUserPreferencesBean;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Base class for all WAI dialog beans providing common functionality
 *
 * @author yanick pignot
 */
public abstract class BaseWaiDialog extends BaseDialogBean implements WaiDialog, WaiDialogAsync,
        IECompatibilityPreference {


    public static final String NODE_ID_PARAMETER = "id";
    public static final String ACTIVITY_PARAMETER = "activity";
    public static final String SERVICE_PARAMETER = "service";
    protected static final String MSG_ERR_INVALID_VALUE_FOR = "msg_err_invalid_value_for";
    private static final Log logger = LogFactory.getLog(BaseWaiDialog.class);
    private static final String MSG_ERR_ACCESS_DENIED = "browse_login_but_not_allow";
    protected Long actionNodeDatabaseID;
    protected LogRecord logRecord = new LogRecord();
    // Common beans and services
    private transient ManagementService managementService;
    private transient ProfileManagerServiceFactory profileManagerServiceFactory;
    private transient NodeService nodeService;
    private transient FileFolderService fileFolderService;
    private transient SearchService searchService;
    private transient DictionaryService dictionaryService;
    private transient NamespaceService namespaceService;
    private transient UserService userService;
    private transient CircabcUserPreferencesBean userPreferencesBean;
    private transient PermissionService permissionService;
    private transient MailService mailService;
    private transient MailToMembersService MailToMembersService;
    private transient MailPreferencesService mailPreferencesService;
    private transient LogService logService;
    private transient MultilingualContentService multilingualContentService;
    private transient ThreadPoolExecutor asyncThreadPoolExecutor;
    private transient BusinessRegistry businessRegistry;
    private CircabcBrowseBean browseBean;
    private CircabcNavigationBean navigator;
    private MapNode actionNode;
    private boolean isFinishedAsync;
    private SecurityService securityService;

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);
        this.isFinishedAsync = false;

        if (parameters != null && parameters.get(NODE_ID_PARAMETER) != null) {
            final String id = parameters.get(NODE_ID_PARAMETER);
            try {
                actionNode = new MapNode(new NodeRef(Repository.getStoreRef(), id));
                actionNodeDatabaseID = (Long) actionNode.getProperties()
                        .get(ContentModel.PROP_NODE_DBID.toString());

                // find it's type so we can see if it's a node we are interested in
                final QName type = actionNode.getType();
                // make sure the type is defined in the data dictionary
                final TypeDefinition typeDef = getDictionaryService().getType(type);

                if (typeDef != null) {
                    // look for Space or File nodes
                    if (ContentModel.TYPE_FOLDER.equals(type) ||
                            ApplicationModel.TYPE_FOLDERLINK.equals(type) ||
                            getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER) ||
                            getDictionaryService().isSubClass(type, ApplicationModel.TYPE_FOLDERLINK)) {
                        getBrowseBean().setActionSpace(actionNode);
                        //resolve icon in-case one has not been set
                        actionNode.addPropertyResolver("icon", getBrowseBean().resolverSpaceIcon);
                        getBrowseBean().setDocument(null);
                    } else {
                        getBrowseBean().setDocument(actionNode);
                        getBrowseBean().setActionSpace(null);
                    }
                }

            } catch (final InvalidNodeRefException refErr) {
                Utils.addErrorMessage(translate(Repository.ERROR_NODEREF, id));
            } catch (final AccessDeniedException err) {
                Utils.addErrorMessage(translate(MSG_ERR_ACCESS_DENIED));
            }

        }
        setLogRecord(parameters);
    }

    protected String getCleanHTML(String value, Boolean permissiveHtml) {
        return getSecurityService().getCleanHTML(value, permissiveHtml);
    }

    protected boolean isCleanHTML(String value, Boolean permissiveHtml) {
        return getSecurityService().isCleanHTML(value, permissiveHtml);
    }

    @Override
    public String finishAsync() {
        final FacesContext context = FacesContext.getCurrentInstance();
        final String defaultOutcome = getDefaultFinishOutcome();
        String outcome = defaultOutcome;

        // check the isFinishedAsync flag to stop the finish button
        // being pressed multiple times
        if (!this.isFinishedAsync) {
            this.isFinishedAsync = true;
            Runnable r = new FinishAsyncRun(context, defaultOutcome,
                    AuthenticationUtil.getFullyAuthenticatedUser());
            try {
                getAsyncThreadPoolExecutor().execute(r);
                // remove container variable
                context.getExternalContext().getSessionMap().remove(
                        AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION);

                Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate("action_background"));
            } catch (Throwable e) {
                // reset the flag so we can re-attempt the operation
                isFinishedAsync = false;
                outcome = getErrorOutcome(e);
                if (!(e instanceof ReportedException)) {
                    Utils.addErrorMessage(formatErrorMessage(e), e);
                }
                ReportedException.throwIfNecessary(e);
            }
        } else {
            Utils.addErrorMessage(Application.getMessage(context, "error_wizard_completed_already"));
        }

        return outcome;
    }

    protected LogRecord getLogRecord() {

        return logRecord;
    }

    private void setLogRecord(final Map<String, String> parameters) {

        if (parameters != null) {
            final String activity = parameters.get(ACTIVITY_PARAMETER);
            if (activity == null) {
                return;
            } else {
                logRecord.setActivity(activity);
            }
            final String service = parameters.get(SERVICE_PARAMETER);
            if (service == null) {
                return;
            } else {
                logRecord.setService(service);
            }
        }
        if (actionNode != null) {
            final NodeRef nodeRef = actionNode.getNodeRef();
            updateLogDocument(nodeRef, this.logRecord);
        }
    }

    public void updateLogDocument(final NodeRef nodeRef, final LogRecord logRecord) {

        logRecord
                .setDocumentID((Long) getNodeService().getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
        final NodeRef igNodeRef = getManagementService().getCurrentInterestGroup(nodeRef);
        if (igNodeRef != null) {

            logRecord
                    .setIgID((Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID));
            logRecord.setIgName((String) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NAME));
        } else {
            final NodeRef categoryNodeRef = getManagementService().getCurrentCategory(nodeRef);
            if (categoryNodeRef != null) {
                logRecord.setIgID(
                        (Long) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NODE_DBID));
                logRecord.setIgName(
                        (String) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NAME));
            } else {
                final NodeRef circabcNodeRef = getManagementService().getCircabcNodeRef();
                logRecord.setIgID(
                        (Long) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NODE_DBID));
                logRecord.setIgName(
                        (String) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NAME));

            }
        }
        logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
        final Path path;
        if (getNodeService().getType(nodeRef).equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
            //tempNodeRef = getMultilingualContentService().getPivotTranslation(nodeRef);
            final NodeRef tempNodeRef = getMultilingualContentService().getPivotTranslation(nodeRef);
            path = getNodeService().getPath(tempNodeRef);

        } else {
            path = getNodeService().getPath(nodeRef);
        }

        logRecord.setPath(PathUtils.getCircabcPath(path, true));
    }

    public Object getRequestValue(final String key) {
        final HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        return request.getAttribute(key);
    }

    public void setRequestValue(final String key, final Object value) {
        final HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        request.setAttribute(key, value);
    }

    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {
        if (logger.isDebugEnabled()) {
            logger.debug("outcome:" + outcome);
        }

        logRecord.setOK(true);
        getLogService().log(logRecord);

        return super.doPostCommitProcessing(context, outcome);
    }

    protected String getErrorOutcome(final Throwable exception) {
        logRecord.setOK(false);
        getLogService().log(logRecord);
        return super.getErrorOutcome(exception);
    }

    public ActionsListWrapper getActionList() {
        // the most of dialog don't need an action list
        return null;
    }

    /**
     * @return true if the cancel button must be displayed
     */
    public boolean isCancelButtonVisible() {
        // the most of dialog need the cancel button
        return true;
    }

    public boolean isFormProvided() {
        // the most of dialog need a form
        return false;
    }

    /**
     * @return the initialized Node at the init time (with id parameter)
     */
    public Node getActionNode() {
        return actionNode;
    }

    @Override
    public boolean getFinishButtonDisabled() {
        // in a wai page, the verfication MUST be done in the Bean and NOT via Javascript.
        return false;
    }

    protected final String translate(final String key, final Object... params) {
        return WebClientHelper.translate(key, params);
    }

    protected String getBestTitle(final Node node) {
        final String name = node.getName();
        final String title = (String) getActionNode().getProperties()
                .get(ContentModel.PROP_TITLE.toString());

        if (title == null || title.trim().length() < 1) {
            return name;
        } else {
            return title;
        }
    }

    protected String getBestTitle(final NodeRef nodeRef) {
        final String name = (String) getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
        final String title = (String) getNodeService().getProperty(nodeRef, ContentModel.PROP_TITLE);

        if (title == null || title.trim().length() < 1) {
            return name;
        } else {
            return title;
        }
    }

    /**
     * @return the browseBean
     */
    protected final CircabcBrowseBean getBrowseBean() {
        if (browseBean == null) {
            browseBean = Beans.getWaiBrowseBean();
        }
        return browseBean;
    }

    /**
     * @param browseBean the browseBean to set
     */
    public final void setBrowseBean(final CircabcBrowseBean browseBean) {
        this.browseBean = browseBean;
    }

    /**
     * @return the ManagementService
     */
    protected final ManagementService getManagementService() {
        if (managementService == null) {
            managementService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getManagementService();
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
     * @return the dictionaryService
     */
    protected final DictionaryService getDictionaryService() {
        if (dictionaryService == null) {
            dictionaryService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getDictionaryService();
        }
        return dictionaryService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public final void setDictionaryService(final DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @return the fileFolderService
     */
    protected final FileFolderService getFileFolderService() {
        if (fileFolderService == null) {
            fileFolderService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getFileFolderService();
        }
        return fileFolderService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public final void setFileFolderService(final FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @return the namespaceService
     */
    protected final NamespaceService getNamespaceService() {
        if (namespaceService == null) {
            namespaceService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getNamespaceService();
        }

        return namespaceService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public final void setNamespaceService(final NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @return the navigator
     */
    protected final CircabcNavigationBean getNavigator() {
        if (navigator == null) {
            navigator = Beans.getWaiNavigator();
        }
        return navigator;
    }

    /**
     * @param navigator the navigator to set
     */
    public final void setNavigator(final CircabcNavigationBean navigator) {
        this.navigator = navigator;
    }

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        if (nodeService == null) {
            nodeService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getNodeService();
        }
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the profileManagerServiceFactory
     */
    protected final ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        if (profileManagerServiceFactory == null) {
            profileManagerServiceFactory = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getProfileManagerServiceFactory();
        }
        return profileManagerServiceFactory;
    }

    /**
     * @param profileManagerServiceFactory the profileManagerServiceFactory to set
     */
    public final void setProfileManagerServiceFactory(
            final ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    /**
     * @return the searchService
     */
    protected final SearchService getSearchService() {
        if (searchService == null) {
            searchService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getSearchService();
        }
        return searchService;
    }

    /**
     * @param searchService the searchService to set
     */
    public final void setSearchService(final SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @return the userService
     */
    protected final UserService getUserService() {
        if (userService == null) {
            userService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getUserService();
        }
        return userService;
    }

    /**
     * @param userService the UserService to set
     */
    public final void setUserService(final UserService userService) {
        this.userService = userService;
    }

    /**
     * @return the userPreferencesBean
     */
    protected final CircabcUserPreferencesBean getUserPreferencesBean() {
        if (userPreferencesBean == null) {
            userPreferencesBean = Beans.getUserPreferencesBean();
        }
        return userPreferencesBean;
    }

    /**
     * @param userPreferencesBean the userPreferencesBean to set
     */
    public final void setUserPreferencesBean(final CircabcUserPreferencesBean userPreferencesBean) {
        this.userPreferencesBean = userPreferencesBean;
    }

    /**
     * @return the permissionService
     */
    protected final PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getPermissionService();
        }
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public final void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the mailService
     */
    protected final MailService getMailService() {
        if (mailService == null) {
            mailService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getMailService();
        }
        return mailService;
    }

    /**
     * @param mailService the mailService to set
     */
    public final void setMailService(final MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * @return the mailToMembersService
     */
    protected final MailToMembersService getMailToMembersService() {
        if (MailToMembersService == null) {
            MailToMembersService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getMailToMembersService();
        }
        return MailToMembersService;
    }

    /**
     * @param mailToMembersService the circabcMailService to set
     */
    public final void setMailToMembersService(final MailToMembersService mailToMembersService) {
        this.MailToMembersService = mailToMembersService;
    }

    /**
     * @return the nodePreferencesService
     */
    protected final MailPreferencesService getMailPreferencesService() {
        if (mailPreferencesService == null) {
            mailPreferencesService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getMailPreferencesService();
        }
        return mailPreferencesService;
    }

    /**
     * @param mailPreferencesService the mailPreferencesService to set
     */
    public final void setMailPreferencesService(final MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
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
     * @return the multilingualContentService
     */
    public MultilingualContentService getMultilingualContentService() {
        if (multilingualContentService == null) {
            multilingualContentService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getMultilingualContentService();
        }
        return multilingualContentService;
    }

    /**
     * @param multilingualContentService the multilingualContentService to set
     */
    public void setMultilingualContentService(
            final MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    /**
     * @return the businessRegistry
     */
    protected final BusinessRegistry getBusinessRegistry() {
        if (businessRegistry == null) {
            businessRegistry = Services.getBusinessRegistry(FacesContext.getCurrentInstance());
        }
        return businessRegistry;
    }

    /**
     * @param businessRegistry the businessRegistry to set
     */
    public final void setBusinessRegistry(final BusinessRegistry businessRegistry) {
        this.businessRegistry = businessRegistry;
    }

    /**
     * @return the securityService
     */
    protected final SecurityService getSecurityService() {
        if (securityService == null) {
            securityService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getSecurityService();
        }
        return securityService;
    }

    /**
     * @param securityService the securityService to set
     */
    public final void setSecurityService(final SecurityService securityService) {
        this.securityService = securityService;
    }

    protected String getServiceFromActionNode() {
        String result = "Library";
        if (actionNode.hasAspect(CircabcModel.ASPECT_LIBRARY)) {
            result = "Library";
        } else if (actionNode.hasAspect(CircabcModel.ASPECT_INFORMATION)) {
            result = "Information";
        } else if (actionNode.hasAspect(CircabcModel.ASPECT_NEWSGROUP)) {
            result = "Newsgroup";
        } else if (actionNode.hasAspect(CircabcModel.ASPECT_EVENT)) {
            result = "Events";
        }

        return result;
    }

    protected ThreadPoolExecutor getAsyncThreadPoolExecutor() {
        if (asyncThreadPoolExecutor == null) {
            asyncThreadPoolExecutor = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getAsyncThreadPoolExecutor();
        }
        return asyncThreadPoolExecutor;
    }

    @Override
    public String getFinishAsyncButtonLabel() {

        return getFinishButtonLabel() + " ...";
    }

    @Override
    public boolean isFinishAsyncButtonDisabled() {

        return false;
    }

    @Override
    public boolean isFinishAsyncButtonVisible() {

        return false;

    }

    @Override
    public String getMode() {
        return IECompatibilityPreference.IE_8;
    }

    private final class FinishAsyncRun implements Runnable {

        protected FacesContext context;
        protected String userName;


        public FinishAsyncRun(FacesContext context, String defaultOutcome,
                              String userName) {
            this.context = context;
            this.userName = userName;
        }


        @Override
        public void run() {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>() {
                public String doWork() {
                    RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
                    RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
                        public String execute() throws Throwable {

                            // call the actual implementation
                            return finishImpl(context, getDefaultFinishOutcome());

                        }
                    };
                    String ignoredOutcome = null;
                    try {
                        LogRecord logRecord = getLogRecord();
                        ignoredOutcome = txnHelper.doInTransaction(callback, false, true);
                        logRecord = getLogRecord();
                        logRecord.setOK(true);


                    } catch (Throwable e) {
                        logRecord.setOK(false);
                    }
                    getLogService().log(logRecord);
                    return ignoredOutcome;

                }
            }, userName);
        }

    }


}
