package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import io.swagger.api.EventsApi;
import io.swagger.model.Appointment;
import io.swagger.model.Event;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class EventsIdGetTest {

  private EventsIdGet eventsIdGet;
  private EventsApi eventsApi;
  private EventService eventService;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    eventsIdGet = new EventsIdGet();
    eventsApi = mock(EventsApi.class);
    eventService = mock(EventService.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("eventsApi", eventsApi);
    setField("eventService", eventService);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = Collections.singletonMap(
      "id",
      "event-123"
    );
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenEventWithMixedUsers_thenSeparatesEmailsAndProfiles()
    throws Exception {
    when(permissionChecker.hasAlfrescoReadPermission("event-123")).thenReturn(
      true
    );

    Event appointment = mock(Event.class);
    when(appointment.getInvitedUsers()).thenReturn(
      Arrays.asList("user1", "ext@example.com", "profile1")
    );
    when(eventsApi.eventsIdGet("event-123")).thenReturn(appointment);

    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-root-id"
    );
    when(eventService.getIGRoot("event-123")).thenReturn(igRoot);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertEquals(appointment, model.get("appointment"));
    assertEquals("event-123", model.get("appointmentId"));
    assertEquals("ig-root-id", model.get("igId"));
    assertEquals(true, model.get("isEvent"));
    assertEquals(
      Arrays.asList("ext@example.com"),
      model.get("invitedExternalEmails")
    );
    assertEquals(
      Arrays.asList("user1", "profile1"),
      model.get("invitedUsersOrProfiles")
    );
  }

  @Test
  public void testExecuteImpl_whenMeeting_thenIsEventFalse() throws Exception {
    when(permissionChecker.hasAlfrescoReadPermission("event-123")).thenReturn(
      true
    );

    Appointment appointment = mock(Appointment.class);
    when(appointment.getInvitedUsers()).thenReturn(Collections.emptyList());
    when(eventsApi.eventsIdGet("event-123")).thenReturn(appointment);

    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-root-id"
    );
    when(eventService.getIGRoot("event-123")).thenReturn(igRoot);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertEquals(false, model.get("isEvent"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(permissionChecker.hasAlfrescoReadPermission("event-123")).thenReturn(
      false
    );

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsNotAcceptable()
    throws Exception {
    when(permissionChecker.hasAlfrescoReadPermission("event-123")).thenReturn(
      true
    );
    when(eventsApi.eventsIdGet("event-123")).thenThrow(
      new RuntimeException("something failed")
    );

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("something failed", status.getMessage());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> invokeExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      EventsIdGet.class.getSuperclass().getDeclaredMethod(
        "executeImpl",
        WebScriptRequest.class,
        Status.class,
        Cache.class
      );
    method.setAccessible(true);
    return (Map<String, Object>) method.invoke(eventsIdGet, req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = EventsIdGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(eventsIdGet, value);
  }
}
