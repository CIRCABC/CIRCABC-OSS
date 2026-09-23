package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.event.AppointmentUtils;
import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.model.*;
import io.swagger.model.alfresco.EventModel;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class EventsApiImplTest {

  private EventsApiImpl eventsApi;

  private EventService eventService;
  private NodeService nodeService;
  private SearchService searchService;
  private PersonService personService;
  private PermissionService permissionService;
  private AuthorityService authorityService;
  private UserService userService;
  private UsersApi usersApi;
  private ApiToolBox apiToolBox;
  private CircabcService circabcService;

  @Before
  public void setUp() throws Exception {
    eventsApi = new EventsApiImpl();

    eventService = mock(EventService.class);
    nodeService = mock(NodeService.class);
    searchService = mock(SearchService.class);
    personService = mock(PersonService.class);
    permissionService = mock(PermissionService.class);
    authorityService = mock(AuthorityService.class);
    userService = mock(UserService.class);
    usersApi = mock(UsersApi.class);
    apiToolBox = mock(ApiToolBox.class);
    circabcService = mock(CircabcService.class);

    setField("eventService", eventService);
    setField("nodeService", nodeService);
    setField("searchService", searchService);
    setField("personService", personService);
    setField("permissionService", permissionService);
    setField("authorityService", authorityService);
    setField("userService", userService);
    setField("usersApi", usersApi);
    setField("apiToolBox", apiToolBox);
    setField("circabcService", circabcService);
  }

  private void setField(String name, Object value) throws Exception {
    Field field = EventsApiImpl.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(eventsApi, value);
  }

  // --- groupsIdEventsGet ---

  @Test
  public void testGroupsIdEventsGet_whenValidDates_thenReturnsEvents() {
    String igId = "test-ig-id";
    Date dateFrom = new Date();
    Date dateTo = new Date();
    NodeRef eventRoot = new NodeRef("workspace://SpacesStore/event-root-id");

    EventItem item = new EventItem();
    item.setEventNodeRef(new NodeRef("workspace://SpacesStore/event-1"));
    List<EventItem> items = List.of(item);

    when(eventService.getIGsEventRoot(igId)).thenReturn(eventRoot);
    when(
      eventService.getEventsBetweenDates(eq(eventRoot), any(), any())
    ).thenReturn(new ArrayList<>(items));

    NodeRef igNodeRef = new NodeRef("workspace://SpacesStore/" + igId);
    when(eventService.getIGRoot(item.getEventNodeRef().getId())).thenReturn(
      igNodeRef
    );

    Appointment appointment = mock(Appointment.class);
    when(appointment.getTimeZoneId()).thenReturn("Europe/Brussels");
    when(
      eventService.getAppointmentByNodeRef(item.getEventNodeRef())
    ).thenReturn(appointment);

    List<EventItem> result = eventsApi.groupsIdEventsGet(
      igId,
      dateFrom,
      dateTo,
      "en"
    );

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(eventService).getIGsEventRoot(igId);
    verify(eventService).getEventsBetweenDates(eq(eventRoot), any(), any());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGroupsIdEventsGet_whenDateFromNull_thenThrows() {
    eventsApi.groupsIdEventsGet("id", null, new Date(), "en");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGroupsIdEventsGet_whenDateToNull_thenThrows() {
    eventsApi.groupsIdEventsGet("id", new Date(), null, "en");
  }

  // --- eventsIdGet ---

  @Test
  public void testEventsIdGet_whenValidId_thenReturnsAppointment() {
    String id = "appointment-id";
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);

    when(nodeService.exists(nodeRef)).thenReturn(true);

    Appointment appointment = mock(Appointment.class);
    when(appointment.getInvitedUsers()).thenReturn(
      new ArrayList<>(List.of("user1"))
    );
    when(eventService.getAppointmentByNodeRef(nodeRef)).thenReturn(appointment);

    NodeRef igRoot = new NodeRef("workspace://SpacesStore/ig-root-id");
    when(eventService.getIGRoot(id)).thenReturn(igRoot);

    Set<String> userIds = new HashSet<>(Set.of("user1"));
    when(circabcService.getUserIds(igRoot.getId())).thenReturn(userIds);

    Appointment result = eventsApi.eventsIdGet(id);

    assertNotNull(result);
    verify(eventService).getAppointmentByNodeRef(nodeRef);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEventsIdGet_whenNodeDoesNotExist_thenThrows() {
    String id = "non-existent-id";
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    when(nodeService.exists(nodeRef)).thenReturn(false);

    eventsApi.eventsIdGet(id);
  }

  // --- eventsIdDelete ---

  @Test
  public void testEventsIdDelete_whenValidId_thenDeletes() {
    String id = "delete-id";
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    when(nodeService.exists(nodeRef)).thenReturn(true);

    eventsApi.eventsIdDelete(id, UpdateMode.Single);

    verify(eventService).deleteAppointment(nodeRef, UpdateMode.Single);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEventsIdDelete_whenNodeDoesNotExist_thenThrows() {
    String id = "non-existent-id";
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    when(nodeService.exists(nodeRef)).thenReturn(false);

    eventsApi.eventsIdDelete(id, UpdateMode.Single);
  }

  // --- usersIdEventsGet (with dateFrom/dateTo) ---

  @Test(expected = IllegalArgumentException.class)
  public void testUsersIdEventsGet_whenUserIdNull_thenThrows() {
    eventsApi.usersIdEventsGet(null, new Date(), new Date());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUsersIdEventsGet_whenDateFromNull_thenThrows() {
    eventsApi.usersIdEventsGet("user1", (Date) null, new Date());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUsersIdEventsGet_whenDateToNull_thenThrows() {
    eventsApi.usersIdEventsGet("user1", new Date(), (Date) null);
  }

  // --- usersIdEventsPost ---

  @Test(expected = IllegalArgumentException.class)
  public void testUsersIdEventsPost_whenInvalidAction_thenThrows() {
    eventsApi.usersIdEventsPost("user1", "meeting-id", "Invalid", "Single");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUsersIdEventsPost_whenInvalidUpdateMode_thenThrows() {
    eventsApi.usersIdEventsPost("user1", "meeting-id", "Accepted", "Invalid");
  }

  // --- groupsIdEventsPost ---

  @Test(expected = IllegalArgumentException.class)
  public void testGroupsIdEventsPost_whenEmptyBody_thenThrows() {
    eventsApi.groupsIdEventsPost("ig-id", "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGroupsIdEventsPost_whenNullBody_thenThrows() {
    eventsApi.groupsIdEventsPost("ig-id", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGroupsIdEventsPost_whenInvalidJson_thenThrows() {
    eventsApi.groupsIdEventsPost("ig-id", "not valid json");
  }

  // --- eventsIdPut ---

  @Test(expected = IllegalArgumentException.class)
  public void testEventsIdPut_whenEmptyBody_thenThrows() {
    eventsApi.eventsIdPut(
      "id",
      "",
      AppointmentUpdateInfo.All,
      UpdateMode.Single
    );
  }

  // --- groupsIdEventsListGet ---

  @Test
  public void testGroupsIdEventsListGet_whenValidParams_thenReturnsPaged() {
    String igId = "ig-id";
    NodeRef igNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igId
    );
    NodeRef eventRoot = new NodeRef("workspace://SpacesStore/event-root");
    Date exactDate = new Date();

    when(
      nodeService.getChildByName(
        igNodeRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(eventRoot);

    EventItem item = new EventItem();
    item.setEventNodeRef(new NodeRef("workspace://SpacesStore/ev-1"));
    item.setDate(exactDate);
    item.setTitle("Test Event");

    when(
      eventService.getAppointments(
        eq(EventFilter.Exact),
        eq(eventRoot),
        any(),
        any()
      )
    ).thenReturn(new ArrayList<>(List.of(item)));

    NodeRef igRoot = new NodeRef("workspace://SpacesStore/ig-root");
    when(eventService.getIGRoot(item.getEventNodeRef().getId())).thenReturn(
      igRoot
    );

    Appointment appointment = mock(Appointment.class);
    when(appointment.getTimeZoneId()).thenReturn("UTC");
    when(
      eventService.getAppointmentByNodeRef(item.getEventNodeRef())
    ).thenReturn(appointment);

    PagedEventItems result = eventsApi.groupsIdEventsListGet(
      igId,
      "Exact",
      exactDate,
      0,
      10,
      "title_ASC"
    );

    assertNotNull(result);
    assertEquals(1, result.getTotal());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGroupsIdEventsListGet_whenEventRootNotFound_thenThrows() {
    String igId = "ig-id";
    NodeRef igNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igId
    );

    when(
      nodeService.getChildByName(
        igNodeRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(null);

    eventsApi.groupsIdEventsListGet(igId, "Exact", new Date(), 0, 10, null);
  }

  // --- usersIdEventsPost (valid case) ---

  @Test
  public void testUsersIdEventsPost_whenValidAccepted_thenSetsStatus() {
    String meetingId = "meeting-id";
    NodeRef meetingRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      meetingId
    );

    when(nodeService.getType(meetingRef)).thenReturn(EventModel.TYPE_EVENT);
    when(
      nodeService.getProperty(meetingRef, EventModel.PROP_KIND_OF_EVENT)
    ).thenReturn("Meeting");

    eventsApi.usersIdEventsPost("user1", meetingId, "Accepted", "Single");

    verify(eventService).setMeetingRequestStatus(
      meetingRef,
      "user1",
      MeetingRequestStatus.Accepted,
      UpdateMode.Single
    );
  }

  @Test
  public void testUsersIdEventsPost_whenNullUserId_thenUsesCurrentUser() {
    String meetingId = "meeting-id";
    NodeRef meetingRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      meetingId
    );

    when(nodeService.getType(meetingRef)).thenReturn(EventModel.TYPE_EVENT);
    when(
      nodeService.getProperty(meetingRef, EventModel.PROP_KIND_OF_EVENT)
    ).thenReturn("Meeting");

    // When userId is empty, it uses AuthenticationUtil.getFullyAuthenticatedUser()
    // We need to set up AuthenticationUtil
    try {
      java.lang.reflect.Field initialized =
        org.alfresco.repo.security.authentication
          .AuthenticationUtil.class.getDeclaredField("initialized");
      initialized.setAccessible(true);
      initialized.set(null, true);
      java.lang.reflect.Field guest = org.alfresco.repo.security.authentication
        .AuthenticationUtil.class.getDeclaredField("defaultGuestUserName");
      guest.setAccessible(true);
      guest.set(null, "guest");
      org.alfresco.repo.security.authentication.AuthenticationUtil.setFullyAuthenticatedUser(
        "currentuser"
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    eventsApi.usersIdEventsPost("", meetingId, "Rejected", "AllOccurences");

    verify(eventService).setMeetingRequestStatus(
      meetingRef,
      "currentuser",
      MeetingRequestStatus.Rejected,
      UpdateMode.AllOccurences
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUsersIdEventsPost_whenNotMeeting_thenThrows() {
    String meetingId = "event-id";
    NodeRef meetingRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      meetingId
    );

    when(nodeService.getType(meetingRef)).thenReturn(EventModel.TYPE_EVENT);
    when(
      nodeService.getProperty(meetingRef, EventModel.PROP_KIND_OF_EVENT)
    ).thenReturn("Event");

    eventsApi.usersIdEventsPost("user1", meetingId, "Accepted", "Single");
  }

  // --- groupsIdEventsListGet with pagination ---

  @Test
  public void testGroupsIdEventsListGet_whenAmountZero_thenReturnsAll() {
    String igId = "ig-id";
    NodeRef igNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igId
    );
    NodeRef eventRoot = new NodeRef("workspace://SpacesStore/event-root");
    Date exactDate = new Date();

    when(
      nodeService.getChildByName(
        igNodeRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(eventRoot);

    EventItem item1 = new EventItem();
    item1.setEventNodeRef(new NodeRef("workspace://SpacesStore/ev-1"));
    item1.setDate(exactDate);
    item1.setTitle("Event 1");
    EventItem item2 = new EventItem();
    item2.setEventNodeRef(new NodeRef("workspace://SpacesStore/ev-2"));
    item2.setDate(exactDate);
    item2.setTitle("Event 2");

    when(
      eventService.getAppointments(
        eq(EventFilter.Exact),
        eq(eventRoot),
        any(),
        any()
      )
    ).thenReturn(new ArrayList<>(List.of(item1, item2)));

    NodeRef igRoot = new NodeRef("workspace://SpacesStore/ig-root");
    when(eventService.getIGRoot(anyString())).thenReturn(igRoot);

    Appointment appointment = mock(Appointment.class);
    when(appointment.getTimeZoneId()).thenReturn("UTC");
    when(eventService.getAppointmentByNodeRef(any())).thenReturn(appointment);

    // amount=0 means return all
    PagedEventItems result = eventsApi.groupsIdEventsListGet(
      igId,
      "Exact",
      exactDate,
      0,
      0,
      null
    );

    assertNotNull(result);
    assertEquals(2, result.getTotal());
    assertEquals(2, result.getData().size());
  }

  @Test
  public void testGroupsIdEventsListGet_whenPaginated_thenReturnsSubset() {
    String igId = "ig-id";
    NodeRef igNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igId
    );
    NodeRef eventRoot = new NodeRef("workspace://SpacesStore/event-root");
    Date exactDate = new Date();

    when(
      nodeService.getChildByName(
        igNodeRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(eventRoot);

    EventItem item1 = new EventItem();
    item1.setEventNodeRef(new NodeRef("workspace://SpacesStore/ev-1"));
    item1.setDate(new Date(1000));
    item1.setTitle("Event 1");
    EventItem item2 = new EventItem();
    item2.setEventNodeRef(new NodeRef("workspace://SpacesStore/ev-2"));
    item2.setDate(new Date(2000));
    item2.setTitle("Event 2");
    EventItem item3 = new EventItem();
    item3.setEventNodeRef(new NodeRef("workspace://SpacesStore/ev-3"));
    item3.setDate(new Date(3000));
    item3.setTitle("Event 3");

    when(
      eventService.getAppointments(
        eq(EventFilter.Exact),
        eq(eventRoot),
        any(),
        any()
      )
    ).thenReturn(new ArrayList<>(List.of(item1, item2, item3)));

    NodeRef igRoot = new NodeRef("workspace://SpacesStore/ig-root");
    when(eventService.getIGRoot(anyString())).thenReturn(igRoot);

    Appointment appointment = mock(Appointment.class);
    when(appointment.getTimeZoneId()).thenReturn("UTC");
    when(eventService.getAppointmentByNodeRef(any())).thenReturn(appointment);

    // startItem=1, amount=1 -> should return only 1 item
    PagedEventItems result = eventsApi.groupsIdEventsListGet(
      igId,
      "Exact",
      exactDate,
      1,
      1,
      null
    );

    assertNotNull(result);
    assertEquals(3, result.getTotal());
    assertEquals(1, result.getData().size());
  }

  // --- groupsIdEventsListGet with null igId ---

  @Test
  public void testGroupsIdEventsListGet_whenNullIgId_thenUsesNullEventRoot() {
    Date exactDate = new Date();

    EventItem item = new EventItem();
    item.setEventNodeRef(new NodeRef("workspace://SpacesStore/ev-1"));
    item.setDate(exactDate);
    item.setTitle("Event");

    when(
      eventService.getAppointments(
        eq(EventFilter.Exact),
        isNull(),
        any(),
        any()
      )
    ).thenReturn(new ArrayList<>(List.of(item)));

    NodeRef igRoot = new NodeRef("workspace://SpacesStore/ig-root");
    when(eventService.getIGRoot(anyString())).thenReturn(igRoot);

    Appointment appointment = mock(Appointment.class);
    when(appointment.getTimeZoneId()).thenReturn("UTC");
    when(eventService.getAppointmentByNodeRef(any())).thenReturn(appointment);

    PagedEventItems result = eventsApi.groupsIdEventsListGet(
      null,
      "Exact",
      exactDate,
      0,
      10,
      null
    );

    assertNotNull(result);
    assertEquals(1, result.getTotal());
  }

  // --- usersIdEventsGet with filter ---

  @Test
  public void testUsersIdEventsGet_withFilter_thenReturnsItems() {
    String userId = "user1";
    Date exactDate = new Date();

    EventItem item = new EventItem();
    item.setEventNodeRef(new NodeRef("workspace://SpacesStore/ev-1"));

    when(
      eventService.getAppointments(
        eq(EventFilter.Exact),
        isNull(),
        eq(userId),
        any()
      )
    ).thenReturn(new ArrayList<>(List.of(item)));

    when(
      nodeService.getProperty(
        item.getEventNodeRef(),
        EventModel.PROP_EVENT_AUDIENCE
      )
    ).thenReturn("Open");

    Appointment appointment = mock(Appointment.class);
    when(appointment.getTimeZoneId()).thenReturn("Europe/Brussels");
    when(
      eventService.getAppointmentByNodeRef(item.getEventNodeRef())
    ).thenReturn(appointment);

    NodeRef igRoot = new NodeRef("workspace://SpacesStore/ig-root");
    when(eventService.getIGRoot(item.getEventNodeRef().getId())).thenReturn(
      igRoot
    );

    List<EventItem> result = eventsApi.usersIdEventsGet(
      userId,
      exactDate,
      EventFilter.Exact
    );

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Open", result.get(0).getMeetingStatus());
  }

  // --- eventsIdDelete with AllOccurences ---

  @Test
  public void testEventsIdDelete_whenAllOccurences_thenDeletesAll() {
    String id = "event-id";
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    when(nodeService.exists(nodeRef)).thenReturn(true);

    eventsApi.eventsIdDelete(id, UpdateMode.AllOccurences);

    verify(eventService).deleteAppointment(nodeRef, UpdateMode.AllOccurences);
  }
}
