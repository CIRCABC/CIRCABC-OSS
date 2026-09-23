package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NotificationsApi;
import io.swagger.model.NotificationDefinition;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesNotificationsGetTest {

  private NodesNotificationsGet webScript;
  private NotificationsApi notificationsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new NodesNotificationsGet();
    notificationsApi = mock(NotificationsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("notificationsApi", notificationsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenLibAdmin_thenReturnsNotifications() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    NotificationDefinition notifications = new NotificationDefinition();
    when(notificationsApi.nodesIdNotificationsGet("test-node-id")).thenReturn(
      notifications
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(notifications, result.get("definition"));
  }

  @Test
  public void testExecuteImpl_whenNwsAdmin_thenReturnsNotifications() {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-node-id",
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(true);

    NotificationDefinition notifications = new NotificationDefinition();
    when(notificationsApi.nodesIdNotificationsGet("test-node-id")).thenReturn(
      notifications
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(notifications, result.get("definition"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-node-id",
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);
    when(notificationsApi.nodesIdNotificationsGet("test-node-id")).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id")
      )
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenInternalServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);
    when(notificationsApi.nodesIdNotificationsGet("test-node-id")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesNotificationsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
