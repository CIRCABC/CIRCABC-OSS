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
package eu.cec.digit.circabc.web.wai.wizard.struct;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.ProfileException;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeadersBeanData;
import eu.cec.digit.circabc.web.wai.dialog.generic.LightDescriptionSizeExceedException;
import eu.cec.digit.circabc.web.wai.wizard.users.InviteCircabcUsersWizard;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.ResourceBundleWrapper;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.alfresco.web.ui.common.component.UIListItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Migration 3.1 -> 3.4.6 - 02/12/2011 Config was moved to Spring. ConfigElement was moved to
 * Spring. This class seems to be developed for CircaBC
 */
public class CreateCircabcNodesWizard extends InviteCircabcUsersWizard {

    public static final String DEFAULT_SPACE_ICON_PATH = "";
    public static final String DEFAULT_SPACE_TYPE_ICON_PATH = "/images/icons/space.gif";
    public static final boolean CREATE_LIBRARY_WHEN_IGROOT_CREATED = true;
    public static final boolean CREATE_NEWSGROUP_WHEN_IGROOT_CREATED = true;
    public static final boolean CREATE_SURVEY_WHEN_IGROOT_CREATED = false;
    public static final boolean CREATE_DIRECTORY_WHEN_IGROOT_CREATED = true;
    public static final boolean CREATE_INFORMATION_WHEN_IGROOT_CREATED = true;
    public static final boolean CREATE_EVENT_WHEN_IGROOT_CREATED = true;
    private static final String LIGHT_DESCRIPTION_LIMIT_EXCEED = "lightDescription_limit_500";
    /**
     * The logger
     */
    private static final Log logger = LogFactory.getLog(CreateCircabcNodesWizard.class);
    private static final long serialVersionUID = 1634570905315116894L;
    /**
     * generic error message for the create circabc wizard
     */
    private static final String MESSAGE_ID_CREATE_CIRCABC_ERROR = "create_new_circabc_error";
    /**
     * generic error message for the create category wizard
     */
    private static final String MESSAGE_ID_CREATE_CATEGORY_ERROR = "create_new_category_error";
    /**
     * generic error message for the create interest group wizard
     */
    private static final String MESSAGE_ID_CREATE_IG_ERROR = "create_new_interest_group_error";

    private static final String MESSAGE_ID_DUPLICATE_NAME = "create_node_duplicate_name";
    private static final String MESSAGE_ID_INVALID_NAME = "create_node_invalid_name";
    private static final String MESSAGE_ID_INVALID_NAME_TOO_BIG = "create_node_invalid_name_too_big";
    private static final String MESSAGE_ID_INVALID_NAME_CHARACTERS = "create_node_invalid_name_characters";

    private static final String ALFRESCO_NAME_REGEX = "(.*[\\\\\"\\\\*\\\\\\\\\\\\>\\\\<\\\\?\\\\/\\\\:\\\\|]+.*)|(.*[\\\\.]?.*[\\\\.]+$)|(.*[ ]+$)";

    /**
     * message 'select admin' for the create circabc wizard
     */
    private static final String MESSAGE_ID_CREATE_CIRCABC_SELECTED_USERS = "create_circabc_wizard_selected_admin";
    /**
     * message 'select admin' for the create category wizard
     */
    private static final String MESSAGE_ID_CREATE_CATEGORY_SELECTED_USERS = "create_category_wizard_selected_admin";
    /**
     * message 'select admin' for the create interest group wizard
     */
    private static final String MESSAGE_ID_CREATE_IG_SELECTED_USERS = "create_interest_group_wizard_selected_admin";

    /**
     * The profile name for the circabc admin
     */
    private static final String CIRCABC_ADMIN_PROFILE = CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN;
    /**
     * The profile name for the category admin
     */
    private static final String CATEGORY_ADMIN_PROFILE = CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN;
    /**
     * The profile name for the interest group leader
     */
    private static final String IG_LEADER_PROFILE = IGRootProfileManagerService.Profiles.IGLEADER;
    private List<String> languages;
    transient private NodeService internalNodeService;
    /**
     * Remember the type of node to create
     */
    private NodeType toCreateNodeType;
    private String newsGroupTitle;
    private String newsGroupIcon;
    private String newsGroupDescription;
    private String libraryTitle;
    private String libraryDescription;
    private String libraryIcon;
    private String surveyTitle;
    private String surveyDescription;
    private String surveyIcon;
    private String informationTitle;
    private String informationDescription;
    private String informationIcon;
    private String eventTitle;
    private String eventDescription;
    private String eventIcon;
    private SelectItem[] domainFilters;
    private SelectItem[] profileFilters;
    private String name;
    private String title;
    private String icon;
    private String description;
    private String lightDescription;
    private Boolean isInterestGroupToCreate;
    private String contact;
    private String categoryHeaderId;
    private Pattern pattern = null;
    /**
     * true if category header is setted by the user
     */
    private boolean categoryHeaderIdSetted;
    /**
     * The I18N bundle
     */
    transient private ResourceBundle bundle;
    private CircabcService circabcService;
    private NodeRef igRootNodeRef;
    private NodeRef catNodeRef;

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    /**
     * Initialises the wizard
     */
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        // reset values specifc to the current node
        this.icon = null;
        this.name = null;
        this.title = null;
        this.contact = null;
        this.description = null;
        this.categoryHeaderId = null;
        this.lightDescription = null;
        this.igRootNodeRef = null;
        this.catNodeRef = null;

