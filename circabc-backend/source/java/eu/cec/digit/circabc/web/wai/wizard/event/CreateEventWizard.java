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
package eu.cec.digit.circabc.web.wai.wizard.event;

import eu.cec.digit.circabc.repo.event.EventImpl;
import eu.cec.digit.circabc.service.event.Event;
import eu.cec.digit.circabc.service.event.EventPriority;
import eu.cec.digit.circabc.service.event.EventType;
import eu.cec.digit.circabc.web.ui.repo.converter.EnumConverter;
import eu.cec.digit.circabc.web.wai.bean.navigation.event.AppointmentWebUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.util.List;
import java.util.Map;

/**
 * Bean that backs the Create event wizard
 *
 * @author yanick pignot
 */
public class CreateEventWizard extends CreateMeetingWizard {

    private static final long serialVersionUID = 625966444708662690L;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        this.logRecord.setService("Event");
        this.logRecord.setActivity("Create event");
        this.isLoggingEnabled = true;
    }


    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        // the contact info are setted in the last step.
        validateContact(getAppointment());
        applyUserSelection();
        validateOccurenceRate();

        // if the fields are not valid, stay in the current step
        if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
            isFinished = false;
            return null;
        } else {
            NodeRef nodeRef = getEventService()
                    .createEvent(getActionNode().getNodeRef(), (Event) getAppointment());
            logRecord
                    .setDocumentID((Long) getNodeService().getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
        }

        return outcome;
    }

    public String getBrowserTitle() {
        return translate("event_create_event_wizard_browser_title");
    }

    public String getPageIconAltText() {
        return translate("event_create_event_wizard_icon_tooltip");
    }

    /**
     * @return the available types
     */
    public List<SelectItem> getEventTypes() {
        return EnumConverter.convertEnumToSelectItem(
                FacesContext.getCurrentInstance(),
                EventType.values(),
                AppointmentWebUtils.MSG_PREFIX_EVENT_TYPE);
    }

    /**
     * @return the available Priorities
     */
    public List<SelectItem> getPriorities() {
        return EnumConverter.convertEnumToSelectItem(
                FacesContext.getCurrentInstance(),
                EventPriority.values(),
                AppointmentWebUtils.MSG_PREFIX_PRIORITY);
    }

    protected void reset() {
        setAppointment(new EventImpl());
        resetFileds();
        resetAppointment(getAppointment());
    }
}
