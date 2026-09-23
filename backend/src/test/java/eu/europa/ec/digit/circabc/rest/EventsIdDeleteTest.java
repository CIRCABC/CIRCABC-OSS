package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EventsApi;
import io.swagger.model.UpdateMode;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class EventsIdDeleteTest {

  private EventsIdDelete webScript;
  private EventsApi eventsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private ApiToolBox apiToolBox;
  private NodeService unsecureNodeService;

  @Before
  public void setUp() throws Exception {
    webScript = new EventsIdDelete();
    eventsApi = mock(EventsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    apiToolBox = mock(ApiToolBox.class);
    unsecureNodeService = mock(NodeService.class);

    setField(EventsIdDelete.class, "eventsApi", eventsApi);
    setField(
      EventsIdDelete.class,
      "currentUserPermissionCheckerService",
      permissionChecker
    );
    setField(CircabcDeclarativeWebScript.class, "apiToolBox", apiToolBox);
    setField(
      CircabcDeclarativeWebScript.class,
      "unsecureNodeService",
      unsecureNodeService
    );

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
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenDeletesEvent() {
    String id = "test-event-id";
    WebScriptRequest req = mockRequest(id, "Single");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfEventPermission(id, EventPermissions.EVEADMIN)
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(parentRef);
    when(apiToolBox.getCircabcPath(nodeRef, true)).thenReturn("/some/path");
    when(apiToolBox.getDatabaseID(nodeRef)).thenReturn(123L);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(eventsApi).eventsIdDelete(id, UpdateMode.Single);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenUpdateModeNull_thenThrows() {
    WebScriptRequest req = mockRequest("test-id", null);
    Status status = new Status();
    Cache cache = new Cache();

    webScript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenUpdateModeEmpty_thenThrows() {
    WebScriptRequest req = mockRequest("test-id", "");
    Status status = new Status();
    Cache cache = new Cache();

    webScript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String id = "test-event-id";
    WebScriptRequest req = mockRequest(id, "Single");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfEventPermission(id, EventPermissions.EVEADMIN)
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsNotAcceptable() {
    String id = "test-event-id";
    WebScriptRequest req = mockRequest(id, "Single");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfEventPermission(id, EventPermissions.EVEADMIN)
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(parentRef);
    when(apiToolBox.getCircabcPath(nodeRef, true)).thenReturn("/some/path");
    when(apiToolBox.getDatabaseID(nodeRef)).thenReturn(123L);

    doThrow(new RuntimeException("delete failed"))
      .when(eventsApi)
      .eventsIdDelete(id, UpdateMode.Single);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("delete failed", status.getMessage());
  }

  private WebScriptRequest mockRequest(String id, String updateMode) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    Match match = new Match(
      "/circabc/events/{id}",
      templateVars,
      "/circabc/events/" + id
    );
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("updateMode")).thenReturn(updateMode);
    return req;
  }

  private void setField(Class<?> clazz, String fieldName, Object value)
    throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
