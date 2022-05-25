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
package eu.cec.digit.circabc.web.wai.wizard.mail;

import eu.cec.digit.circabc.action.evaluator.AnyIgServicesAdminEvaluator;
import eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.user.LdapLimitExceededException;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.ProfileUtils;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.AbstractMailToUsersBean;
import eu.cec.digit.circabc.web.wai.dialog.content.edit.CreateContentBaseDialog.AttachementWrapper;
import eu.cec.digit.circabc.web.wai.dialog.notification.NotificationUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.*;

/**
 * Bean that backs the "Send to All Members" WAI Dialog.
 *
 * @author Stephane Clinckart
 */
public class MailToMembersWizard extends AbstractMailToUsersBean {

    protected static final String USER_SPECIFIED_TWICE = "user_specified_twice";
    private static final String ERROR_DURING_QUERY_EXCECUTION = "error_during_query_excecution";
    private static final String PLEASE_AFFINE_YOUR_SEARCH_TO_MUCH_RESULTS = "please_affine_your_search_to_much_results";
    private static final long serialVersionUID = -6045607817876349903L;
    private static final Log logger = LogFactory.getLog(MailToMembersWizard.class);
    /**
     * I18N message strings
     */
    private static final String MESSAGE_ID_MAIL_TO_MEMBERS_ERROR = "mail_to_members_process_failed";
    private static final String MSG_MAIL_SUCCESS = "mail_to_members_success";
    private static final String USER_NOT_SELECTED = "user_not_selected";
    private static final String LOGIN_REGEX = "<PROFILE>";
    private static final String USERNAME_REGEX = "<NAME>";

    private static final String ALL_CIRCABC_ADMIN_VALUE = "__ALL_CIRCABC_ADMIN_";
    private static final String ALL_CIRCABC_ADMIN_MSG = "mail_to_all_circabc_admin";

    private static final String ALL_CATEGORY_ADMINS_VALUE = "__ALL_CATEGORY_ADMINS_";
    private static final String ALL_CATEGORY_ADMINS_MSG = "mail_to_members_all_cat_admins";

    private static final String ALL_CATEGORY_ADMIN_VALUE = "__ALL_CATEGORY_ADMIN_";
    private static final String ALL_CATEGORY_ADMIN_MSG = "mail_to_category_admin";

    private static final String ALL_IG_LEADERS = "__ALL_IG_LEADERS";
    private static final String ALL_IG_LEADERS_MSG = "mail_to_members_all_ig_leaders";


    private static final Map<String, String> CIRCABC_SELECTION = new HashMap<>(1);
    private static final Map<String, String> CATEGORY_SELECTION = new HashMap<>(1);

    private static final Map<String, String> IG_ROOT_SELECTION = new HashMap<>(5);
    /**
     * Index of the USERS search filter index
     */
    private static final int USERS_IDX = 0;
    /**
     * Index of the PROFILES search filter index
     */
    private static final int PROFILES_IDX = 1;
    private static final String STEP_NOTIFY = "mail-to-members-step2";

    static {

        CIRCABC_SELECTION.put(ALL_CIRCABC_ADMIN_VALUE, ALL_CIRCABC_ADMIN_MSG);

        CATEGORY_SELECTION.put(ALL_CATEGORY_ADMIN_VALUE, ALL_CATEGORY_ADMIN_MSG);

        IG_ROOT_SELECTION.put(ALL_IG_LEADERS, ALL_IG_LEADERS_MSG);
    }

    /**
     * list of user/group profile wrapper objects
     */
    public transient final Set<SelectedUser> selectedMembers = new HashSet<>();
    private final Class<?>[] CALLBACK_METHOD_ARGS = {Map.class, String.class};
    /**
     * data model for table of profiles for users
     */
    public transient DataModel selectedUsersDataModel = null;
    public Boolean onlineMembersOnly = Boolean.FALSE;
    private int realSearchResult = 0;

    private NodeRef attachLink;
    private List<AttachementWrapper> attachedLinks;
    private String rootIg;

    private Boolean insideIg;

