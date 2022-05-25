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

import eu.cec.digit.circabc.model.UserModel;
import org.alfresco.service.cmr.repository.NodeRef;

import javax.faces.context.FacesContext;
import java.util.Map;


/**
 * Bean that backs the "Edit Own Notification" WAI page.
 *
 * @author Yanick Pignot
 */
public class EditOwnNotificationDialog extends EditAuthorityNotificationDialog {

    public static final String BEAN_NAME = "EditOwnNotificationDialog";

    /**
     *
     */
    private static final long serialVersionUID = -298735948907020207L;

    private String globalStatus;

    @Override
    public void init(Map<String, String> parameters) {

        //prevent null pointer in restaure time
        if (parameters != null) {

            parameters.put(PARAM_AUTHORITY, getNavigator().getCurrentUser().getUserName());
            parameters.put(PARAM_STATUS, "");

            super.init(parameters);
        }

        // init the panel.
        getNotificationStatusPanel().isPanelDisplayed();
        this.globalStatus = null;
        notificationStatus = getNotificationStatusPanel().getUserNotificationStatus();
    }

    @Override
    public String getContainerTitle() {
        return translate("notification_edit_own_dialog_title", getActionNode().getName());
    }

    public String getBrowserTitle() {
        return translate("notification_edit_own_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("notification_edit_own_dialog_icon_tooltip");
    }


    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        if (this.globalStatus != null && this.globalStatus.length() > 0) {
            final Boolean statusBool = Boolean.valueOf(this.globalStatus);

            if (statusBool.equals(getCurrentGlobalStatus()) == false) {
                final NodeRef person = getNavigator().getCurrentUser().getPerson();
                getNodeService().setProperty(person, UserModel.PROP_GLOBAL_NOTIFICATION, statusBool);
            }
        }

        return super.finishImpl(context, outcome);
    }

    public String getGlobalNotificationStatus() {
        if (globalStatus == null) {
            this.globalStatus = getCurrentGlobalStatus().toString();
        }
        return globalStatus;
    }

    public void setGlobalNotificationStatus(final String status) {
        this.globalStatus = status;
    }

    private Boolean getCurrentGlobalStatus() {
        return getNotificationStatusPanel().getNotificationReport().getGlobalNotificationStatus()
                .toBoolean();
    }

}
