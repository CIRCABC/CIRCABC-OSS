package io.swagger.api;

import io.swagger.model.Appointment;
import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.EventFilter;
import io.swagger.model.EventItem;
import io.swagger.model.PagedEventItems;
import io.swagger.model.UpdateMode;
import java.util.Date;
import java.util.List;

/**
 * Business operations for managing events and calendars within CIRCABC.
 *
 * <p>This API defines the contract for the event-related REST endpoints exposed by
 * the CIRCABC backend. Events belong either to an Interest Group calendar or to an
 * individual user's calendar. The operations cover retrieving events (optionally
 * filtered by date range, period or paged), creating, updating and deleting events
 * within an Interest Group, and responding to meeting requests on behalf of a user.
 *
 * <p>Implementations of this interface contain the concrete business logic and are
 * invoked by the corresponding Alfresco webscript endpoint classes.
 */
public interface EventsApi {
  /**
   * Retrieves the events and calendar of a single Interest Group.
   *
   * <p>Backs the {@code GET /groups/{id}/events} endpoint. When a specific month is
   * requested the calendar object for that month is returned, otherwise the calendar
   * for the current month is used.
   *
   * @param id the identifier of the Interest Group
   * @param dateFrom the start of the date range to retrieve events for
   * @param dateTo the end of the date range to retrieve events for
   * @param language the language used to localise the returned events
   * @return the list of events matching the given Interest Group and date range
   */
  List<EventItem> groupsIdEventsGet(
    String id,
    Date dateFrom,
    Date dateTo,
    String language
  );

  /**
   * Retrieves a paged list of events of a single Interest Group matching a filter.
   *
   * @param igId the identifier of the Interest Group
   * @param filter the filter expression applied to the events
   * @param exactDate the reference date used together with the filter
   * @param startItem the zero-based index of the first item to return
   * @param amount the maximum number of items to return
   * @param sort the sort order applied to the results
   * @return the paged collection of events matching the filter
   */
  PagedEventItems groupsIdEventsListGet(
    String igId,
    String filter,
    Date exactDate,
    int startItem,
    int amount,
    String sort
  );

  /**
   * Creates a new event within an Interest Group.
   *
   * <p>Backs the {@code POST /groups/{id}/events} endpoint.
   *
   * @param id the identifier of the Interest Group in which the event is created
   * @param appointmentBody the serialised appointment payload describing the event
   */
  void groupsIdEventsPost(String id, String appointmentBody);

  /**
   * Deletes an event within an Interest Group.
   *
   * @param id the identifier of the event to delete
   * @param updateMode indicates whether a single occurrence or the whole series is
   *     affected for recurring events
   */
  void eventsIdDelete(String id, UpdateMode updateMode);

  /**
   * Retrieves the details of a single event.
   *
   * @param id the identifier of the event
   * @return the appointment describing the event
   */
  Appointment eventsIdGet(String id);

  /**
   * Replaces an existing event within an Interest Group.
   *
   * @param id the identifier of the event to replace
   * @param appointmentBody the serialised appointment payload with the new values
   * @param appointmentUpdateInfo additional information describing how the update is
   *     applied
   * @param updateMode indicates whether a single occurrence or the whole series is
   *     affected for recurring events
   */
  void eventsIdPut(
    String id,
    String appointmentBody,
    AppointmentUpdateInfo appointmentUpdateInfo,
    UpdateMode updateMode
  );

  /**
   * Retrieves the events of a given user within a date range.
   *
   * @param userId the identifier of the user
   * @param dateFrom the start of the date range
   * @param dateTo the end of the date range
   * @return the list of events for the user within the given range
   */
  List<EventItem> usersIdEventsGet(String userId, Date dateFrom, Date dateTo);

  /**
   * Retrieves the events of a given user relative to a period.
   *
   * <p>The period is expressed by the {@code filter} (for example previous, exact or
   * future) evaluated against the supplied reference date.
   *
   * @param userId the identifier of the user
   * @param exactDate the reference date used to evaluate the period
   * @param filter the period filter applied to the user's events
   * @return the list of events for the user matching the requested period
   */
  List<EventItem> usersIdEventsGet(
    String userId,
    Date exactDate,
    EventFilter filter
  );

  /**
   * Accepts or rejects a meeting request on behalf of a user.
   *
   * @param userId the identifier of the user responding to the meeting request
   * @param meetingId the identifier of the meeting being responded to
   * @param action the response action to apply (for example accept or reject)
   * @param updateMode indicates whether a single occurrence or the whole series is
   *     affected for recurring meetings
   */
  void usersIdEventsPost(
    String userId,
    String meetingId,
    String action,
    String updateMode
  );
}
