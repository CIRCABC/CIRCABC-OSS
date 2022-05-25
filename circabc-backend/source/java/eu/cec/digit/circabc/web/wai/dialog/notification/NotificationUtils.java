/**
 * Copyright 2006 European Community
 * <p>
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * <p>
 * https://joinup.ec.europa.eu/software/page/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
/**
 *
 */
package eu.cec.digit.circabc.web.wai.dialog.notification;

import eu.cec.digit.circabc.service.notification.AuthorityNotification;
import eu.cec.digit.circabc.service.notification.GlobalNotificationStatus;
import eu.cec.digit.circabc.service.notification.NotificationStatus;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.ProfileUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

/**
 * Util methods for the Web part Notification Process
 *
 * @author Yanick Pignot
 */
public abstract class NotificationUtils {

    private static final String MSG_USER = "user";
    private static final String MSG_PROFILE = "profile";
    private static final String MSG_INHERITED = "notification_status_inherited";
    private static final String MSG_SUBSCRIBED = "notification_status_suscribed";
    private static final String MSG_UNSUBSCRIBED = "notification_status_unsuscribed";
    private static final String MSG_ENABLED = "edit_user_details_global_notification_active";
    private static final String MSG_DISABLED = "edit_user_details_global_notification_nonactive";

    private static final String MSG_GLOBAL_NOTIF_ENABLE = "edit_user_details_global_notification_active";
    private static final String MSG_GLOBAL_NOTIF_DISABLE = "edit_user_details_global_notification_nonactive";

    private static final String GLOBAL_NOTIF_VALUE_ENABLE = "true";
    private static final String GLOBAL_NOTIF_VALUE_DISABLE = "false";

    private static final IGRootProfileManagerService igRootProfileManager = Services
            .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
            .getIGRootProfileManagerService();
    private static final ManagementService managementService = Services
            .getCircabcServiceRegistry(FacesContext.getCurrentInstance()).getManagementService();

    public static NotificationWrapper wrappNotification(
            final AuthorityNotification authorityNotification, final NodeRef node) {
        String name = null;

        switch (authorityNotification.getAuthorityType()) {
            case USER:
                name = PermissionUtils.computeUserLogin(authorityNotification.getAuthority());
                break;

            case GROUP:
                name = ProfileUtils
                        .getProfilename(node, authorityNotification.getAuthority(), igRootProfileManager,
                                managementService);
                break;

            default:
                return null;
        }

        return new NotificationWrapper(
                authorityNotification.getAuthorityType(),
                name,
                authorityNotification.getNotificationStatus(),
                authorityNotification.getAuthority(),
                node.getId(),
                authorityNotification.getInherited()
        );

    }

    public static String translateStatus(NotificationStatus status) {
        String displayStatus = null;

        switch (status) {
            case UNSUBSCRIBED:
                displayStatus = translate(MSG_UNSUBSCRIBED);
                break;
            case SUBSCRIBED:
                displayStatus = translate(MSG_SUBSCRIBED);
                break;
            case INHERITED:
                displayStatus = translate(MSG_INHERITED);
                break;
        }

        return displayStatus;
    }

    public static String translateStatus(GlobalNotificationStatus status) {
        String displayStatus = null;

        switch (status) {
            case DISABLED:
                displayStatus = translate(MSG_DISABLED);
                break;
            case ENABLED:
                displayStatus = translate(MSG_ENABLED);
                break;

        }

        return displayStatus;
    }

    public static String translateAuthorityType(AuthorityType type) {
        String displayType = null;

        switch (type) {
            case GROUP:
                displayType = translate(MSG_PROFILE);
                break;
            case USER:
                displayType = translate(MSG_USER);
                break;
            default:
                // only users an group (profile) are managed by notification service.
                break;
        }

        return displayType;
    }

    public static SelectItem[] getStatusesAsSelectItem(Node currentNode) {
        final NotificationStatus[] statusEnums = NotificationStatus.values();
        final int size = statusEnums.length;
        final boolean isUnderIg = NavigableNodeType.IG_ROOT.isNodeFromType(currentNode);

        SelectItem[] notificationStatuses = new SelectItem[isUnderIg ? size - 1 : size];
        int inc = 0;

        for (final NotificationStatus st : statusEnums) {
            if (isUnderIg && st.equals(NotificationStatus.INHERITED)) {
                // don't add the Inherited status under an IG
            } else {
                notificationStatuses[inc] = new SelectItem(st.toString(),
                        NotificationUtils.translateStatus(st));
                inc++;
            }
        }

        return notificationStatuses;
    }

    public static SelectItem[] getGlobalStatusesAsSelectItem() {
        return new SelectItem[]{
                new SelectItem(GLOBAL_NOTIF_VALUE_ENABLE, translate(MSG_GLOBAL_NOTIF_ENABLE)),
                new SelectItem(GLOBAL_NOTIF_VALUE_DISABLE, translate(MSG_GLOBAL_NOTIF_DISABLE))
        };
    }

    private static String translate(final String key) {
        return WebClientHelper.translate(key);
    }
}
