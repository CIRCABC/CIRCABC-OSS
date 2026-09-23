package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NotificationsApi;
import io.swagger.model.PasteNotificationsState;
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

public class NodesIdNotificationsPasteGetTest {

  private NodesIdNotificationsPasteGet webScript;
  private NotificationsApi notificationsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new NodesIdNotificationsPasteGet();
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
  }

  @Test
  public void testExecuteImpl_whenUserIsAdmin_thenReturnsPasteState() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-node-id")
    ).thenReturn(true);
    when(
      notificationsApi.getPasteNotificationsState("test-node-id")
    ).thenReturn(new PasteNotificationsState(true, false));

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(true, model.get("pasteEnabled"));
    assertEquals(false, model.get("pasteAllEnabled"));
  }

  @Test
  public void testExecuteImpl_whenBothFlagsTrue_thenModelReflectsBoth() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-node-id")
    ).thenReturn(true);
    when(
      notificationsApi.getPasteNotificationsState("test-node-id")
    ).thenReturn(new PasteNotificationsState(true, true));

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(true, model.get("pasteEnabled"));
    assertEquals(true, model.get("pasteAllEnabled"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-node-id")
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-node-id")
    ).thenReturn(true);
    when(notificationsApi.getPasteNotificationsState("test-node-id")).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id")
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-node-id")
    ).thenReturn(true);
    when(notificationsApi.getPasteNotificationsState("test-node-id")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesIdNotificationsPasteGet.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
