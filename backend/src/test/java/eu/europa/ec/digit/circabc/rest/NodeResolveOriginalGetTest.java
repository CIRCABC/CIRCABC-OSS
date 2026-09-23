package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NodesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodeResolveOriginalGetTest {

  private static final String VALID_UUID =
    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

  private NodeResolveOriginalGet webScript;
  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new NodeResolveOriginalGet();
    nodesApi = mock(NodesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
    setField("nodesApi", nodesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodeResolveOriginalGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockOriginalId(String originalId) {
    Map<String, String> vars = new HashMap<>();
    vars.put("originalId", originalId);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenInvalidUuid_thenBadRequest() {
    mockOriginalId("not-a-uuid");

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    verify(nodesApi, never()).resolveByOriginalNodeRef(anyString());
  }

  @Test
  public void testExecuteImpl_whenNoNode_thenNotFound() {
    mockOriginalId(VALID_UUID);
    when(nodesApi.resolveByOriginalNodeRef(VALID_UUID)).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_FOUND, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenResolved_thenReturnsNodeModel() {
    mockOriginalId(VALID_UUID);
    Node node = new Node();
    when(nodesApi.resolveByOriginalNodeRef(VALID_UUID)).thenReturn(node);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertSame(node, model.get("n"));
  }
}
