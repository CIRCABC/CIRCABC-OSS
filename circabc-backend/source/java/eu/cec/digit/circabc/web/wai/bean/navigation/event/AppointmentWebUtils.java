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
package eu.cec.digit.circabc.web.wai.bean.navigation.event;

import eu.cec.digit.circabc.service.event.*;
import eu.cec.digit.circabc.web.WebClientHelper;


/**
 * Utilitary methods for Appointments
 *
 * @author yanick pignot
 */
public class AppointmentWebUtils {

    public static final String MSG_PREFIX_MEETING_TYPE = "event_create_meetings_wizard_step1_type_";
    public static final String MSG_PREFIX_AVAILABILITY = "event_create_meetings_wizard_step1_availability_";
    public static final String MSG_PREFIX_AUDIENCE_STATUS = "event_create_meetings_wizard_step1_audience_status_";
    public static final String MSG_PREFIX_TIMES_OCCURENCE = "event_create_meetings_wizard_step1_occurs_";
    public static final String MSG_PREFIX_EVERY_TIMES_OCCURENCE = MSG_PREFIX_TIMES_OCCURENCE;
    public static final String MSG_PREFIX_REQUEST_STATUS = "event_view_meetings_details_dialog_";

    public static final String MSG_PREFIX_EVENT_TYPE = "event_create_event_wizard_step1_type_";
    public static final String MSG_PREFIX_PRIORITY = "event_create_event_wizard_step1_priority_";

    private AppointmentWebUtils() {
    }

    public static String translate(final EventType eventType) {
        return translate(eventType, MSG_PREFIX_EVENT_TYPE);
    }

    public static String translate(final MeetingType meetingType) {
        return translate(meetingType, MSG_PREFIX_MEETING_TYPE);
    }

    public static String translate(final MeetingAvailability meetingAvailability) {
        return translate(meetingAvailability, MSG_PREFIX_AVAILABILITY);
    }

    public static String translate(final EventPriority eventPriority) {
        return translate(eventPriority, MSG_PREFIX_PRIORITY);
    }

    public static String translate(final AudienceStatus audienceStatus) {
        return translate(audienceStatus, MSG_PREFIX_AUDIENCE_STATUS);
    }

    public static String translate(final TimesOccurence timesOccurence) {
        return translate(timesOccurence, MSG_PREFIX_TIMES_OCCURENCE);
    }

    public static String translate(final EveryTimesOccurence everyTimesOccurence) {
        return translate(everyTimesOccurence, MSG_PREFIX_EVERY_TIMES_OCCURENCE);
    }

    public static String translate(final MeetingRequestStatus meetingRequestStatus) {
        return translate(meetingRequestStatus, MSG_PREFIX_REQUEST_STATUS);
    }


    private static String translate(final Enum enumItem, String messagePrefix) {
        if (enumItem == null) {
            return "";
        }
        return WebClientHelper.translate(messagePrefix + enumItem.name().toLowerCase());
    }


}
