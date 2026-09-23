package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
import io.swagger.model.Node;
import io.swagger.model.permissions.NewsGroupPermissions;
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

public class ForumPostTest {

  private ForumPost forumPost;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-node-id";
  private static final String VALID_JSON =
    "{\"name\":\"Test Forum\",\"title\":{},\"description\":{}}";

  @Before
  public void setUp() throws Exception {
    forumPost = new ForumPost();
    forumsApi = mock(ForumsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("forumsApi", forumsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenPermissionGranted_thenReturnsForum()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        TEST_ID,
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(true);

    Node resultNode = new Node();
    resultNode.setName("Test Forum");
    when(
      forumsApi.forumsIdSubforumsPost(eq(TEST_ID), any(Node.class))
    ).thenReturn(resultNode);

    Map<String, Object> model = forumPost.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultNode, model.get("forum"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        TEST_ID,
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(false);

    Map<String, Object> model = forumPost.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsServerError()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("not json");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = forumPost.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test(expected = NullPointerException.class)
  public void testExecuteImpl_whenNullContent_thenThrowsNPE() throws Exception {
    when(req.getContent()).thenReturn(null);

    forumPost.executeImpl(req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ForumPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(forumPost, value);
  }
}
