package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EventsApi;
import io.swagger.model.permissions.EventPermissions;
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
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsIdEventsPostTest {

  private GroupsIdEventsPost webScript;
  private EventsApi eventsApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsIdEventsPost();
    eventsApi = mock(EventsApi.class);
    nodeService = mock(NodeService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("eventsApi", eventsApi);
    setField("nodeService", nodeService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
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
  public void testExecuteImpl_whenUserHasPermission_thenCreatesEvent()
    throws Exception {
    String igId = "test-ig-id";
    String appointmentBody = "{\"title\":\"Meeting\"}";
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igId
    );
    NodeRef evtNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "events-id"
    );

    WebScriptRequest req = mockRequest(igId, appointmentBody);
    Status status = new Status();
    Cache cache = new Cache();

    when(
      nodeService.getChildByName(eq(groupRef), any(), eq("Events"))
    ).thenReturn(evtNodeRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfEventPermission(
        eq("events-id"),
        eq(EventPermissions.EVEADMIN)
      )
    ).thenReturn(true);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(eventsApi).groupsIdEventsPost(igId, appointmentBody);
  }

  @Test
  public void testExecuteImpl_whenUserLacksPermission_thenReturnsForbidden()
    throws Exception {
    String igId = "test-ig-id";
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igId
    );
    NodeRef evtNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "events-id"
    );

    WebScriptRequest req = mockRequest(igId, "{\"title\":\"Meeting\"}");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      nodeService.getChildByName(eq(groupRef), any(), eq("Events"))
    ).thenReturn(evtNodeRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfEventPermission(
        eq("events-id"),
        eq(EventPermissions.EVEADMIN)
      )
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(eventsApi);
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsNotAcceptable()
    throws Exception {
    String igId = "test-ig-id";
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igId
    );
    NodeRef evtNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "events-id"
    );

    WebScriptRequest req = mockRequest(igId, "{\"title\":\"Meeting\"}");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      nodeService.getChildByName(eq(groupRef), any(), eq("Events"))
    ).thenReturn(evtNodeRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfEventPermission(
        eq("events-id"),
        eq(EventPermissions.EVEADMIN)
      )
    ).thenReturn(true);
    doThrow(new RuntimeException("Something went wrong"))
      .when(eventsApi)
      .groupsIdEventsPost(anyString(), anyString());

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("Something went wrong", status.getMessage());
  }

  private WebScriptRequest mockRequest(String igId, String body)
    throws Exception {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Content content = mock(Content.class);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", igId);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(body);

    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsIdEventsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
