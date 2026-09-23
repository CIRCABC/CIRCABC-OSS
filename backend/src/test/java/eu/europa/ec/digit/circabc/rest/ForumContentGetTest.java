package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
import io.swagger.model.PagedNodes;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ForumContentGetTest {

  private ForumContentGet forumContentGet;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    forumContentGet = new ForumContentGet();
    forumsApi = mock(ForumsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("forumsApi", forumsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  @Test
  public void testExecuteImpl_whenValidId_thenReturnsData() {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);

    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-node-id"
      )
    ).thenReturn(true);

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(new ArrayList<>());
    pagedNodes.setTotal(5L);
    when(forumsApi.getForumById("test-node-id", 0, 100, null)).thenReturn(
      pagedNodes
    );

    Map<String, Object> result = forumContentGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(new ArrayList<>(), result.get("data"));
    assertEquals(5L, result.get("total"));
  }

  @Test
  public void testExecuteImpl_whenPageAndLimitProvided_thenUsesParameters() {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("page")).thenReturn("3");
    when(req.getParameter("limit")).thenReturn("25");
    when(req.getParameter("order")).thenReturn("created_ASC");

    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-node-id"
      )
    ).thenReturn(true);

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(new ArrayList<>());
    pagedNodes.setTotal(0L);
    when(
      forumsApi.getForumById("test-node-id", 2, 25, "created_ASC")
    ).thenReturn(pagedNodes);

    Map<String, Object> result = forumContentGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(forumsApi).getForumById("test-node-id", 2, 25, "created_ASC");
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "restricted-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);

    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "restricted-id"
      )
    ).thenReturn(false);

    Map<String, Object> result = forumContentGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullId_thenReturnsEmptyModel() {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);

    Map<String, Object> result = forumContentGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(forumsApi);
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsInternalError() {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);

    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-node-id"
      )
    ).thenReturn(true);
    when(forumsApi.getForumById("test-node-id", 0, 100, null)).thenThrow(
      new RuntimeException("unexpected error")
    );

    Map<String, Object> result = forumContentGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ForumContentGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(forumContentGet, value);
  }
}
