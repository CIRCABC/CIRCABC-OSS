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

import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.service.event.*;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.wizard.event.CreateEventWizard;
import eu.cec.digit.circabc.web.wai.wizard.event.InternalUser;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Bean that back the the common actions for meetings and event dialogs
 *
 * @author Yanick Pignot
 */
public abstract class AppointmentDialogBase extends CreateEventWizard {

    private static final String CHOICE_THIS = UpdateMode.Single.name();
    private static final String CHOICE_SERIES = UpdateMode.AllOccurences.name();
    private static final String CHOICE_FUTURS = UpdateMode.FuturOccurences.name();

    private static final String MSG_DIALOG_MEETING_INSTRUCTION = "event_edit_recurrent_meeting_dialog_description";
    private static final String MSG_DIALOG_EVENT_INSTRUCTION = "event_edit_recurrent_event_dialog_instruction";

    private String occurenceChoice = null;


    @Override
    public void init(Map<String, String> parameters) {
        if (parameters != null) {
            super.init(parameters);

            checkParams(parameters);

            reset();
            resetFileds();

            occurenceChoice = CHOICE_THIS;
        }
    }

    @Override
    protected void checkParams(Map<String, String> parameters) {
        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id is a manatory parameter");
        } else if (!NavigableNodeType.EVENT_APPOINTMENT.isNodeFromType(getActionNode())) {
            throw new IllegalArgumentException("Type " + EventModel.TYPE_EVENT + " expected");
        }
    }

    @Override
    protected void reset() {
        final RetryingTransactionHelper txnHelper = Repository
                .getRetryingTransactionHelper(FacesContext.getCurrentInstance());
        final RetryingTransactionCallback<Appointment> callback = new RetryingTransactionCallback<Appointment>() {
            public Appointment execute() throws Throwable {
                return getEventService().getAppointmentByNodeRef(getActionNode().getNodeRef());
            }
        };
        final Appointment appointment = txnHelper.doInTransaction(callback, true);
        setAppointment(appointment);
    }

    @Override
    public boolean getFinishButtonDisabled() {
        return false;
    }

    protected void setupAppointement() {

        if (AudienceStatus.Closed.equals(getAppointment().getAudienceStatus())) {
            final List<String> users = getAppointment().getInvitedUsers();
            internalUsers = new ArrayList<>(users.size());
            externalUser = "";

            NodeRef userRef = null;
            Map<QName, Serializable> properties;
            for (final String user : users) {
                if (getPersonService().personExists(user)) {
                    userRef = getPersonService().getPerson(user);
                    properties = getNodeService().getProperties(userRef);
                    internalUsers.add(
                            new InternalUser(user,
                                    (String) properties.get(ContentModel.PROP_FIRSTNAME),
                                    (String) properties.get(ContentModel.PROP_LASTNAME),
                                    (String) properties.get(ContentModel.PROP_EMAIL))
                    );
                } else {
                    externalUser += user + "\n";
                }
            }
        } else {
            internalUsers = new ArrayList<>(5);
            externalUser = "";
        }

        Collections.sort(internalUsers);

        final OccurenceRate occurencerate = getAppointment().getOccurenceRate();
        final MainOccurence occurencemain = occurencerate.getMainOccurence();
        if (!MainOccurence.OnlyOnce.equals(occurencemain)) {
            if (MainOccurence.Times.equals(occurencemain)) {
                setTimesOneAsString(String.valueOf(occurencerate.getTimes()));
            } else {
                setEveryAsString(String.valueOf(occurencerate.getEvery()));
                setTimesTwoAsString(String.valueOf(occurencerate.getTimes()));
            }
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        Utils.addErrorMessage("DIALOG NOT IMPLEMENTED YET !!! ");

        return outcome;
    }


    public boolean isMeeting() {
        return getAppointment() instanceof Meeting;
    }

    public boolean isRecurrent() {
        return !MainOccurence.OnlyOnce.equals(getAppointment().getOccurenceRate().getMainOccurence());
    }

    /**
     * @return the occurenceChoice
     */
    public final String getOccurenceChoice() {
        return occurenceChoice;
    }

    /**
     * @param occurenceChoice the occurenceChoice to set
     */
    public final void setOccurenceChoice(String occurenceChoice) {
        if (occurenceChoice == null) {
            // jsf should not pass a null object in a submit. But can in a refresh of the page.
            return;
        } else if (!(occurenceChoice.equals(CHOICE_THIS) || occurenceChoice.equals(CHOICE_SERIES)
                || occurenceChoice.equals(CHOICE_FUTURS))) {
            throw new IllegalArgumentException(
                    "Only following choices are possible: " + CHOICE_THIS + " Or " + CHOICE_SERIES + " Or "
                            + CHOICE_FUTURS);
        }

        this.occurenceChoice = occurenceChoice;
    }

    public String getInstruction() {
        return isMeeting() ? translate(MSG_DIALOG_MEETING_INSTRUCTION)
                : translate(MSG_DIALOG_EVENT_INSTRUCTION);
    }

    protected UpdateMode getUpdateMode() {
        return UpdateMode.valueOf(this.occurenceChoice);
    }
}
