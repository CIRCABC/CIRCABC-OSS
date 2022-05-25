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

import eu.cec.digit.circabc.service.notification.AuthorityNotification;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Bean that backs the "Manage Notifications" WAI page.
 *
 * @author Yanick Pignot
 */
public class ManageNotificationDialog extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "ManageNotificationDialog";
    private static final long serialVersionUID = 6677774407135361466L;
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(ManageNotificationDialog.class);
    private transient NotificationSubscriptionService notificationSubscriptionService;
    private Boolean interestGroup;


    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("A node id is a mandatory parameter");
        }

        interestGroup = null;
    }

    public Node getCurrentNode() {
        return getActionNode();
    }

    public boolean isCurrentNodeInterestGroup() {
        if (interestGroup == null) {
            interestGroup = NavigableNodeType.IG_ROOT.isNodeFromType(getActionNode());
        }
        return interestGroup;
    }

    public List<NotifiableUser> getUsers() {
        List<NotifiableUser> result = new ArrayList<>();
        result.addAll(
                getNotificationSubscriptionService().getNotifiableUsers(getActionNode().getNodeRef()));
        return result;
    }

    public List<NotificationWrapper> getNotifications() {
        final Set<AuthorityNotification> notifications = getNotificationSubscriptionService()
                .getNotifications(getActionNode().getNodeRef());
        final List<NotificationWrapper> wrappers = new ArrayList<>(notifications.size());

        NotificationWrapper wrapper = null;

        for (final AuthorityNotification notification : notifications) {
            wrapper = NotificationUtils.wrappNotification(notification, getActionNode().getNodeRef());

            if (wrapper == null) {
                logger.error(
                        "The repository is corrupeted. A notification has been setted with a non-managed Authotity Type. Only "
                                + AuthorityType.GROUP + "  and " + AuthorityType.USER + " are allowed. "
                                + "\n\tAuthority found: " + notification.getAuthority()
                                + "\n\tFrom type:       " + notification.getAuthorityType()
                                + "\n\tWith the status: " + notification.getNotificationStatus()
                                + "\n\tAuthority found: " + notification.getAuthorityType()
                                + "\n\tOn node:         " + getActionNode());

            }

            wrappers.add(wrapper);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    wrappers.size() + " Notification status found for the node " + getActionNode().getId()
                            + "\n\tNotifications: " + wrappers
                            + "\n\tFor node:      " + getActionNode());
        }

        return wrappers;
    }

    @Override
    public void restored() {
        //interestGroup = null;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        // nothing to do
        return outcome;
    }

    public String getBrowserTitle() {
        return translate("notification_view_other_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("notification_view_other_dialog_icon_tooltip");
    }

    @Override
    public String getContainerTitle() {
        return translate("notification_view_other_dialog_title", getBestTitle(getActionNode()));
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
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
}
