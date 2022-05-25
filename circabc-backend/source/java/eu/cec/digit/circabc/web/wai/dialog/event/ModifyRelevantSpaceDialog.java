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

import eu.cec.digit.circabc.service.event.AppointmentUpdateInfo;

import javax.faces.context.FacesContext;
import java.util.Map;


/**
 * Bean that back the relevant space edition dialog for a meeting
 *
 * @author Yanick Pignot
 */
public class ModifyRelevantSpaceDialog extends AppointmentDialogBase {

    public static final String BEAN_NAME = "ModifyRelevantSpaceDialog";
    /**
     *
     */
    private static final long serialVersionUID = 1444813930081721315L;
    private static final String MSG_DIALOG_TITLE = "event_modif_space_dialog_title";
    private static final String MSG_DIALOG_DESC = "event_modif_space_dialog_description";
    private static final String MSG_DIALOG_BROWSER_TITLE = "event_modif_space_dialog_browser_title";
    private static final String MSG_DIALOG_ICON_TOOLTIP = "event_modif_space_dialog_icon_tooltip";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        if (parameters != null) {
            setupAppointement();
        }
        this.logRecord.setService("Event");
        this.logRecord.setActivity("Modify relevant space");
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        getEventService()
                .updateAppointment(getActionNode().getNodeRef(), getAppointment(), getUpdateMode(),
                        AppointmentUpdateInfo.RelevantSpace);

        return outcome;
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
        return translate(MSG_DIALOG_DESC, getAppointment().getTitle());
    }
}
