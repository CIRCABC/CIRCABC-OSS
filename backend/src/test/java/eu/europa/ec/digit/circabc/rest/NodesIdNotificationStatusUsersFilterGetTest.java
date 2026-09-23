package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NotificationsApi;
import io.swagger.model.PagedNotificationSubscribedUsers;
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

public class NodesIdNotificationStatusUsersFilterGetTest {

  private NodesIdNotificationStatusUsersFilterGet webscript;
  private NotificationsApi notificationsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new NodesIdNotificationStatusUsersFilterGet();
    notificationsApi = mock(NotificationsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("notificationsApi", notificationsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserHasLibAdminPermission_thenReturnsData()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(anyString(), any())
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("userName")).thenReturn(null);
    when(req.getParameter("firstName")).thenReturn(null);
    when(req.getParameter("lastName")).thenReturn(null);
    when(req.getParameter("email")).thenReturn(null);

    PagedNotificationSubscribedUsers pagedResult =
      new PagedNotificationSubscribedUsers(Collections.emptyList(), 0);
    when(
      notificationsApi.getNotifiableUsers(
        eq("test-node-id"),
        eq(0),
        eq(10),
        isNull(),
        isNull(),
        isNull(),
        isNull()
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("data"));
    assertEquals(0L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(anyString(), any())
    ).thenReturn(false);
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(anyString(), any())
    ).thenReturn(false);
    when(permissionChecker.isGroupAdmin(anyString())).thenReturn(false);
    when(req.getParameter("userName")).thenReturn(null);
    when(req.getParameter("firstName")).thenReturn(null);
    when(req.getParameter("lastName")).thenReturn(null);
    when(req.getParameter("email")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPageIsZero_thenReturnsError()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(anyString(), any())
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn("0");
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("userName")).thenReturn(null);
    when(req.getParameter("firstName")).thenReturn(null);
    when(req.getParameter("lastName")).thenReturn(null);
    when(req.getParameter("email")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLimitIsNegative_thenReturnsError()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(anyString(), any())
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("-1");
    when(req.getParameter("userName")).thenReturn(null);
    when(req.getParameter("firstName")).thenReturn(null);
    when(req.getParameter("lastName")).thenReturn(null);
    when(req.getParameter("email")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPageParamMissing_thenDefaultsToPageOne()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(anyString(), any())
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("5");
    when(req.getParameter("userName")).thenReturn("user1");
    when(req.getParameter("firstName")).thenReturn(null);
    when(req.getParameter("lastName")).thenReturn(null);
    when(req.getParameter("email")).thenReturn(null);

    PagedNotificationSubscribedUsers pagedResult =
      new PagedNotificationSubscribedUsers(Collections.emptyList(), 3);
    when(
      notificationsApi.getNotifiableUsers(
        eq("test-node-id"),
        eq(0),
        eq(5),
        eq("user1"),
        isNull(),
        isNull(),
        isNull()
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(3L, model.get("total"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field =
      NodesIdNotificationStatusUsersFilterGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
