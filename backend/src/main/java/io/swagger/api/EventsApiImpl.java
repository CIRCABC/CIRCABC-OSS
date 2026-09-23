package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.event.AppointmentUtils;
import eu.europa.ec.digit.circabc.rest.service.event.EventImpl;
import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import eu.europa.ec.digit.circabc.rest.service.event.MeetingImpl;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.model.Appointment;
import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.AudienceStatus;
import io.swagger.model.Event;
import io.swagger.model.EventFilter;
import io.swagger.model.EventItem;
import io.swagger.model.EventPriority;
import io.swagger.model.EventType;
import io.swagger.model.EveryTimesOccurence;
import io.swagger.model.InterestGroup;
import io.swagger.model.InterestGroupProfile;
import io.swagger.model.LogRecord;
import io.swagger.model.MainOccurence;
import io.swagger.model.Meeting;
import io.swagger.model.MeetingAvailability;
import io.swagger.model.MeetingRequestStatus;
import io.swagger.model.OccurenceRate;
import io.swagger.model.PagedEventItems;
import io.swagger.model.TimesOccurence;
import io.swagger.model.UpdateMode;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.EventModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.text.SimpleDateFormat;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.MalformedNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Business-logic implementation of {@link EventsApi} that manages calendar appointments (events and
 * meetings) within CIRCABC Interest Groups.
 *
 * <p>This class backs the {@code /events}, {@code /groups/{id}/events} and {@code
 * /users/{id}/events} REST endpoints. It supports creating, reading, updating and deleting
 * appointments, listing appointments for an Interest Group or a user (with filtering, paging and
 * sorting), and accepting/rejecting meeting invitations. Incoming appointment payloads are received
 * as raw JSON strings and are parsed into {@link Appointment} instances ({@link Event} or {@link
 * Meeting}) by the private {@code parse*} helpers, which also perform mandatory-field validation.
 *
 * @author schwerr
 */
public class EventsApiImpl implements EventsApi {

  /** Message fragment appended when a mandatory JSON property is missing. */
  private static final String IS_MANDATORY = "' is mandatory.";

  /** Message prefix used when reporting a missing or invalid JSON property. */
  private static final String PROPERTY = "Property '";

  /** Logger for this implementation. */
  private final Log logger = LogFactory.getLog(EventsApiImpl.class);

  @Autowired
  private EventService eventService;

  @Autowired
  private PersonService personService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private SearchService searchService;

  @Autowired
  private UserService userService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private UsersApi usersApi;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private CircabcService circabcService;

  /**
   * Lists all appointments of an Interest Group that fall between two dates.
   *
   * <p>Backs {@code GET /groups/{id}/events}. The returned items are enriched with their Interest
   * Group id and time zone and sorted by appointment date in descending order.
   *
   * @param id the Interest Group id whose events root is queried
   * @param dateFrom inclusive lower bound of the date range; must not be {@code null}
   * @param dateTo inclusive upper bound of the date range; must not be {@code null}
   * @param language the requested content language
   * @return the matching events, enriched and sorted by date (most recent first)
   * @throws IllegalArgumentException if {@code dateFrom} or {@code dateTo} is {@code null}
   */
  @Override
  public List<EventItem> groupsIdEventsGet(
    String id,
    Date dateFrom,
    Date dateTo,
    String language
  ) {
    if (dateFrom == null || dateTo == null) {
      throw new IllegalArgumentException(
        "'dateFrom' and 'dateTo' cannot be null."
      );
    }

    NodeRef eventRoot = eventService.getIGsEventRoot(id);

    List<EventItem> eventItems = eventService.getEventsBetweenDates(
      eventRoot,
      AppointmentUtils.convertDateToDateValue(dateFrom),
      AppointmentUtils.convertDateToDateValue(dateTo)
    );

    adaptEventItemsWithIGIdAndTimeZone(eventItems);

    sortEventItems("appointmentDate_DESC", eventItems);

    return eventItems;
  }

