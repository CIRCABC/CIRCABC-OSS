package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
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
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpaceGetTest {

  private SpaceGet webScript;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new SpaceGet();
    spacesApi = mock(SpacesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("spacesApi", spacesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpaceGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenUnpaginated_thenReturnsAllChildren() {
    String spaceId = "space-id-1";
    mockTemplateVars(spaceId);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(req.getParameter("folderOnly")).thenReturn(null);
    when(req.getParameter("fileOnly")).thenReturn(null);
    when(req.getParameter("skipExpiredItems")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(spaceId)).thenReturn(true);

    List<Node> nodes = new ArrayList<>();
    nodes.add(new Node());
    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(nodes);
    pagedNodes.setTotal(1L);
    when(
      spacesApi.spaceGetChildren(spaceId, -1, -1, "", false, false, false)
    ).thenReturn(pagedNodes);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(nodes, model.get("data"));
    assertEquals(1L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenPaginated_thenPassesPageAndLimit() {
    String spaceId = "space-id-2";
    mockTemplateVars(spaceId);
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("page")).thenReturn("2");
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("order")).thenReturn("name_ASC");
    when(req.getParameter("folderOnly")).thenReturn("true");
    when(req.getParameter("fileOnly")).thenReturn("false");
    when(req.getParameter("skipExpiredItems")).thenReturn("true");
    when(permissionChecker.hasAlfrescoReadPermission(spaceId)).thenReturn(true);

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(new ArrayList<>());
    pagedNodes.setTotal(0L);
    // page "2" -> parsed as 2-1=1
    when(
      spacesApi.spaceGetChildren(spaceId, 1, 10, "name_ASC", true, false, true)
    ).thenReturn(pagedNodes);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(0L, model.get("total"));
    verify(spacesApi).spaceGetChildren(
      spaceId,
      1,
      10,
      "name_ASC",
      true,
      false,
      true
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String spaceId = "space-id-3";
    mockTemplateVars(spaceId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(spaceId)).thenReturn(
      false
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(spacesApi);
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    String spaceId = "invalid-id";
    mockTemplateVars(spaceId);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(req.getParameter("folderOnly")).thenReturn(null);
    when(req.getParameter("fileOnly")).thenReturn(null);
    when(req.getParameter("skipExpiredItems")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(spaceId)).thenReturn(true);
    when(
      spacesApi.spaceGetChildren(spaceId, -1, -1, "", false, false, false)
    ).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "invalid-id")
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError() {
    String spaceId = "space-id-4";
    mockTemplateVars(spaceId);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(req.getParameter("folderOnly")).thenReturn(null);
    when(req.getParameter("fileOnly")).thenReturn(null);
    when(req.getParameter("skipExpiredItems")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(spaceId)).thenReturn(true);
    when(
      spacesApi.spaceGetChildren(spaceId, -1, -1, "", false, false, false)
    ).thenThrow(new RuntimeException("unexpected"));

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
