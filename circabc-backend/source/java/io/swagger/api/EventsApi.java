package io.swagger.api;

import eu.cec.digit.circabc.service.event.*;
import io.swagger.model.PagedEventItems;

import java.util.Date;
import java.util.List;

public interface EventsApi {

    // get the events and the calendar object of one Interest group
    // if the parameter month is selected, then it return the calendar object
    // for the specified month else it returns for the current month
    // GET /groups/{id}/events",
    List<EventItem> groupsIdEventsGet(String id, Date dateFrom, Date dateTo, String language);

    /**
     * get the events of one Interest group given a filter
     */
    PagedEventItems groupsIdEventsListGet(
            String igId, String filter, Date exactDate, int startItem, int amount, String sort);

    // create a new event within the Interest group
    // POST /groups/{id}/events",
    void groupsIdEventsPost(String id, String appointmentBody);

    /**
     * deletes an event within the interest group
     */
    void eventsIdDelete(String id, UpdateMode updateMode);

    /**
     * get the details of an event
     */
    Appointment eventsIdGet(String id);

    /**
     * replaces an event within the interest group
     */
    void eventsIdPut(
            String id,
            String appointmentBody,
            AppointmentUpdateInfo appointmentUpdateInfo,
            UpdateMode updateMode);

    /**
     * get the list of events of a given user within a date range
     */
    List<EventItem> usersIdEventsGet(String userId, Date dateFrom, Date dateTo);

    /**
     * get the list of events of a given user respect to a period (previous, exact, future)
     */
    List<EventItem> usersIdEventsGet(String userId, Date exactDate, EventFilter filter);

    /**
     * accept or reject a meeting request for the given user
     */
    void usersIdEventsPost(String userId, String meetingId, String action, String updateMode);
}
