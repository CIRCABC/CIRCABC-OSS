package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SubspacesIdGetTest {

  private SubspacesIdGet subspacesIdGet;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    subspacesIdGet = new SubspacesIdGet();
    spacesApi = mock(SpacesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("spacesApi", spacesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SubspacesIdGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(subspacesIdGet, value);
  }

  private void mockRequest(
    String id,
    String sort,
    String order,
    String language,
    String skipExpired
  ) {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(language);
    when(req.getParameter("sort")).thenReturn(sort);
    when(req.getParameter("order")).thenReturn(order);
    when(req.getParameter("skipExpiredItems")).thenReturn(skipExpired);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenIdIsNull_thenThrowsIllegalArgument() {
    mockRequest(null, null, null, null, null);
    subspacesIdGet.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenIdIsEmpty_thenThrowsIllegalArgument() {
    mockRequest("", null, null, null, null);
    subspacesIdGet.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    mockRequest("test-id", "name", "asc", null, null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        anyString(),
        any()
      )
    ).thenReturn(false);

    Map<String, Object> result = subspacesIdGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteImpl_whenHasPermission_thenReturnsOnlyFolders() {
    mockRequest("test-id", "name", "asc", null, null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        anyString(),
        any()
      )
    ).thenReturn(true);

    Node folder = new Node();
    folder.setType(ContentModel.TYPE_FOLDER.toString());
    folder.setId("folder-1");

    Node content = new Node();
    content.setType(ContentModel.TYPE_CONTENT.toString());
    content.setId("content-1");

    List<Node> nodes = new ArrayList<>();
    nodes.add(folder);
    nodes.add(content);

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(nodes);

    when(
      spacesApi.spaceGetChildren(
        "test-id",
        0,
        -1,
        "name_asc",
        true,
        false,
        false
      )
    ).thenReturn(pagedNodes);

    Map<String, Object> result = subspacesIdGet.executeImpl(req, status, cache);

    assertNotNull(result);
    List<Node> data = (List<Node>) result.get("data");
    assertEquals(1, data.size());
    assertEquals("folder-1", data.get(0).getId());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsNotAcceptable() {
    mockRequest("test-id", "name", "asc", null, null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        anyString(),
        any()
      )
    ).thenReturn(true);

    when(
      spacesApi.spaceGetChildren(
        "test-id",
        0,
        -1,
        "name_asc",
        true,
        false,
        false
      )
    ).thenThrow(new RuntimeException("Something went wrong"));

    Map<String, Object> result = subspacesIdGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteImpl_whenNoFolders_thenReturnsEmptyList() {
    mockRequest("test-id", "name", "desc", null, null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        anyString(),
        any()
      )
    ).thenReturn(true);

    Node content = new Node();
    content.setType(ContentModel.TYPE_CONTENT.toString());

    List<Node> nodes = new ArrayList<>();
    nodes.add(content);

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(nodes);

    when(
      spacesApi.spaceGetChildren(
        "test-id",
        0,
        -1,
        "name_desc",
        true,
        false,
        false
      )
    ).thenReturn(pagedNodes);

    Map<String, Object> result = subspacesIdGet.executeImpl(req, status, cache);

    assertNotNull(result);
    List<Node> data = (List<Node>) result.get("data");
    assertTrue(data.isEmpty());
  }
}
