package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ArchiveApi;
import io.swagger.model.ArchiveNode;
import io.swagger.model.PagedArchiveNodes;
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

public class GroupsDocumentsDeletedGetTest {

  private GroupsDocumentsDeletedGet webScript;
  private ArchiveApi archiveApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsDocumentsDeletedGet();
    archiveApi = mock(ArchiveApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("archiveApi", archiveApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-group-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenAdminAndNoLanguage_thenReturnsData() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("5");
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("order")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);

    PagedArchiveNodes pagedResult = new PagedArchiveNodes();
    ArchiveNode node = new ArchiveNode();
    pagedResult.setData(List.of(node));
    pagedResult.setTotal(1L);

    when(
      archiveApi.groupsIdDocumentsDeletedGet("test-group-id", 5, 1, null)
    ).thenReturn(pagedResult);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(List.of(node), model.get("data"));
    assertEquals(1L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenAdminWithLanguage_thenReturnsData() {
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("order")).thenReturn("name_ASC");
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);

    PagedArchiveNodes pagedResult = new PagedArchiveNodes();
    pagedResult.setData(Collections.emptyList());
    pagedResult.setTotal(0L);

    when(
      archiveApi.groupsIdDocumentsDeletedGet(
        "test-group-id",
        null,
        null,
        "name_ASC"
      )
    ).thenReturn(pagedResult);

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
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);

    PagedArchiveNodes pagedResult = new PagedArchiveNodes();
    pagedResult.setData(Collections.emptyList());
    pagedResult.setTotal(0L);

    when(
      archiveApi.groupsIdDocumentsDeletedGet("test-group-id", 10, 1, null)
    ).thenReturn(pagedResult);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(archiveApi).groupsIdDocumentsDeletedGet(
      "test-group-id",
      10,
      1,
      null
    );
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);
    when(
      archiveApi.groupsIdDocumentsDeletedGet("test-group-id", null, null, null)
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
    Field field = GroupsDocumentsDeletedGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
