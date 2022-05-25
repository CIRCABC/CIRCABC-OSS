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
package eu.cec.digit.circabc.web.wai.part;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.admin.debug.ServerConfigurationService;
import eu.cec.digit.circabc.service.customisation.ApplicationCustomisationService;
import eu.cec.digit.circabc.service.search.autonomy.AutonomySearchService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.navigation.AspectResolver;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import eu.cec.digit.circabc.web.ui.repo.component.UIAutonomySearch;
import eu.cec.digit.circabc.web.wai.dialog.search.AutonomySearchResultDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Bean that back the left menu.
 *
 * @author yanick pignot
 */
public class LeftMenuBean implements Serializable {

    public static final String BEAN_NAME = "WaiLeftMenuBean";
    private static final long serialVersionUID = -7581124182630093270L;
    private static final Log logger = LogFactory.getLog(LeftMenuBean.class);
    private static final String MSG_EDIT_PROPERTIES_SPACE = "edit_space_action_title";
    private static final String MSG_EDIT_PROPERTIES_FORUM = "edit_discussion_action_title";
    private static final String MSG_EDIT_PROPERTIES_CIRCABC = "edit_circabc_action_title";
    private static final String MSG_EDIT_PROPERTIES_CATEGORY = "edit_category_action_title";
    private static final String MSG_EDIT_PROPERTIES_IGROOT = "edit_igroot_action_title";
    private static final String MSG_EDIT_PROPERTIES_LIBRARY = "edit_library_action_title";
    private static final String MSG_EDIT_PROPERTIES_NEWSGROUP = "edit_newsgroup_action_title";
    private static final String MSG_EDIT_PROPERTIES_SURVEY = "edit_survey_action_title";
    private static final String MSG_EDIT_PROPERTIES_INFORMATION = "edit_information_action_title";
    private static final String MSG_EDIT_PROPERTIES_EVENT = "edit_event_action_title";
    private static final String MSG_EDIT_PROPERTIES_HEADER = "edit_header_action_title";

    private static String hostName;
    private static String helpLink;
    private static String eLearningLink;
    private static String eLearningLinkTitle;
    private static Boolean eLearningEnabled;
    private static Boolean isDisplayServerInformationActive = Boolean.valueOf(CircabcConfiguration
            .getProperty(CircabcConfiguration.IS_DISPLAY_SERVER_INFORMATION_ACTIVATE));

