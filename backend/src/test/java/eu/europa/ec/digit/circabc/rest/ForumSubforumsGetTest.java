package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ForumSubforumsGetTest {

  private ForumSubforumsGet webScript;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new ForumSubforumsGet();
    forumsApi = mock(ForumsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("forumsApi", forumsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenEmptySortOrder_thenCallsDefaultMethod() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn("");
    when(req.getParameter("order")).thenReturn("");
    when(permissionChecker.hasAlfrescoReadPermission("test-id")).thenReturn(
      true
    );
    List<Node> nodes = Collections.singletonList(new Node());
    when(forumsApi.forumsIdSubforumsGet("test-id")).thenReturn(nodes);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("test-id", model.get("id"));
    assertEquals(nodes, model.get("nodes"));
    verify(forumsApi).forumsIdSubforumsGet("test-id");
  }

  @Test
  public void testExecuteImpl_whenSortAndOrder_thenCallsSortedMethod() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn("created");
    when(req.getParameter("order")).thenReturn("DESC");
    when(permissionChecker.hasAlfrescoReadPermission("test-id")).thenReturn(
      true
    );
    List<Node> nodes = Collections.singletonList(new Node());
    when(forumsApi.forumsIdSubforumsGet("test-id", "created_DESC")).thenReturn(
      nodes
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(nodes, model.get("nodes"));
    verify(forumsApi).forumsIdSubforumsGet("test-id", "created_DESC");
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn("");
    when(req.getParameter("order")).thenReturn("");
    when(permissionChecker.hasAlfrescoReadPermission("test-id")).thenReturn(
      false
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIdIsNull_thenReturnsEmptyModel() {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn("");
    when(req.getParameter("order")).thenReturn("");

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertFalse(model.containsKey("nodes"));
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale() {
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("sort")).thenReturn("");
    when(req.getParameter("order")).thenReturn("");
    when(permissionChecker.hasAlfrescoReadPermission("test-id")).thenReturn(
      true
    );
    when(forumsApi.forumsIdSubforumsGet("test-id")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("nodes"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ForumSubforumsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
