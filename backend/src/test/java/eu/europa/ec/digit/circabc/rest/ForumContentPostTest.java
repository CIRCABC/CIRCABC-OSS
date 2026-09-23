package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
import io.swagger.model.Node;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
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

public class ForumContentPostTest {

  private ForumContentPost webScript;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;

  @Before
  public void setUp() throws Exception {
    webScript = new ForumContentPost();
    forumsApi = mock(ForumsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("forumsApi", forumsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsPost()
    throws Exception {
    String nodeId = "test-node-id";
    WebScriptRequest req = mockRequest(nodeId, "{\"name\":\"test\"}");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        nodeId,
        NewsGroupPermissions.NWSPOST
      )
    ).thenReturn(true);

    Node resultNode = new Node();
    resultNode.setName("test");
    when(forumsApi.forumsIdContentPost(eq(nodeId), any(Node.class))).thenReturn(
      resultNode
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultNode, model.get("post"));
  }

  @Test
  public void testExecuteImpl_whenNullId_thenReturnsEmptyModel() {
    WebScriptRequest req = mockRequest(null, "{}");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertFalse(model.containsKey("post"));
    verifyNoInteractions(forumsApi);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String nodeId = "test-node-id";
    WebScriptRequest req = mockRequest(nodeId, "{\"name\":\"test\"}");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        nodeId,
        NewsGroupPermissions.NWSPOST
      )
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    String nodeId = "test-node-id";
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", nodeId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new IOException("read error"));

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testGetForumsApi_returnsInjectedInstance() {
    assertEquals(forumsApi, webScript.getForumsApi());
  }

  private WebScriptRequest mockRequest(String id, String body) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    try {
      Content content = mock(Content.class);
      when(req.getContent()).thenReturn(content);
      when(content.getContent()).thenReturn(body);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ForumContentPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