  /**
   * Creates a new appointment (event or meeting) in an Interest Group.
   *
   * <p>Backs {@code POST /groups/{id}/events}. The raw JSON body is parsed and, depending on its
   * {@code appointmentTypeEvent} flag, either an event or a meeting is created. The operation is
   * performed with {@link MLPropertyInterceptor} multilingual awareness temporarily disabled and is
   * recorded in a {@link LogRecord}.
   *
   * @param id the Interest Group id whose events root receives the new appointment
   * @param appointmentBody the JSON payload describing the appointment to create
   * @throws IllegalArgumentException if the body is invalid or the appointment type is neither an
   *     event nor a meeting
   */
  @Override
  public void groupsIdEventsPost(String id, String appointmentBody) {
    LogRecord logRecord = new LogRecord();
    logRecord.setService("Event");

    NodeRef eventRoot = eventService.getIGsEventRoot(id);

    Appointment appointment = parseBodyJSON(appointmentBody, false);

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    // create an event or meeting according to the appointment type
    try {
      MLPropertyInterceptor.setMLAware(false);

      switch (appointment) {
        case Event event -> {
          logRecord.setActivity("Create event");
          NodeRef nodeRef = eventService.createEvent(eventRoot, event);
          logRecord.setDocumentID(
            (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID)
          );
        }
        case Meeting meeting -> {
          logRecord.setActivity("Create meeting");
          /* NodeRef nodeRef = */
          eventService.createMeeting(eventRoot, meeting);
        }
        default -> throw new IllegalArgumentException(
          "Invalid appointment type provided. Must be Event or Meeting."
        );
      }
    } catch (Exception e) {
      logger.error("Exception when creating event or meeting.", e);
      throw e;
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }

  /**
   * Deletes an appointment.
   *
   * <p>Backs {@code DELETE /events/{id}}. For recurring appointments the {@code updateMode}
   * determines whether a single occurrence, all occurrences or future occurrences are removed.
   *
   * @param id the appointment node id to delete
   * @param updateMode the scope of the deletion for recurring appointments
   * @see io.swagger.api.EventsApi#eventsIdDelete(java.lang.String,
   *     eu.cec.digit.circabc.service.event.UpdateMode)
   */
  @Override
  public void eventsIdDelete(String id, UpdateMode updateMode) {
    NodeRef appointmentNodeRef = getAppointmentNodeRef(id);

    eventService.deleteAppointment(appointmentNodeRef, updateMode);
  }

  /**
   * Retrieves a single appointment by id.
   *
   * <p>Backs {@code GET /events/{id}}. The appointment's invited users are adapted so that internal
   * user ids are resolved to email addresses when the user is not a member of the owning Interest
   * Group (see {@link #adaptInvitedUsers(String, List)}).
   *
   * @param id the appointment node id to retrieve
   * @return the appointment with its invited-user list adapted
   * @see io.swagger.api.EventsApi#eventsIdGet(java.lang.String)
   */
  @Override
  public Appointment eventsIdGet(String id) {
    NodeRef appointmentNodeRef = getAppointmentNodeRef(id);

    Appointment appointment = eventService.getAppointmentByNodeRef(
      appointmentNodeRef
    );

    String igId = eventService.getIGRoot(id).getId();
    List<String> newInvitedUsers = adaptInvitedUsers(
      igId,
      appointment.getInvitedUsers()
    );
    appointment.setInvitedUsers(newInvitedUsers);

    return appointment;
  }

  /**
   * Adapts a list of invited users so that it only contains values usable by the client.
   *
   * <p>Email addresses and user ids that still belong to the Interest Group are kept as-is; user
   * ids that are no longer members are replaced by the corresponding email address.
   *
   * @param igId the Interest Group id used to resolve current members
   * @param invitedUsers the original invited users (user ids and/or emails)
   * @return the adapted list of invited users
   */
  private List<String> adaptInvitedUsers(
    String igId,
    List<String> invitedUsers
  ) {
    List<String> newInvitedUsers = new ArrayList<>();

    Set<String> userIds = circabcService.getUserIds(igId);

    for (String invitedUser : invitedUsers) {
      // check if it is an email
      if (invitedUser.contains("@")) {
        newInvitedUsers.add(invitedUser);
        continue;
      }
      if (userIds.contains(invitedUser)) {
        newInvitedUsers.add(invitedUser);
      } else {
        newInvitedUsers.add(userService.getUserEmail(invitedUser));
      }
    }

    return newInvitedUsers;
  }

  /**
   * Updates an existing appointment.
   *
   * <p>Backs {@code PUT /events/{id}}. The {@code appointmentUpdateInfo} selects which section of
   * the appointment is updated; when {@link AppointmentUpdateInfo#All} is used, the audience,
   * contact information, relevant space and general information are updated in sequence. The update
   * runs with {@link MLPropertyInterceptor} multilingual awareness temporarily disabled.
   *
   * @param id the appointment node id to update
   * @param appointmentBody the JSON payload with the new appointment data
   * @param appointmentUpdateInfo the section of the appointment to update
   * @param updateMode the scope of the update for recurring appointments
   * @throws IllegalArgumentException if {@link AppointmentUpdateInfo#RelevantSpace} is requested for
   *     a non-meeting appointment, or if the body is invalid
   * @see io.swagger.api.EventsApi#eventsIdPut(java.lang.String, java.lang.String,
   *     eu.cec.digit.circabc.service.event.AppointmentUpdateInfo,
   *     eu.cec.digit.circabc.service.event.UpdateMode)
   */
  @Override
  public void eventsIdPut(
    String id,
    String appointmentBody,
    AppointmentUpdateInfo appointmentUpdateInfo,
    UpdateMode updateMode
  ) {
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      MLPropertyInterceptor.setMLAware(false);

      NodeRef appointmentNodeRef = getAppointmentNodeRef(id);

      Appointment appointment = parseBodyJSON(appointmentBody, true);

      if (
        !(appointment instanceof Meeting) &&
        appointmentUpdateInfo.equals(AppointmentUpdateInfo.RelevantSpace)
      ) {
        throw new IllegalArgumentException(
          "'RelevantSpace' is only applicable for Meetings."
        );
      }

      if (AppointmentUpdateInfo.All.equals(appointmentUpdateInfo)) {
        eventService.updateAppointment(
          appointmentNodeRef,
          appointment,
          updateMode,
          AppointmentUpdateInfo.Audience,
          false
        );
        eventService.updateAppointment(
          appointmentNodeRef,
          appointment,
          updateMode,
          AppointmentUpdateInfo.ContactInformation,
          false
        );
        eventService.updateAppointment(
          appointmentNodeRef,
          appointment,
          updateMode,
          AppointmentUpdateInfo.RelevantSpace,
          false
        );
        eventService.updateAppointment(
          appointmentNodeRef,
          appointment,
          updateMode,
          AppointmentUpdateInfo.GeneralInformation,
          true
        );
      } else {
        eventService.updateAppointment(
          appointmentNodeRef,
          appointment,
          updateMode,
          appointmentUpdateInfo
        );
      }
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }

  /**
   * Lists a user's appointments matching a filter, optionally around an exact date.
   *
   * <p>Backs {@code GET /users/{id}/events} (filter-based variant). Each returned item is enriched
   * with its audience/meeting status, time zone and owning Interest Group id.
   *
   * @param userId the id of the user whose appointments are listed
   * @param exactDate optional reference date used by the filter; may be {@code null}
   * @param filter the filter selecting which appointments to return
   * @return the matching, enriched event items
   * @see io.swagger.api.EventsApi#usersIdEventsGet(java.lang.String, java.util.Date,
   *     eu.cec.digit.circabc.service.event.EventFilter)
   */
  @Override
  public List<EventItem> usersIdEventsGet(
    String userId,
    Date exactDate,
    EventFilter filter
  ) {
    List<EventItem> items = eventService.getAppointments(
      filter,
      null,
      userId,
      AppointmentUtils.convertDateToDateValue(exactDate)
    );
    for (EventItem eventItem : items) {
      String audience = (String) nodeService.getProperty(
        eventItem.getEventNodeRef(),
        EventModel.PROP_EVENT_AUDIENCE
      );
      eventItem.setMeetingStatus(audience);

      String timeZone = eventService
        .getAppointmentByNodeRef(eventItem.getEventNodeRef())
        .getTimeZoneId();
      eventItem.setTimeZone(timeZone);

      NodeRef igNodeRef = eventService.getIGRoot(
        eventItem.getEventNodeRef().getId()
      );
      eventItem.setInterestGroup(igNodeRef.getId());
    }
    return items;
  }

  /**
   * Lists a user's appointments occurring within a date range.
   *
   * <p>Backs {@code GET /users/{id}/events} (date-range variant). A Lucene search (run as the
   * system user) returns events between the two dates that are either {@code Open} or {@code Closed}
   * with the user among the invited users. For {@code Open} events the user must also be a member of
   * the owning Interest Group. Returned items are enriched with their meeting status and time zone.
   *
   * @param userId the id of the user whose appointments are listed; must not be {@code null}
   * @param dateFrom inclusive lower bound of the date range; must not be {@code null}
   * @param dateTo inclusive upper bound of the date range; must not be {@code null}
   * @return the matching, enriched event items
   * @throws IllegalArgumentException if {@code userId}, {@code dateFrom} or {@code dateTo} is {@code
   *     null}
   * @see io.swagger.api.EventsApi#usersIdEventsGet(java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public List<EventItem> usersIdEventsGet(
    String userId,
    Date dateFrom,
    Date dateTo
  ) {
    if (userId == null) {
      throw new IllegalArgumentException("'userId' cannot be null.");
    }

    if (dateFrom == null || dateTo == null) {
      throw new IllegalArgumentException(
        "'dateFrom' and 'dateTo' cannot be null."
      );
    }

    List<EventItem> eventItems = new ArrayList<>();

    ResultSet resultSet = null;

    try {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

      String startDate = simpleDateFormat.format(dateFrom);
      String endDate = simpleDateFormat.format(dateTo);

      String query =
        "TYPE:\"{http://www.cc.cec/circabc/model/events/1.0}event\" AND @ce\\:date:[\"" +
        startDate +
        "\" TO \"" +
        endDate +
        "\"] AND (@ce\\:audience:\"Open\" OR (@ce\\:audience:\"Closed\" AND @ce\\:invitedUsers:*" +
        userId +
        "*))";

      final SearchParameters searchParameters = new SearchParameters();

      searchParameters.setQuery(query);
      searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
      searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
      searchParameters.addSort("@" + EventModel.PROP_EVENT_DATE, true);

      resultSet = AuthenticationUtil.runAs(
        () -> searchService.query(searchParameters),
        AuthenticationUtil.getSystemUserName()
      );

      for (final ResultSetRow row : resultSet) {
        NodeRef eventNodeRef = row.getNodeRef();

        NodeRef igNodeRef = eventService.getIGRoot(eventNodeRef.getId());

        String audienceStatus = (String) nodeService.getProperty(
          eventNodeRef,
          EventModel.PROP_EVENT_AUDIENCE
        );

        // check if the userId is part of the IG where the event was defined (for Open events only)
        if (
          "Open".equals(audienceStatus) && !userInIG(userId, igNodeRef.getId())
        ) {
          continue;
        }

        EventItem eventItem = eventService.buildEventItem(
          igNodeRef.getId(),
          "",
          eventNodeRef
        );
        eventItem.setMeetingStatus(audienceStatus);

        String timeZone = eventService
          .getAppointmentByNodeRef(eventItem.getEventNodeRef())
          .getTimeZoneId();
        eventItem.setTimeZone(timeZone);
        eventItems.add(eventItem);
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw e;
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
    }

    return eventItems;
  }

  /**
   * Accepts or rejects a meeting invitation on behalf of a user.
   *
   * <p>Backs {@code POST /users/{id}/events}. When {@code userId} is empty the currently
   * authenticated user is used. The target node is validated to be a meeting before the request
   * status is updated.
   *
   * @param userId the id of the user responding to the invitation; if {@code null}/empty the
   *     authenticated user is used
   * @param meetingId the node id of the meeting the response applies to
   * @param action the response, either {@code "Accepted"} or {@code "Rejected"}
   * @param updateMode the scope of the response: {@code "Single"}, {@code "AllOccurences"} or {@code
   *     "FuturOccurences"}
   * @throws IllegalArgumentException if {@code action} or {@code updateMode} is invalid, or if the
   *     target node is not a meeting
   * @see io.swagger.api.EventsApi#usersIdEventsPost(java.lang.String, java.lang.String,
   *     java.lang.String, java.lang.String)
   */
  @Override
  public void usersIdEventsPost(
    String userId,
    String meetingId,
    String action,
    String updateMode
  ) {
    if (!("Rejected".equals(action) || "Accepted".equals(action))) {
      throw new IllegalArgumentException(
        "Invalid action. Must be 'Accepted' or 'Rejected'"
      );
    }

    if (
      !("Single".equals(updateMode) ||
        "AllOccurences".equals(updateMode) ||
        "FuturOccurences".equals(updateMode))
    ) {
      throw new IllegalArgumentException(
        "Invalid update mode. Must be " +
          "'Single', 'AllOccurences' or 'FuturOccurences'"
      );
    }

    if (userId == null || userId.isEmpty()) {
      userId = AuthenticationUtil.getFullyAuthenticatedUser();
    }

    NodeRef meetingNodeRef = Converter.createNodeRefFromId(meetingId);

    if (
      !(nodeService.getType(meetingNodeRef).equals(EventModel.TYPE_EVENT) &&
        "Meeting".equals(
          nodeService.getProperty(meetingNodeRef, EventModel.PROP_KIND_OF_EVENT)
        ))
    ) {
      throw new IllegalArgumentException(
        "Node with id " + meetingId + " is not a meeting."
      );
    }

    // once we know it's a meeting, apply the action
    eventService.setMeetingRequestStatus(
      meetingNodeRef,
      userId,
      MeetingRequestStatus.valueOf(action),
      UpdateMode.valueOf(updateMode)
    );
  }

  /**
   * Returns a paged, sorted list of appointments for an Interest Group (or across all groups).
   *
   * <p>Backs {@code GET /groups/{id}/events/list}. When {@code igId} is {@code null} appointments
   * from all Interest Groups are considered; otherwise only the {@code Events} folder of the given
   * group is used. Results are enriched with Interest Group id and time zone, sorted according to
   * {@code sort}, and then paged.
   *
   * @param igId the Interest Group id to restrict the listing to, or {@code null} for all groups
   * @param filter the {@link EventFilter} name selecting which appointments to return
   * @param exactDate optional reference date used by the filter; may be {@code null}
   * @param startItem zero-based index of the first item to return
   * @param amount maximum number of items to return; {@code 0} means return all items
   * @param sort the sort key (e.g. {@code appointmentDate_DESC}); ignored if not recognised
   * @return a {@link PagedEventItems} holding the requested page and the total result size
   * @throws IllegalArgumentException if the events root of the given Interest Group cannot be found
   * @see io.swagger.api.EventsApi#groupsIdEventsListGet(String, String, Date, int, int, String)
   */
  @Override
  public PagedEventItems groupsIdEventsListGet(
    String igId,
    String filter,
    Date exactDate,
    int startItem,
    int amount,
    String sort
  ) {
    NodeRef eventRoot = null;

    // if eventRoot is null, list all IGs
    if (igId != null) {
      eventRoot = Converter.createNodeRefFromId(igId);

      eventRoot = nodeService.getChildByName(
        eventRoot,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      );

      if (eventRoot == null) {
        throw new IllegalArgumentException(
          "The event root of IG " + igId + " was not found."
        );
      }
    }

    String userName = AuthenticationUtil.getFullyAuthenticatedUser();

    List<EventItem> eventItems = eventService.getAppointments(
      EventFilter.valueOf(filter),
      eventRoot,
      userName,
      AppointmentUtils.convertDateToDateValue(exactDate)
    );

    adaptEventItemsWithIGIdAndTimeZone(eventItems);

    int resultSize = eventItems.size();

    sortEventItems(sort, eventItems);

    List<EventItem> pagedEventItems;

    if (amount == 0) {
      // limit == 0 means that we want all items
      pagedEventItems = eventItems;
    } else {
      pagedEventItems = new ArrayList<>();

      int endItem = Math.min(startItem + amount, resultSize);

      for (int index = startItem; index < endItem; index++) {
        pagedEventItems.add(eventItems.get(index));
      }
    }

    return new PagedEventItems(pagedEventItems, resultSize);
  }

  private void sortEventItems(final String sort, List<EventItem> eventItems) {
    if (sort == null || (!sort.endsWith("_ASC") && !sort.endsWith("_DESC"))) {
      return;
    }

    Comparator<EventItem> comparator = resolveEventComparator(sort);
    if (comparator == null) {
      return;
    }

    if (sort.endsWith("_DESC")) {
      comparator = comparator.reversed();
    }

    eventItems.sort(comparator);
  }

  private Comparator<EventItem> resolveEventComparator(String sort) {
    if (sort.startsWith("appointmentDate")) {
      return Comparator.comparing(EventItem::getDate);
    } else if (sort.startsWith("title")) {
      return Comparator.comparing(
        EventItem::getTitle,
        String.CASE_INSENSITIVE_ORDER
      );
    } else if (sort.startsWith("appointmentType")) {
      return Comparator.comparing(EventItem::getEventType);
    } else if (sort.startsWith("contact")) {
      return Comparator.comparing(
        EventItem::getContact,
        String.CASE_INSENSITIVE_ORDER
      );
    } else if (sort.startsWith("meetingStatus")) {
      return Comparator.comparing(
        EventItem::getMeetingStatus,
        String.CASE_INSENSITIVE_ORDER
      );
    }
    return null;
  }

  /**
   * Checks if the given userId belongs to the given IG
   */
  private boolean userInIG(String userId, String igId) {
    List<InterestGroupProfile> profiles = usersApi.getUserMembership(userId);

    for (InterestGroupProfile profile : profiles) {
      InterestGroup ig = profile.getInterestGroup();

      if (ig.getId().equals(igId)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Sets each interest group of the list of events to the id of the interest group and timezone. This id is
   * returned later as part of the events.
   */
  private void adaptEventItemsWithIGIdAndTimeZone(List<EventItem> items) {
    for (EventItem item : items) {
      item.setInterestGroup(
        eventService.getIGRoot(item.getEventNodeRef().getId()).getId()
      );
      String timeZone = eventService
        .getAppointmentByNodeRef(item.getEventNodeRef())
        .getTimeZoneId();
      item.setTimeZone(timeZone);
    }
  }

  /**
   * Gets the nodeRef with the id of the given appointment
   */
  private NodeRef getAppointmentNodeRef(String id) {
    NodeRef appointmentNodeRef = Converter.createNodeRefFromId(id);

    if (!nodeService.exists(appointmentNodeRef)) {
      throw new IllegalArgumentException(
        "The Appointment with id '" + id + "' does not exist."
      );
    }

    return appointmentNodeRef;
  }

  /**
   * Parses the json string retrieved from the client and extracts all fields.
   */
  private Appointment parseBodyJSON(String appointmentBody, boolean forUpdate) {
    if (appointmentBody == null || appointmentBody.isEmpty()) {
      throw new IllegalArgumentException(
        "The body (appointment data) cannot be empty. It must contain the appointment data to add/update."
      );
    }

    JSONParser parser = new JSONParser();

    JSONObject json;

    try {
      json = (JSONObject) parser.parse(appointmentBody);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "Error when parsing the body (appointment data).",
        e
      );
    }

    Appointment appointment;

    boolean appointmentTypeEvent;

    try {
      appointmentTypeEvent = (Boolean) json.get("appointmentTypeEvent");
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "'appointmentTypeEvent' must be 'true' for an Event or 'false' for a Meeting.",
        e
      );
    }

    if (appointmentTypeEvent) {
      appointment = parseEventProperties(json);
    } else {
      appointment = parseMeetingProperties(json);
    }

    parseCommonProperties(json, appointment);

    parseDateInfo(json, appointment);
    parseOccurrenceRate(json, appointment);

    if (forUpdate) {
      parseUpdateId(json, appointment);
    }

    parseAttendantsInfo(json, appointment);

    return appointment;
  }

  private Appointment parseEventProperties(JSONObject json) {
    Event event = new EventImpl();
    event.setEventType(
      EventType.valueOf(getCheckedStringProperty(json, "eventType"))
    );
    event.setPriority(
      EventPriority.valueOf(getCheckedStringProperty(json, "eventPriority"))
    );
    return event;
  }

  private Appointment parseMeetingProperties(JSONObject json) {
    Meeting meeting = new MeetingImpl();

    Object meetingPublicAvailability = json.get("meetingPublicAvailability");
    if (!(meetingPublicAvailability instanceof Boolean)) {
      throw new IllegalArgumentException(
        "'meetingPublicAvailability' must be 'true' or 'false'."
      );
    }

    meeting.setAvailability(
      ((boolean) meetingPublicAvailability)
        ? MeetingAvailability.Public
        : MeetingAvailability.Private
    );
    meeting.setOrganization((String) json.get("meetingOrganisation"));
    meeting.setAgenda((String) json.get("meetingAgenda"));
    meeting.setMeetingTypeString(getCheckedStringProperty(json, "meetingType"));

    meeting.setLibrarySection(resolveLibrarySection(json));

    return meeting;
  }

  private NodeRef resolveLibrarySection(JSONObject json) {
    String librarySectionString = (String) json.get("meetingLibrarySection");
    if (librarySectionString == null || librarySectionString.isEmpty()) {
      return null;
    }

    NodeRef librarySectionNodeRef;
    try {
      librarySectionNodeRef = Converter.createNodeRefFromId(
        librarySectionString
      );
    } catch (MalformedNodeRefException e) {
      throw new IllegalArgumentException(
        "'meetingLibrarySection' must be a valid node reference.",
        e
      );
    }

    NodeRef parentNodeRef = librarySectionNodeRef;
    while (
      parentNodeRef != null &&
      !nodeService
        .getAspects(parentNodeRef)
        .contains(CircabcModel.ASPECT_LIBRARY_ROOT)
    ) {
      parentNodeRef = nodeService
        .getPrimaryParent(parentNodeRef)
        .getParentRef();
    }

    if (
      parentNodeRef == null ||
      !nodeService
        .getAspects(parentNodeRef)
        .contains(CircabcModel.ASPECT_LIBRARY_ROOT)
    ) {
      throw new IllegalArgumentException(
        "'meetingLibrarySection' must be a valid node inside the Library."
      );
    }

    if (
      !AccessStatus.ALLOWED.equals(
        permissionService.hasPermission(parentNodeRef, PermissionService.READ)
      )
    ) {
      throw new IllegalArgumentException(
        "'meetingLibrarySection' error. The given user has no access to the Library."
      );
    }

    return librarySectionNodeRef;
  }

  private void parseCommonProperties(JSONObject json, Appointment appointment) {
    appointment.setTitle(getCheckedStringProperty(json, "title"));
    appointment.setTimeZoneId(getCheckedStringProperty(json, "timezone"));
    appointment.setEventAbstract(getCheckedNullProperty(json, "abstract"));
    appointment.setInvitationMessage(
      getCheckedNullProperty(json, "invitationMessage")
    );
    appointment.setLocation(getCheckedNullProperty(json, "location"));

    String language = getCheckedStringProperty(json, "language");
    Locale locale = Locale.of(language);
    I18NUtil.setContentLocale(locale);
    I18NUtil.setLocale(locale);
    appointment.setLanguage(language);

    try {
      appointment.setEnableNotification(
        (Boolean) json.get("enableNotification")
      );
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "'enableNotification' must be a boolean value.",
        e
      );
    }

    try {
      appointment.setUseBCC((Boolean) json.get("useBCC"));
    } catch (Exception e) {
      appointment.setUseBCC(false);
    }

    appointment.setName(getCheckedStringProperty(json, "contactName"));
    appointment.setPhone((String) json.get("contactPhone"));
    appointment.setEmail(getCheckedStringProperty(json, "contactEmail"));
    appointment.setUrl(getCheckedNullProperty(json, "contactURL"));
  }

  private void parseUpdateId(JSONObject json, Appointment appointment) {
    String appointmentId = getCheckedStringProperty(json, "id");

    if (appointmentId.isEmpty()) {
      throw new IllegalArgumentException("The appointment id cannot be empty.");
    }

    NodeRef igRootRef = Converter.createNodeRefFromId(appointmentId);

    if (!nodeService.exists(igRootRef)) {
      throw new IllegalArgumentException(
        "The appointment with id '" + appointmentId + "' does not exist."
      );
    }

    appointment.setId(appointmentId);
  }

  /**
   * Parses the info about the event attendants (invited users and external emails)
   */
  private void parseAttendantsInfo(JSONObject json, Appointment appointment) {
    JSONObject attendantsInfo = (JSONObject) getCheckedObjectProperty(
      json,
      "attendantsInfo"
    );

    boolean audienceStatusOpen;

    try {
      audienceStatusOpen = (Boolean) attendantsInfo.get("audienceStatusOpen");
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "'audienceStatusOpen' must be 'true' or 'false'.",
        e
      );
    }

    appointment.setAudienceStatus(
      audienceStatusOpen ? AudienceStatus.Open : AudienceStatus.Closed
    );

    if (audienceStatusOpen) {
      return;
    }

    Set<String> attendants = new HashSet<>();
    collectInvitedAuthorities(attendantsInfo, attendants);
    collectExternalEmails(attendantsInfo, attendants);

    if (attendants.isEmpty()) {
      throw new IllegalArgumentException(
        "'audienceStatusOpen == true', but no attendants are provided."
      );
    }

    appointment.setInvitedUsers(new ArrayList<>(attendants));
  }

