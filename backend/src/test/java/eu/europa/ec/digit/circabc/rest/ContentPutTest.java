package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.ContentApi;
import io.swagger.api.NodesApi;
import io.swagger.model.Node;
import io.swagger.model.NotifiableUser;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentPutTest {

  private ContentPut contentPut;
  private ContentApi contentApi;
  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private NodeService nodeService;
  private NotificationService notificationService;
  private NotificationSubscriptionService notificationSubscriptionService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  private static final String TEST_ID = "test-node-id";
  private static final String VALID_JSON =
    "{\"name\":\"test.txt\",\"title\":{},\"description\":{},\"properties\":{\"issue_date\":null,\"expiration_date\":null,\"reference\":null,\"author\":null,\"status\":null,\"security\":null,\"url\":null}}";

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    contentPut = new ContentPut();
    contentApi = mock(ContentApi.class);
    nodesApi = mock(NodesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    nodeService = mock(NodeService.class);
    notificationService = mock(NotificationService.class);
    notificationSubscriptionService = mock(
      NotificationSubscriptionService.class
    );

    setField("contentApi", contentApi);
    setField("nodesApi", nodesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("nodeService", nodeService);
    setField("notificationService", notificationService);
    setField(
      "notificationSubscriptionService",
      notificationSubscriptionService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("notify")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsNode()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(TEST_ID),
        eq(LibraryPermissions.LIBMANAGEOWN),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(false);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    Set<NotifiableUser> users = new HashSet<>();
    when(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    ).thenReturn(users);

    Node resultNode = new Node();
    when(nodesApi.getNodeById(TEST_ID)).thenReturn(resultNode);

    Map<String, Object> result = contentPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(resultNode, result.get("node"));
    verify(contentApi).contentIdPut(eq(TEST_ID), any(Node.class));
    verify(notificationService).notifyNewFiles(
      eq(nodeRef),
      anyList(),
      eq(users),
      any()
    );
  }

  @Test
  public void testExecuteImpl_whenNotifyFalse_thenSkipsNotification()
    throws Exception {
    when(req.getParameter("notify")).thenReturn("false");
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(TEST_ID),
        eq(LibraryPermissions.LIBMANAGEOWN),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(false);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    Node resultNode = new Node();
    when(nodesApi.getNodeById(TEST_ID)).thenReturn(resultNode);

    Map<String, Object> result = contentPut.executeImpl(req, status, cache);

    assertNotNull(result);
    verifyNoInteractions(notificationService);
    verifyNoInteractions(notificationSubscriptionService);
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(TEST_ID),
        eq(LibraryPermissions.LIBMANAGEOWN),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(false);

    Map<String, Object> result = contentPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenWorkingCopyNotOwner_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(TEST_ID),
        eq(LibraryPermissions.LIBMANAGEOWN),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.isWorkingCopyOwner(TEST_ID)
    ).thenReturn(false);

    Map<String, Object> result = contentPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(TEST_ID),
        eq(LibraryPermissions.LIBMANAGEOWN),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(false);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    doThrow(new InvalidNodeRefException("bad ref", nodeRef))
      .when(contentApi)
      .contentIdPut(eq(TEST_ID), any(Node.class));

    Map<String, Object> result = contentPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(TEST_ID),
        eq(LibraryPermissions.LIBMANAGEOWN),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(false);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("not valid json");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = contentPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(TEST_ID),
        eq(LibraryPermissions.LIBMANAGEOWN),
        eq(LibraryPermissions.LIBEDITONLY)
      )
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(false);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    Set<NotifiableUser> users = new HashSet<>();
    when(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    ).thenReturn(users);

    Node resultNode = new Node();
    when(nodesApi.getNodeById(TEST_ID)).thenReturn(resultNode);

    Map<String, Object> result = contentPut.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(contentApi).contentIdPut(eq(TEST_ID), any(Node.class));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(contentPut, value);
  }
}
