package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.TopicsApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class PostsDeleteTest {

  private PostsDelete postsDelete;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private ApiToolBox apiToolBox;
  private NodeService nodeService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

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

    postsDelete = new PostsDelete();
    topicsApi = mock(TopicsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    apiToolBox = mock(ApiToolBox.class);
    nodeService = mock(NodeService.class);

    setField(PostsDelete.class, "topicsApi", topicsApi);
    setField(
      PostsDelete.class,
      "currentUserPermissionCheckerService",
      permissionChecker
    );
    setField(CircabcDeclarativeWebScript.class, "apiToolBox", apiToolBox);
    setField(
      CircabcDeclarativeWebScript.class,
      "unsecureNodeService",
      nodeService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void setField(Class<?> clazz, String fieldName, Object value)
    throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(postsDelete, value);
  }

  @Test
  public void testExecuteImpl_whenNewsGroupPermission_thenDeletesPost() {
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        "test-id",
        NewsGroupPermissions.NWSMODERATE,
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(true);

    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(apiToolBox.getCurrentInterestGroup(any(NodeRef.class))).thenReturn(
      igRef
    );
    when(apiToolBox.getCircabcPath(any(NodeRef.class), eq(true))).thenReturn(
      "/path"
    );
    when(apiToolBox.getDatabaseID(any(NodeRef.class))).thenReturn(1L);

    Map<String, Object> result = postsDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(topicsApi).postsIdDelete("test-id");
  }

  @Test
  public void testExecuteImpl_whenLibraryPermissionOnDocumentPost_thenDeletesPost() {
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        "test-id",
        NewsGroupPermissions.NWSMODERATE,
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(false);
    when(permissionChecker.isDocumentPost("test-id")).thenReturn(true);
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-id",
        LibraryPermissions.LIBFULLEDIT,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(apiToolBox.getCurrentInterestGroup(any(NodeRef.class))).thenReturn(
      igRef
    );
    when(apiToolBox.getCircabcPath(any(NodeRef.class), eq(true))).thenReturn(
      "/path"
    );
    when(apiToolBox.getDatabaseID(any(NodeRef.class))).thenReturn(1L);

    Map<String, Object> result = postsDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(topicsApi).postsIdDelete("test-id");
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden() {
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        "test-id",
        NewsGroupPermissions.NWSMODERATE,
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(false);
    when(permissionChecker.isDocumentPost("test-id")).thenReturn(false);

    Map<String, Object> result = postsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(topicsApi, never()).postsIdDelete(anyString());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest() {
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        "test-id",
        NewsGroupPermissions.NWSMODERATE,
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(true);

    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(apiToolBox.getCurrentInterestGroup(any(NodeRef.class))).thenReturn(
      igRef
    );
    when(apiToolBox.getCircabcPath(any(NodeRef.class), eq(true))).thenReturn(
      "/path"
    );
    when(apiToolBox.getDatabaseID(any(NodeRef.class))).thenReturn(1L);
    doThrow(new InvalidNodeRefException(igRef))
      .when(topicsApi)
      .postsIdDelete("test-id");

    Map<String, Object> result = postsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenInternalServerError() {
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        "test-id",
        NewsGroupPermissions.NWSMODERATE,
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(true);

    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(apiToolBox.getCurrentInterestGroup(any(NodeRef.class))).thenReturn(
      igRef
    );
    when(apiToolBox.getCircabcPath(any(NodeRef.class), eq(true))).thenReturn(
      "/path"
    );
    when(apiToolBox.getDatabaseID(any(NodeRef.class))).thenReturn(1L);
    doThrow(new RuntimeException("unexpected"))
      .when(topicsApi)
      .postsIdDelete("test-id");

    Map<String, Object> result = postsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
