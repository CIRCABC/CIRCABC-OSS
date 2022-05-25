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
package eu.cec.digit.circabc.web.wai.bean.notification;

import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import eu.cec.digit.circabc.service.notification.UserNotificationReport;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.ProfileUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.wai.dialog.notification.NotificationUtils;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.User;

import javax.faces.context.FacesContext;
import java.io.Serializable;

/**
 * Bean that back the notification status panel to add in any page
 *
 * @author yanick pignot
 */
public class NotificationStatusPanel implements Serializable {

    public static final String MSG_NOTIFIABLE = "notification_panel_result_positive";
    public static final String MSG_NOT_NOTIFIABLE = "notification_panel_result_negative";
    public static final String BEAN_NAME = "NotificationStatusPanel";
    private static final long serialVersionUID = 1825200822620206557L;
    private transient UserNotificationReport notificationReport;

    private transient NotificationSubscriptionService notificationSubscriptionService;
    private transient ProfileManagerServiceFactory profileManagerServiceFactory;
    private transient ManagementService managementService;
    private CircabcNavigationBean navigator;
    private CircabcBrowseBean browseBean;

    // used for cache purposes to prevent that the methods are not called too many time
    private transient FacesContext rememberedFacesContext = null;

    public boolean isPanelDisplayed() {
        final FacesContext fc = FacesContext.getCurrentInstance();

        if (rememberedFacesContext == null || !fc.equals(rememberedFacesContext)) {
            rememberedFacesContext = fc;

            notificationReport = null;

            final User currentUser = getNavigator().getCurrentUser();

            Node actionNode = getBrowseBean().getActionSpace();
            if (actionNode == null) {
                actionNode = getBrowseBean().getDocument();
            }

            if (actionNode == null || currentUser == null) {
                return false;
            }

            notificationReport = getNotificationSubscriptionService().getUserNotificationReport(
                    actionNode.getNodeRef(),
                    currentUser.getUserName()
            );
        }

        //if the report is null, don't display the panel
        return notificationReport != null;
    }

    public void reset() {
        rememberedFacesContext = null;
        notificationReport = null;
    }


    public UserNotificationReport getNotificationReport() {
        return notificationReport;
    }

    /**
     *
     */
    public final String getUserNotificationStatus() {
        if (notificationReport == null) {
            return null;
        } else {
            return NotificationUtils.translateStatus(notificationReport.getUserNotificationStatus())
                    .toUpperCase();
        }
    }

    /**
     *
     */
    public final String getProfileNotificationStatus() {
        if (notificationReport == null) {
            return null;
        } else {
            return NotificationUtils.translateStatus(notificationReport.getProfileNotificationStatus())
                    .toUpperCase();
        }
    }

    /**
     *
     */
    public final String getGlobalNotificationStatus() {
        if (notificationReport == null) {
            return null;
        } else {
            return NotificationUtils.translateStatus(notificationReport.getGlobalNotificationStatus())
                    .toUpperCase();
        }
    }

    /**
     *
     */
    public final String getUser() {
        if (notificationReport == null) {
            return null;
        } else {
            return notificationReport.getUserAuthority();
        }
    }

    public final String getUserDisplayName() {
        final String username = getUser();

        if (username != null) {
            return PermissionUtils.computeUserLogin(username);
        } else {
            return null;
        }

    }

    /**
     *
     */
    public final String getProfile() {
        if (notificationReport == null || notificationReport.getUserProfile() == null) {
            return null;
        } else {
            return ProfileUtils
                    .getProfilename(notificationReport.getLocation(), notificationReport.getUserProfile(),
                            getProfileManagerServiceFactory().getIGRootProfileManagerService(),
                            getManagementService());
        }
    }

    /**
     *
     */
    public final String getNotificationResult() {
        if (notificationReport == null) {
            return null;
        } else if (notificationReport.isUserNotifiable()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            return Application.getMessage(fc, MSG_NOTIFIABLE);
        } else {
            FacesContext fc = FacesContext.getCurrentInstance();
            return Application.getMessage(fc, MSG_NOT_NOTIFIABLE);
        }
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
            final NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
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
     * @return the managementService
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
    public final void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
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
            ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
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
    public final void setBrowseBean(CircabcBrowseBean browseBean) {
        this.browseBean = browseBean;
    }

}
