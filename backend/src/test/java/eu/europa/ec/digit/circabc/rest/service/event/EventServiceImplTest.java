package eu.europa.ec.digit.circabc.rest.service.event;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.config.CircabcConfig;
import io.swagger.exception.CircabcRuntimeException;
import io.swagger.model.Appointment;
import io.swagger.model.EventItem;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.EventModel;
import io.swagger.util.ApiToolBox;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

public class EventServiceImplTest {

  private EventServiceImpl eventService;
  private NodeService nodeService;
  private PermissionService permissionService;
  private NamespaceService namespaceService;
  private SearchService searchService;
  private AuthorityService authorityService;
  private PersonService personService;
  private ApiToolBox apiToolBox;
  private CircabcConfig circabcConfig;

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    eventService = new EventServiceImpl();
    nodeService = mock(NodeService.class);
    permissionService = mock(PermissionService.class);
    namespaceService = mock(NamespaceService.class);
    searchService = mock(SearchService.class);
    authorityService = mock(AuthorityService.class);
    personService = mock(PersonService.class);
    apiToolBox = mock(ApiToolBox.class);
    circabcConfig = mock(CircabcConfig.class);

    setField("nodeService", nodeService);
    setField("permissionService", permissionService);
    setField("namespaceService", namespaceService);
    setField("searchService", searchService);
    setField("authorityService", authorityService);
    setField("personService", personService);
    setField("apiToolBox", apiToolBox);
    setField("circabcConfig", circabcConfig);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = EventServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(eventService, value);
  }

  // --- getBestTitle tests ---

  @Test
  public void testGetBestTitle_whenTitleExists_thenReturnsTitle() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "name.txt"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).thenReturn(
      "My Title"
    );

    assertEquals("My Title", eventService.getBestTitle(nodeRef));
  }

  @Test
  public void testGetBestTitle_whenTitleIsNull_thenReturnsName() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "name.txt"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );

    assertEquals("name.txt", eventService.getBestTitle(nodeRef));
  }

  @Test
  public void testGetBestTitle_whenTitleIsBlank_thenReturnsName() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "name.txt"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).thenReturn(
      "   "
    );

    assertEquals("name.txt", eventService.getBestTitle(nodeRef));
  }

  // --- getAllOccurences tests ---

  @Test(expected = IllegalArgumentException.class)
  public void testGetAllOccurences_whenNull_thenThrows() {
    eventService.getAllOccurences(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetAllOccurences_whenWrongType_thenThrows() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);

    eventService.getAllOccurences(nodeRef);
  }

  @Test
  public void testGetAllOccurences_whenEventDefinition_thenReturnsItems() {
    NodeRef definitionRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "def-id"
    );
    NodeRef containerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    when(nodeService.getType(definitionRef)).thenReturn(
      EventModel.TYPE_EVENT_DEFINITION
    );

    ChildAssociationRef containerAssoc = mock(ChildAssociationRef.class);
    when(containerAssoc.getChildRef()).thenReturn(containerRef);
    when(
      nodeService.getChildAssocs(
        definitionRef,
        EventModel.ASSOC_BASE_EVANT_DATE_CONTAINER_CONTAINER,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(containerAssoc));

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(childRef);
    when(nodeService.getChildAssocs(containerRef)).thenReturn(
      Collections.singletonList(childAssoc)
    );

    when(apiToolBox.getCurrentInterestGroup(definitionRef)).thenReturn(igRef);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "Test IG Title"
    );

    Date eventDate = new Date();
    when(
      nodeService.getProperty(childRef, EventModel.PROP_EVENT_DATE)
    ).thenReturn(eventDate);
    when(
      nodeService.getProperty(childRef, EventModel.PROP_EVENT_NAME)
    ).thenReturn("Contact");
    when(
      nodeService.getProperty(childRef, EventModel.PROP_KIND_OF_EVENT)
    ).thenReturn("Event");
    when(
      nodeService.getProperty(childRef, EventModel.PROP_EVENT_TITLE)
    ).thenReturn("Event Title");
    when(
      nodeService.getProperty(childRef, EventModel.PROP_EVENT_START_TIME)
    ).thenReturn(eventDate);
    when(
      nodeService.getProperty(childRef, EventModel.PROP_EVENT_END_TIME)
    ).thenReturn(eventDate);
    when(
      nodeService.getProperty(childRef, EventModel.PROP_EVENT_ABSTRACT)
    ).thenReturn("Abstract");
    when(
      nodeService.getProperty(childRef, EventModel.PROP_EVENT_LOCATION)
    ).thenReturn("Location");

    ChildAssociationRef parentAssoc1 = mock(ChildAssociationRef.class);
    when(parentAssoc1.getParentRef()).thenReturn(containerRef);
    when(nodeService.getPrimaryParent(childRef)).thenReturn(parentAssoc1);

    ChildAssociationRef parentAssoc2 = mock(ChildAssociationRef.class);
    when(parentAssoc2.getParentRef()).thenReturn(definitionRef);
    when(nodeService.getPrimaryParent(containerRef)).thenReturn(parentAssoc2);

    when(
      nodeService.getProperty(
        definitionRef,
        EventModel.PROP_EVENT_OCCURENCE_RATE
      )
    ).thenReturn("OnlyOnce|null|null|-1|-1");

    List<EventItem> result = eventService.getAllOccurences(definitionRef);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Event Title", result.get(0).getTitle());
    assertEquals("TestIG", result.get(0).getInterestGroup());
  }

  // --- getAllAppointments tests ---

  @Test
  public void testGetAllAppointments_whenValidEventRoot_thenReturnsAppointments() {
    NodeRef eventRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-root"
    );
    NodeRef eventDefRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-def"
    );

    when(
      nodeService.hasAspect(eventRoot, CircabcModel.ASPECT_EVENT_ROOT)
    ).thenReturn(true);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(eventDefRef);
    when(nodeService.getChildAssocs(eventRoot)).thenReturn(
      Collections.singletonList(childAssoc)
    );
    when(
      nodeService.hasAspect(eventDefRef, CircabcModel.ASPECT_EVENT)
    ).thenReturn(true);
    when(nodeService.getType(eventDefRef)).thenReturn(
      EventModel.TYPE_EVENT_DEFINITION
    );

    Map<QName, Serializable> props = new HashMap<>();
    props.put(EventModel.PROP_EVENT_TITLE, "Test Event");
    props.put(EventModel.PROP_EVENT_AUDIENCE, "Open");
    props.put(EventModel.PROP_EVENT_START_DATE, new Date());
    props.put(EventModel.PROP_EVENT_START_TIME, "10:00");
    props.put(EventModel.PROP_EVENT_END_TIME, "11:00");
    props.put(EventModel.PROP_EVENT_ABSTRACT, "Abstract");
    props.put(EventModel.PROP_EVENT_EMAIL, "test@test.com");
    props.put(EventModel.PROP_EVENT_INVITATION_MESSAGE, "");
    props.put(EventModel.PROP_EVENT_LANGUAGE, "EN");
    props.put(EventModel.PROP_EVENT_LOCATION, "Room");
    props.put(EventModel.PROP_EVENT_NAME, "Name");
    props.put(EventModel.PROP_EVENT_PHONE, "123");
    props.put(EventModel.PROP_EVENT_URL, "http://test.com");
    props.put(EventModel.PROP_EVENT_TIMEZONE, "Europe/Brussels");
    props.put(EventModel.PROP_EVENT_OCCURENCE_RATE, "OnlyOnce|null|null|-1|-1");
    props.put(EventModel.PROP_KIND_OF_EVENT, "Event");
    props.put(EventModel.PROP_EVENT_PRIORITY, "Medium");
    when(nodeService.getProperties(eventDefRef)).thenReturn(props);

    List<Appointment> result = eventService.getAllAppointments(eventRoot);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Test Event", result.get(0).getTitle());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetAllAppointments_whenNoEventRootAspect_thenThrows() {
    NodeRef eventRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-root"
    );
    when(
      nodeService.hasAspect(eventRoot, CircabcModel.ASPECT_EVENT_ROOT)
    ).thenReturn(false);

    eventService.getAllAppointments(eventRoot);
  }

  @Test
  public void testGetAllAppointments_whenEmptyChildren_thenReturnsEmpty() {
    NodeRef eventRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-root"
    );
    when(
      nodeService.hasAspect(eventRoot, CircabcModel.ASPECT_EVENT_ROOT)
    ).thenReturn(true);
    when(nodeService.getChildAssocs(eventRoot)).thenReturn(
      Collections.emptyList()
    );

    List<Appointment> result = eventService.getAllAppointments(eventRoot);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // --- getIGsEventRoot tests ---

  @Test
  public void testGetIGsEventRoot_whenIGExists_thenReturnsEventRoot() {
    String igId = "ig-id-123";
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, igId);
    NodeRef eventRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-root"
    );

    when(nodeService.exists(igRef)).thenReturn(true);
    when(
      nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Events")
    ).thenReturn(eventRoot);

    assertEquals(eventRoot, eventService.getIGsEventRoot(igId));
  }

  @Test(expected = CircabcRuntimeException.class)
  public void testGetIGsEventRoot_whenIGDoesNotExist_thenThrows() {
    String igId = "nonexistent";
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, igId);
    when(nodeService.exists(igRef)).thenReturn(false);

    eventService.getIGsEventRoot(igId);
  }

  @Test(expected = CircabcRuntimeException.class)
  public void testGetIGsEventRoot_whenEventRootNull_thenThrows() {
    String igId = "ig-id-123";
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, igId);
    when(nodeService.exists(igRef)).thenReturn(true);
    when(
      nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Events")
    ).thenReturn(null);

    eventService.getIGsEventRoot(igId);
  }

  // --- getIGRoot tests ---

  @Test
  public void testGetIGRoot_whenEventExists_thenReturnsIGRoot() {
    String eventId = "event-id-123";
    NodeRef eventRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      eventId
    );
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-root"
    );

    when(nodeService.exists(eventRef)).thenReturn(true);
    when(
      nodeService.hasAspect(eventRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(nodeService.hasAspect(igRoot, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(igRoot);
    when(nodeService.getPrimaryParent(eventRef)).thenReturn(parentAssoc);

    assertEquals(igRoot, eventService.getIGRoot(eventId));
  }

  @Test(expected = CircabcRuntimeException.class)
  public void testGetIGRoot_whenEventDoesNotExist_thenThrows() {
    String eventId = "nonexistent";
    NodeRef eventRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      eventId
    );
    when(nodeService.exists(eventRef)).thenReturn(false);

    eventService.getIGRoot(eventId);
  }

  // --- getAppointmentByNodeRef tests ---

  @Test(expected = IllegalArgumentException.class)
  public void testGetAppointmentByNodeRef_whenNull_thenThrows() {
    eventService.getAppointmentByNodeRef(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetAppointmentByNodeRef_whenWrongType_thenThrows() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);

    eventService.getAppointmentByNodeRef(nodeRef);
  }

  // --- buildEventItem tests ---

  @Test
  public void testBuildEventItem_populatesAllFields() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-node"
    );
    NodeRef containerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container"
    );
    NodeRef definitionRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "definition"
    );

    Date eventDate = new Date();
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_DATE)
    ).thenReturn(eventDate);
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_NAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_KIND_OF_EVENT)
    ).thenReturn("Meeting");
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_TITLE)
    ).thenReturn("Title");
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_START_TIME)
    ).thenReturn(eventDate);
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_END_TIME)
    ).thenReturn(eventDate);
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_ABSTRACT)
    ).thenReturn(null);
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_LOCATION)
    ).thenReturn(null);

    ChildAssociationRef parentAssoc1 = mock(ChildAssociationRef.class);
    when(parentAssoc1.getParentRef()).thenReturn(containerRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(parentAssoc1);

    ChildAssociationRef parentAssoc2 = mock(ChildAssociationRef.class);
    when(parentAssoc2.getParentRef()).thenReturn(definitionRef);
    when(nodeService.getPrimaryParent(containerRef)).thenReturn(parentAssoc2);

    when(
      nodeService.getProperty(
        definitionRef,
        EventModel.PROP_EVENT_OCCURENCE_RATE
      )
    ).thenReturn("OnlyOnce|null|null|-1|-1");
    when(
      nodeService.getProperty(definitionRef, EventModel.PROP_EVENT_ABSTRACT)
    ).thenReturn("Def Abstract");
    when(
      nodeService.getProperty(definitionRef, EventModel.PROP_EVENT_LOCATION)
    ).thenReturn("Def Location");

    EventItem item = eventService.buildEventItem(
      "MyIG",
      "My IG Title",
      nodeRef
    );

    assertEquals("Title", item.getTitle());
    assertEquals("MyIG", item.getInterestGroup());
    assertEquals("My IG Title", item.getInterestGroupTitle());
    assertEquals("John", item.getContact());
    assertEquals("Def Abstract", item.getDescription());
    assertEquals("Def Location", item.getLocation());
    assertEquals(eventDate, item.getDate());
  }

  @Test
  public void testBuildEventItem_whenAbstractAndLocationOnNode_thenUsesNodeValues() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-node"
    );
    NodeRef containerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container"
    );
    NodeRef definitionRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "definition"
    );

    Date eventDate = new Date();
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_DATE)
    ).thenReturn(eventDate);
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_NAME)
    ).thenReturn("Contact");
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_KIND_OF_EVENT)
    ).thenReturn("Event");
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_TITLE)
    ).thenReturn("T");
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_START_TIME)
    ).thenReturn(eventDate);
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_END_TIME)
    ).thenReturn(eventDate);
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_ABSTRACT)
    ).thenReturn("Node Abstract");
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_LOCATION)
    ).thenReturn("Node Location");

    ChildAssociationRef parentAssoc1 = mock(ChildAssociationRef.class);
    when(parentAssoc1.getParentRef()).thenReturn(containerRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(parentAssoc1);

    ChildAssociationRef parentAssoc2 = mock(ChildAssociationRef.class);
    when(parentAssoc2.getParentRef()).thenReturn(definitionRef);
    when(nodeService.getPrimaryParent(containerRef)).thenReturn(parentAssoc2);

    when(
      nodeService.getProperty(
        definitionRef,
        EventModel.PROP_EVENT_OCCURENCE_RATE
      )
    ).thenReturn("OnlyOnce|null|null|-1|-1");

    EventItem item = eventService.buildEventItem("IG", "IG Title", nodeRef);

    assertEquals("Node Abstract", item.getDescription());
    assertEquals("Node Location", item.getLocation());
  }

  // --- getMeetingStatus tests ---

  @Test
  public void testGetMeetingStatus_whenUserNameNull_thenReturnsNotApplicable() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node"
    );
    assertEquals(
      io.swagger.model.MeetingRequestStatus.NotApplicable,
      eventService.getMeetingStatus(nodeRef, null)
    );
  }

  @Test
  public void testGetMeetingStatus_whenNotSingleEvent_thenReturnsNotApplicable() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);

    assertEquals(
      io.swagger.model.MeetingRequestStatus.NotApplicable,
      eventService.getMeetingStatus(nodeRef, "user1")
    );
  }

  @Test
  public void testGetMeetingStatus_whenSingleEventButNotMeeting_thenReturnsNotApplicable() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node"
    );
    when(nodeService.getType(nodeRef)).thenReturn(EventModel.TYPE_EVENT);
    when(
      nodeService.getProperty(nodeRef, EventModel.PROP_KIND_OF_EVENT)
    ).thenReturn("Event");

    assertEquals(
      io.swagger.model.MeetingRequestStatus.NotApplicable,
      eventService.getMeetingStatus(nodeRef, "user1")
    );
  }

  // --- getEventsByMonth tests ---

  @Test
  public void testGetEventsByMonth_returnsEventsForMonth() {
    NodeRef eventRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root"
    );
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig");

    when(circabcConfig.getEventsDirectStoreAccess()).thenReturn(true);
    when(permissionService.hasPermission(eventRoot, "EveAdmin")).thenReturn(
      org.alfresco.service.cmr.security.AccessStatus.ALLOWED
    );

    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(igRef);
    when(nodeService.getPrimaryParent(eventRoot)).thenReturn(parentAssoc);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "IG"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "IG Title"
    );
    when(nodeService.getChildAssocs(eventRoot)).thenReturn(
      Collections.emptyList()
    );

    List<EventItem> result = eventService.getEventsByMonth(eventRoot, 1, 2024);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // --- getCurrentFutureEvents / getCurrentPreviousEvents ---

  @Test
  public void testGetCurrentFutureEvents_returnsEmptyWhenNoEvents() {
    NodeRef eventRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root"
    );
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig");

    when(circabcConfig.getEventsDirectStoreAccess()).thenReturn(true);
    when(permissionService.hasPermission(eventRoot, "EveAdmin")).thenReturn(
      org.alfresco.service.cmr.security.AccessStatus.ALLOWED
    );

    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(igRef);
    when(nodeService.getPrimaryParent(eventRoot)).thenReturn(parentAssoc);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "IG"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "Title"
    );
    when(nodeService.getChildAssocs(eventRoot)).thenReturn(
      Collections.emptyList()
    );

    List<EventItem> result = eventService.getCurrentFutureEvents(eventRoot);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetCurrentPreviousEvents_returnsEmptyWhenNoEvents() {
    NodeRef eventRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root"
    );
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig");

    when(circabcConfig.getEventsDirectStoreAccess()).thenReturn(true);
    when(permissionService.hasPermission(eventRoot, "EveAdmin")).thenReturn(
      org.alfresco.service.cmr.security.AccessStatus.ALLOWED
    );

    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(igRef);
    when(nodeService.getPrimaryParent(eventRoot)).thenReturn(parentAssoc);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "IG"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "Title"
    );
    when(nodeService.getChildAssocs(eventRoot)).thenReturn(
      Collections.emptyList()
    );

    List<EventItem> result = eventService.getCurrentPreviousEvents(eventRoot);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // --- getAllOccurences with single event type ---

  @Test
  public void testGetAllOccurences_whenSingleEvent_thenUsesParentContainer() {
    NodeRef singleEventRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "single"
    );
    NodeRef containerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container"
    );
    NodeRef definitionRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "def"
    );
    NodeRef igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig");

    when(nodeService.getType(singleEventRef)).thenReturn(EventModel.TYPE_EVENT);

    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(containerRef);
    when(nodeService.getPrimaryParent(singleEventRef)).thenReturn(parentAssoc);

    when(nodeService.getChildAssocs(containerRef)).thenReturn(
      Collections.emptyList()
    );
    when(apiToolBox.getCurrentInterestGroup(singleEventRef)).thenReturn(igRef);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "IG"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "Title"
    );

    List<EventItem> result = eventService.getAllOccurences(singleEventRef);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // --- getBestTitle edge case ---

  @Test
  public void testGetBestTitle_whenBothNull_thenReturnsNull() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "no-title"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      null
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );

    String result = eventService.getBestTitle(nodeRef);

    assertNull(result);
  }
}