  private void collectInvitedAuthorities(
    JSONObject attendantsInfo,
    Set<String> attendants
  ) {
    JSONArray invitedUsersOrProfiles = (JSONArray) attendantsInfo.get(
      "invitedUsersOrProfiles"
    );

    for (
      int i = 0;
      invitedUsersOrProfiles != null && i < invitedUsersOrProfiles.size();
      i++
    ) {
      String authority = (String) invitedUsersOrProfiles.get(i);

      if (authority == null || authority.isEmpty()) {
        throw new IllegalArgumentException(
          "'audienceStatusOpen == true', but no user ids are provided."
        );
      }

      if (!authorityService.authorityExists(authority)) {
        throw new IllegalArgumentException(
          "The autority '" +
            authority +
            "' included in 'invitedUsersOrProfiles' does not exist."
        );
      }

      if (
        AuthorityType.getAuthorityType(authority).equals(AuthorityType.GROUP)
      ) {
        attendants.addAll(apiToolBox.getUsersFromGroup(authority));
      } else {
        attendants.add(authority);
      }
    }
  }

  private void collectExternalEmails(
    JSONObject attendantsInfo,
    Set<String> attendants
  ) {
    String invitedExternalEmails = getCheckedNullProperty(
      attendantsInfo,
      "invitedExternalEmails"
    );

    StringTokenizer tokens = new StringTokenizer(
      invitedExternalEmails,
      "\n",
      false
    );

    while (tokens.hasMoreTokens()) {
      String email = tokens.nextToken().trim();
      if (email.isEmpty()) {
        continue;
      }

      String authority = userService.getUserByEmail(email);
      if (authority == null) {
        attendants.add(email);
      } else {
        if (!personService.personExists(authority)) {
          userService.createLdapUser(authority, true);
        }
        attendants.add(authority);
      }
    }
  }

