package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EventsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UsersIdEventsPostTest {

  private UsersIdEventsPost webscript;
  private EventsApi eventsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new UsersIdEventsPost();
    eventsApi = mock(EventsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("eventsApi", eventsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("meetingId")).thenReturn("meeting-123");
    when(req.getParameter("action")).thenReturn("Accept");
    when(req.getParameter("updateMode")).thenReturn("ALL");
  }

  @Test
  public void testExecuteImpl_whenCurrentUser_thenSuccess() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("Accept", model.get("action"));
    verify(eventsApi).usersIdEventsPost(
      "testuser",
      "meeting-123",
      "Accept",
      "ALL"
    );
  }

  @Test
  public void testExecuteImpl_whenDifferentUser_thenForbidden() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(eventsApi);
  }

  @Test
  public void testExecuteImpl_whenEventsApiThrows_thenNotAcceptable() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    doThrow(new RuntimeException("Event error"))
      .when(eventsApi)
      .usersIdEventsPost("testuser", "meeting-123", "Accept", "ALL");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("Event error", status.getMessage());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UsersIdEventsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
