package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.TopicsApi;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class TopicRepliesGetTest {

  private TopicRepliesGet topicRepliesGet;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    topicRepliesGet = new TopicRepliesGet();
    topicsApi = mock(TopicsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("topicsApi", topicsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-topic-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsData()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(anyString(), any())
    ).thenReturn(true);

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(Collections.singletonList(new Node()));
    pagedNodes.setTotal(1L);

    when(topicsApi.getTopicReplies("test-topic-id", 0, 25, null)).thenReturn(
      pagedNodes
    );

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    assertEquals(pagedNodes.getData(), result.get("data"));
    assertEquals(1L, result.get("total"));
  }

  @Test
  public void testExecuteImpl_whenPageAndLimitProvided_thenParsesCorrectly()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(anyString(), any())
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn("3");
    when(req.getParameter("limit")).thenReturn("10");

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(Collections.emptyList());
    pagedNodes.setTotal(0L);

    when(topicsApi.getTopicReplies("test-topic-id", 2, 10, null)).thenReturn(
      pagedNodes
    );

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    assertEquals(0L, result.get("total"));
  }

  @Test
  public void testExecuteImpl_whenPageIsZero_thenReturnsPageZero()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(anyString(), any())
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn("0");

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(Collections.emptyList());
    pagedNodes.setTotal(0L);

    when(topicsApi.getTopicReplies("test-topic-id", 0, 25, null)).thenReturn(
      pagedNodes
    );

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(anyString(), any())
    ).thenReturn(false);
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(anyString(), any())
    ).thenReturn(false);

    Map<String, Object> result = invokeExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(anyString(), any())
    ).thenReturn(true);
    when(
      topicsApi.getTopicReplies(anyString(), anyInt(), anyInt(), any())
    ).thenThrow(
      new InvalidNodeRefException(
        "bad ref",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad")
      )
    );

    Map<String, Object> result = invokeExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIdIsNull_thenReturnsEmptyModel()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(any(), any())
    ).thenReturn(true);

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    assertFalse(result.containsKey("data"));
  }

  private Map<String, Object> invokeExecuteImpl() throws Exception {
    java.lang.reflect.Method method = org.springframework.extensions.webscripts
      .DeclarativeWebScript.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      topicRepliesGet,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = TopicRepliesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(topicRepliesGet, value);
  }
}
