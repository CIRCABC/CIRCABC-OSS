package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ClipboardAction;
import io.swagger.api.ClipboardApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesIdPasteLinkTest {

  private NodesIdPasteLink webScript;
  private NodeService nodeService;
  private ClipboardApi clipboardApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String NODE_ID = "test-folder-id";
  private static final NodeRef FOLDER_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    NODE_ID
  );

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesIdPasteLink.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Before
  public void setUp() throws Exception {
    webScript = new NodesIdPasteLink();
    nodeService = mock(NodeService.class);
    clipboardApi = mock(ClipboardApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("nodeService", nodeService);
    setField("clipboardApi", clipboardApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", NODE_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    when(nodeService.exists(FOLDER_REF)).thenReturn(true);
    when(nodeService.getType(FOLDER_REF)).thenReturn(ContentModel.TYPE_FOLDER);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenPasteLinkSucceeds()
    throws Exception {
    String[] nodeIds = { "node1", "node2" };
    when(req.getParameterValues("nodeIds")).thenReturn(nodeIds);
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(clipboardApi).paste(
      nodeIds,
      FOLDER_REF,
      ClipboardAction.LINK.getValue()
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenFolderDoesNotExist_thenThrows() {
    when(nodeService.exists(FOLDER_REF)).thenReturn(false);

    webScript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenNodeIsNotFolder_thenThrows() {
    when(nodeService.getType(FOLDER_REF)).thenReturn(ContentModel.TYPE_CONTENT);

    webScript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String[] nodeIds = { "node1" };
    when(req.getParameterValues("nodeIds")).thenReturn(nodeIds);
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPasteThrowsException_thenReturnsNotAcceptable()
    throws Exception {
    String[] nodeIds = { "node1" };
    when(req.getParameterValues("nodeIds")).thenReturn(nodeIds);
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        NODE_ID
      )
    ).thenReturn(true);
    doThrow(new RuntimeException("paste failed"))
      .when(clipboardApi)
      .paste(nodeIds, FOLDER_REF, ClipboardAction.LINK.getValue());

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("paste failed", status.getMessage());
  }
}
