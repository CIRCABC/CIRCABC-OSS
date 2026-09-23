package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.ClipboardAction;
import io.swagger.api.ClipboardApi;
import io.swagger.model.NotifiableUser;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesIdPasteMoveTest {

  private NodesIdPasteMove webScript;
  private org.alfresco.service.cmr.repository.NodeService nodeService;
  private ClipboardApi clipboardApi;
  private NotificationService notificationService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private NotificationSubscriptionService notificationSubscriptionService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new NodesIdPasteMove();
    nodeService = mock(org.alfresco.service.cmr.repository.NodeService.class);
    clipboardApi = mock(ClipboardApi.class);
    notificationService = mock(NotificationService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    notificationSubscriptionService = mock(
      NotificationSubscriptionService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("nodeService", nodeService);
    setField("clipboardApi", clipboardApi);
    setField("notificationService", notificationService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField(
      "notificationSubscriptionService",
      notificationSubscriptionService
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesIdPasteMove.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  private void mockValidFolder(String id) {
    NodeRef folderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      id
    );
    when(nodeService.exists(folderRef)).thenReturn(true);
    when(nodeService.getType(folderRef)).thenReturn(ContentModel.TYPE_FOLDER);
  }

  @Test
  public void testExecuteImpl_whenValidMove_thenReturnsModel()
    throws Exception {
    String id = "folder-id";
    String[] nodeIds = { "node-1", "node-2" };
    mockTemplateVars(id);
    mockValidFolder(id);
    when(req.getParameterValues("nodeIds")).thenReturn(nodeIds);
    when(req.getParameter("notify")).thenReturn("true");
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(id)
    ).thenReturn(true);

    NodeRef folderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      id
    );
    Set<NotifiableUser> users = new HashSet<>();
    when(
      notificationSubscriptionService.getNotifiableUsers(folderRef)
    ).thenReturn(users);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(clipboardApi).paste(
      nodeIds,
      folderRef,
      ClipboardAction.MOVE.getValue()
    );
    verify(notificationService).notifyNewFiles(
      eq(folderRef),
      anyList(),
      eq(users),
      any()
    );
  }

  @Test
  public void testExecuteImpl_whenNotifyFalse_thenSkipsNotification()
    throws Exception {
    String id = "folder-id";
    String[] nodeIds = { "node-1" };
    mockTemplateVars(id);
    mockValidFolder(id);
    when(req.getParameterValues("nodeIds")).thenReturn(nodeIds);
    when(req.getParameter("notify")).thenReturn("false");
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(id)
    ).thenReturn(true);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(clipboardApi).paste(
      any(String[].class),
      any(NodeRef.class),
      eq(ClipboardAction.MOVE.getValue())
    );
    verifyNoInteractions(notificationService);
  }

  @Test
  public void testExecuteImpl_whenFolderNotFound_thenThrowsIllegalArgument() {
    String id = "missing-folder";
    mockTemplateVars(id);
    NodeRef folderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      id
    );
    when(nodeService.exists(folderRef)).thenReturn(false);

    try {
      webScript.executeImpl(req, status, cache);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains(id));
    }
  }

  @Test
  public void testExecuteImpl_whenNotAFolder_thenThrowsIllegalArgument() {
    String id = "document-id";
    mockTemplateVars(id);
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);

    try {
      webScript.executeImpl(req, status, cache);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains(id));
    }
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String id = "folder-id";
    String[] nodeIds = { "node-1" };
    mockTemplateVars(id);
    mockValidFolder(id);
    when(req.getParameterValues("nodeIds")).thenReturn(nodeIds);
    when(req.getParameter("notify")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(id)
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPasteThrowsException_thenReturnsNotAcceptable()
    throws Exception {
    String id = "folder-id";
    String[] nodeIds = { "node-1" };
    mockTemplateVars(id);
    mockValidFolder(id);
    when(req.getParameterValues("nodeIds")).thenReturn(nodeIds);
    when(req.getParameter("notify")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(id)
    ).thenReturn(true);

    NodeRef folderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      id
    );
    doThrow(new RuntimeException("paste failed"))
      .when(clipboardApi)
      .paste(nodeIds, folderRef, ClipboardAction.MOVE.getValue());

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }
}
