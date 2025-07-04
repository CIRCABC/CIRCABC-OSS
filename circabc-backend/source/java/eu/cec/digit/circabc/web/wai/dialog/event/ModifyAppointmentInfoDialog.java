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

import eu.cec.digit.circabc.service.event.Appointment;
import eu.cec.digit.circabc.service.event.AppointmentUpdateInfo;
import java.util.Map;
import javax.faces.context.FacesContext;
import org.alfresco.web.ui.common.Utils;

/**
 * Bean that back the general info edition dialog for an appointement (as well an event that a
 * meeting)
 *
 * @author Yanick Pignot
 */
public class ModifyAppointmentInfoDialog extends AppointmentDialogBase {

  public static final String BEAN_NAME = "ModifyAppointmentInfoDialog";
  /**
   *
   */
  private static final long serialVersionUID = 1122213930081721315L;
  private static final String MSG_DIALOG_TITLE_MEETING =
    "event_modif_information_dialog_title";
  private static final String MSG_DIALOG_TITLE_EVENT =
    "event_modif_information_dialog_event_title";
  private static final String MSG_DIALOG_DESC_MEETING =
    "event_modif_information_dialog_description";
  private static final String MSG_DIALOG_DESC_EVENT =
    "event_modif_information_dialog_event_description";
  private static final String MSG_DIALOG_BROWSER_TITLE =
    "event_modif_information_dialog_browser_title";
  private static final String MSG_DIALOG_ICON_TOOLTIP =
    "event_modif_information_dialog_icon_tooltip";

  @Override
  public void init(Map<String, String> parameters) {
    super.init(parameters);
    if (parameters != null) {
      setupAppointement();
    }
    this.logRecord.setService("Event");
    this.logRecord.setActivity("Modify appointment info");
  }

  @Override
  protected String finishImpl(FacesContext context, String outcome)
    throws Exception {
    this.validateDetails(getAppointment());

    // if the fields are not valid, stay in the current step
    if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
      isFinished = false;
      return null;
    } else {
      getEventService()
        .updateAppointment(
          getActionNode().getNodeRef(),
          getAppointment(),
          getUpdateMode(),
          AppointmentUpdateInfo.GeneralInformation
        );
    }

    return outcome;
  }

  @Override
  protected void validateDetails(Appointment appointment) {
    final String title = appointment.getTitle();

    if (title == null || title.trim().length() < 1) {
      Utils.addErrorMessage(translate(MSG_ERROR_TITLE_MISSING));
    }
  }

  public String getBrowserTitle() {
    return translate(MSG_DIALOG_BROWSER_TITLE);
  }

  public String getPageIconAltText() {
    return translate(MSG_DIALOG_ICON_TOOLTIP);
  }

  public String getContainerTitle() {
    return isMeeting()
      ? translate(MSG_DIALOG_TITLE_MEETING)
      : translate(MSG_DIALOG_TITLE_EVENT);
  }

  public String getContainerDescription() {
    return isMeeting()
      ? translate(MSG_DIALOG_DESC_MEETING, getAppointment().getTitle())
      : translate(MSG_DIALOG_DESC_EVENT, getAppointment().getTitle());
  }
}