    /**
     * Initializes the wizard
     */
    public void init(final Map<String, String> params) {
        super.init(params);
        this.selectedMembers.clear();
        setSubject("");
        setBody("");
        this.selectedUsersDataModel = null;
        this.onlineMembersOnly = Boolean.FALSE;
        this.attachedLinks = new ArrayList<>();

        final NavigableNode root = (NavigableNode) getNavigator().getCurrentIGRoot();
        rootIg = root == null ? null : root.getId();

        insideIg = this.getActionNode().hasAspect(CircabcModel.ASPECT_IGROOT);
    }

    /**
     * @see org.alfresco.web.bean.wizard.AbstractWizardBean#finish()
     */
    protected final String finishImpl(final FacesContext context, final String outcome)
            throws Exception {
        try {
            if (validateMailData()) {
                final int nbOfUsers = mailToMembers(context, getActionNode().getNodeRef());

                UIContextService.getInstance(context).notifyBeans();

                logRecord.setInfo("Send to " + nbOfUsers + " users");

                if (logger.isDebugEnabled()) {
                    logger.debug("outcome=" + outcome);
                }

                Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_MAIL_SUCCESS, nbOfUsers));

                return outcome;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("subject or body invalid");
                }

                isFinished = false;
                return null;
            }

        } catch (final Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error("Invitation error", e);
            }
            Utils.addErrorMessage(translate(MESSAGE_ID_MAIL_TO_MEMBERS_ERROR, e.getMessage()), e);

            return null;
        }
    }

    @Override
    public boolean getFinishButtonDisabled() {
        boolean disabled = true;

        String stepName = Application.getWizardManager().getCurrentStepName();
        if (STEP_NOTIFY.equals(stepName)) {
            disabled = false;
        }

        return disabled;
    }

    protected int mailToMembers(final FacesContext context, final NodeRef nodeRef) {
        final User user = Application.getCurrentUser(context);
        String from = (String) getNodeService().getProperty(user.getPerson(), ContentModel.PROP_EMAIL);

        if (from == null || from.length() == 0) {
            // if the user does not have an email address get the default
            // one from the config service
            from = Application.getClientConfig(context).getFromEmailAddress();
        }

        final Set<NodeRef> persons = new HashSet<>(this.selectedMembers.size());

        final NodeRef currentNode = getActionNode().getNodeRef();
        final ProfileManagerService profileService = getProfileManagerServiceFactory()
                .getProfileManagerService(currentNode);
        final NavigationBusinessSrv navigationBusinessSrv = getBusinessRegistry()
                .getNavigationBusinessSrv();

        for (final SelectedUser selectedMember : this.selectedMembers) {
            final String authority = selectedMember.getAuthority();

            if (getPersonService().personExists(authority) == true) {
                persons.add(getPersonService().getPerson(authority));
            } else if (authority.equals(ALL_CIRCABC_ADMIN_VALUE)) {
                if (NavigableNodeType.CATEGORY.isNodeFromType(getActionNode()) == false) {
                    throw new IllegalAccessError(
                            "Mail to ALL category admins is allowed only under a category node.");
                }

                final CircabcRootProfileManagerService circabcRootProfileService = getProfileManagerServiceFactory()
                        .getCircabcRootProfileManagerService();

                final NodeRef circabcNodeRef = navigationBusinessSrv.getCircabcRoot();

                final Profile circabcAdmin = circabcRootProfileService
                        .getProfile(circabcNodeRef, CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN);
                addPersonsInProfile(persons, circabcNodeRef, circabcRootProfileService,
                        circabcAdmin.getPrefixedAlfrescoGroupName());
            } else if (authority.equals(ALL_CATEGORY_ADMINS_VALUE)) {
                if (NavigableNodeType.CIRCABC_ROOT.isNodeFromType(getActionNode()) == false) {
                    throw new IllegalAccessError(
                            "Mail to ALL category admins is allowed only under circabc root.");
                }

                final CategoryProfileManagerService catProfileService = getProfileManagerServiceFactory()
                        .getCategoryProfileManagerService();
                for (final NodeRef category : navigationBusinessSrv.getCategories()) {
                    final Profile catAdmin = catProfileService
                            .getProfile(category, CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN);
                    addPersonsInProfile(persons, category, catProfileService,
                            catAdmin.getPrefixedAlfrescoGroupName());
                }
            } else {
                addPersonsInProfile(persons, currentNode, profileService, authority);
            }
        }

        for (final NodeRef person : persons) {
            final Map<String, Object> extraModelParams = new HashMap<>();
            final Map<String, String> extraBodyParams = new HashMap<>();
            final Map<QName, Serializable> personProperties = getNodeService().getProperties(person);

            final String fullName = getUserService()
                    .getUserFullName((String) personProperties.get(ContentModel.PROP_USERNAME));

            final String login = PermissionUtils.computeUserLogin(personProperties);

            extraBodyParams.put(USERNAME_REGEX, fullName);
            extraBodyParams.put(LOGIN_REGEX, login);
            extraModelParams.put(AbstractMailToUsersBean.ATTACHED_FILES, attachedLinks);

            mailToUser(person, nodeRef, from, extraModelParams, extraBodyParams);
        }

        return persons.size();
    }

    /**
     * @param persons
     * @param currentNode
     * @param profileService
     * @param authority
     */
    private void addPersonsInProfile(final Set<NodeRef> persons, final NodeRef currentNode,
                                     final ProfileManagerService profileService, final String authority) {
        final Profile profile = profileService.getProfileFromGroup(currentNode, authority);
        for (final String userId : profileService
                .getPersonInProfile(currentNode, profile.getProfileName())) {
            persons.add(getPersonService().getPerson(userId));
        }
    }

    public SelectItem[] getFilters() {

        return new SelectItem[]
                {
                        new SelectItem("" + USERS_IDX,
                                NotificationUtils.translateAuthorityType(AuthorityType.USER))
                        , new SelectItem("" + PROFILES_IDX,
                        NotificationUtils.translateAuthorityType(AuthorityType.GROUP))
                };
    }

    public Boolean getOnlineMembersOnly() {
        return this.onlineMembersOnly;
    }

    /**
     * Query callback method executed by the Generic Picker component. This method is part of the
     * contract to the Generic Picker, it is up to the backing bean to execute whatever query is
     * appropriate and return the results.
     *
     * @param filterIndex Index of the filter drop-down selection
     * @param contains    Text from the contains textbox
     * @return An array of SelectItem objects containing the results to display in the picker.
     */
    public SelectItem[] pickerSendMailToMembersCallback(final int filterIndex,
                                                        final String contains) {
        final FacesContext fc = FacesContext.getCurrentInstance();

        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(fc);
        final RetryingTransactionCallback<List<? extends SelectItem>> callback = new RetryingTransactionCallback<List<? extends SelectItem>>() {
            public List<? extends SelectItem> execute() throws Throwable {
                final List<SortableSelectItem> items = new ArrayList<>();

                final List<String> alreadyInvitedAuthorities;

                if (selectedMembers == null || selectedMembers.size() < 1) {
                    alreadyInvitedAuthorities = Collections.emptyList();
                } else {
                    alreadyInvitedAuthorities = new ArrayList<>(selectedMembers.size());
                    for (final SelectedUser sel : selectedMembers) {
                        alreadyInvitedAuthorities.add(sel.authority);
                    }
                }

                if (filterIndex == PROFILES_IDX) {
                    if (NavigableNodeType.CIRCABC_ROOT.isNodeFromType(getActionNode())) {
                        //Add Profiles
                        items.addAll(ProfileUtils
                                .buildMailableProfileItems(getActionNode(), contains, alreadyInvitedAuthorities,
                                        logger));

                        //Add category Admin's
                        final String allAdmins = translate(ALL_CATEGORY_ADMINS_MSG);
                        items.add(new SortableSelectItem(ALL_CATEGORY_ADMINS_VALUE, allAdmins, allAdmins));
                    } else if (NavigableNodeType.CATEGORY.isNodeFromType(getActionNode())) {
                        //Add circabc Admin's
                        final String allAdmins = translate(ALL_CIRCABC_ADMIN_MSG);
                        items.add(new SortableSelectItem(ALL_CIRCABC_ADMIN_VALUE, allAdmins, allAdmins));

                        //Add category profiles
                        items.addAll(ProfileUtils
                                .buildMailableProfileItems(getActionNode(), contains, alreadyInvitedAuthorities,
                                        logger));

                        //Add ig Admin's
                        for (final Map.Entry<String, String> entry : IG_ROOT_SELECTION.entrySet()) {
                            final String profileDesc = translate(entry.getValue());
                            items.add(new SortableSelectItem(entry.getKey(), profileDesc, profileDesc));
                        }
                    } else if (NavigableNodeType.IG_ROOT.isNodeFromType(getActionNode())) {
                        //Add category Admin's if current user is an admin in at least one service
                        final AnyIgServicesAdminEvaluator isAdmin = new AnyIgServicesAdminEvaluator();
                        if (isAdmin.evaluate(getActionNode())) {
                            final String allAdmins = translate(ALL_CATEGORY_ADMINS_MSG);
                            items.add(new SortableSelectItem(ALL_CATEGORY_ADMIN_VALUE, allAdmins, allAdmins));
                        }
                        //Add profiles
                        items.addAll(ProfileUtils
                                .buildMailableProfileItems(getActionNode(), contains, alreadyInvitedAuthorities,
                                        logger));
                    } else {
                        //Add profiles
                        items.addAll(ProfileUtils
                                .buildMailableProfileItems(getActionNode(), contains, alreadyInvitedAuthorities,
                                        logger));
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("The Profile search is performed successfully and return " + items
                                + ". Filter Index: " + filterIndex + ". Expression: " + contains + ".");
                    }
                } else if (filterIndex == USERS_IDX) {
                    if (NavigableNodeType.CIRCABC_ROOT.isNodeFromType(getActionNode())) {
                        items.addAll(PermissionUtils
                                .buildCircabcUserItems(getActionNode(), contains, getOnlineMembersOnly(),
                                        alreadyInvitedAuthorities, logger));
                    } else if (NavigableNodeType.CATEGORY.isNodeFromType(getActionNode())) {
                        items.addAll(PermissionUtils
                                .buildCategoryUserItems(getActionNode(), contains, getOnlineMembersOnly(),
                                        alreadyInvitedAuthorities, logger));
                    } else if (NavigableNodeType.IG_ROOT.isNodeFromType(getActionNode())) {
                        items.addAll(PermissionUtils
                                .buildInvitedUserItemsRecursivly(getActionNode(), contains, getOnlineMembersOnly(),
                                        alreadyInvitedAuthorities, logger));
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "The User search is performed successfully and return " + items + ". Filter Index: "
                                        + filterIndex + ". Expression: " + contains + ".");
                    }
                } else {
                    logger.error("The picker is called with an invalid index parameter " + filterIndex
                            + ". This last is not taken in account yet.");
                }
                return items.size() != 0 ? items : Collections.<SortableSelectItem>emptyList();
            }
        };

        List<? extends SelectItem> result = null;
        try {
            result = (List<? extends SelectItem>) AuthenticationUtil
                    .runAs(new AuthenticationUtil.RunAsWork<Object>() {
                        public List<? extends SelectItem> doWork() {
                            return txnHelper.doInTransaction(callback, false, true);
                        }
                    }, AuthenticationUtil.getSystemUserName());
        } catch (final LdapLimitExceededException ldap) {
            if (logger.isErrorEnabled()) {
                logger.error("Error LimitExceeded during query execution.", ldap);
            }
            Utils.addErrorMessage(Application.getMessage(fc, PLEASE_AFFINE_YOUR_SEARCH_TO_MUCH_RESULTS),
                    ldap);
        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during query execution.", e);
            }
            Utils.addErrorMessage(Application.getMessage(fc, ERROR_DURING_QUERY_EXCECUTION), e);
        }
        if (result == null) {
            result = Collections.<SelectItem>emptyList();
        }
        return result.toArray(new SelectItem[result.size()]);
    }

    /**
     * Returns the properties for current user-roles JSF DataModel
     *
     * @return JSF DataModel representing the current user-roles
     */
    public DataModel getSelectedUsersDataModel() {
        if (this.selectedUsersDataModel == null) {
            this.selectedUsersDataModel = new ListDataModel();
        }

        this.selectedUsersDataModel.setWrappedData(new ArrayList<>(this.selectedMembers));

        return this.selectedUsersDataModel;
    }


    public String getBrowserTitle() {
        return translate("mail_to_members_browser_title");
    }

    public String getPageIconAltText() {
        return translate("mail_to_members_icon_tooltip");
    }

    /**
     * @return the max number of user that can be returned by the query
     */
    public int getMaxSearchResult() {
        return PermissionUtils.MAX_ELEMENTS_IN_LIST;
    }

    /**
     * @return the real number of entries returned by the user search query
     */
    public boolean isSearchResultLimitExceeded() {
        return this.realSearchResult > PermissionUtils.MAX_ELEMENTS_IN_LIST;
    }


    @Override
    public boolean getNextButtonDisabled() {
        return this.selectedMembers.isEmpty();
    }


    /**
     * Action handler called when the Add button is pressed to process the current selection
     */
    public void addSelection(final ActionEvent event) {
        final UIGenericPicker picker = (UIGenericPicker) event.getComponent().findComponent("picker");
        // test error

        final String[] results = picker.getSelectedResults();
        if (results == null) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(translate(USER_NOT_SELECTED)));

            return;
        }

        NodeRef rootNode = null;

        final NavigableNodeType type = getNavigator().getCurrentNodeType();

        if (type == null) {
            throw new IllegalStateException(
                    "Impossible to perform a Circabc invitation in an non Circabc node.");
        }

        if (type.isStrictlyUnder(NavigableNodeType.IG_ROOT)) {
            rootNode = getNavigator().getCurrentIGRoot().getNodeRef();
        } else {
            rootNode = getNavigator().getCurrentNode().getNodeRef();
        }

        final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                .getProfileManagerService(rootNode);

        String authority;
        String userName;
        boolean foundExisting = false;
        SelectedUser wrapper;
        final StringBuilder label = new StringBuilder(64);
        AuthorityType authType;
        CircabcUserDataBean user;
        CircabcUserDataBean ldapUserDetail;

        final Set<String> onlineUsers = getUserService().getAllOnlineUsers();

        for (String result : results) {
            authority = result;
            userName = "";

            // only add if authority not already present in the list with same CircaRole
            foundExisting = false;
            for (SelectedUser selectedMember : this.selectedMembers) {
                wrapper = selectedMember;
                if (authority.equals(wrapper.getAuthority())) {
                    foundExisting = true;
                    break;
                }
            }

            // if found existing then user has to
            if (foundExisting == false) {
                label.setLength(0);

                // build a display label showing the user and their profile for the space
                authType = AuthorityType.getAuthorityType(authority);

                if (authority.equals(ALL_CIRCABC_ADMIN_VALUE)) {
                    AuthenticationUtil.runAs(new RunAsWork<Object>() {
                        public Object doWork() throws Exception {
                            final NodeRef circabcNodeRef = getNavigationBusinessSrv().getCircabcRoot();
                            final Set<String> circabcInvitedUsers = getProfileManagerServiceFactory()
                                    .getCircabcRootProfileManagerService().getInvitedUsers(circabcNodeRef);
                            for (final String circabcInvitedUser : circabcInvitedUsers) {
                                addUserToSet(onlineUsers, circabcInvitedUser);
                            }
                            return null;
                        }
                    }, AuthenticationUtil.getAdminUserName());
                    /****/
                } else if (authority.equals(ALL_CATEGORY_ADMINS_VALUE)) {
                    AuthenticationUtil.runAs(new RunAsWork<Object>() {
                        public Object doWork() throws Exception {
                            for (final NodeRef category : getNavigationBusinessSrv().getCategories()) {
                                final Set<String> categoryInvitedUsers = getProfileManagerServiceFactory()
                                        .getCategoryProfileManagerService().getInvitedUsers(category);
                                for (final String categoryInvitedUser : categoryInvitedUsers) {
                                    addUserToSet(onlineUsers, categoryInvitedUser);
                                }
                            }
                            return null;
                        }
                    }, AuthenticationUtil.getAdminUserName());
                } else if (authority.equals(ALL_CATEGORY_ADMIN_VALUE)) {
                    AuthenticationUtil.runAs(new RunAsWork<Object>() {
                        public Object doWork() throws Exception {
                            final Set<String> categoryInvitedUsers = getProfileManagerServiceFactory()
                                    .getCategoryProfileManagerService()
                                    .getInvitedUsers(getNavigator().getCurrentCategory().getNodeRef());
                            for (final String categoryInvitedUser : categoryInvitedUsers) {
                                addUserToSet(onlineUsers, categoryInvitedUser);
                            }
                            return null;
                        }
                    }, AuthenticationUtil.getAdminUserName());
                } else if (authority.equals(ALL_IG_LEADERS)) {
                    final NodeRef categoryRoot = getNavigator().getCurrentNode().getNodeRef();
                    for (final NodeRef ig : getNavigationBusinessSrv().getInterestGroups(categoryRoot)) {
                        final List<Profile> igProfiles = getProfileManagerServiceFactory()
                                .getIGRootProfileManagerService().getProfiles(ig);
                        for (final Profile profile : igProfiles) {
                            if (profile.isAdmin()) {
                                Set<String> containedAuthorities = Collections.emptySet();
                                if (getAuthorityService().authorityExists(profile.getPrefixedAlfrescoGroupName())) {
                                    containedAuthorities = getAuthorityService()
                                            .getContainedAuthorities(AuthorityType.USER,
                                                    profile.getPrefixedAlfrescoGroupName(), true);
                                } else {
                                    if (logger.isErrorEnabled()) {
                                        logger.error(
                                                "Authority does not exists: " + profile.getPrefixedAlfrescoGroupName());
                                    }
                                }

                                for (final String igUser : containedAuthorities) {
                                    addUserToSet(onlineUsers, igUser);
                                }
                            }
                        }
                    }

                } else if (IG_ROOT_SELECTION.containsKey(authority)) {
                    userName = authority;
                    label.append(translate(IG_ROOT_SELECTION.get(authority)));
                } else if (authType == AuthorityType.GUEST || getPersonService().personExists(authority)) {

                    // clean user id
                    if (authority.contains("@")) {
                        authority = authority.substring(0, authority.indexOf('@'));
                    }
                    userName = authority;

                    if (!getPersonService().personExists(userName)) {
                        user = new CircabcUserDataBean();
                        user.setUserName(userName);

                        ldapUserDetail = getUserService().getLDAPUserDataByUid(authority);
                        user.copyLdapProperties(ldapUserDetail);
                        user.setHomeSpaceNodeRef(getManagementService().getGuestHomeNodeRef());
                        getUserService().createUser(user, false);
                    }
                    addUserToSet(onlineUsers, userName);
                } else {
                    userName = authority;

                    final Profile profileFromGroup = profileManagerService
                            .getProfileFromGroup(rootNode, authority);
                    // get the the name of the group
                    label.append(profileFromGroup.getProfileDisplayName());
                    addUserToSet(onlineUsers, userName, label);
                }
            } else
            // foundExisting = true
            {
                FacesContext.getCurrentInstance()
                        .addMessage(null, new FacesMessage(translate(USER_SPECIFIED_TWICE)));
            }
        }
    }

    private StringBuilder getLabelFromUser(final String userName) {
        // found a User authority
        final NodeRef ref = getPersonService().getPerson(userName);
        final String firstName = (String) getNodeService()
                .getProperty(ref, ContentModel.PROP_FIRSTNAME);
        final String lastName = (String) getNodeService().getProperty(ref, ContentModel.PROP_LASTNAME);
        final StringBuilder label = new StringBuilder();
        label.append(firstName).append(" ").append(lastName != null ? lastName : "");
        return label;
    }

    private void addUserToSet(final Set<String> onlineUsers, final String userName) {
        addUserToSet(onlineUsers, userName, getLabelFromUser(userName));
    }

    private void addUserToSet(final Set<String> onlineUsers, final String userName,
                              final StringBuilder label) {
        if (this.onlineMembersOnly) {
            if (onlineUsers.contains(userName)) {
                this.selectedMembers.add(new SelectedUser(userName, label.toString()));
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("User:" + userName + " is not online");
                }
            }
        } else {
            this.selectedMembers.add(new SelectedUser(userName, label.toString()));
        }
    }

    /**
     * Action handler called when the Remove button is pressed to remove a user+Circarole
     */
    public void removeSelection(final ActionEvent event) {
        final SelectedUser wrapper = (SelectedUser) this.selectedUsersDataModel.getRowData();
        if (wrapper != null) {
            this.selectedMembers.remove(wrapper);
        }
    }

    public Class<?>[] getCallBackMethodArgs() {
        return CALLBACK_METHOD_ARGS;
    }

    @Override
    public final String getBuildTextMessage() {

        return "true";
    }

    @Override
    protected Map<String, Object> getDisplayModelToAdd() {
        return Collections.emptyMap();
    }

    @Override
    protected MailTemplate getMailTemplateDefinition() {
        return MailTemplate.SEND_TO_MEMBERS;
    }

    public boolean isOnlineMembersOnly() {
        return onlineMembersOnly;
    }

    public void setOnlineMembersOnly(boolean onlineMembersOnly) {
        this.onlineMembersOnly = onlineMembersOnly;
    }

    public void revertMembersOnlineOnly(final ActionEvent event) {
        //this.onlineMembersOnly = !this.onlineMembersOnly;
    }

    /**
     * @return the attachLink
     */
    public NodeRef getAttachLink() {
        return attachLink;
    }

    /**
     * @param attachLink the attachLink to set
     */
    public void setAttachLink(NodeRef attachLink) {

        if (attachLink != null) {
            this.attachLink = attachLink;

            if (!linkAlreadyAdded(attachedLinks, attachLink)) {
                AttachementWrapper aw = new AttachementWrapper(attachLink,
                        getNodeService().getProperty(attachLink, ContentModel.PROP_NAME).toString(), true);

                attachedLinks.add(aw);


            }
        }

        this.attachLink = null;

    }

    private boolean linkAlreadyAdded(List<AttachementWrapper> attachedLinks2,
                                     NodeRef attachLink2) {
        Boolean result = false;

        for (AttachementWrapper aw : attachedLinks2) {
            if (aw.getAttachRef().getId().equals(attachLink2.getId())) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * @return the rootIg
     */
    public String getRootIg() {
        return rootIg;
    }

    /**
     * @param rootIg the rootIg to set
     */
    public void setRootIg(String rootIg) {
        this.rootIg = rootIg;
    }

    /**
     * @return the attachedLinks
     */
    public List<AttachementWrapper> getAttachedLinks() {
        return attachedLinks;
    }

    /**
     * @param attachedLinks the attachedLinks to set
     */
    public void setAttachedLinks(List<AttachementWrapper> attachedLinks) {
        this.attachedLinks = attachedLinks;
    }

    public void removeAttachement(ActionEvent event) {

        UIActionLink ual = (UIActionLink) event.getComponent();
        String removingNode = (String) ual.getParameterMap().get("removingNoderef");

        if (removingNode != null) {
            Iterator<AttachementWrapper> i = this.attachedLinks.iterator();

            while (i.hasNext()) {
                AttachementWrapper n = i.next();
                if (n.getAttachRef().getId().equals(removingNode)) {
                    i.remove();
                    break;
                }

            }
        }

    }

    public Boolean getInsideIg() {
        return insideIg;
    }

    /**
     * @param insideIg the insideIg to set
     */
    public void setInsideIg(Boolean insideIg) {
        this.insideIg = insideIg;
    }

    /**
     * Simple wrapper class to represent a user selected combination
     */
    public static class SelectedUser {

        private String authority;

        private String label;

        public SelectedUser(final String authority, final String label) {
            this.authority = authority;
            this.label = label;
        }

        public String getAuthority() {
            return this.authority;
        }

        public String getLabel() {
            return this.label;
        }

        public String toString() {
            return authority;
        }

        public boolean equals(final Object anObject) {
            if (anObject == null || !(anObject instanceof SelectedUser)) {
                return false;
            }
            final SelectedUser anotherObject = (SelectedUser) anObject;
            if (this.authority != null && this.authority.equals(anotherObject.getAuthority())) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.authority.hashCode();
        }
    }
}
