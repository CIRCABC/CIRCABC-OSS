package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.TopicsApi;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class TopicsDeleteTest {

  private TopicsDelete topicsDelete;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private ApiToolBox apiToolBox;
  private NodeService unsecureNodeService;

  @Before
  public void setUp() throws Exception {
    topicsDelete = new TopicsDelete();
    topicsApi = mock(TopicsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    apiToolBox = mock(ApiToolBox.class);
    unsecureNodeService = mock(NodeService.class);

    setField(TopicsDelete.class, "topicsApi", topicsApi);
    setField(
      TopicsDelete.class,
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
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
  public void testExecuteImpl_whenHasPermission_thenDeletesAndReturnsOk()
    throws Exception {
    String nodeId = "test-node-id";
    WebScriptRequest req = mockRequest(nodeId);
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoDeletePermission(nodeId)
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      nodeId
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(parentRef);
    when(apiToolBox.getCircabcPath(nodeRef, true)).thenReturn("/some/path");
    when(apiToolBox.getDatabaseID(nodeRef)).thenReturn(1L);

    Map<String, Object> result = topicsDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(topicsApi).topicsIdDelete(nodeId);
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    String nodeId = "test-node-id";
    WebScriptRequest req = mockRequest(nodeId);
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoDeletePermission(nodeId)
    ).thenReturn(false);

    Map<String, Object> result = topicsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(topicsApi, never()).topicsIdDelete(anyString());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    String nodeId = "invalid-node-id";
    WebScriptRequest req = mockRequest(nodeId);
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoDeletePermission(nodeId)
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      nodeId
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenThrow(
      new InvalidNodeRefException(nodeRef)
    );

    Map<String, Object> result = topicsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError()
    throws Exception {
    String nodeId = "test-node-id";
    WebScriptRequest req = mockRequest(nodeId);
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoDeletePermission(nodeId)
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      nodeId
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> result = topicsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private WebScriptRequest mockRequest(String nodeId) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", nodeId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    return req;
  }

  private void setField(Class<?> clazz, String fieldName, Object value)
    throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(topicsDelete, value);
  }
}
