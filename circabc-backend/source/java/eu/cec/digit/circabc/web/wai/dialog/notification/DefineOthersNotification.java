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
package eu.cec.digit.circabc.web.wai.dialog.notification;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import eu.cec.digit.circabc.repo.notification.AuthorityNotificationImpl;
import eu.cec.digit.circabc.service.notification.AuthorityNotification;
import eu.cec.digit.circabc.service.notification.NotificationStatus;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.ProfileUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import java.text.MessageFormat;
import java.util.*;


/**
 * Bean that backs the "Define Others' Notification" WAI page.
 *
 * @author Yanick Pignot
 */
public class DefineOthersNotification extends BaseWaiDialog {

    /**
     * Index of the USERS search filter index
     */
    public static final int USERS_IDX = 0;
    /**
     * Index of the PROFILES search filter index
     */
    public static final int PROFILES_IDX = 1;
    protected static final String USER_SPECIFIED_TWICE = "notification_define_other_dialog_user_specified_twice";
    /**
     *
     */
    private static final long serialVersionUID = -6125314998875374221L;
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(DefineOthersNotification.class);
    /**
     * Data model for table of notifications for users
     */
    private transient DataModel userNotificationDataModel = null;
    /**
     * list of notification status wrapper objects
     */
    private List<NotificationWrapper> userNotifications = null;
    /**
     * The list of available notification status
     */
    private SelectItem[] notificationStatuses;

