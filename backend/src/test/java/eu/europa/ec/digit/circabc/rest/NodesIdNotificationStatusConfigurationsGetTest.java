package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NotificationsApi;
import io.swagger.model.PagedNotificationConfigurations;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesIdNotificationStatusConfigurationsGetTest {

  private NodesIdNotificationStatusConfigurationsGet webscript;
  private NotificationsApi notificationsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new NodesIdNotificationStatusConfigurationsGet();
    notificationsApi = mock(NotificationsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("notificationsApi", notificationsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");
  }

  @Test
  public void testExecuteImpl_whenUserHasLibAdminPermission_thenReturnsData()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        anyString(),
        any()
      )
    ).thenReturn(true);

    PagedNotificationConfigurations pagedResult =
      new PagedNotificationConfigurations(Collections.emptyList(), 0);
    when(
      notificationsApi.getNotifications(
        anyString(),
        anyInt(),
        anyInt(),
        anyString(),
        any(),
        any(),
        any()
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("data"));
    assertEquals(0L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        anyString(),
        any()
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        anyString(),
        any()
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.isGroupAdmin(anyString())
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPageIsZero_thenReturnsServerError()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        anyString(),
        any()
      )
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn("0");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLimitIsNegative_thenReturnsServerError()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        anyString(),
        any()
      )
    ).thenReturn(true);
    when(req.getParameter("limit")).thenReturn("-1");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageIsNull_thenDefaultsToEnUS()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        anyString(),
        any()
      )
    ).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);

    PagedNotificationConfigurations pagedResult =
      new PagedNotificationConfigurations(Collections.emptyList(), 5);
    when(
      notificationsApi.getNotifications(
        anyString(),
        anyInt(),
        anyInt(),
        eq("en-US"),
        any(),
        any(),
        any()
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(5L, model.get("total"));
    verify(notificationsApi).getNotifications(
      eq("test-node-id"),
      eq(0),
      eq(10),
      eq("en-US"),
      any(),
      any(),
      any()
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field =
      NodesIdNotificationStatusConfigurationsGet.class.getDeclaredField(
        fieldName
      );
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
