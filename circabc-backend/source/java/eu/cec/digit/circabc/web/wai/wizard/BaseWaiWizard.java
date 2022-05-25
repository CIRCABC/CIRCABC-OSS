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
package eu.cec.digit.circabc.web.wai.wizard;

import eu.cec.digit.circabc.business.api.BusinessRegistry;
import eu.cec.digit.circabc.repo.app.SecurityService;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.mail.MailService;
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
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * Base class for all WAI wizard beans providing common functionality
 *
 * @author yanick pignot
 */
public abstract class BaseWaiWizard extends BaseWizardBean implements WaiWizard {

    public static final String NODE_ID_PARAMETER = "id";
    public static final String ACTIVITY_PARAMETER = "activity";
    public static final String SERVICE_PARAMETER = "service";
    protected static final String MSG_ERR_INVALID_VALUE_FOR = "msg_err_invalid_value_for";
    private static final Log logger = LogFactory.getLog(BaseWaiWizard.class);
    private static final long serialVersionUID = -5821309036194548072L;
    private static final String ICON = "icon";
    protected LogRecord logRecord = new LogRecord();
    protected boolean isLoggingEnabled = true;
    //	Common beans and services
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
    private transient MailPreferencesService mailPreferencesService;
    private transient LogService logService;
    private transient BusinessRegistry businessRegistry;
    private CircabcBrowseBean browseBean;
    private CircabcNavigationBean navigator;
    private MapNode actionNode;
    private SecurityService securityService;

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null && parameters.get(NODE_ID_PARAMETER) != null) {
            final String id = parameters.get(NODE_ID_PARAMETER);
            try {
                actionNode = new MapNode(new NodeRef(Repository.getStoreRef(), id));

                // find it's type so we can see if it's a node we are interested in
                final QName type = actionNode.getType();

                // make sure the type is defined in the data dictionary
                final TypeDefinition typeDef = getDictionaryService().getType(type);

                if (typeDef != null) {
                    // look for Space or File nodes
                    if ((ContentModel.TYPE_FOLDER.equals(type) || getDictionaryService()
                            .isSubClass(type, ContentModel.TYPE_FOLDER)) &&
                            (ApplicationModel.TYPE_FOLDERLINK.equals(type) || getDictionaryService()
                                    .isSubClass(type, ApplicationModel.TYPE_FOLDERLINK))) {
                        getBrowseBean().setActionSpace(actionNode);
                        //resolve icon in-case one has not been set
                        actionNode.addPropertyResolver(ICON, getBrowseBean().resolverSpaceIcon);
                        getBrowseBean().setDocument(null);
                    } else {
                        getBrowseBean().setDocument(actionNode);
                        getBrowseBean().setActionSpace(null);
                    }
                }

            } catch (final InvalidNodeRefException refErr) {
                Utils.addErrorMessage(translate(Repository.ERROR_NODEREF, id));
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
            updateLogDocument(nodeRef);
        }

    }

    public void updateLogDocument(final NodeRef nodeRef) {

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
        final Path path = actionNode.getNodePath();
        logRecord.setPath(PathUtils.getCircabcPath(path, true));
    }

    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {
        if (logger.isDebugEnabled()) {
            logger.debug("outcome:" + outcome);
        }
        if (isLoggingEnabled) {
            logRecord.setOK(true);
            getLogService().log(logRecord);
        }
        return outcome;
    }

    protected String getErrorOutcome(final Throwable exception) {
        if (isLoggingEnabled) {
            logRecord.setOK(false);
            getLogService().log(logRecord);
        }
        return null;
    }

    /**
     * @return the initialized Node at the init time (with id parameter)
     */
    protected Node getActionNode() {
        return actionNode;
    }


    public ActionsListWrapper getActionList() {
        // the most of wizards don't need an action list on the left menu
        return null;
    }

    public boolean isCancelButtonVisible() {
        // the most of wizards don't need a cancel button
        return true;
    }

    protected String translate(final String key, final Object... params) {
        return WebClientHelper.translate(key, params);
    }

    protected String getBestTitle(final Node node) {
        final String name = node.getName();
        final String title = (String) node.getProperties().get(ContentModel.PROP_TITLE.toString());

        if (title == null || title.trim().length() < 1) {
            return name;
        } else {
            return title;
        }
    }

    public boolean isFormProvided() {
        return false;
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
     * @param managementService the ManagementService to set
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
     * @return the UserService
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
}