    static {
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            hostName = "Cannot get computer name ";
        }
    }

    protected AutonomySearchService autonomySearchService = null;
    protected NodeService nodeService;
    protected ApplicationCustomisationService applicationCustomisationService;
    private CircabcNavigationBean navigator;
    private transient DictionaryService dictionaryService;
    private ServerConfigurationService serverConfigurationService;

    public void doNothingAction(ActionEvent event) {

    }

    public String getRevisionNumber() {
        String result = "";

        try {

            result = "Rev: " + serverConfigurationService
                    .getConfigurationFileResume(serverConfigurationService.getCircabcVersionProps())
                    .get("version.revision");

        } catch (FileNotFoundException e) {
            if (logger.isErrorEnabled()) {
                logger.error("File not found during getting revision number", e);
            }
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("IOException during getting revision number", e);
            }
        } catch (DocumentException e) {
            if (logger.isErrorEnabled()) {
                logger.error("DocumentException during getting revision number", e);
            }
        }

        return result;

    }

    public String getLibraryId() {
        return getIGNode().getLibrary().getId();
    }

    public String getInformationId() {
        return getIGNode().getInformation().getId();
    }

    public String getEventId() {
        return getIGNode().getEvent().getId();
    }

    public String getNewsgroupId() {
        return getIGNode().getNewsgroup().getId();
    }

    public String getSurveyId() {
        return getIGNode().getSurvey().getId();
    }


    /**
     * @return true if the menu must display le IG part of the menu
     */
    public boolean isInterestGroupDisplay() {
        return getIGNode() != null ;
    }

    /**
     * @return true if the current ig has a library service (and user has right to see it)
     */
    public boolean isLibraryDisplay() {
        return getIGNode() != null && getIGNode().getLibrary() != null;
    }

    /**
     * @return true if the current ig has a directory service (and user has right to see it)
     */
    public boolean isDirectoryDisplay() {
        return getIGNode() != null && getIGNode().getDirectory() != null;
    }

    /**
     * @return true if the current ig has a newsgroup service (and user has right to see it)
     */
    public boolean isNewsgroupDisplay() {
        return getIGNode() != null && getIGNode().getNewsgroup() != null;
    }

    /**
     * @return true if the current ig has a survey service (and user has right to see it)
     */
    public boolean isSurveyDisplay() {
        // disable the survey service in the menu
        return false;
    }

    /**
     * @return true if the current ig has a information service (and user has right to see it)
     */
    public boolean isInformationDisplay() {

        return getIGNode() != null && getIGNode().getInformation() != null;
    }

    /**
     * @return true if the current ig has a Event service (and user has right to see it)
     */
    public boolean isEventDisplay() {
        return getIGNode() != null && getIGNode().getEvent() != null;
    }

    /**
     * @return true if the current browsable node is a Library root or a children of it
     */
    public boolean isLibraryActivated() {
        final NavigableNode service = getNavigator().getCurrentIGService();
        return service != null && service.hasAspect(CircabcModel.ASPECT_LIBRARY);
        //return service != null && ((Boolean)service.get(IGServicesNode.IS_LIBRARY));
    }

    /**
     * @return true if the current browsable node is a newsgroup root or a children of it
     */
    public boolean isNewsgroupActivated() {
        final NavigableNode service = getNavigator().getCurrentIGService();
        final boolean result = service != null && service.hasAspect(CircabcModel.ASPECT_NEWSGROUP);
        if (result) {
            //get sure that NewsGroupBean is ready because left-menu.jsp uses NewsGroupBean.actions
            FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "NewsGroupBean");
        }
        return result;
    }

    /**
     * @return true if the current browsable node is a Survey root or a children of it
     */
    public boolean isSurveyActivated() {
        return false;
    }

    /**
     * @return true if the current browsable node is a Directory root or a children of it
     */
    public boolean isDirectoryActivated() {
        final NavigableNode service = getNavigator().getCurrentIGService();
        return service != null && !service.hasAspect(CircabcModel.ASPECT_SURVEY) && !service
                .hasAspect(CircabcModel.ASPECT_LIBRARY) && !service.hasAspect(CircabcModel.ASPECT_NEWSGROUP)
                && !service.hasAspect(CircabcModel.ASPECT_INFORMATION) && !service
                .hasAspect(CircabcModel.ASPECT_EVENT);
        //return service != null && ((Boolean)service.get("isDirectoryChild"));
    }

    /**
     * @return true if the current browsable node is a Information root or a children of it
     */
    public boolean isInformationActivated() {
        final NavigableNode service = getNavigator().getCurrentIGService();
        return service != null && service.hasAspect(CircabcModel.ASPECT_INFORMATION);
        //return service != null && ((Boolean)service.get("isInformationChild"));
    }

    /**
     * @return true if the current browsable node is a Event root or a children of it
     */
    public boolean isEventActivated() {
        final NavigableNode service = getNavigator().getCurrentIGService();
        return service != null && service.hasAspect(CircabcModel.ASPECT_EVENT);
        //return service != null && ((Boolean)service.get("isEventChild"));
    }

    /**
     * Util method that get a Label for Edit Space Properties action label differently according the
     * type of the current node.
     *
     * @return the right label for the edit space properties action
     */
    public String getEditPropertiesLabel() {
        final FacesContext fc = FacesContext.getCurrentInstance();
        String label = null;
        final Node actionNode = Beans.getWaiBrowseBean().getActionSpace();
        NavigableNodeType type;

        if (actionNode != null) {
            type = AspectResolver.resolveType(actionNode);
        } else {
            type = getNavigator().getCurrentNodeType();
        }

        if (type == null) {
            label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_SPACE);
        } else {
            switch (type) {
                case LIBRARY:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_LIBRARY);
                    break;
                case LIBRARY_FORUM:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_FORUM);
                    break;
                case NEWSGROUP:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_NEWSGROUP);
                    break;
                case INFORMATION:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_INFORMATION);
                    break;
                case EVENT:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_EVENT);
                    break;
                case SURVEY:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_SURVEY);
                    break;
                case IG_ROOT:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_IGROOT);
                    break;
                case CATEGORY:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_CATEGORY);
                    break;
                case CIRCABC_ROOT:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_CIRCABC);
                    break;
                case CATEGORY_HEADER:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_HEADER);
                    break;
                default:
                    label = Application.getMessage(fc, MSG_EDIT_PROPERTIES_SPACE);
            }
        }

        return label;
    }


    /**
     * Return if the left menu displays the administration Service Menu
     *
     * @return true if the current user is not a guest
     */
    public boolean isAdminView() {
        return getNavigator() != null && !getNavigator().getIsGuest();
    }

    /**
     * @return the isDisplayServerInformationActive
     */
    public boolean isDisplayServerInformationActive() {
        return isDisplayServerInformationActive;
    }

    /**
     * Helper that return the current ig node or null if the navigation is above it (Circabc, cat
     * header or category)
     */
    private InterestGroupNode getIGNode() {
        InterestGroupNode interestGroupNode = null;
        if (getNavigator() != null) {
            interestGroupNode = (InterestGroupNode) getNavigator().getCurrentIGRoot();
        }
        return interestGroupNode;
    }

    /**
     * For instance, the clipboard can only be displayed under the Library. The current node must be a
     * space.
     *
     * @return if the clipboard must be displayed or not.
     */
    public boolean isPastEnableHere() {
        try {
            final NavigableNode service = getNavigator().getCurrentIGService();

            if (service == null || (!(service.hasAspect(CircabcModel.ASPECT_LIBRARY)) && !(service
                    .hasAspect(CircabcModel.ASPECT_INFORMATION)))) {
                return false;
            } else {
                String userName = AuthenticationUtil.getRunAsUser();
                try {

                    AuthenticationUtil.setRunAsUserSystem();
                    final QName type = getNavigator().getCurrentNode().getType();

                    // make sure the type is defined in the data dictionary
                    final TypeDefinition typeDef = getDictionaryService().getType(type);

                    if (typeDef != null) {
                        return ContentModel.TYPE_FOLDER.equals(type) || getDictionaryService()
                                .isSubClass(type, ContentModel.TYPE_FOLDER);
                    } else {
                        return false;
                    }
                } finally {
                    AuthenticationUtil.setRunAsUser(userName);
                }
            }

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during call to isPastEnableHere", e);
            }
            return false;
        }
    }

    /**
     * Action called from the Autonomy Search component.
     */
    public void searchAutonomy(final ActionEvent event) {

        final UIComponent component = event.getComponent();

        UIAutonomySearch search = null;

        if (component instanceof UIAutonomySearch) {
            search = (UIAutonomySearch) component;
        } else {
            search = (UIAutonomySearch) component.getParent();
        }

        CircabcNavigationBean navigationBean = Beans.getWaiNavigator();

        navigationBean.setSearchContext(search.getSearchContext());

        final FacesContext fc = FacesContext.getCurrentInstance();

        if (search.getSearchContext() != null) {

            ((AutonomySearchResultDialog)
                    Beans.getBean(AutonomySearchResultDialog.BEAN_NAME)).reset();

            fc.getApplication().getNavigationHandler().handleNavigation(fc,
                    null, AutonomySearchResultDialog.WAI_DIALOG_CALL);
        }

        final ViewHandler viewHandler = fc.getApplication().getViewHandler();
        final UIViewRoot viewRoot = viewHandler.createView(fc,
                CircabcNavigationHandler.WAI_DIALOG_CONTAINER_PAGE);
        viewRoot.setViewId(CircabcNavigationHandler.WAI_DIALOG_CONTAINER_PAGE);
        fc.setViewRoot(viewRoot);
        fc.renderResponse();
    }

    public String getHelpLink() {

        NodeRef rootRef = Beans.getWaiNavigator().getCircabcHomeNode().getNodeRef();

        if (this.getNodeService().getProperty(rootRef, CircabcModel.PROP_HELP_LINK) != null) {
            LeftMenuBean.helpLink = this.getNodeService()
                    .getProperty(rootRef, CircabcModel.PROP_HELP_LINK).toString();
        }

        return LeftMenuBean.helpLink;
    }

    /**
     * Gets the value of the autonomyEnabled
     *
     * @return the autonomyEnabled
     */
    public boolean isAutonomyEnabled() {
        return autonomySearchService.isEnabled() && CircabcConfig.ENT;
    }

    /**
     * Sets the value of the autonomySearchService
     *
     * @param autonomySearchService the autonomySearchService to set.
     */
    public void setAutonomySearchService(AutonomySearchService autonomySearchService) {
        this.autonomySearchService = autonomySearchService;
    }

    /**
     * Return the hostname of the server
     */
    public String getHostName() {
        return hostName;
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
    public final void setNavigator(CircabcNavigationBean navigator) {
        this.navigator = navigator;
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
    public final void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @return the serverConfigurationService
     */
    public ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    /**
     * @param serverConfigurationService the serverConfigurationService to set
     */
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    /**
     * @return the nodeService
     */
    public NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the eLearningLink
     */
    public String geteLearningLink() {

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {
                if (applicationCustomisationService.geteLearningLink() != null) {
                    LeftMenuBean.eLearningLink = applicationCustomisationService.geteLearningLink();
                }

                return LeftMenuBean.eLearningLink;

            }
        }, AuthenticationUtil.getAdminUserName());

        return LeftMenuBean.eLearningLink;
    }

    /**
     * @return the eLearningEnabled
     */
    public Boolean geteLearningEnabled() {

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>() {
            public Boolean doWork() {
                if (applicationCustomisationService.getDisplayeLearningLink() != null) {
                    LeftMenuBean.eLearningEnabled = applicationCustomisationService.getDisplayeLearningLink();
                } else {
                    LeftMenuBean.eLearningEnabled = false;
                }
                return LeftMenuBean.eLearningEnabled;
            }
        }, AuthenticationUtil.getAdminUserName());

        return LeftMenuBean.eLearningEnabled;
    }

    /**
     * @return the eLearningLinkTitle
     */
    public String geteLearningLinkTitle() {

        return WebClientHelper
                .translate("welcome_page_elearning", CircabcConfiguration.getApplicationName());
    }

    /**
     * @return the applicationCustomisationService
     */
    public ApplicationCustomisationService getApplicationCustomisationService() {
        return applicationCustomisationService;
    }

    /**
     * @param applicationCustomisationService the applicationCustomisationService to set
     */
    public void setApplicationCustomisationService(
            ApplicationCustomisationService applicationCustomisationService) {
        this.applicationCustomisationService = applicationCustomisationService;
    }

}
