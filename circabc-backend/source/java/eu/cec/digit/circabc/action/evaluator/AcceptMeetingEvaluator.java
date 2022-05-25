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
package eu.cec.digit.circabc.action.evaluator;

import eu.cec.digit.circabc.service.event.Appointment;
import eu.cec.digit.circabc.service.event.AudienceStatus;
import eu.cec.digit.circabc.service.event.Meeting;
import eu.cec.digit.circabc.service.event.MeetingRequestStatus;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.event.AppointmentDetailsBean;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

import java.util.HashMap;


/**
 * Evaluate if the current user can accept a meeting. It means check if it is not already done.
 *
 * @author Yanick Pignot
 */
public class AcceptMeetingEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = 216436852785621419L;

    public boolean evaluate(final Node node) {
        final MeetingRequestStatus userStatus = getCurrentUserStatusOnMeeting(node);

        if (userStatus == null) {
            return false;
        } else {
            return !userStatus.equals(MeetingRequestStatus.Accepted);
        }
    }

    protected MeetingRequestStatus getCurrentUserStatusOnMeeting(Node node) {
        final Appointment appointment = (Appointment) node.getProperties()
                .get(AppointmentDetailsBean.PROPERTY_APPOINTEMENT_OBJECT);

        if (isClosedMeeting(appointment)) {
            final HashMap<String, MeetingRequestStatus> statuses = appointment.getAudience();
            final String userName = getCurrentUserName();

            if (userName == null || !statuses.containsKey(userName)) {
                return null;
            } else {
                return statuses.get(userName);
            }
        } else {
            return null;
        }
    }

    private String getCurrentUserName() {
        final CircabcNavigationBean navigator = Beans.getWaiNavigator();

        return (navigator.isGuest()) ? null : navigator.getCurrentUser().getUserName();
    }

    private boolean isClosedMeeting(Appointment appointment) {
        return appointment != null && appointment instanceof Meeting && AudienceStatus.Closed
                .equals(appointment.getAudienceStatus());
    }
}
