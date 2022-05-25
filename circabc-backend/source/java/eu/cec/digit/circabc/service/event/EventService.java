/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.event;

import com.google.ical.values.DateValue;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * @author Slobodan Filipovic
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface EventService {

    /**
     * Create circabc event
     *
     * @param eventRoot root event NodeRef of interest group
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"eventRoot", "event"})
    NodeRef createEvent(final NodeRef eventRoot, final Event event);

    /**
     * Crete circabc event
     *
     * @param eventRoot root event NodeRef of interest group
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {"eventRoot", "event", "enableMailSending"})
    NodeRef createEvent(final NodeRef eventRoot, final Event event, final boolean enableMailSending);

    /**
     * Create circabc meeting
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"eventRoot", "meeting"})
    NodeRef createMeeting(final NodeRef eventRoot, final Meeting meeting);

    /**
     * Crete circabc meeting
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {"eventRoot", "meeting", "enableMailSending"})
    NodeRef createMeeting(
            final NodeRef eventRoot, final Meeting meeting, final boolean enableMailSending);

    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"eventRoot"})
    @Deprecated
    void deleteAllEvents(final NodeRef eventRoot);

    /**
     * Delete appintment(event or meeting) depending of mode parameter it can delete only one , future
     * ,or all instances of appointmen in wich appointmentNodeRef bellongs
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"appointmentNodeRef", "mode"})
    void deleteAppointment(final NodeRef appointmentNodeRef, final UpdateMode mode);

    /**
     * Get all appointments for event root
     *
     * @param eventRoot root event NodeRef of interest group
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"eventRoot"})
    List<Appointment> getAllAppointments(final NodeRef eventRoot);

    /**
     * @param filter
     * @return
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"filter", "appointementNodeRef"})
    List<EventItem> getAllOccurences(final NodeRef appointementNodeRef);

    /**
     * @param eventItemId
     * @return
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"eventItemId"})
    Appointment getAppointmentByNodeRef(final NodeRef eventItemId);

    /**
     * @param filter
     * @param eventRoot
     * @param userName
     * @param date
     * @return
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {"filter", "eventRoot", "userName", "date"})
    List<EventItem> getAppointments(
            final EventFilter filter,
            final NodeRef eventRoot,
            final String userName,
            final DateValue date);

    /**
     * @param eventRoot
     * @param date
     * @return
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"eventRoot", "date"})
    List<EventItem> getCalendarEventsOnDate(final NodeRef eventRoot, final DateValue date);

    /**
     * @param eventRoot
     * @param from
     * @param to
     * @return
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"eventRoot", "from", "to"})
    List<EventItem> getEventsBetweenDates(
            final NodeRef eventRoot, final DateValue from, final DateValue to);

    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {"meetingDefinitionNodeRef", "recurrenceId"})
    NodeRef getMeetingNodeRef(final NodeRef meetingDefinitionNodeRef, final String recurrenceId);

    /**
     * @param eventItemId
     * @param userName
     * @return
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"eventItemId", "userName"})
    MeetingRequestStatus getMeetingStatus(final NodeRef eventItemId, final String userName);

    /**
     * Set Meeting request status
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {
            "meetingNodeRef",
            "userName",
            "meetingRequestStatus",
            "mode"
    })
    void setMeetingRequestStatus(
            final NodeRef meetingNodeRef,
            final String userName,
            final MeetingRequestStatus meetingRequestStatus,
            final UpdateMode mode);

    /**
     * @param appointmentNodeRef
     * @param appointment
     * @param mode
     * @param updateInfo
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {
            "appointmentNodeRef",
            "appointment",
            "mode",
            "updateInfo"
    })
    void updateAppointment(
            final NodeRef appointmentNodeRef,
            final Appointment appointment,
            final UpdateMode mode,
            final AppointmentUpdateInfo updateInfo);

    /**
     * @param appointmentNodeRef
     * @param appointment
     * @param mode
     * @param updateInfo
     * @param sendNotification
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {
            "appointmentNodeRef",
            "appointment",
            "mode",
            "updateInfo",
            "sendNotification"
    })
    void updateAppointment(
            final NodeRef appointmentNodeRef,
            final Appointment appointment,
            final UpdateMode mode,
            final AppointmentUpdateInfo updateInfo,
            final Boolean sendNotification);

    /**
     * Get the event root of the given IG (given by igId)
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"igId"})
    NodeRef getIGsEventRoot(String igId);

    /**
     * Gets the IGRoot of a node.
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"eventId"})
    NodeRef getIGRoot(String eventId);

    /**
     * @param interesGroupName
     * @param nodeRef
     * @return
     */
    EventItem buildEventItem(
            final String interestGroupName, final String interestGroupTitle, NodeRef nodeRef);

    /**
     * @param appointmentNodeRef
     * @param appointment
     * @param mode
     * @param updateInfo
     * @param oldAppointment
     * @param appointmentDefinition
     * @param eventRoot
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {
            "appointmentNodeRef",
            "appointment",
            "mode",
            "updateInfo",
            "oldAppointment",
            "appointmentDefinition",
            "eventRoot"
    })
    public void sendNotificationsAfterUpdate(
            NodeRef appointmentNodeRef,
            Appointment appointment,
            UpdateMode mode,
            AppointmentUpdateInfo updateInfo,
            Appointment oldAppointment,
            final NodeRef appointmentDefinition,
            final NodeRef eventRoot);
}
