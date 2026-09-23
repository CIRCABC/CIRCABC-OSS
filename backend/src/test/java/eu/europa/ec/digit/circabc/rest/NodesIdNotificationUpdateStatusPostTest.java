package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NotificationsApi;
import io.swagger.model.NotificationStatus;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesIdNotificationUpdateStatusPostTest {

  private NodesIdNotificationUpdateStatusPost webscript;
  private NotificationsApi notificationsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new NodesIdNotificationUpdateStatusPost();
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
  }

  @Test
  public void testExecuteImpl_whenLibAdmin_thenSubscribes() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);
    when(req.getParameter("authority")).thenReturn("user1");
    when(req.getParameter("status")).thenReturn("true");

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(notificationsApi).setNotificationStatus(
      "test-node-id",
      "user1",
      NotificationStatus.SUBSCRIBED
    );
  }

  @Test
  public void testExecuteImpl_whenStatusSubscribed_thenSubscribes() {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-node-id",
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(true);
    when(req.getParameter("authority")).thenReturn("user1");
    when(req.getParameter("status")).thenReturn("subscribed");

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(notificationsApi).setNotificationStatus(
      "test-node-id",
      "user1",
      NotificationStatus.SUBSCRIBED
    );
  }

  @Test
  public void testExecuteImpl_whenStatusFalse_thenUnsubscribes() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-node-id")
    ).thenReturn(true);
    when(req.getParameter("authority")).thenReturn("user1");
    when(req.getParameter("status")).thenReturn("false");

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(notificationsApi).setNotificationStatus(
      "test-node-id",
      "user1",
      NotificationStatus.UNSUBSCRIBED
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
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-node-id")
    ).thenReturn(false);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);
    when(req.getParameter("authority")).thenReturn("user1");
    when(req.getParameter("status")).thenReturn("true");
    doThrow(
      new InvalidNodeRefException(
        "bad ref",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad")
      )
    )
      .when(notificationsApi)
      .setNotificationStatus(
        anyString(),
        anyString(),
        any(NotificationStatus.class)
      );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenInternalError() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);
    when(req.getParameter("authority")).thenReturn("user1");
    when(req.getParameter("status")).thenReturn("true");
    doThrow(new RuntimeException("unexpected"))
      .when(notificationsApi)
      .setNotificationStatus(
        anyString(),
        anyString(),
        any(NotificationStatus.class)
      );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesIdNotificationUpdateStatusPost.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