  /**
   * Parses the info about the date and time.
   */
  private void parseDateInfo(JSONObject json, Appointment appointment) {
    JSONObject dateInfo = (JSONObject) getCheckedObjectProperty(
      json,
      "dateInfo"
    );

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    try {
      appointment.setStartDateAsDate(
        simpleDateFormat.parse(getCheckedStringProperty(dateInfo, "date"))
      );
    } catch (java.text.ParseException e) {
      throw new IllegalArgumentException(
        "'date' is not well formed. Must be yyyy-MM-dd",
        e
      );
    }

    try {
      LocalTime startTime = LocalTime.parse(
        getCheckedStringProperty(dateInfo, "startTime")
      );
      appointment.setStartTime(startTime);
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "'startTime' is not well formed. Must be HH:mm",
        e
      );
    }

    try {
      LocalTime endTime = LocalTime.parse(
        getCheckedStringProperty(dateInfo, "endTime")
      );
      appointment.setEndTime(endTime);
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "'endTime' is not well formed. Must be HH:mm",
        e
      );
    }

    appointment.setDateAsDate(appointment.getStartDateAsDate());
  }

  /**
   * Parse the information about the occurrence rate of the event. If it should be only once or
   * repeat, and how many times.
   */
  private void parseOccurrenceRate(JSONObject json, Appointment appointment) {
    JSONObject repeatsInfo = (JSONObject) getCheckedObjectProperty(
      json,
      "repeatsInfo"
    );

    OccurenceRate occurenceRate = new OccurenceRate();
    MainOccurence mainOccurence = MainOccurence.valueOf(
      getCheckedStringProperty(repeatsInfo, "mainOccurence")
    );
    occurenceRate.setMainOccurence(mainOccurence);

    if (mainOccurence.equals(MainOccurence.EveryTimes)) {
      occurenceRate.setEveryTimesOccurence(
        EveryTimesOccurence.valueOf(
          getCheckedStringProperty(repeatsInfo, "everyTimesOccurence")
        )
      );
      occurenceRate.setEvery(
        getCheckedLongProperty(repeatsInfo, "everyTime").intValue()
      );
    } else if (mainOccurence.equals(MainOccurence.Times)) {
      occurenceRate.setTimesOccurence(
        TimesOccurence.valueOf(
          getCheckedStringProperty(repeatsInfo, "timesOccurence")
        )
      );
    }

    if (!mainOccurence.equals(MainOccurence.OnlyOnce)) {
      occurenceRate.setTimes(
        getCheckedLongProperty(repeatsInfo, "times").intValue()
      );
    }

    if (
      (occurenceRate.getEvery() < 2 && occurenceRate.getEvery() > 10) ||
      (occurenceRate.getTimes() < 2 && occurenceRate.getTimes() > 10)
    ) {
      throw new IllegalArgumentException(
        "Invalid frequency values. Must be between 2 and 10"
      );
    }

    appointment.setOccurenceRate(occurenceRate);
  }

  /**
   * Check if the given String property exists and throws and exception otherwise
   */
  private String getCheckedStringProperty(JSONObject json, String name) {
    String value;

    try {
      value = (String) json.get(name);
    } catch (Exception e) {
      throw new IllegalArgumentException("'" + name + "' must be a string.", e);
    }

    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException(PROPERTY + name + IS_MANDATORY);
    }

    return value;
  }

  /**
   * Check if the given Long property exists and throws and exception otherwise
   */
  private Long getCheckedLongProperty(JSONObject json, String name) {
    Object property = json.get(name);

    if (property == null) {
      throw new IllegalArgumentException(PROPERTY + name + IS_MANDATORY);
    }

    long value;

    try {
      value = (long) json.get(name);
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "'" + name + "' must be an integer.",
        e
      );
    }

    return value;
  }

  /**
   * Check if the given Object property exists and throws and exception otherwise
   */
  private Object getCheckedObjectProperty(JSONObject json, String name) {
    Object value = json.get(name);

    if (value == null) {
      throw new IllegalArgumentException(PROPERTY + name + IS_MANDATORY);
    }

    return value;
  }

  /**
   * Get a String property from the json by name, and return the empty String if the property is
   * null
   */
  private String getCheckedNullProperty(JSONObject json, String name) {
    String value = (String) json.get(name);

    return value == null ? "" : value;
  }
}
