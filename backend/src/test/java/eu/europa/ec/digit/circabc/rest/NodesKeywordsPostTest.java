package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesKeywordsPostTest {

  private NodesKeywordsPost nodesKeywordsPost;
  private KeywordsApi keywordsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String NODE_ID = "test-node-id";
  private static final String VALID_JSON =
    "{\"id\":\"keyword-id\",\"title\":{\"en\":\"Test Keyword\"}}";

  @Before
  public void setUp() throws Exception {
    nodesKeywordsPost = new NodesKeywordsPost();
    keywordsApi = mock(KeywordsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("keywordsApi", keywordsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = Map.of("id", NODE_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserHasPermission_thenPostsKeyword()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(NODE_ID),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(true);
    setupRequestBody(VALID_JSON);

    Map<String, Object> result = nodesKeywordsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(keywordsApi).nodesIdKeywordsPost(
      eq(NODE_ID),
      any(KeywordDefinition.class)
    );
  }

  @Test
  public void testExecuteImpl_whenUserLacksPermission_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(NODE_ID),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(false);

    Map<String, Object> result = nodesKeywordsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(keywordsApi, never()).nodesIdKeywordsPost(
      anyString(),
      any(KeywordDefinition.class)
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(NODE_ID),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(true);
    setupRequestBody("not valid json");

    Map<String, Object> result = nodesKeywordsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(NODE_ID),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(true);
    setupRequestBody(VALID_JSON);
    doThrow(new RuntimeException("unexpected"))
      .when(keywordsApi)
      .nodesIdKeywordsPost(anyString(), any(KeywordDefinition.class));

    Map<String, Object> result = nodesKeywordsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setupRequestBody(String json) throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json);
    when(req.getContent()).thenReturn(content);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesKeywordsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(nodesKeywordsPost, value);
  }
}
