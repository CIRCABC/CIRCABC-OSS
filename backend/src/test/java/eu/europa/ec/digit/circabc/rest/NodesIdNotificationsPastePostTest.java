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

public class NodesIdNotificationsPastePostTest {

  private NodesIdNotificationsPastePost webScript;
  private NotificationsApi notificationsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new NodesIdNotificationsPastePost();
    notificationsApi = mock(NotificationsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("notificationsApi", notificationsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenAdminAndPasteEnabled_thenCallsApi()
    throws Exception {
    when(permissionCheckerService.isGroupAdmin("test-node-id")).thenReturn(
      true
    );
    when(req.getParameter("pasteEnable")).thenReturn("true");
    when(req.getParameter("pasteAllEnable")).thenReturn("false");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(notificationsApi).setPasteNotificationsState(
      "test-node-id",
      true,
      false
    );
  }

  @Test
  public void testExecuteImpl_whenAdminAndBothEnabled_thenCallsApiWithBothTrue()
    throws Exception {
    when(permissionCheckerService.isGroupAdmin("test-node-id")).thenReturn(
      true
    );
    when(req.getParameter("pasteEnable")).thenReturn("true");
    when(req.getParameter("pasteAllEnable")).thenReturn("true");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(notificationsApi).setPasteNotificationsState(
      "test-node-id",
      true,
      true
    );
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden()
    throws Exception {
    when(permissionCheckerService.isGroupAdmin("test-node-id")).thenReturn(
      false
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(notificationsApi);
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(permissionCheckerService.isGroupAdmin("test-node-id")).thenReturn(
      true
    );
    when(req.getParameter("pasteEnable")).thenReturn("true");
    when(req.getParameter("pasteAllEnable")).thenReturn("false");
    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id")
      )
    )
      .when(notificationsApi)
      .setPasteNotificationsState("test-node-id", true, false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError()
    throws Exception {
    when(permissionCheckerService.isGroupAdmin("test-node-id")).thenReturn(
      true
    );
    when(req.getParameter("pasteEnable")).thenReturn("true");
    when(req.getParameter("pasteAllEnable")).thenReturn("false");
    doThrow(new RuntimeException("unexpected"))
      .when(notificationsApi)
      .setPasteNotificationsState("test-node-id", true, false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesIdNotificationsPastePost.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
