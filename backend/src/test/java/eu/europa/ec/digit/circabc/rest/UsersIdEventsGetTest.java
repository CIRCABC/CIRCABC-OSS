package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EventsApi;
import io.swagger.model.EventItem;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UsersIdEventsGetTest {

  private UsersIdEventsGet webscript;
  private EventsApi eventsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new UsersIdEventsGet();
    eventsApi = mock(EventsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("eventsApi", eventsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidDates_thenReturnsEventItems()
    throws Exception {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    when(req.getParameter("startDate")).thenReturn("2026-01-01");
    when(req.getParameter("endDate")).thenReturn("2026-01-31");

    List<EventItem> items = List.of(new EventItem());
    when(
      eventsApi.usersIdEventsGet(
        eq("testuser"),
        any(Date.class),
        any(Date.class)
      )
    ).thenReturn(items);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertEquals(items, model.get("eventItems"));
  }

  @Test
  public void testExecuteImpl_whenEmptyResults_thenReturnsEmptyList()
    throws Exception {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    when(req.getParameter("startDate")).thenReturn("2026-05-01");
    when(req.getParameter("endDate")).thenReturn("2026-05-31");

    when(
      eventsApi.usersIdEventsGet(
        eq("testuser"),
        any(Date.class),
        any(Date.class)
      )
    ).thenReturn(Collections.emptyList());

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("eventItems"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(false);
    when(req.getParameter("startDate")).thenReturn("2026-01-01");
    when(req.getParameter("endDate")).thenReturn("2026-01-31");

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidStartDate_thenReturnsError()
    throws Exception {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    when(req.getParameter("startDate")).thenReturn("not-a-date");
    when(req.getParameter("endDate")).thenReturn("2026-01-31");

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenEventsApiThrows_thenReturnsError()
    throws Exception {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    when(req.getParameter("startDate")).thenReturn("2026-01-01");
    when(req.getParameter("endDate")).thenReturn("2026-01-31");
    when(
      eventsApi.usersIdEventsGet(
        eq("testuser"),
        any(Date.class),
        any(Date.class)
      )
    ).thenThrow(new RuntimeException("DB error"));

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("DB error", status.getMessage());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> invokeExecuteImpl() throws Exception {
    var method = UsersIdEventsGet.class.getSuperclass().getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    return (Map<String, Object>) method.invoke(webscript, req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UsersIdEventsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
