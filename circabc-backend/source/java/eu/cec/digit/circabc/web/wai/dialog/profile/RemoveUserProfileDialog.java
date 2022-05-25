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
package eu.cec.digit.circabc.web.wai.dialog.profile;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.iam.SynchronizationService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileException;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import io.swagger.api.HistoryApi;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bean that backs remove user profile dialog (for interest group and category).
 *
 * @author Yanick Pignot
 */
public class RemoveUserProfileDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "RemoveUserProfileDialog";
    private static final String USERNAME_REGEX = "<USERNAME>";
    private static final String FIRSTNAME_REGEX = "<USER_FIRST_NAME>";
    private static final String LASTNAME_REGEX = "<USER_LAST_NAME>";
    private static final String ECASUNAME_REGEX = "<USER_ECAS_USERNAME>";
    private static final String KEY_PROFILE = "profile";
    private static final String CAN_NOT_DELETE_LAST_CIRCABC_ADMIN = "can_not_delete_last_circabc_admin";
    private static final long serialVersionUID = 222220868117140631L;
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(RemoveUserProfileDialog.class);
    private static final String NOTIFY_YES = "yes";
    private static final String NOTIFY_NO = "no";
    private String userName;
    private String interestGroupName;
    private String categoryName;
    private boolean isSelfRemoveUserProfile;
    private String name;
    private SynchronizationService synchronizationService;
    private PersonService personService;
    private HistoryApi historyApi;
    private boolean interestGroup = false;
    private String notifyDirAdmins = NOTIFY_NO;
    private ContainerType containerType = ContainerType.UNKNOWN;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            if (getActionNode() == null) {
                throw new IllegalArgumentException("The node id is a mandatory parameter");
            } else if (parameters.get("userName") == null) {
                throw new IllegalArgumentException("The user name is a mandatory parameter ");
            }

            userName = parameters.get("userName");
            interestGroupName = parameters.get("interestGroupName");
            categoryName = parameters.get("categoryName");
            interestGroup = getActionNode().hasAspect(CircabcModel.ASPECT_IGROOT);
            if (interestGroupName != null) {
                containerType = ContainerType.INTEREST_GROUP;
                name = interestGroupName;
            }

            if (categoryName != null) {
                containerType = ContainerType.CATEGORY;
                name = categoryName;
            }

            String currentUserName = getNavigator().getCurrentUserName();
            isSelfRemoveUserProfile = userName.equalsIgnoreCase(currentUserName);
        }

    }

    public String getConfirmationMessage() {
        String message = translate("uninvite_user_confirm");
        if (isSelfRemoveUserProfile) {
            message = translate("self_uninvite_user_confirm");
        }

        return message;

    }

    @Override
    public String getCancelButtonLabel() {
        return translate("no");
    }

    @Override
    public String getFinishButtonLabel() {
        return translate("yes");
    }

    @Override
    public String getContainerTitle() {
        String message = translate("uninvite_user_title");
        if (isSelfRemoveUserProfile && containerType == ContainerType.INTEREST_GROUP) {
            message = translate("self_uninvite_user_title_ig", name);
        } else if (isSelfRemoveUserProfile && containerType == ContainerType.CATEGORY) {
            message = translate("self_uninvite_user_title_category", name);
        }
        return message;
    }

    @Override
    public String getContainerDescription() {
        final String message;
        if (isSelfRemoveUserProfile && containerType == ContainerType.INTEREST_GROUP) {
            message = translate("self_uninvite_user_description_ig", name);
        } else if (isSelfRemoveUserProfile && containerType == ContainerType.CATEGORY) {
            message = translate("self_uninvite_user_description_category", name);
        } else {
            message = translate("uninvite_user_description",
                    "<i><b>" + PermissionUtils.computeUserLogin(userName) + "</b></i>");
        }
        return message;

    }

    private Set<String> buildInvitedSet() {
        final FacesContext context = FacesContext.getCurrentInstance();
        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<Set<String>> callback = new RetryingTransactionCallback<Set<String>>() {
            public Set<String> execute() throws Throwable {
                final NodeRef nodeRef = getActionNode().getNodeRef();
                final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                        .getProfileManagerService(nodeRef);
                Set<String> invitedUsers;
                invitedUsers = profileManagerService.getInvitedUsersProfiles(nodeRef).keySet();
                return invitedUsers;

            }
        };

        return txnHelper.doInTransaction(callback, true);
    }

    @Override
    protected String finishImpl(final FacesContext context, String outcome) throws Exception {
        try {
            final NodeRef nodeRef = getActionNode().getNodeRef();
            final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                    .getProfileManagerService(nodeRef);
            final String personProfile = profileManagerService.getPersonProfile(nodeRef, userName);
            if (personProfile == null) {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "Can not not delete last admin : " + userName + ", noderef :" + nodeRef.toString());
                }
                Utils.addErrorMessage(
                        "Person profile is null : " + userName + " noderef : " + nodeRef.toString());
            }
            Profile profile = profileManagerService.getProfile(nodeRef, personProfile);
            if (profile.isAdmin() && getNumberofAdmins(nodeRef, profileManagerService) == 1) {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Can not not delete last admin : " + userName + ", noderef :" + nodeRef.toString());
                }

                final String message;
                if (getActionNode().hasAspect(CircabcModel.ASPECT_CIRCABC_ROOT)) {
                    message = CAN_NOT_DELETE_LAST_CIRCABC_ADMIN;
                } else if (getActionNode().hasAspect(CircabcModel.ASPECT_CATEGORY)) {
                    message = "can_not_delete_last_category_admin";
                } else {
                    message = "self_uninvite_error_message_last_ig_leader";
                }
                Utils.addErrorMessage(translate(message));
                outcome = null;

            } else {
                logRecord.setInfo("Removed membership for user " + userName);

                historyApi.logOldMembership(userName, nodeRef.getId());
                profileManagerService.uninvitePerson(nodeRef, userName, false);
                historyApi.registerCleanPermissions(nodeRef, userName);
            }
            if (CircabcConfig.ENT) {
                if (interestGroup || (containerType == ContainerType.INTEREST_GROUP)) {
                    List<String> ecordaThemeIds = synchronizationService
                            .getEcordaThemeIds(getActionNode().getNodeRef());
                    for (String ecordaThemeId : ecordaThemeIds) {
                        synchronizationService.revokeThemeRole(userName, ecordaThemeId, SynchronizationService.DEFAULT_ECORDA_ROLE);
                    }
                }
            }

            if (NOTIFY_YES.equals(this.notifyDirAdmins)) {
                final Set<String> dirAdmins = getUserService()
                        .getUsersWithPermission(getActionNode().getNodeRef(),
                                DirectoryPermissions.DIRADMIN.toString());
                for (String dirAdmin : dirAdmins) {
                    if (getPersonService().personExists(dirAdmin)) {
                        final NodeRef adminPerson = getPersonService()
                                .getPerson(dirAdmin);
                        final Boolean globalNotification = (Boolean) getNodeService()
                                .getProperty(adminPerson,
                                        UserModel.PROP_GLOBAL_NOTIFICATION);
                        if (globalNotification != null && globalNotification) {

                            final NodeRef person = getPersonService()
                                    .getPerson(userName);
                            final Map<String, Object> model = getMailPreferencesService()
                                    .buildDefaultModel(nodeRef, person, null);
                            model.putAll(buildTemplateModel(person, profile
                                    .getTitle().getDefaultValue(), nodeRef));
                            MailWrapper mailWrapper = getMailPreferencesService()
                                    .getDefaultMailTemplate(
                                            nodeRef,
                                            MailTemplate.REMOVE_MEMBERSHIP_NOTIFICATION);
                            String from = getMailService()
                                    .getNoReplyEmailAddress();
                            final Map<QName, Serializable> personProperties = getNodeService()
                                    .getProperties(adminPerson);
                            final String to = (String) personProperties
                                    .get(ContentModel.PROP_EMAIL);
                            boolean html = true;
                            try {
                                getMailService().send(from, to, null,
                                        mailWrapper.getSubject(model),
                                        mailWrapper.getBody(model), html, false);
                            } catch (MessagingException e) {
                                if (logger.isErrorEnabled()) {
                                    logger.error(e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (ProfileException profileException) {
            if (logger.isErrorEnabled()) {
                logger.error("Unexpected error:" + profileException.getExplanation(), profileException);
            }

            Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, profileException.getExplanation()),
                    profileException);

            outcome = null;

        } catch (Throwable err) {
            if (logger.isErrorEnabled()) {
                logger.error("Unexpected error:" + err.getMessage(), err);
            }

            Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, err.getMessage()), err);

            outcome = null;
        }

        return outcome;
    }

    private Map<String, Object> buildTemplateModel(final NodeRef person, final String profile,
                                                   NodeRef igNodeRef) {
        final Map<QName, Serializable> personProperties = getNodeService().getProperties(person);
        final Map<String, Object> params = new HashMap<>(10);

        final String login = PermissionUtils.computeUserLogin(personProperties);

        // TODO check to delete what is not needed
        params.put(FIRSTNAME_REGEX, (String) personProperties.get(ContentModel.PROP_FIRSTNAME));
        params.put(LASTNAME_REGEX, (String) personProperties.get(ContentModel.PROP_LASTNAME));
        params.put(ECASUNAME_REGEX, (String) personProperties.get(UserModel.PROP_ECAS_USER_NAME));
        params.put(USERNAME_REGEX, login);
        params.put(KEY_PROFILE, profile);
        Node createdNode = new Node(igNodeRef);
        params.put("<NODE_BEST_TITLE>", getBestTitle(createdNode));
        params.put("<NODE_REF>",
                WebClientHelper.getGeneratedWaiFullUrl(createdNode, ExtendedURLMode.HTTP_WAI_BROWSE));
        // params.put("interestGroup",igNodeRef);

        return params;
    }

    private int getNumberofAdmins(final NodeRef nodeRef,
                                  final ProfileManagerService profileManagerService) {
        int currentNodeAdminCount = 0;
        for (final String authority : buildInvitedSet()) {
            String personProfile;
            Profile profile;
            personProfile = profileManagerService.getPersonProfile(nodeRef, authority);
            if (personProfile == null) {
                continue;
            }
            profile = profileManagerService.getProfile(nodeRef, personProfile);
            if (profile.isAdmin()) {
                currentNodeAdminCount++;
            }
        }
        return currentNodeAdminCount;
    }

    public String getBrowserTitle() {
        String message = translate("uninvite_user_browser_title");
        if (isSelfRemoveUserProfile) {
            message = translate("self_uninvite_user_browser_title");
        }
        return message;
    }

    public String getPageIconAltText() {
        String message = translate("uninvite_user_icon_tooltip");
        if (isSelfRemoveUserProfile) {
            message = translate("self_uninvite_user_icon_tooltip");
        }
        return message;
    }

    public SynchronizationService getSynchronizationService() {
        return synchronizationService;
    }

    public void setSynchronizationService(SynchronizationService synchronizationService) {
        this.synchronizationService = synchronizationService;
    }

    /**
     * @return the historyApi
     */
    public HistoryApi getHistoryApi() {
        return historyApi;
    }

    /**
     * @param historyApi the historyApi to set
     */
    public void setHistoryApi(HistoryApi historyApi) {
        this.historyApi = historyApi;
    }

    @Override
    public boolean isFinishAsyncButtonVisible() {

        return true;

    }

    public boolean isInterestGroup() {
        return interestGroup;
    }

    public void setInterestGroup(boolean interestGroup) {
        this.interestGroup = interestGroup;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public String getNotifyDirAdmins() {
        return notifyDirAdmins;
    }

    public void setNotifyDirAdmins(String notifyDirAdmins) {
        this.notifyDirAdmins = notifyDirAdmins;
    }

    private enum ContainerType {
        UNKNOWN, INTEREST_GROUP, CATEGORY
    }
}
