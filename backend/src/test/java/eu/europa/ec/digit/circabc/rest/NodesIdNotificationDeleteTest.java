package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NotificationsApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
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

public class NodesIdNotificationDeleteTest {

  private NodesIdNotificationDelete webScript;
  private NotificationsApi notificationsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new NodesIdNotificationDelete();
    notificationsApi = mock(NotificationsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("notificationsApi", notificationsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("authority")).thenReturn("testAuthority");
  }

  @Test
  public void testExecuteImpl_whenUserHasLibraryPermission_thenSuccess() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(notificationsApi).removeNotification(
      "test-node-id",
      "testAuthority"
    );
  }

  @Test
  public void testExecuteImpl_whenUserHasNewsGroupPermission_thenSuccess() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-node-id",
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(notificationsApi).removeNotification(
      "test-node-id",
      "testAuthority"
    );
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-node-id",
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenRuntimeException_thenInternalServerError() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);
    doThrow(new RuntimeException("unexpected"))
      .when(notificationsApi)
      .removeNotification("test-node-id", "testAuthority");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesIdNotificationDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
