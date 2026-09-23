package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ExpiredApi;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
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

public class GroupsDocumentsExpiredGetTest {

  private GroupsDocumentsExpiredGet webScript;
  private ExpiredApi expiredApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsDocumentsExpiredGet();
    expiredApi = mock(ExpiredApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("expiredApi", expiredApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsData() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("order")).thenReturn("name");
    when(permissionCheckerService.isGroupAdmin("test-ig-id")).thenReturn(true);

    Node node = new Node();
    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(List.of(node));
    pagedNodes.setTotal(1L);
    when(
      expiredApi.groupsIdDocumentsExpiredGet("test-ig-id", 10, 1, "name")
    ).thenReturn(pagedNodes);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(List.of(node), model.get("data"));
    assertEquals(1L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenNullLimitAndPage_thenPassesNull() {
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(permissionCheckerService.isGroupAdmin("test-ig-id")).thenReturn(true);

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(Collections.emptyList());
    pagedNodes.setTotal(0L);
    when(
      expiredApi.groupsIdDocumentsExpiredGet("test-ig-id", null, null, null)
    ).thenReturn(pagedNodes);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("data"));
    assertEquals(0L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenEmptyLimitAndPage_thenUsesDefaults() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("");
    when(req.getParameter("page")).thenReturn("");
    when(req.getParameter("order")).thenReturn(null);
    when(permissionCheckerService.isGroupAdmin("test-ig-id")).thenReturn(true);

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(Collections.emptyList());
    pagedNodes.setTotal(0L);
    when(
      expiredApi.groupsIdDocumentsExpiredGet("test-ig-id", 25, 1, null)
    ).thenReturn(pagedNodes);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(expiredApi).groupsIdDocumentsExpiredGet("test-ig-id", 25, 1, null);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(permissionCheckerService.isGroupAdmin("test-ig-id")).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("order")).thenReturn(null);
    when(permissionCheckerService.isGroupAdmin("test-ig-id")).thenReturn(true);
    when(
      expiredApi.groupsIdDocumentsExpiredGet("test-ig-id", 10, 1, null)
    ).thenThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(
        "invalid",
        new org.alfresco.service.cmr.repository.NodeRef(
          "workspace://SpacesStore/bad-id"
        )
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsDocumentsExpiredGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
