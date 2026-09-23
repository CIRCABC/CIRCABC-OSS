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
package eu.europa.ec.digit.circabc.rest.service.event;

import com.google.ical.values.DateValue;
import io.swagger.model.Appointment;
import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.Event;
import io.swagger.model.EventFilter;
import io.swagger.model.EventItem;
import io.swagger.model.Meeting;
import io.swagger.model.MeetingRequestStatus;
import io.swagger.model.UpdateMode;
import java.util.List;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service contract for managing CIRCABC calendar events, meetings and their appointments within an
 * Interest Group.
 *
 * <p>An Interest Group owns a single "event root" node under which all of its calendar entries are
 * stored. This service exposes operations to create events and meetings, to read and filter their
 * occurrences (including recurring appointments), to update or delete appointments, to resolve the
 * event root / IG root for a given node, and to manage meeting participation status. All operations
 * work against Alfresco {@link org.alfresco.service.cmr.repository.NodeRef} references and are
 * audited via the Alfresco {@link org.alfresco.service.Auditable} mechanism.
 *
 * @author Slobodan Filipovic
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface EventService {
  /**
   * Creates a new CIRCABC event under the given Interest Group event root.
   *
   * @param eventRoot the event root NodeRef of the Interest Group under which the event is created
   * @param event the event definition to create
   * @return the NodeRef of the newly created event
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = { "eventRoot", "event" }
  )
  NodeRef createEvent(final NodeRef eventRoot, final Event event);

  /**
   * Creates a new CIRCABC event under the given Interest Group event root, optionally sending
   * notification e-mails to the relevant recipients.
   *
   * @param eventRoot the event root NodeRef of the Interest Group under which the event is created
   * @param event the event definition to create
   * @param enableMailSending {@code true} to send notification e-mails, {@code false} to suppress
   *     them
   * @return the NodeRef of the newly created event
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "eventRoot", "event", "enableMailSending",
    }
  )
  NodeRef createEvent(
    final NodeRef eventRoot,
    final Event event,
    final boolean enableMailSending
  );

  /**
   * Creates a new CIRCABC meeting under the given Interest Group event root.
   *
   * @param eventRoot the event root NodeRef of the Interest Group under which the meeting is created
   * @param meeting the meeting definition to create
   * @return the NodeRef of the newly created meeting
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = { "eventRoot", "meeting" }
  )
  NodeRef createMeeting(final NodeRef eventRoot, final Meeting meeting);

  /**
   * Creates a new CIRCABC meeting under the given Interest Group event root, optionally sending
   * notification e-mails to the relevant recipients.
   *
   * @param eventRoot the event root NodeRef of the Interest Group under which the meeting is created
   * @param meeting the meeting definition to create
   * @param enableMailSending {@code true} to send notification e-mails, {@code false} to suppress
   *     them
   * @return the NodeRef of the newly created meeting
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "eventRoot", "meeting", "enableMailSending",
    }
  )
  NodeRef createMeeting(
    final NodeRef eventRoot,
    final Meeting meeting,
    final boolean enableMailSending
  );

  /**
   * Deletes an appointment (event or meeting occurrence). Depending on the given mode, this removes
   * only the single occurrence, this and all future occurrences, or every occurrence of the
   * recurring appointment to which the given node belongs.
   *
   * @param appointmentNodeRef the NodeRef of the appointment occurrence to delete
   * @param mode controls the scope of the deletion (single, future, or all occurrences)
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "appointmentNodeRef", "mode",
    }
  )
  void deleteAppointment(
    final NodeRef appointmentNodeRef,
    final UpdateMode mode
  );

  /**
   * Retrieves all appointments defined under the given Interest Group event root.
   *
   * @param eventRoot the event root NodeRef of the Interest Group
   * @return the list of all appointments under the event root
   */
  @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = { "eventRoot" })
  List<Appointment> getAllAppointments(final NodeRef eventRoot);

  /**
   * Returns all occurrences of the recurring appointment identified by the given node.
   *
   * @param appointementNodeRef the NodeRef of the appointment (definition) whose occurrences are
   *     resolved
   * @return the list of individual occurrences of the appointment
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "filter", "appointementNodeRef",
    }
  )
  List<EventItem> getAllOccurences(final NodeRef appointementNodeRef);

  /**
   * Retrieves a single appointment by its node reference.
   *
   * @param eventItemId the NodeRef of the appointment to retrieve
   * @return the matching appointment
   */
  @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = { "eventItemId" })
  Appointment getAppointmentByNodeRef(final NodeRef eventItemId);

  /**
   * Retrieves the event items visible under the given event root, filtered for a specific user and
   * anchored on the supplied date.
   *
   * @param filter the filter controlling which events are returned
   * @param eventRoot the event root NodeRef of the Interest Group
   * @param userName the user for whom the events are resolved (used for visibility/participation)
   * @param date the reference date used together with the filter
   * @return the list of matching event items
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "filter", "eventRoot", "userName", "date",
    }
  )
  List<EventItem> getAppointments(
    final EventFilter filter,
    final NodeRef eventRoot,
    final String userName,
    final DateValue date
  );

  /**
   * Retrieves the calendar event items occurring on the given date under the specified event root.
   *
   * @param eventRoot the event root NodeRef of the Interest Group
   * @param date the date for which events are retrieved
   * @return the list of event items occurring on that date
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = { "eventRoot", "date" }
  )
  List<EventItem> getCalendarEventsOnDate(
    final NodeRef eventRoot,
    final DateValue date
  );

  /**
   * Retrieves the calendar event items occurring within the given date range (inclusive) under the
   * specified event root.
   *
   * @param eventRoot the event root NodeRef of the Interest Group
   * @param from the start date of the range
   * @param to the end date of the range
   * @return the list of event items occurring between the two dates
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = { "eventRoot", "from", "to" }
  )
  List<EventItem> getEventsBetweenDates(
    final NodeRef eventRoot,
    final DateValue from,
    final DateValue to
  );

  /**
   * Resolves the NodeRef of a specific occurrence of a recurring meeting.
   *
   * @param meetingDefinitionNodeRef the NodeRef of the meeting definition
   * @param recurrenceId the identifier of the recurrence (occurrence) to resolve
   * @return the NodeRef of the matching meeting occurrence
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "meetingDefinitionNodeRef", "recurrenceId",
    }
  )
  NodeRef getMeetingNodeRef(
    final NodeRef meetingDefinitionNodeRef,
    final String recurrenceId
  );

  /**
   * Returns the participation status of the given user for the specified meeting.
   *
   * @param eventItemId the NodeRef of the meeting event item
   * @param userName the user whose meeting request status is retrieved
   * @return the meeting request status for the user
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = { "eventItemId", "userName" }
  )
  MeetingRequestStatus getMeetingStatus(
    final NodeRef eventItemId,
    final String userName
  );

  /**
   * Sets the meeting request (participation) status for a user on the given meeting.
   *
   * @param meetingNodeRef the NodeRef of the meeting
   * @param userName the user whose status is updated
   * @param meetingRequestStatus the new participation status to apply
   * @param mode controls the scope of the change (single, future, or all occurrences)
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "meetingNodeRef", "userName", "meetingRequestStatus", "mode",
    }
  )
  void setMeetingRequestStatus(
    final NodeRef meetingNodeRef,
    final String userName,
    final MeetingRequestStatus meetingRequestStatus,
    final UpdateMode mode
  );

  /**
   * Updates an existing appointment (event or meeting occurrence).
   *
   * @param appointmentNodeRef the NodeRef of the appointment to update
   * @param appointment the new appointment data to apply
   * @param mode controls the scope of the update (single, future, or all occurrences)
   * @param updateInfo additional information describing how the update should be applied
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "appointmentNodeRef", "appointment", "mode", "updateInfo",
    }
  )
  void updateAppointment(
    final NodeRef appointmentNodeRef,
    final Appointment appointment,
    final UpdateMode mode,
    final AppointmentUpdateInfo updateInfo
  );

  /**
   * Updates an existing appointment (event or meeting occurrence), optionally sending notifications
   * to the affected participants.
   *
   * @param appointmentNodeRef the NodeRef of the appointment to update
   * @param appointment the new appointment data to apply
   * @param mode controls the scope of the update (single, future, or all occurrences)
   * @param updateInfo additional information describing how the update should be applied
   * @param sendNotification {@code true} to notify participants of the change, {@code false} to
   *     suppress notifications
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "appointmentNodeRef",
      "appointment",
      "mode",
      "updateInfo",
      "sendNotification",
    }
  )
  void updateAppointment(
    final NodeRef appointmentNodeRef,
    final Appointment appointment,
    final UpdateMode mode,
    final AppointmentUpdateInfo updateInfo,
    final Boolean sendNotification
  );

  /**
   * Returns the event root node of the Interest Group identified by the given IG id.
   *
   * @param igId the identifier of the Interest Group
   * @return the NodeRef of the Interest Group's event root
   */
  @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = { "igId" })
  NodeRef getIGsEventRoot(String igId);

  /**
   * Returns the Interest Group root node that contains the given event.
   *
   * @param eventId the identifier of the event whose IG root is resolved
   * @return the NodeRef of the containing Interest Group root
   */
  @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = { "eventId" })
  NodeRef getIGRoot(String eventId);

  /**
   * Builds an {@link io.swagger.model.EventItem} representation for the given node, enriched with
   * the owning Interest Group's name and title.
   *
   * @param interestGroupName the technical name of the owning Interest Group
   * @param interestGroupTitle the display title of the owning Interest Group
   * @param nodeRef the NodeRef of the event/appointment node to represent
   * @return the built event item
   */
  EventItem buildEventItem(
    final String interestGroupName,
    final String interestGroupTitle,
    NodeRef nodeRef
  );

  /**
   * Sends the notifications triggered by an appointment update, comparing the previous and new
   * state to determine the appropriate recipients and messages.
   *
   * @param appointmentNodeRef the NodeRef of the updated appointment occurrence
   * @param appointment the appointment in its new (updated) state
   * @param mode the scope with which the update was applied (single, future, or all occurrences)
   * @param updateInfo additional information describing how the update was applied
   * @param oldAppointment the appointment in its previous state, used to detect changes
   * @param appointmentDefinition the NodeRef of the appointment definition
   * @param eventRoot the event root NodeRef of the owning Interest Group
   */
  @Auditable(
    /* key = Auditable.Key.ARG_0, */ parameters = {
      "appointmentNodeRef",
      "appointment",
      "mode",
      "updateInfo",
      "oldAppointment",
      "appointmentDefinition",
      "eventRoot",
    }
  )
  public void sendNotificationsAfterUpdate(
    NodeRef appointmentNodeRef,
    Appointment appointment,
    UpdateMode mode,
    AppointmentUpdateInfo updateInfo,
    Appointment oldAppointment,
    final NodeRef appointmentDefinition,
    final NodeRef eventRoot
  );
}
