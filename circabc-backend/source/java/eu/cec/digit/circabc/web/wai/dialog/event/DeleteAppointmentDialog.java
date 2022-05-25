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

import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;

import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * Bean that back the general info edition dialog for an appointement (as well an event that a
 * meeting)
 *
 * @author Yanick Pignot
 */
public class DeleteAppointmentDialog extends AppointmentDialogBase {

    public static final String BEAN_NAME = "DeleteAppointmentDialog";
    /**
     *
     */
    private static final long serialVersionUID = 1111113930081721315L;
    private static final String MSG_DIALOG_TITLE_MEETING = "event_delete_meeting_dialog_title";
    private static final String MSG_DIALOG_TITLE_EVENT = "event_delete_event_dialog_title";

    private static final String MSG_DIALOG_DESC = "event_delete_meeting_dialog_description";
    private static final String MSG_DIALOG_BROWSER_TITLE = "event_delete_meeting_dialog_browser_title";
    private static final String MSG_DIALOG_ICON_TOOLTIP = "event_delete_meeting_dialog_icon_tooltip";

    private static final String MSG_DIALOG_MEETING_CONFIRMATION = "event_delete_meeting_dialog_confirmation_single";
    private static final String MSG_DIALOG_RECURRENT_MEETING_CONFIRMATION = "event_delete_meeting_dialog_confirmation_recurrent";
    private static final String MSG_DIALOG_EVENT_CONFIRMATION = "event_delete_event_dialog_confirmation_single";
    private static final String MSG_DIALOG_RECURRENT_EVENT_CONFIRMATION = "event_delete_event_dialog_confirmation_recurrent";

    private static final String MSG_MAIL_INFO_MEETING = "event_delete_meeting_dialog_confirmation_mail";
    private static final String MSG_MAIL_INFO_EVENT = "event_delete_event_dialog_confirmation_mail";

    private static final String MSG_DELETE_ERROR = "event_delete_meeting_dialog_error";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        this.logRecord.setService("Event");
        this.logRecord.setActivity("Delete appointment");
    }

    public String getBrowserTitle() {
        return translate(MSG_DIALOG_BROWSER_TITLE);
    }

    public String getPageIconAltText() {
        return translate(MSG_DIALOG_ICON_TOOLTIP);
    }

    public String getContainerTitle() {
        return isMeeting() ? translate(MSG_DIALOG_TITLE_MEETING) : translate(MSG_DIALOG_TITLE_EVENT);
    }

    public String getContainerDescription() {
        return translate(MSG_DIALOG_DESC, getAppointment().getTitle());
    }

    public String getMailInformation() {
        if (isRecurrent() && !isAudienceOpen()) {
            return isMeeting() ? translate(MSG_MAIL_INFO_MEETING) : translate(MSG_MAIL_INFO_EVENT);
        } else {
            // message only for recurrent events/meetings
            return "";
        }
    }

    public String getConfirmation() {
        if (isMeeting()) {
            if (isRecurrent()) {
                return translate(MSG_DIALOG_RECURRENT_MEETING_CONFIRMATION);
            } else {
                return translate(MSG_DIALOG_MEETING_CONFIRMATION, getAppointment().getTitle());
            }
        } else {
            if (isRecurrent()) {
                return translate(MSG_DIALOG_RECURRENT_EVENT_CONFIRMATION);
            } else {
                return translate(MSG_DIALOG_EVENT_CONFIRMATION, getAppointment().getTitle());
            }

        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        try {
            final NodeRef appointementNodeRef = getActionNode().getNodeRef();

            getEventService().deleteAppointment(appointementNodeRef, getUpdateMode());

            final InterestGroupNode igRoot = (InterestGroupNode) getNavigator().getCurrentIGRoot();

            getBrowseBean().clickWai(igRoot.getEvent().getId());

            return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                    + CircabcNavigationHandler.OUTCOME_SEPARATOR
                    + CircabcBrowseBean.PREFIXED_WAI_BROWSE_OUTCOME;
        } catch (Exception e) {
            Utils.addErrorMessage(translate(MSG_DELETE_ERROR, e.getMessage()));
            return null;
        }
    }

}
