package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NotificationsApi;
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

public class NodesIdNotificationStatusUsersIdGetTest {

  private NodesIdNotificationStatusUsersIdGet webscript;
  private NotificationsApi notificationsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new NodesIdNotificationStatusUsersIdGet();
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
    templateVars.put("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserIsSubscribed_thenReturnsTrue() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    when(
      notificationsApi.isUsersubscribedForNotification(
        "test-node-id",
        "testuser"
      )
    ).thenReturn(true);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertNotNull(model.get("result"));
  }

  @Test
  public void testExecuteImpl_whenUserIsNotSubscribed_thenReturnsFalse() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    when(
      notificationsApi.isUsersubscribedForNotification(
        "test-node-id",
        "testuser"
      )
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertNotNull(model.get("result"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    when(
      notificationsApi.isUsersubscribedForNotification(
        "test-node-id",
        "testuser"
      )
    ).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id")
      )
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    when(
      notificationsApi.isUsersubscribedForNotification(
        "test-node-id",
        "testuser"
      )
    ).thenThrow(new RuntimeException("unexpected"));

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesIdNotificationStatusUsersIdGet.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
