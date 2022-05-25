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
package eu.cec.digit.circabc.web.wai.dialog.event;

import eu.cec.digit.circabc.service.event.MeetingRequestStatus;
import eu.cec.digit.circabc.service.event.UpdateMode;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.Map;


/**
 * Bean that back the Accept Meeting Processes
 *
 * @author Yanick Pignot
 */
public class AcceptMeetingRequestDialog extends AppointmentDialogBase {

    public static final String BEAN_NAME = "AcceptMeetingRequestDialog";
    public static final String DIALOG_OUTCOME = "wai:dialog:acceptMeetingRequestWai";
    /**
     *
     */
    private static final long serialVersionUID = 1100813930081721315L;
    private static final String MSG_DIALOG_TITLE = "event_accept_recurrent_meeting_dialog_title";
    private static final String MSG_DIALOG_DESC = "event_accept_recurrent_meeting_dialog_description";
    private static final String MSG_DIALOG_BROWSER_TITLE = "event_accept_recurrent_meeting_dialog_browser_title";
    private static final String MSG_DIALOG_ICON_TOOLTIP = "event_accept_recurrent_meeting_dialog_icon_tooltip";
    private static final String MSG_ACCEPT_CURRENT_STATUS = "event_accept_meeting_message_current";
    private static final String MSG_ACCEPT_SPECIFIC_STATUS = "event_accept_meeting_message_specific";
    private static final String MSG_ACCEPT_CURRENT_SERIES_STATUS = "event_accept_meeting_message_specific";
    private static final String MSG_ACCEPT_SPECIFIC_SERIES_STATUS = "event_accept_meeting_series_message_specific";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        this.logRecord.setService("Event");
        this.logRecord.setActivity("Accept meeting request");
    }

    /**
     * Action called upon completion of the Accepted Single Meeting without going into dialog
     *
     * @param event The action event
     */
    public void acceptMeetingPreTreatment(ActionEvent event) {
        final UIComponent component = event.getComponent();
        final Map<String, String> parameters = ((UIActionLink) component).getParameterMap();

        this.init(parameters);

        if (!isMeeting()) {
            throw new IllegalStateException("Impossible to call this action on an event.");
        }
        if (!isRecurrent()) {
            // refresh and click
            getBrowseBean().refreshBrowsing();
        }
    }

    public String getOutcome() {
        if (isRecurrent()) {
            return DIALOG_OUTCOME;
        } else {
            acceptSingleMeeting();
            return null;
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        if (getUpdateMode().equals(UpdateMode.Single)) {
            acceptSingleMeeting();
        } else {
            acceptMultipleMeeting();
        }
        return outcome;
    }

    private void acceptSingleMeeting() {
        final NodeRef appointementNodeRef = getActionNode().getNodeRef();
        final String userName = getNavigator().getCurrentUser().getUserName();

        getEventService()
                .setMeetingRequestStatus(appointementNodeRef, userName, MeetingRequestStatus.Accepted,
                        UpdateMode.Single);

        if (isCurrentNode(getActionNode())) {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_ACCEPT_CURRENT_STATUS));
        } else {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    translate(MSG_ACCEPT_SPECIFIC_STATUS, getAppointment().getTitle()));
        }
    }

    private void acceptMultipleMeeting() {
        final NodeRef appointementNodeRef = getActionNode().getNodeRef();
        final String userName = getNavigator().getCurrentUser().getUserName();

        getEventService()
                .setMeetingRequestStatus(appointementNodeRef, userName, MeetingRequestStatus.Accepted,
                        getUpdateMode());

        if (isCurrentNode(getActionNode())) {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    translate(MSG_ACCEPT_CURRENT_SERIES_STATUS));
        } else {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    translate(MSG_ACCEPT_SPECIFIC_SERIES_STATUS, getAppointment().getTitle()));
        }
    }

    public String getBrowserTitle() {
        return translate(MSG_DIALOG_BROWSER_TITLE);
    }

    public String getPageIconAltText() {
        return translate(MSG_DIALOG_ICON_TOOLTIP);
    }

    public String getContainerTitle() {
        return translate(MSG_DIALOG_TITLE);
    }

    public String getContainerDescription() {
        return translate(MSG_DIALOG_DESC);
    }

    private boolean isCurrentNode(Node node) {
        return getNavigator().getCurrentNodeId().equals(node.getId());
    }
}