        this.categoryHeaderIdSetted = false;
        if (pattern == null) {
            pattern = Pattern.compile(ALFRESCO_NAME_REGEX);
        }

        newsGroupTitle = CircabcConfiguration
                .getProperty(CircabcConfiguration.NEWSGROUP_NODE_TITLE_PROPERTIES);
        newsGroupDescription = CircabcConfiguration
                .getProperty(CircabcConfiguration.NEWSGROUP_NODE_DESCRIPTION_PROPERTIES);
        newsGroupIcon = ManagementService.DEFAULT_NEWSGROUP_ICON_NAME;

        libraryTitle = CircabcConfiguration
                .getProperty(CircabcConfiguration.LIBRARY_NODE_TITLE_PROPERTIES);
        libraryDescription = CircabcConfiguration
                .getProperty(CircabcConfiguration.LIBRARY_NODE_DESCRIPTION_PROPERTIES);
        libraryIcon = ManagementService.DEFAULT_LIBRARY_ICON_NAME;

        surveyTitle = CircabcConfiguration
                .getProperty(CircabcConfiguration.SURVEY_NODE_TITLE_PROPERTIES);
        surveyDescription = CircabcConfiguration
                .getProperty(CircabcConfiguration.SURVEY_NODE_DESCRIPTION_PROPERTIES);
        surveyIcon = ManagementService.DEFAULT_SURVEY_ICON_NAME;

        informationTitle = CircabcConfiguration
                .getProperty(CircabcConfiguration.INFORMATION_NODE_TITLE_PROPERTIES);
        informationDescription = CircabcConfiguration
                .getProperty(CircabcConfiguration.INFORMATION_NODE_DESCRIPTION_PROPERTIES);
        informationIcon = ManagementService.DEFAULT_INFORMATION_ICON_NAME;

        eventTitle = CircabcConfiguration.getProperty(CircabcConfiguration.EVENT_NODE_TITLE_PROPERTIES);
        eventDescription = CircabcConfiguration
                .getProperty(CircabcConfiguration.EVENT_NODE_DESCRIPTION_PROPERTIES);
        eventIcon = ManagementService.DEFAULT_EVENT_ICON_NAME;

        setBundle(Application.getBundle(FacesContext.getCurrentInstance()));

        final Set<QName> aspects = getActionNode().getAspects();
        final QName type = getActionNode().getType();

        // if the current node is a category
        if (aspects.contains(CircabcModel.ASPECT_CATEGORY)) {  // create a new interest group
            toCreateNodeType = NodeType.IGROOT;
        }
        // if the current node is a circabc node
        else if (aspects.contains(CircabcModel.ASPECT_CIRCABC_ROOT)) {  // create a newcategory node
            toCreateNodeType = NodeType.CATEGORY;
        }
        //if the current node is a category Header node
        else if (type.equals(ContentModel.TYPE_CATEGORY)) {  // create a newcategory node
            toCreateNodeType = NodeType.CATEGORY;
            this.categoryHeaderId = getActionNode().getNodeRefAsString();
        }
        // if the current node is the company home
        else if (getActionNode().getNodeRef().equals(getManagementService().getCompanyHomeNodeRef())) {
            // create a circabc node
            toCreateNodeType = NodeType.CIRCABC;

            // set the default values
            name = CircabcConfiguration
                    .getProperty(CircabcConfiguration.CIRCABC_ROOT_NODE_NAME_PROPERTIES);
            title = CircabcConfiguration
                    .getProperty(CircabcConfiguration.CIRCABC_ROOT_NODE_TITLE_PROPERTIES);
            description = CircabcConfiguration
                    .getProperty(CircabcConfiguration.CIRCABC_ROOT_NODE_DESCRIPTION_PROPERTIES);
        } else {
            toCreateNodeType = null;
        }

