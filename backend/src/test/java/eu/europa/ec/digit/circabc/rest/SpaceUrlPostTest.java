package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NodesApi;
import io.swagger.api.SpacesApi;
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
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpaceUrlPostTest {

  private static final String TEST_ID = "test-node-id";
  private static final String VALID_JSON =
    "{\"name\":\"test-url\",\"properties\":{\"url\":\"http://example.com\"}}";

  private SpaceUrlPost spaceUrlPost;
  private SpacesApi spacesApi;
  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    spaceUrlPost = new SpaceUrlPost();
    spacesApi = mock(SpacesApi.class);
    nodesApi = mock(NodesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("spacesApi", spacesApi);
    setField("nodesApi", nodesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsNode()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        TEST_ID
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    Node expectedNode = new Node();
    expectedNode.setName("test-url");
    when(spacesApi.spacesIdUrlPost(eq(TEST_ID), any(Node.class))).thenReturn(
      expectedNode
    );

    Map<String, Object> result = spaceUrlPost.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(expectedNode, result.get("n"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        TEST_ID
      )
    ).thenReturn(false);

    Map<String, Object> result = spaceUrlPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        TEST_ID
      )
    ).thenReturn(true);
    when(req.getContent()).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> result = spaceUrlPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError() {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        TEST_ID
      )
    ).thenReturn(true);
    when(req.getContent()).thenThrow(new RuntimeException("unexpected"));

    Map<String, Object> result = spaceUrlPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        TEST_ID
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    Node expectedNode = new Node();
    when(spacesApi.spacesIdUrlPost(eq(TEST_ID), any(Node.class))).thenReturn(
      expectedNode
    );

    Map<String, Object> result = spaceUrlPost.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(expectedNode, result.get("n"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpaceUrlPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(spaceUrlPost, value);
  }
}
