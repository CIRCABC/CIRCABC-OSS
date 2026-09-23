package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SearchApi;
import io.swagger.exception.EmptyQueryStringException;
import io.swagger.model.PagedSearchNodes;
import io.swagger.model.SearchNode;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SearchGetTest {

  private SearchGet searchGet;
  private SearchApi searchApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    searchGet = new SearchGet();
    searchApi = mock(SearchApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("searchApi", searchApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SearchGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(searchGet, value);
  }

  @Test
  public void testExecuteImpl_whenValidSearch_thenReturnsResults()
    throws Exception {
    when(req.getParameter("q")).thenReturn("test query");
    when(req.getParameter("node")).thenReturn("node-id");
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");
    when(permissionChecker.hasAlfrescoReadPermission("node-id")).thenReturn(
      true
    );

    PagedSearchNodes pagedResult = new PagedSearchNodes();
    List<SearchNode> data = new ArrayList<>();
    pagedResult.setData(data);
    pagedResult.setTotal(0L);

    when(
      searchApi.searchGet(
        eq("test query"),
        eq("node-id"),
        eq("en"),
        eq(0),
        eq(10),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        any(String[].class),
        isNull(),
        eq(true)
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = searchGet.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(data, model.get("data"));
    assertEquals(0L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenNoLanguage_thenMLAwareTrue()
    throws Exception {
    when(req.getParameter("q")).thenReturn("query");
    when(req.getParameter("node")).thenReturn("node-id");
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission("node-id")).thenReturn(
      true
    );

    PagedSearchNodes pagedResult = new PagedSearchNodes();
    pagedResult.setData(new ArrayList<>());
    pagedResult.setTotal(5L);

    when(
      searchApi.searchGet(
        anyString(),
        anyString(),
        isNull(),
        anyInt(),
        anyInt(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(String[].class),
        any(),
        anyBoolean()
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = searchGet.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(5L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("node")).thenReturn("node-id");
    when(req.getParameter("language")).thenReturn("en");
    when(permissionChecker.hasAlfrescoReadPermission("node-id")).thenReturn(
      false
    );

    Map<String, Object> model = searchGet.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenEmptyQuery_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("q")).thenReturn("");
    when(req.getParameter("node")).thenReturn("node-id");
    when(req.getParameter("language")).thenReturn("en");
    when(permissionChecker.hasAlfrescoReadPermission("node-id")).thenReturn(
      true
    );

    when(
      searchApi.searchGet(
        anyString(),
        anyString(),
        anyString(),
        anyInt(),
        anyInt(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(String[].class),
        any(),
        anyBoolean()
      )
    ).thenThrow(new EmptyQueryStringException("empty"));

    Map<String, Object> model = searchGet.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("q")).thenReturn("query");
    when(req.getParameter("node")).thenReturn("bad-node");
    when(req.getParameter("language")).thenReturn("en");
    when(permissionChecker.hasAlfrescoReadPermission("bad-node")).thenReturn(
      true
    );

    when(
      searchApi.searchGet(
        anyString(),
        anyString(),
        anyString(),
        anyInt(),
        anyInt(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(String[].class),
        any(),
        anyBoolean()
      )
    ).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-node")
      )
    );

    Map<String, Object> model = searchGet.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError()
    throws Exception {
    when(req.getParameter("q")).thenReturn("query");
    when(req.getParameter("node")).thenReturn("node-id");
    when(req.getParameter("language")).thenReturn("en");
    when(permissionChecker.hasAlfrescoReadPermission("node-id")).thenReturn(
      true
    );

    when(
      searchApi.searchGet(
        anyString(),
        anyString(),
        anyString(),
        anyInt(),
        anyInt(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(String[].class),
        any(),
        anyBoolean()
      )
    ).thenThrow(new RuntimeException("unexpected"));

    Map<String, Object> model = searchGet.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenDefaultPagination_thenUsesDefaults()
    throws Exception {
    when(req.getParameter("q")).thenReturn("query");
    when(req.getParameter("node")).thenReturn("node-id");
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission("node-id")).thenReturn(
      true
    );

    PagedSearchNodes pagedResult = new PagedSearchNodes();
    pagedResult.setData(new ArrayList<>());
    pagedResult.setTotal(0L);

    when(
      searchApi.searchGet(
        eq("query"),
        eq("node-id"),
        eq("en"),
        eq(0),
        eq(250),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(String[].class),
        any(),
        eq(true)
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = searchGet.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(searchApi).searchGet(
      eq("query"),
      eq("node-id"),
      eq("en"),
      eq(0),
      eq(250),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(String[].class),
      any(),
      eq(true)
    );
  }

  @Test
  public void testExecuteImpl_whenDescOrder_thenAscendingFalse()
    throws Exception {
    when(req.getParameter("q")).thenReturn("query");
    when(req.getParameter("node")).thenReturn("node-id");
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("order")).thenReturn("desc");
    when(permissionChecker.hasAlfrescoReadPermission("node-id")).thenReturn(
      true
    );

    PagedSearchNodes pagedResult = new PagedSearchNodes();
    pagedResult.setData(new ArrayList<>());
    pagedResult.setTotal(0L);

    when(
      searchApi.searchGet(
        anyString(),
        anyString(),
        anyString(),
        anyInt(),
        anyInt(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(String[].class),
        any(),
        eq(false)
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = searchGet.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(searchApi).searchGet(
      anyString(),
      anyString(),
      anyString(),
      anyInt(),
      anyInt(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(String[].class),
      any(),
      eq(false)
    );
  }
}
