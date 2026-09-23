package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsKeywordsPostTest {

  private GroupsKeywordsPost webScript;
  private KeywordsApi keywordsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsKeywordsPost();
    keywordsApi = mock(KeywordsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("keywordsApi", keywordsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsKeywordsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String igId) {
    Map<String, String> vars = new HashMap<>();
    vars.put("igId", igId);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsKeyword()
    throws Exception {
    String igId = "test-ig-id";
    mockTemplateVars(igId);
    when(permissionChecker.isGroupAdmin(igId)).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(
      "{\"id\":\"kw-1\",\"title\":{\"en\":\"Test\"}}"
    );
    when(req.getContent()).thenReturn(content);

    KeywordDefinition expected = new KeywordDefinition();
    expected.setId("kw-1");
    when(
      keywordsApi.groupsIdKeywordsPost(eq(igId), any(KeywordDefinition.class))
    ).thenReturn(expected);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(expected, model.get("keyword"));
    verify(keywordsApi).groupsIdKeywordsPost(
      eq(igId),
      any(KeywordDefinition.class)
    );
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden()
    throws Exception {
    String igId = "test-ig-id";
    mockTemplateVars(igId);
    when(permissionChecker.isGroupAdmin(igId)).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    String igId = "test-ig-id";
    mockTemplateVars(igId);
    when(permissionChecker.isGroupAdmin(igId)).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsBadRequest()
    throws Exception {
    String igId = "test-ig-id";
    mockTemplateVars(igId);
    when(permissionChecker.isGroupAdmin(igId)).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("not valid json {{{");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
