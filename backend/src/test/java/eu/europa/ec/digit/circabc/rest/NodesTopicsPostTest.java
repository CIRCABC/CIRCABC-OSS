package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import io.swagger.model.Node;
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

public class NodesTopicsPostTest {

  private NodesTopicsPost nodesTopicsPost;
  private ContentApi contentApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String NODE_ID = "test-node-id";

  @Before
  public void setUp() throws Exception {
    nodesTopicsPost = new NodesTopicsPost();
    contentApi = mock(ContentApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("contentApi", contentApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", NODE_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsTopic()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    String jsonBody =
      "{\"name\":\"Test Topic\",\"title\":{\"en\":\"Test Title\"}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    Node resultNode = new Node();
    resultNode.setId("created-id");
    resultNode.setName("Test Topic");
    when(
      contentApi.contentIdTopicsPost(eq(NODE_ID), any(Node.class))
    ).thenReturn(resultNode);

    Map<String, Object> model = nodesTopicsPost.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultNode, model.get("topic"));
    verify(contentApi).contentIdTopicsPost(eq(NODE_ID), any(Node.class));
  }

  @Test
  public void testExecuteImpl_whenNameIsNull_thenUsesDefaultTitle()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    String jsonBody =
      "{\"name\":\"null\",\"title\":{\"en\":\"Fallback Title\"}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    Node resultNode = new Node();
    when(
      contentApi.contentIdTopicsPost(eq(NODE_ID), any(Node.class))
    ).thenReturn(resultNode);

    Map<String, Object> model = nodesTopicsPost.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultNode, model.get("topic"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(false);

    Map<String, Object> model = nodesTopicsPost.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = nodesTopicsPost.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    String jsonBody = "{\"name\":\"Topic\",\"title\":{\"en\":\"Title\"}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    when(
      contentApi.contentIdTopicsPost(eq(NODE_ID), any(Node.class))
    ).thenThrow(new RuntimeException("unexpected"));

    Map<String, Object> model = nodesTopicsPost.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesTopicsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(nodesTopicsPost, value);
  }
}