        this.isInterestGroupToCreate = NodeType.IGROOT.equals(this.toCreateNodeType);
    }

    // ------------------------------------------------------------------------------
    // Wizard implementation

    /**
     * @see org.alfresco.web.bean.wizard.AbstractWizardBean#finish()
     */
    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
            public String execute() throws Throwable {
                try {
                    String newOutcome;
                    try {
                        // create the right circabc node according the type to create
                        switch (toCreateNodeType) {
                            // Create the Circabc root node
                            case CIRCABC:
                                createCircabc(context);
                                break;
                            // Create the Category root node
                            case CATEGORY:
                                createCategory(context);
                                break;
                            // Create the Interest Group
                            case IGROOT:
                                createIGRoot(context);
                                break;
                        }

                    } catch (LightDescriptionSizeExceedException e) {
                        Utils.addErrorMessage(translate(LIGHT_DESCRIPTION_LIMIT_EXCEED));

                        return null;
                    } catch (final Throwable e) {
                        // rollback the transaction
                        Utils.addErrorMessage(
                                getErrorMessage(),
                                e);

                        switch (toCreateNodeType) {
                            case CIRCABC:
                                if (logger.isErrorEnabled()) {
                                    logger.error("Impossible to create a Circabc root folder named " + getName()
                                            + " under the space " + getActionNode().getName(), e);
                                }
                                break;
                            case CATEGORY:
                                if (logger.isErrorEnabled()) {
                                    logger.error("Impossible to create a Category named " + getName()
                                            + " under the Circabc root node " + getActionNode().getName(), e);
                                }
                                break;
                            case IGROOT:
                                if (logger.isErrorEnabled()) {
                                    logger.error("Impossible to create an Interest group named " + getName()
                                            + " under the Category " + getActionNode().getName(), e);
                                }
                                break;
                        }
                    }

                    // invalidate the current view to view the niew space
                    getBrowseBean().contextUpdated();

                    if (toCreateNodeType == NodeType.CIRCABC) {
                        // only one step for instance for the create CIRCABC node
                        newOutcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
                    } else {
                        newOutcome = AlfrescoNavigationHandler.CLOSE_WIZARD_OUTCOME;
                    }

                    UIContextService.getInstance(context).notifyBeans();

                    if (logger.isDebugEnabled()) {
                        logger.debug("outcome=" + outcome + " new outcome=" + newOutcome);
                    }
                    return newOutcome;
                } catch (final ProfileException pe) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Profile:" + pe.getProfileName() + " Explanation:" + pe.getExplanation(),
                                pe);
                    }
                    Utils.addErrorMessage(translate(MESSAGE_ID_INVITATION_ERROR, pe.getExplanation()), pe);
                    return null;
                } catch (final Throwable e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Invitation error", e);
                    }
                    Utils.addErrorMessage(translate(MESSAGE_ID_INVITATION_ERROR, e.getMessage()), e);
                    return null;
                }
            }

        };

        return txnHelper.doInTransaction(callback, false);
    }

    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {
        super.doPostCommitProcessing(context, outcome);
        if (getCircabcService().syncEnabled()) {
            if (igRootNodeRef != null) {
                getCircabcService().addInterestGroupNode(catNodeRef, igRootNodeRef);
            }
        }
        return outcome;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.wizard.BaseWizardBean#next()
     */
    @Override
    public String next() {
        //  get the current step
        final int step = Application.getWizardManager().getState().getCurrentStep();

        // we can't create two nodes with the same name
        // in the same folder.
        if (step == 2) {
            this.name = StringUtils.strip(this.name);
            if (this.name == null || this.name.length() < 1) {
                FacesContext.getCurrentInstance().addMessage(
                        null, new FacesMessage(getBundle().getString(MESSAGE_ID_INVALID_NAME)));

                //stay in the current step
                Application.getWizardManager().getState().setCurrentStep(step - 1);

            } else if (this.name.length() > 42) {
                FacesContext.getCurrentInstance().addMessage(
                        null, new FacesMessage(getBundle().getString(MESSAGE_ID_INVALID_NAME_TOO_BIG)));

                //stay in the current step
                Application.getWizardManager().getState().setCurrentStep(step - 1);
            } else if (isNotValidAlfrescoName()) {
                FacesContext.getCurrentInstance().addMessage(
                        null, new FacesMessage(getBundle().getString(MESSAGE_ID_INVALID_NAME_CHARACTERS)));

                //stay in the current step
                Application.getWizardManager().getState().setCurrentStep(step - 1);
            } else if (getNodeService()
                    .getChildByName(getActionNode().getNodeRef(), ContentModel.ASSOC_CONTAINS, this.name)
                    != null) {
                FacesContext.getCurrentInstance().addMessage(
                        null, new FacesMessage(getBundle().getString(MESSAGE_ID_DUPLICATE_NAME)));

                //stay in the current step
                Application.getWizardManager().getState().setCurrentStep(step - 1);
            }

            //means we are in category creation wizard
            if (getNodeService().hasAspect(getActionNode().getNodeRef(), CircabcModel.ASPECT_CATEGORY)) {
                if (this.lightDescription.length()
                        > LightDescriptionSizeExceedException.MAX_CHARACTER_LIMIT) {
                    FacesContext.getCurrentInstance().addMessage(
                            null, new FacesMessage(getBundle().getString(LIGHT_DESCRIPTION_LIMIT_EXCEED)));
                    Application.getWizardManager().getState().setCurrentStep(1);
                }
            }

        }

        return super.next();
    }

    private boolean isNotValidAlfrescoName() {
        Matcher matcher = pattern.matcher(this.name);
        return matcher.matches();
    }

    protected void createCircabc(final FacesContext context) {
        final NodeRef circabcNodeRef = getManagementService()
                .createCircabc(getActionNode().getNodeRef());

        //	apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, this.icon);
        uiFacetsProps.put(ContentModel.PROP_TITLE, this.title == null ? "" : this.title);
        uiFacetsProps
                .put(ContentModel.PROP_DESCRIPTION, this.description == null ? "" : this.description);

        getNodeService().addAspect(circabcNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        // to uncomment if the wizard should take in account invitation at this moment
        //grantUsers(context, circabcNodeRef);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Circabc root folder '" + getName() + "' created under the " + getActionNode().getName()
                            + "\n  Title: " + getTitle()
                            + "\n  Description: " + getDescription()
                    //+ "\n  Circabc Admin(s):" + super.userGroupProfiles
            );
        }
    }

    protected void createCategory(final FacesContext context) {
        final NodeRef headerNodeRef = new NodeRef(categoryHeaderId);
        final NodeRef categoryNodeRef = getManagementService().createCategory(
                getNavigator().getCircabcHomeNode().getNodeRef(),
                getName(),
                headerNodeRef
        );

        //	apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, this.icon);
        uiFacetsProps.put(ContentModel.PROP_TITLE,
                getSecurityService().getCleanHTML(
                        (this.title == null || this.title.trim().length() < 1) ? getName() : this.title,
                        false));
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION,
                getSecurityService().getCleanHTML(this.description == null ? "" : this.description, true));
        getNodeService().addAspect(categoryNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        if (getCircabcService().syncEnabled()) {
            getCircabcService().addCategoryNode(headerNodeRef, categoryNodeRef);
        }

        grantUsers(context, categoryNodeRef);

        //reset cache
        final CategoryHeadersBeanData categoryHeadersBeanData = (CategoryHeadersBeanData) Beans
                .getBean("CategoryHeadersBeanData");
        categoryHeadersBeanData.reset();
        if (logger.isDebugEnabled()) {
            logger.debug("Category '" + getName() + "' created under " + getActionNode().getName()
                    + "\n  Title: " + getTitle()
                    + "\n  Description: " + getDescription()
                    + "\n  Category Header: " + categoryHeaderId
                    + "\n  Category Admin(s):" + super.userGroupProfiles
            );
        }
    }

    protected void createIGRoot(final FacesContext context) {
        igRootNodeRef = getManagementService().createIGRoot(
                this.getActionNode().getNodeRef(), getName(), getContact());
        catNodeRef = this.getActionNode().getNodeRef();
        {
            //	apply the uifacets aspect - icon, title and description props
            final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
            uiFacetsProps.put(ApplicationModel.PROP_ICON, this.icon);
            uiFacetsProps.put(ContentModel.PROP_TITLE,
                    getSecurityService().getCleanHTML(
                            (this.title == null || this.title.trim().length() < 1) ? getName() : this.title,
                            false));
            uiFacetsProps.put(ContentModel.PROP_DESCRIPTION,
                    getSecurityService()
                            .getCleanHTML(this.description == null ? "" : this.description, true));

            String finalValue = Jsoup.parse(this.lightDescription).text();

            if (finalValue.length() > LightDescriptionSizeExceedException.MAX_CHARACTER_LIMIT) {
                throw new LightDescriptionSizeExceedException(translate(LIGHT_DESCRIPTION_LIMIT_EXCEED));
            }

            uiFacetsProps.put(CircabcModel.PROP_LIGHT_DESCRIPTION, finalValue == null ? "" : finalValue);

            getNodeService().addAspect(igRootNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        }

        // create the library
        if (CREATE_LIBRARY_WHEN_IGROOT_CREATED) {
            createLibrary(igRootNodeRef);
        }

        if (CREATE_NEWSGROUP_WHEN_IGROOT_CREATED) {
            createNewsGroup(igRootNodeRef);
        }

        if (CREATE_SURVEY_WHEN_IGROOT_CREATED) {
            createSurvey(igRootNodeRef);
        }

        if (CREATE_DIRECTORY_WHEN_IGROOT_CREATED) {
            createDirectory(igRootNodeRef);
        }

        if (CREATE_INFORMATION_WHEN_IGROOT_CREATED) {
            createInformation(igRootNodeRef);
        }

        if (CREATE_EVENT_WHEN_IGROOT_CREATED) {
            createEvent(igRootNodeRef);
        }

        grantUsers(context, igRootNodeRef);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Interest group '" + getName() + "' created under the Category " + getActionNode()
                            .getName()
                            + "\n  Title: " + getTitle()
                            + "\n  Description: " + getDescription()
                            + "\n  Contact information: " + getContact()
                            + "\n  Leader(s):" + super.userGroupProfiles
            );
        }
    }

    protected NodeRef createLibrary(final NodeRef igRootNodeRef) {
        final NodeRef lib = getManagementService().createLibrary(igRootNodeRef);

        //	apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, this.libraryIcon);

        MLText titleValue = new MLText();

        for (String lang : languages) {

            ResourceBundle bundle = ResourceBundleWrapper
                    .getResourceBundle("alfresco.messages.webclient", new Locale(lang.toLowerCase()));
            titleValue.addValue(new Locale(lang), bundle.getString("library_menu"));

        }

        uiFacetsProps.put(ContentModel.PROP_TITLE, titleValue);
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.libraryDescription);
        getNodeService().addAspect(lib, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Library created under " + getNodeService()
                    .getProperty(igRootNodeRef, ContentModel.PROP_NAME)
                    + "\n  Title: " + this.libraryTitle
                    + "\n  Description: " + this.libraryDescription
            );
        }

        return lib;
    }

    protected NodeRef createNewsGroup(final NodeRef igRootNodeRef) {
        final NodeRef circabcNewsGroupNodeRef = getManagementService().createNewsGroup(igRootNodeRef);
        //apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, this.newsGroupIcon);

        MLText titleValue = new MLText();

        for (String lang : languages) {

            ResourceBundle bundle = ResourceBundleWrapper
                    .getResourceBundle("alfresco.messages.webclient", new Locale(lang.toLowerCase()));
            titleValue.addValue(new Locale(lang), bundle.getString("newsgroup_menu"));

        }

        uiFacetsProps.put(ContentModel.PROP_TITLE, titleValue);
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.newsGroupDescription);
        getNodeService()
                .addAspect(circabcNewsGroupNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        if (logger.isDebugEnabled()) {
            logger.debug("NewsGroup created under " + getNodeService()
                    .getProperty(igRootNodeRef, ContentModel.PROP_NAME)
                    + "\n  Title: " + this.newsGroupTitle
                    + "\n  Description: " + this.newsGroupDescription
            );
        }

        return circabcNewsGroupNodeRef;
    }

    protected NodeRef createSurvey(final NodeRef igRootNodeRef) {
        final NodeRef survey = getManagementService().createSurvey(igRootNodeRef);

        //	apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, this.surveyIcon);
        uiFacetsProps.put(ContentModel.PROP_TITLE, this.surveyTitle);
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.surveyDescription);
        getNodeService().addAspect(survey, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Survey created under " + getNodeService()
                    .getProperty(igRootNodeRef, ContentModel.PROP_NAME)
                    + "\n  Title: " + this.surveyTitle
                    + "\n  Description: " + this.surveyDescription
            );
        }

        // reset permission cache on the action node
        getActionNode().reset();
        // reset permission cache on all navigation node
        getNavigator().updateCircabcNavigationContext();

        return survey;
    }

    protected NodeRef createInformation(final NodeRef igRootNodeRef) {
        final NodeRef inf = getManagementService().createInformationService(igRootNodeRef);

        //	apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, this.informationIcon);

        MLText titleValue = new MLText();

        for (String lang : languages) {

            ResourceBundle bundle = ResourceBundleWrapper
                    .getResourceBundle("alfresco.messages.webclient", new Locale(lang.toLowerCase()));
            titleValue.addValue(new Locale(lang), bundle.getString("information_menu"));

        }

        uiFacetsProps.put(ContentModel.PROP_TITLE, titleValue);
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.informationDescription);
        getNodeService().addAspect(inf, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Information created under " + getNodeService()
                    .getProperty(igRootNodeRef, ContentModel.PROP_NAME)
                    + "\n  Title: " + this.informationTitle
                    + "\n  Description: " + this.informationDescription
            );
        }

        return inf;
    }

    protected NodeRef createEvent(final NodeRef igRootNodeRef) {
        final NodeRef event = getManagementService().createEventService(igRootNodeRef);

        //	apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, this.eventIcon);

        MLText titleValue = new MLText();

        for (String lang : languages) {

            ResourceBundle bundle = ResourceBundleWrapper
                    .getResourceBundle("alfresco.messages.webclient", new Locale(lang.toLowerCase()));
            titleValue.addValue(new Locale(lang), bundle.getString("event_menu"));

        }
        uiFacetsProps.put(ContentModel.PROP_TITLE, titleValue);
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.eventDescription);
        getNodeService().addAspect(event, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Event created under " + getNodeService()
                    .getProperty(igRootNodeRef, ContentModel.PROP_NAME)
                    + "\n  Title: " + this.eventTitle
                    + "\n  Description: " + this.eventDescription
            );
        }

        return event;
    }

    @Override
    public String getPageIconAltText() {
        switch (toCreateNodeType) {
            case CIRCABC:
                return translate("create_circabc_wizard_title");
            case CATEGORY:
                return translate("create_category_wizard_icon_tooltip");
            default:
                return translate("create_interest_group_wizard_icon_tooltip");
        }

    }

    @Override
    public String getBrowserTitle() {
        switch (toCreateNodeType) {
            case CIRCABC:
                return translate("create_circabc_wizard_title");
            case CATEGORY:
                return translate("create_category_wizard_browser_title");
            default: /// IGROOT
                return translate("create_interest_group_wizard_browser_title");
        }
    }

    protected NodeRef createDirectory(final NodeRef igRootNodeRef) {
        final NodeRef directory = getManagementService().createDirectory(igRootNodeRef);

        if (logger.isDebugEnabled()) {
            logger.debug("Directory created under "
                    + getNodeService().getProperty(igRootNodeRef, ContentModel.PROP_NAME));
        }

        return directory;
    }

    /**
     * @return the select admin message according the type of node to create
     */
    public String getSelectedUserMessage() {
        String msgId = null;

        switch (toCreateNodeType) {
            case CIRCABC:
                msgId = MESSAGE_ID_CREATE_CIRCABC_SELECTED_USERS;
                break;
            case CATEGORY:
                msgId = MESSAGE_ID_CREATE_CATEGORY_SELECTED_USERS;
                break;
            case IGROOT:
                msgId = MESSAGE_ID_CREATE_IG_SELECTED_USERS;
                break;
        }

        return getBundle().getString(msgId);
    }

    /**
     * @return the error according the type of node to create
     */
    public String getErrorMessage() {
        String msgId = null;

        switch (toCreateNodeType) {
            case CIRCABC:
                msgId = MESSAGE_ID_CREATE_CIRCABC_ERROR;
                break;
            case CATEGORY:
                msgId = MESSAGE_ID_CREATE_CATEGORY_ERROR;
                break;
            case IGROOT:
                msgId = MESSAGE_ID_CREATE_IG_ERROR;
                break;
        }

        return getBundle().getString(msgId);
    }

    /**
     * @return true if the properties of the space must be displayed as read only
     */
    public boolean isPropertiesReadOnly() {
        // set read only the field 'name', 'title' and 'descritpion' if the created node is the circabc root folder
        return toCreateNodeType == NodeType.CIRCABC;
    }

    /**
     * @return true if the user can set the contact information property
     */
    public boolean isContactInformationDisplayed() {
        // Contact Information only for the IGRoot
        return toCreateNodeType == NodeType.IGROOT;
    }

    /**
     * Util metho which construct a data model with rows passed in parameter
     */
    public DataModel getCategoryListDataModel() {
        // get the available categories
        final List<NodeRef> categories = getManagementService().getExistingCategoryHeaders();

        // construct the list of wrappers of categories
        final List<CategoryWrapper> categoriesWrapper = new ArrayList<>(categories.size());
        for (final NodeRef ref : categories) {
            // select the first element by default
            if (this.categoryHeaderId == null || this.categoryHeaderId.length() < 1) {
                this.categoryHeaderId = ref.toString();
            }

            final Serializable name = getNodeService().getProperty(ref, ContentModel.PROP_NAME);
            final Serializable desc = getNodeService().getProperty(ref, ContentModel.PROP_DESCRIPTION);

            categoriesWrapper.add(new CategoryWrapper(ref,
                    name == null ? "" : name.toString(),
                    desc == null ? "" : desc.toString()));
        }

        // construct the data model
        final DataModel dataModel = new ListDataModel();
        dataModel.setWrappedData(categoriesWrapper);

        return dataModel;
    }

    /**
     * Action handler called when the Add button is pressed to process the current selection
     */
    public void addSelectedUsers(final ActionEvent event) {
        final UIGenericPicker picker = (UIGenericPicker) event.getComponent().findComponent("picker");

        final String[] results = picker.getSelectedResults();
        if (results != null) {
            String profile = null;

            switch (toCreateNodeType) {
                case CIRCABC:
                    profile = CIRCABC_ADMIN_PROFILE;
                    break;
                case CATEGORY:
                    profile = CATEGORY_ADMIN_PROFILE;
                    break;
                case IGROOT:
                    profile = IG_LEADER_PROFILE;
                    break;
            }

            if (profile == null) {
                logger.error(
                        "The application is in a illegal state. Impossible to determine which kind of administrator must be setted for the creation of such a node. The createion of the special space '"
                                + getName() + "'failed.");

                throw new IllegalStateException(
                        "Impossible to determine which kind of administrator must be setted for the creation of such a node");
            }

            // invite all selected users
            for (final String authority : results) {
                String userName = "";

                // only add if authority not already present in the list with same CircaRole
                boolean foundExisting = false;
                for (final UserGroupProfile wrapper : this.userGroupProfiles) {
                    if (authority.equals(wrapper.getAuthority())) {
                        foundExisting = true;
                        break;
                    }
                }

                // if found existing then user has to
                if (foundExisting == false) {
                    final StringBuilder label = new StringBuilder(64);

                    // build a display label showing the user and their profile for the space
                    final AuthorityType authType = AuthorityType.getAuthorityType(authority);

                    if (authType == AuthorityType.GUEST || authType == AuthorityType.USER) {
                        userName = authority;

                        if (!getPersonService().personExists(userName)) {
                            getUserService().createLdapUser(userName,true);
                        }

                        // found a User authority
                        final NodeRef ref = getPersonService().getPerson(userName);
                        final String firstName = (String) getNodeService()
                                .getProperty(ref, ContentModel.PROP_FIRSTNAME);
                        final String lastName = (String) getNodeService()
                                .getProperty(ref, ContentModel.PROP_LASTNAME);

                        label
                                .append(firstName)
                                .append(" ")
                                .append(lastName != null ? lastName : "")
                                .append(" (")
                                .append(getBundle().getString(profile))
                                .append(")"
                                );

                    }

                    this.userGroupProfiles.add(new UserGroupProfile(userName, profile, label.toString()));
                } else
                // foundExisting = true
                {
                    FacesContext.getCurrentInstance().addMessage(
                            null, new FacesMessage(
                                    getBundle().getString(InviteCircabcUsersWizard.USER_SPECIFIED_TWICE)));
                }
            }
        }
    }

    @Override
    public boolean getFinishButtonDisabled() {
        if (toCreateNodeType == NodeType.CIRCABC) {
            // only one step for instance for the create CIRCABC node
            return false;
        } else {
            return super.getFinishButtonDisabled();
        }
    }

    public boolean getNextButtonDisabled() {
        final boolean requiredPropertiesSet = name != null && name.length() > 0;

        final int currentStep = Application.getWizardManager().getState().getCurrentStep();

        if (currentStep == 1) {
            return false;
        }

        // for the category, category heder should be setted
        if (toCreateNodeType == NodeType.CATEGORY) {

            if (currentStep == 2) {
                //the value of the second step of the create category (Select Category Header)  is always setted.
                return false;
            } else {
                return (!requiredPropertiesSet) ? true
                        : ((!categoryHeaderIdSetted) ? false : userGroupProfiles.isEmpty());
            }
        } else {
            return (!requiredPropertiesSet) ? true : userGroupProfiles.isEmpty();
        }


    }

    public SelectItem[] getDomainFilters() {
        if (domainFilters == null) {
            domainFilters = PermissionUtils.getDomainFilters(true, false, false);
        }
        return domainFilters;
    }

    public SelectItem[] getProfileFilters() {
        if (profileFilters == null) {
            profileFilters = PermissionUtils.getDomainFilters(true, false, false);
        }
        return profileFilters;
    }

    @Override
    protected String getDefaultCancelOutcome() {
        return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
    }

    @Override
    protected String getDefaultFinishOutcome() {
        return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
    }

    /**
     * @return the contact
     */
    public String getContact() {
        return contact;
    }

    /**
     * @param contact the contact to set
     */
    public void setContact(final String contact) {
        if (contact != null && !(contact.length() == 0)) {
            Whitelist basicWhitelist = new Whitelist();
            basicWhitelist.addTags("p", "span", "strong", "b", "i", "u", "br", "sub", "sup", "a");
            basicWhitelist.addAttributes(":all", "style");
            this.contact = Jsoup.clean(contact, basicWhitelist);
        } else {
            this.contact = contact;
        }
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        if (description != null && !(description.length() == 0)) {
            Whitelist basicWhitelist = new Whitelist();
            basicWhitelist.addTags("p", "span", "strong", "b", "i", "u", "br", "sub", "sup", "a");
            basicWhitelist.addAttributes(":all", "style");
            this.description = Jsoup.clean(description, basicWhitelist);
        } else {
            this.description = description;
        }
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(final String icon) {
        this.icon = icon;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @return the categoryHeader
     */
    public String getCategoryHeaderId() {
        return categoryHeaderId;
    }

    /**
     * @param categoryHeaderId the categoryHeader to set
     */
    public void setCategoryHeaderId(final String categoryHeaderId) {
        // only the selected category is not set as null
        if (categoryHeaderId != null) {
            this.categoryHeaderIdSetted = true;
            this.categoryHeaderId = categoryHeaderId;
        }
    }

    /**
     * Returns a list of icons to allow the user to select from. The list can change according to the
     * type of space being created.
     *
     * @return A list of icons
     */
    @SuppressWarnings("unchecked")
    public List<UIListItem> getIcons() {
        // NOTE: we can't cache this list as it depends on the space type
        //       which the user can change during the advanced space wizard

        List<UIListItem> icons = null;
        final List<String> iconNames = new ArrayList<>(8);

        final QName type = ContentModel.TYPE_FOLDER;
        final String typePrefixForm = type.toPrefixString(getNamespaceService());

        final Config config = Application.getConfigService(FacesContext.getCurrentInstance()).
                getConfig(typePrefixForm + " icons");
        if (config != null) {
            final ConfigElement iconsCfg = config.getConfigElement("icons");
            if (iconsCfg != null) {
                boolean first = true;
                for (final ConfigElement icon : iconsCfg.getChildren()) {
                    final String iconName = icon.getAttribute("name");
                    final String iconPath = icon.getAttribute("path");

                    if (iconName != null && iconPath != null) {
                        if (first) {
                            // if this is the first icon create the list and make
                            // the first icon in the list the default

                            icons = new ArrayList<>(iconsCfg.getChildCount());
                            if (this.icon == null) {
                                // set the default if it is not already
                                this.icon = iconName;
                            }
                            first = false;
                        }

                        final UIListItem item = new UIListItem();
                        item.setValue(iconName);
                        item.setImage(iconPath);
                        icons.add(item);
                        iconNames.add(iconName);
                    }
                }
            }
        }

        // if we didn't find any icons display one default choice
        if (icons == null) {
            icons = new ArrayList<>(1);
            this.icon = ManagementService.DEFAULT_SPACE_ICON_NAME;

            final UIListItem item = new UIListItem();
            item.setValue(ManagementService.DEFAULT_SPACE_ICON_NAME);
            item.setImage("/images/icons/space-icon-default.gif");
            icons.add(item);
            iconNames.add(ManagementService.DEFAULT_SPACE_ICON_NAME);
        }

        // make sure the current value for the icon is valid for the
        // current list of icons about to be displayed
        if (iconNames.contains(this.icon) == false) {
            this.icon = iconNames.get(0);
        }

        return icons;
    }

    protected final NodeService getInternalNodeService() {
        if (internalNodeService == null) {
            internalNodeService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNonSecuredNodeService();
        }
        return this.internalNodeService;
    }

    public final void setInternalNodeService(final NodeService internalNodeService) {
        this.internalNodeService = internalNodeService;
    }

    /**
     * @return the libraryDescription
     */
    public String getLibraryDescription() {
        return libraryDescription;
    }

    /**
     * @param libraryDescription the libraryDescription to set
     */
    public void setLibraryDescription(final String libraryDescription) {
        this.libraryDescription = libraryDescription;
    }

    /**
     * @return the libraryIcon
     */
    public String getLibraryIcon() {
        return libraryIcon;
    }

    /**
     * @param libraryIcon the libraryIcon to set
     */
    public void setLibraryIcon(final String libraryIcon) {
        this.libraryIcon = libraryIcon;
    }

    /**
     * @return the libraryTitle
     */
    public String getLibraryTitle() {
        return libraryTitle;
    }

    /**
     * @param libraryTitle the libraryTitle to set
     */
    public void setLibraryTitle(final String libraryTitle) {
        this.libraryTitle = libraryTitle;
    }

    /**
     * @return the newsGroupDescription
     */
    public String getNewsGroupDescription() {
        return newsGroupDescription;
    }

    /**
     * @param newsGroupDescription the newsGroupDescription to set
     */
    public void setNewsGroupDescription(final String newsGroupDescription) {
        this.newsGroupDescription = newsGroupDescription;
    }

    /**
     * @return the newsGroupIcon
     */
    public String getNewsGroupIcon() {
        return newsGroupIcon;
    }

    /**
     * @param newsGroupIcon the newsGroupIcon to set
     */
    public void setNewsGroupIcon(final String newsGroupIcon) {
        this.newsGroupIcon = newsGroupIcon;
    }

    /**
     * @return the newsGroupTitle
     */
    public String getNewsGroupTitle() {
        return newsGroupTitle;
    }

    /**
     * @param newsGroupTitle the newsGroupTitle to set
     */
    public void setNewsGroupTitle(final String newsGroupTitle) {
        this.newsGroupTitle = newsGroupTitle;
    }

    /**
     * @return the surveyDescription
     */
    public String getSurveyDescription() {
        return surveyDescription;
    }

    /**
     * @param surveyDescription the surveyDescription to set
     */
    public void setSurveyDescription(final String surveyDescription) {
        this.surveyDescription = surveyDescription;
    }

    /**
     * @return the surveyIcon
     */
    public String getSurveyIcon() {
        return surveyIcon;
    }

    /**
     * @param surveyIcon the surveyIcon to set
     */
    public void setSurveyIcon(final String surveyIcon) {
        this.surveyIcon = surveyIcon;
    }

    /**
     * @return the surveyTitle
     */
    public String getSurveyTitle() {
        return surveyTitle;
    }

    /**
     * @param surveyTitle the surveyTitle to set
     */
    public void setSurveyTitle(final String surveyTitle) {
        this.surveyTitle = surveyTitle;
    }

    /**
     * @return the bundle
     */
    protected ResourceBundle getBundle() {
        if (bundle == null) {
            setBundle(Application.getBundle(FacesContext.getCurrentInstance()));
        }
        return bundle;
    }

    /**
     * @param bundle the bundle to set
     */
    protected void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * @return the lightDescription
     */
    public String getLightDescription() {
        return lightDescription;
    }

    /**
     * @param lightDescription the lightDescription to set
     */
    public void setLightDescription(String lightDescription) {
        this.lightDescription = lightDescription;
    }

    /**
     * @return the isInterestGroupToCreate
     */
    public Boolean getIsInterestGroupToCreate() {
        return isInterestGroupToCreate;
    }

    /**
     * @param isInterestGroupToCreate the isInterestGroupToCreate to set
     */
    public void setIsInterestGroupToCreate(Boolean isInterestGroupToCreate) {
        this.isInterestGroupToCreate = isInterestGroupToCreate;
    }

    public CircabcService getCircabcService() {
        if (circabcService == null) {
            circabcService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getCircabcService();
        }
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }

    /**
     * Enum used internally to detemine wich node to create
     */
    private enum NodeType {
        CIRCABC,
        CATEGORY,
        IGROOT
    }

    /**
     * Simple wrapper class to represent a a category
     */
    public static class CategoryWrapper {

        private String name;
        private String description;
        private NodeRef nodeRef;

        public CategoryWrapper(final NodeRef nodeRef, final String name, final String description) {
            super();
            this.name = name;
            this.nodeRef = nodeRef;
            this.description = description;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * @return the nodeRef
         */
        public NodeRef getNodeRef() {
            return nodeRef;
        }

        /**
         * @param nodeRef the nodeRef to set
         */
        public void setNodeRef(final NodeRef nodeRef) {
            this.nodeRef = nodeRef;
        }

        /**
         * @return the noderef to string
         */
        public String getId() {
            return this.nodeRef.toString();
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param description the description to set
         */
        public void setDescription(final String description) {
            this.description = description;
        }
    }
}
