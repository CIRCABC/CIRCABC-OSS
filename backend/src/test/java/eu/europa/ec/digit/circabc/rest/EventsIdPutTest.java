package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EventsApi;
import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.UpdateMode;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class EventsIdPutTest {

  private EventsIdPut eventsIdPut;
  private EventsApi eventsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String EVENT_ID = "event-id-123";

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

    eventsIdPut = new EventsIdPut();
    eventsApi = mock(EventsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("eventsApi", eventsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", EVENT_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = EventsIdPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(eventsIdPut, value);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenSuccess() throws Exception {
    when(req.getParameter("updateMode")).thenReturn("Single");
    when(req.getParameter("updateInfo")).thenReturn("GeneralInformation");
    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENT_ID,
        EventPermissions.EVEADMIN
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("{\"title\":\"Updated Event\"}");

    Map<String, Object> model = eventsIdPut.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(eventsApi).eventsIdPut(
      eq(EVENT_ID),
      eq("{\"title\":\"Updated Event\"}"),
      eq(AppointmentUpdateInfo.GeneralInformation),
      eq(UpdateMode.Single)
    );
  }

  @Test
  public void testExecuteImpl_whenAllOccurences_thenSuccess() throws Exception {
    when(req.getParameter("updateMode")).thenReturn("AllOccurences");
    when(req.getParameter("updateInfo")).thenReturn("All");
    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENT_ID,
        EventPermissions.EVEADMIN
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("{}");

    Map<String, Object> model = eventsIdPut.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(eventsApi).eventsIdPut(
      eq(EVENT_ID),
      eq("{}"),
      eq(AppointmentUpdateInfo.All),
      eq(UpdateMode.AllOccurences)
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenInvalidUpdateMode_thenThrows() {
    when(req.getParameter("updateMode")).thenReturn("InvalidMode");
    when(req.getParameter("updateInfo")).thenReturn("GeneralInformation");

    eventsIdPut.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenInvalidUpdateInfo_thenThrows() {
    when(req.getParameter("updateMode")).thenReturn("Single");
    when(req.getParameter("updateInfo")).thenReturn("InvalidInfo");

    eventsIdPut.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden()
    throws Exception {
    when(req.getParameter("updateMode")).thenReturn("Single");
    when(req.getParameter("updateInfo")).thenReturn("GeneralInformation");
    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENT_ID,
        EventPermissions.EVEADMIN
      )
    ).thenReturn(false);

    Map<String, Object> model = eventsIdPut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(eventsApi);
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenNotAcceptable()
    throws Exception {
    when(req.getParameter("updateMode")).thenReturn("FuturOccurences");
    when(req.getParameter("updateInfo")).thenReturn("Audience");
    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENT_ID,
        EventPermissions.EVEADMIN
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new RuntimeException("read error"));

    Map<String, Object> model = eventsIdPut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }
}
