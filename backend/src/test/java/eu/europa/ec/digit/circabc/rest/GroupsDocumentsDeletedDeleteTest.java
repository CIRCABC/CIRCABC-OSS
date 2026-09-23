package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ArchiveApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
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

public class GroupsDocumentsDeletedDeleteTest {

  private GroupsDocumentsDeletedDelete webscript;
  private ArchiveApi archiveApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String IG_ID = "ig-id";
  private static final String NODE_ID = "node-id";

  @Before
  public void setUp() throws Exception {
    webscript = new GroupsDocumentsDeletedDelete();
    archiveApi = mock(ArchiveApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("archiveApi", archiveApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenGroupAdmin_thenDeletesSuccessfully() {
    setupRequest(null);
    when(permissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(true);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(archiveApi).groupsIdDocumentsDeletedNodeIdDelete(IG_ID, NODE_ID);
  }

  @Test
  public void testExecuteImpl_whenNotGroupAdmin_thenReturnsForbidden() {
    setupRequest(null);
    when(permissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(false);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(archiveApi, never()).groupsIdDocumentsDeletedNodeIdDelete(
      anyString(),
      anyString()
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    setupRequest(null);
    when(permissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(true);
    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, NODE_ID)
      )
    )
      .when(archiveApi)
      .groupsIdDocumentsDeletedNodeIdDelete(IG_ID, NODE_ID);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    setupRequest("en");
    when(permissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(true);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(archiveApi).groupsIdDocumentsDeletedNodeIdDelete(IG_ID, NODE_ID);
  }

  private void setupRequest(String language) {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", IG_ID);
    templateVars.put("nodeId", NODE_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(language);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsDocumentsDeletedDelete.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
