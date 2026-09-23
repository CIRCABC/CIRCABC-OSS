package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NodesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

public class PathGetTest {

  private PathGet pathGet;
  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    pathGet = new PathGet();
    nodesApi = mock(NodesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("nodesApi", nodesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenLanguageNull_thenReturnsPath() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionChecker.hasAlfrescoReadPermission("test-node-id")
    ).thenReturn(true);
    List<Node> nodes = Collections.singletonList(new Node());
    when(nodesApi.getPathByNode("test-node-id")).thenReturn(nodes);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertEquals(nodes, model.get("nodes"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsPath() {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      permissionChecker.hasAlfrescoReadPermission("test-node-id")
    ).thenReturn(true);
    List<Node> nodes = Collections.singletonList(new Node());
    when(nodesApi.getPathByNode("test-node-id")).thenReturn(nodes);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertEquals(nodes, model.get("nodes"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionChecker.hasAlfrescoReadPermission("test-node-id")
    ).thenReturn(false);

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionChecker.hasAlfrescoReadPermission("test-node-id")
    ).thenReturn(true);
    when(nodesApi.getPathByNode("test-node-id")).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id")
      )
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionChecker.hasAlfrescoReadPermission("test-node-id")
    ).thenReturn(true);
    when(nodesApi.getPathByNode("test-node-id")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() {
    try {
      java.lang.reflect.Method method =
        PathGet.class.getSuperclass().getDeclaredMethod(
          "executeImpl",
          WebScriptRequest.class,
          Status.class,
          Cache.class
        );
      method.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, Object> result = (Map<String, Object>) method.invoke(
        pathGet,
        req,
        status,
        cache
      );
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PathGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(pathGet, value);
  }
}
