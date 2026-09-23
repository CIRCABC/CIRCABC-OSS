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
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodeOriginalNodeRefPutTest {

  private NodeOriginalNodeRefPut webScript;
  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new NodeOriginalNodeRefPut();
    nodesApi = mock(NodesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
    setField("nodesApi", nodesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodeOriginalNodeRefPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  private void mockBody(String body) throws Exception {
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(body);
  }

  @Test
  public void testExecuteImpl_whenValidBody_thenSetsRef() throws Exception {
    mockTemplateVars("n1");
    mockBody("{\"originalNodeRef\":\"workspace://SpacesStore/old-1\"}");
    when(
      nodesApi.setOriginalNodeRef("n1", "workspace://SpacesStore/old-1")
    ).thenReturn(new Node());

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("workspace://SpacesStore/old-1", model.get("originalNodeRef"));
    verify(nodesApi).setOriginalNodeRef("n1", "workspace://SpacesStore/old-1");
  }

  @Test
  public void testExecuteImpl_whenInvalidBody_thenBadRequest()
    throws Exception {
    mockTemplateVars("n1");
    mockBody("{\"wrong\":\"x\"}");

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    verify(nodesApi, never()).setOriginalNodeRef(anyString(), anyString());
  }
}
