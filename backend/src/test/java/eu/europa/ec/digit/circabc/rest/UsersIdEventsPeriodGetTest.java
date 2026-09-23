package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EventsApi;
import io.swagger.model.EventFilter;
import io.swagger.model.EventItem;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

@SuppressWarnings("unchecked")
public class UsersIdEventsPeriodGetTest {

  private UsersIdEventsPeriodGet webscript;
  private EventsApi eventsApi;
  private AuthenticationService authenticationService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new UsersIdEventsPeriodGet();
    eventsApi = mock(EventsApi.class);
    authenticationService = mock(AuthenticationService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("eventsApi", eventsApi);
    setField("authenticationService", authenticationService);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UsersIdEventsPeriodGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }

  @Test
  public void testExecuteImpl_whenFuturePeriod_thenReturnsEvents() {
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(req.getParameter("period")).thenReturn("future");
    when(req.getParameter("exactDate")).thenReturn(null);

    List<EventItem> events = Collections.singletonList(new EventItem());
    when(
      eventsApi.usersIdEventsGet(
        eq("testuser"),
        any(Date.class),
        eq(EventFilter.Future)
      )
    ).thenReturn(events);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(events, model.get("eventItems"));
  }

  @Test
  public void testExecuteImpl_whenPreviousPeriod_thenReturnsEvents() {
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(req.getParameter("period")).thenReturn("previous");
    when(req.getParameter("exactDate")).thenReturn(null);

    List<EventItem> events = Collections.emptyList();
    when(
      eventsApi.usersIdEventsGet(
        eq("testuser"),
        any(Date.class),
        eq(EventFilter.Previous)
      )
    ).thenReturn(events);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(events, model.get("eventItems"));
  }

  @Test
  public void testExecuteImpl_whenExactPeriodWithValidDate_thenReturnsEvents() {
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(req.getParameter("period")).thenReturn("exact");
    when(req.getParameter("exactDate")).thenReturn("2026-05-04");

    List<EventItem> events = Collections.singletonList(new EventItem());
    when(
      eventsApi.usersIdEventsGet(
        eq("testuser"),
        any(Date.class),
        eq(EventFilter.Exact)
      )
    ).thenReturn(events);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(events, model.get("eventItems"));
  }

  @Test
  public void testExecuteImpl_whenExactPeriodWithInvalidDate_thenReturnsError() {
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(req.getParameter("period")).thenReturn("exact");
    when(req.getParameter("exactDate")).thenReturn("invalid-date");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullPeriod_thenReturnsError() {
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(req.getParameter("period")).thenReturn(null);
    when(req.getParameter("exactDate")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidPeriod_thenReturnsError() {
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(req.getParameter("period")).thenReturn("invalid");
    when(req.getParameter("exactDate")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenDifferentUser_thenAccessDenied() {
    when(authenticationService.getCurrentUserName()).thenReturn("otheruser");
    when(req.getParameter("period")).thenReturn("future");
    when(req.getParameter("exactDate")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAdminUser_thenAllowed() {
    when(authenticationService.getCurrentUserName()).thenReturn("admin");
    when(req.getParameter("period")).thenReturn("future");
    when(req.getParameter("exactDate")).thenReturn(null);

    List<EventItem> events = Collections.emptyList();
    when(
      eventsApi.usersIdEventsGet(
        eq("testuser"),
        any(Date.class),
        eq(EventFilter.Future)
      )
    ).thenReturn(events);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(events, model.get("eventItems"));
  }
}