    private transient NotificationSubscriptionService notificationSubscriptionService;
    private transient PersonService personService;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("A node id is a mandatory parameter");
        }

        userNotifications = null;
        userNotificationDataModel = null;
        notificationStatuses = null;
        fillDatas();
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Start to update notification status on the node " + getActionNode().getNodeRef());
        }

        if (logger.isDebugEnabled()) {
            logger
                    .debug("-- 1.  Start to remove authorities that the notification status is removed ...");
        }

        final Set<AuthorityNotification> oldNotifications = getNotificationSubscriptionService()
                .getNotifications(getActionNode().getNodeRef());
        for (Iterator<AuthorityNotification> iterator = oldNotifications.iterator();
             iterator.hasNext(); ) {
            AuthorityNotification authorityNotification = (AuthorityNotification) iterator.next();
            if (authorityNotification.getInherited()) {
                iterator.remove();
            }
        }

        Set<AuthorityNotification> newNotifications = Collections.emptySet();

        if (userNotifications != null) {
            newNotifications = new HashSet<>(userNotifications.size());
            for (NotificationWrapper wrapper : userNotifications) {
                newNotifications
                        .add(new AuthorityNotificationImpl(wrapper.getStatusValue(), wrapper.getAuthority()));
            }
        }

        SetView<AuthorityNotification> differenceOldNew = Sets
                .difference(oldNotifications, newNotifications);
        SetView<AuthorityNotification> differenceNewOld = Sets
                .difference(newNotifications, oldNotifications);

        for (AuthorityNotification notif : differenceOldNew) {
            // remove the user
            getNotificationSubscriptionService()
                    .removeNotification(getActionNode().getNodeRef(), notif.getAuthority());

        }

        if (logger.isDebugEnabled()) {
            logger.debug("-- 2.  Update others ... ");
        }

        for (AuthorityNotification notif : differenceNewOld) {
            String info = MessageFormat
                    .format("status {0} for user {1}", notif.getAuthority(), notif.getNotificationStatus());
            logRecord.addInfo(info);
            // remove the user
            getNotificationSubscriptionService()
                    .setNotificationStatus(getActionNode().getNodeRef(), notif.getAuthority(),
                            notif.getNotificationStatus());

        }

        if (logger.isDebugEnabled()) {
            logger.debug("Notifications successfull update for the node " + getActionNode().getNodeRef()
                    + "\n\tBefore:   " + oldNotifications
                    + "\n\tAfter:    " + getNotificationSubscriptionService()
                    .getNotifications(getActionNode().getNodeRef())
            );
        }

        return outcome;
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

    public SelectItem[] getStatuses() {
        if (notificationStatuses == null) {
            notificationStatuses = NotificationUtils.getStatusesAsSelectItem(getActionNode());
        }

        return notificationStatuses;

    }

    public SelectItem[] pickerCallback(final int filterIndex, final String contains) {
        List<SortableSelectItem> result = null;

        final List<String> alreadyInvitedAuthorities = getAlreadySettedAuthorities();

        if (filterIndex == PROFILES_IDX) {
            result = ProfileUtils
                    .buildMailableProfileItems(getActionNode(), contains, alreadyInvitedAuthorities, logger);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "The Profile search is performed successfully and return " + result + ". Filter Index: "
                                + filterIndex + ". Expression: " + contains + ".");
            }

        } else if (filterIndex == USERS_IDX) {
            result = PermissionUtils
                    .buildInvitedUserItems(getActionNode(), contains, false, alreadyInvitedAuthorities,
                            logger);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "The User search is performed successfully and return " + result + ". Filter Index: "
                                + filterIndex + ". Expression: " + contains + ".");
            }
        } else {
            logger.error("The picker is called with an invalid index parameter " + filterIndex
                    + ". This last is not taken in account yet.");

            result = Collections.emptyList();
        }
        return result.toArray(new SelectItem[result.size()]);
    }

    /**
     * Returns the properties for current user-roles JSF DataModel
     *
     * @return JSF DataModel representing the current user-roles
     */
    public DataModel getUserNotificationDataModel() {
        if (this.userNotificationDataModel == null) {
            this.userNotificationDataModel = new ListDataModel();
        }

        this.userNotificationDataModel.setWrappedData(this.userNotifications);

        return this.userNotificationDataModel;
    }

    /**
     * Action handler called when the Add button is pressed to process the current selection
     */
    public void addSelection(ActionEvent event) {
        final IGRootProfileManagerService profileManager = getProfileManagerServiceFactory()
                .getIGRootProfileManagerService();
        final NodeRef igRoot = getManagementService()
                .getCurrentInterestGroup(getActionNode().getNodeRef());
        final UIGenericPicker picker = (UIGenericPicker) event.getComponent()
                .findComponent("define-notif-picker");
        final UISelectOne profilePicker = (UISelectOne) event.getComponent()
                .findComponent("define-notif-statuses");

        final String[] results = picker.getSelectedResults();
        if (results != null) {
            final String status = (String) profilePicker.getValue();

            if (status != null) {
                // invite all selected users
                for (String authority : results) {
                    // only add if authority not already present in the list with same CircaRole
                    boolean foundExisting = false;

                    for (final NotificationWrapper wrap : userNotifications) {
                        if (authority.equals(wrap.getAuthority())) {
                            foundExisting = true;
                            break;
                        }
                    }

                    // if found existing then user has to
                    if (foundExisting == false) {
                        StringBuilder label = new StringBuilder(64);

                        AuthorityType authType = AuthorityType.getAuthorityType(authority);

                        if (authType == AuthorityType.USER) {
                            // found a User authority
                            NodeRef ref = getPersonService().getPerson(authority);
                            String firstName = (String) getNodeService()
                                    .getProperty(ref, ContentModel.PROP_FIRSTNAME);
                            String lastName = (String) getNodeService()
                                    .getProperty(ref, ContentModel.PROP_LASTNAME);
                            String email = (String) getNodeService().getProperty(ref, ContentModel.PROP_EMAIL);

                            label.append(firstName)
                                    .append(" ")
                                    .append(lastName != null ? lastName : "")
                                    .append(" (")
                                    .append(email)
                                    .append(")");

                        } else {
                            final Profile profileFromGroup = profileManager
                                    .getProfileFromGroup(igRoot, authority);
                            // get the the name of the group
                            label.append(
                                    profileFromGroup.getProfileDisplayName());
                        }

                        this.userNotifications.add(
                                new NotificationWrapper(
                                        AuthorityType.getAuthorityType(authority),
                                        label.toString(),
                                        NotificationStatus.valueOf(status),
                                        authority,
                                        getActionNode().getId(),
                                        false));
                    } else {
                        // foundExisting = true
                        FacesContext.getCurrentInstance()
                                .addMessage(null, new FacesMessage(translate(USER_SPECIFIED_TWICE)));
                    }
                }
            }
        }
    }

    /**
     * Action handler called when the Remove button is pressed to remove a user+Circarole
     */
    public void removeSelection(ActionEvent event) {
        NotificationWrapper wrapper = (NotificationWrapper) this.userNotificationDataModel.getRowData();
        if (wrapper != null) {
            this.userNotifications.remove(wrapper);
        }
    }


    private List<String> getAlreadySettedAuthorities() {
        final List<String> invitedList = new ArrayList<>(this.userNotifications.size());

        for (final NotificationWrapper wrap : userNotifications) {
            invitedList.add(wrap.getAuthority());
        }

        return invitedList;
    }

    private void fillDatas() {
        final Set<AuthorityNotification> notifications = getNotificationSubscriptionService()
                .getNotifications(getActionNode().getNodeRef());
        userNotifications = new ArrayList<>(notifications.size());

        NotificationWrapper wrapper = null;

        for (final AuthorityNotification notification : notifications) {
            wrapper = NotificationUtils.wrappNotification(notification, getActionNode().getNodeRef());

            if (wrapper != null) {
                if (!wrapper.getInherited()) {
                    userNotifications.add(wrapper);
                }

            }
        }
    }

    public String getBrowserTitle() {
        return translate("notification_define_other_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("notification_define_other_dialog_icon_tooltip");
    }

    @Override
    public String getContainerTitle() {
        return translate("notification_define_other_dialog_title", getActionNode().getName());
    }

    /**
     * @return the notificationSubscriptionService
     */
    protected final NotificationSubscriptionService getNotificationSubscriptionService() {
        if (notificationSubscriptionService == null) {
            notificationSubscriptionService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNotificationSubscriptionService();
        }
        return notificationSubscriptionService;
    }

    /**
     * @param notificationSubscriptionService the notificationSubscriptionService to set
     */
    public final void setNotificationSubscriptionService(
            NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
    }

    /**
     * @return the personService
     */
    protected final PersonService getPersonService() {
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public final void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean isFinishAsyncButtonVisible() {

        return true;

    }
}
