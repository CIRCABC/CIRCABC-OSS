package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.NodesApi;
import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpacePutTest {

  private SpacePut spacePut;
  private SpacesApi spacesApi;
  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private NotificationService notificationService;
  private NotificationSubscriptionService notificationSubscriptionService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-node-id";
  private static final String VALID_JSON =
    "{\"name\":\"Test Space\",\"title\":{\"en\":\"English Title\"},\"description\":{\"en\":\"English Desc\"}}";

  @Before
  public void setUp() throws Exception {
    spacePut = new SpacePut();
    spacesApi = mock(SpacesApi.class);
    nodesApi = mock(NodesApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    notificationService = mock(NotificationService.class);
    notificationSubscriptionService = mock(
      NotificationSubscriptionService.class
    );

    setField("spacesApi", spacesApi);
    setField("nodesApi", nodesApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);
    setField("notificationService", notificationService);
    setField(
      "notificationSubscriptionService",
      notificationSubscriptionService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("notify")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenPermissionGranted_thenUpdatesAndNotifies()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    Node returnedNode = new Node();
    returnedNode.setId(TEST_ID);
    when(nodesApi.getNodeById(TEST_ID)).thenReturn(returnedNode);
    when(notificationSubscriptionService.getNotifiableUsers(any())).thenReturn(
      new HashSet<>()
    );

    Map<String, Object> model = spacePut.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(returnedNode, model.get("node"));
    verify(spacesApi).spacesIdPut(eq(TEST_ID), any(Node.class));
    verify(notificationService).notifyNewFiles(any(), any(), any(), any());
  }

  @Test
  public void testExecuteImpl_whenNotifyFalse_thenSkipsNotification()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);
    when(req.getParameter("notify")).thenReturn("false");

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    Node returnedNode = new Node();
    when(nodesApi.getNodeById(TEST_ID)).thenReturn(returnedNode);

    Map<String, Object> model = spacePut.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(spacesApi).spacesIdPut(eq(TEST_ID), any(Node.class));
    verify(notificationService, never()).notifyNewFiles(
      any(),
      any(),
      any(),
      any()
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(false);

    Map<String, Object> model = spacePut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsServerError()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("not valid json");
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    Map<String, Object> model = spacePut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenDuplicateName_thenReturnsConflict()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    doThrow(
      new DuplicateChildNodeNameException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TEST_ID),
        null,
        "Test Space",
        null
      )
    )
      .when(spacesApi)
      .spacesIdPut(eq(TEST_ID), any(Node.class));

    Map<String, Object> model = spacePut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_CONFLICT, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TEST_ID)
      )
    )
      .when(spacesApi)
      .spacesIdPut(eq(TEST_ID), any(Node.class));

    Map<String, Object> model = spacePut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpacePut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(spacePut, value);
  }
}
