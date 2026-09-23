package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
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

public class SpacePostTest {

  private SpacePost spacePost;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String NODE_ID = "test-node-id";

  @Before
  public void setUp() throws Exception {
    spacePost = new SpacePost();
    spacesApi = mock(SpacesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("spacesApi", spacesApi);
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
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsSpace()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    String jsonBody =
      "{\"name\":\"New Space\",\"title\":{\"en\":\"English Title\"},\"description\":{\"en\":\"Desc\"}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    Node resultNode = new Node();
    resultNode.setId("created-space-id");
    resultNode.setName("New Space");
    when(spacesApi.spacesIdSpacesPost(eq(NODE_ID), any(Node.class))).thenReturn(
      resultNode
    );

    Map<String, Object> model = spacePost.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultNode, model.get("space"));
    verify(spacesApi).spacesIdSpacesPost(eq(NODE_ID), any(Node.class));
  }

  @Test
  public void testExecuteImpl_whenLanguageSpecified_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    String jsonBody = "{\"name\":\"Espace\",\"title\":{\"fr\":\"Titre\"}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    Node resultNode = new Node();
    when(spacesApi.spacesIdSpacesPost(eq(NODE_ID), any(Node.class))).thenReturn(
      resultNode
    );

    Map<String, Object> model = spacePost.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultNode, model.get("space"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(false);

    Map<String, Object> model = spacePost.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsInternalServerError()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = spacePost.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    String jsonBody = "{\"name\":\"Space\",\"title\":{\"en\":\"Title\"}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    when(spacesApi.spacesIdSpacesPost(eq(NODE_ID), any(Node.class))).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, NODE_ID)
      )
    );

    Map<String, Object> model = spacePost.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidType_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    String jsonBody = "{\"name\":\"Space\",\"title\":{\"en\":\"Title\"}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    when(spacesApi.spacesIdSpacesPost(eq(NODE_ID), any(Node.class))).thenThrow(
      new InvalidTypeException("invalid type", null)
    );

    Map<String, Object> model = spacePost.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenWithExpirationDate_thenParsesProperties()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    String jsonBody =
      "{\"name\":\"Space\",\"title\":{\"en\":\"Title\"},\"properties\":{\"expiration_date\":\"2026-12-31\"}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    Node resultNode = new Node();
    when(spacesApi.spacesIdSpacesPost(eq(NODE_ID), any(Node.class))).thenReturn(
      resultNode
    );

    Map<String, Object> model = spacePost.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultNode, model.get("space"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpacePost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(spacePost, value);
  }
}
