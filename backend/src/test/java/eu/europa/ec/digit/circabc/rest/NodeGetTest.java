package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NodesApi;
import io.swagger.model.Node;
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

public class NodeGetTest {

  private NodeGet webScript;
  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new NodeGet();
    nodesApi = mock(NodesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("nodesApi", nodesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodeGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidNodeNoLanguage_thenReturnsModel() {
    String nodeId = "test-node-id";
    mockTemplateVars(nodeId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(nodeId)).thenReturn(true);
    Node node = new Node();
    when(nodesApi.getNodeById(nodeId)).thenReturn(node);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertSame(node, model.get("n"));
    verify(nodesApi).getNodeById(nodeId);
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsModel() {
    String nodeId = "test-node-id";
    mockTemplateVars(nodeId);
    when(req.getParameter("language")).thenReturn("fr");
    when(permissionChecker.hasAlfrescoReadPermission(nodeId)).thenReturn(true);
    Node node = new Node();
    when(nodesApi.getNodeById(nodeId)).thenReturn(node);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertSame(node, model.get("n"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String nodeId = "test-node-id";
    mockTemplateVars(nodeId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(nodeId)).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNodeDoesNotExist_thenReturnsNotFound() {
    String nodeId = "missing-id";
    mockTemplateVars(nodeId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(nodeId)).thenReturn(true);
    when(nodesApi.getNodeById(nodeId)).thenThrow(
      new InvalidNodeRefException(
        "Node does not exist: workspace://SpacesStore/missing-id",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "missing-id")
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_FOUND, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    String nodeId = "invalid-id";
    mockTemplateVars(nodeId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(nodeId)).thenReturn(true);
    when(nodesApi.getNodeById(nodeId)).thenThrow(
      new InvalidNodeRefException(
        "invalid reference format",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "invalid-id")
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError() {
    String nodeId = "test-node-id";
    mockTemplateVars(nodeId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(nodeId)).thenReturn(true);
    when(nodesApi.getNodeById(nodeId)).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
