package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NotificationsApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
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

public class NodesAuthorityNotificationsDeleteTest {

  private NodesAuthorityNotificationsDelete webScript;
  private NotificationsApi notificationsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;

  @Before
  public void setUp() throws Exception {
    webScript = new NodesAuthorityNotificationsDelete();
    notificationsApi = mock(NotificationsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("notificationsApi", notificationsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);
  }

  @Test
  public void testExecuteImpl_whenLibAdmin_thenDeletesNotification() {
    String id = "test-node-id";
    String authority = "testUser";

    WebScriptRequest req = mockRequest(id, authority, null);
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        id,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(notificationsApi).nodesIdNotificationsAuthorityDelete(id, authority);
  }

  @Test
  public void testExecuteImpl_whenNwsAdmin_thenDeletesNotification() {
    String id = "test-node-id";
    String authority = "testUser";

    WebScriptRequest req = mockRequest(id, authority, null);
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        id,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        id,
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(true);

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(notificationsApi).nodesIdNotificationsAuthorityDelete(id, authority);
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden() {
    String id = "test-node-id";
    String authority = "testUser";

    WebScriptRequest req = mockRequest(id, authority, null);
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        id,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        id,
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(false);

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(notificationsApi, never()).nodesIdNotificationsAuthorityDelete(
      anyString(),
      anyString()
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest() {
    String id = "invalid-node-id";
    String authority = "testUser";

    WebScriptRequest req = mockRequest(id, authority, null);
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        id,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);
    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id)
      )
    )
      .when(notificationsApi)
      .nodesIdNotificationsAuthorityDelete(id, authority);

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertTrue(model.isEmpty());
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenInternalServerError() {
    String id = "test-node-id";
    String authority = "testUser";

    WebScriptRequest req = mockRequest(id, authority, null);
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        id,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);
    doThrow(new RuntimeException("unexpected"))
      .when(notificationsApi)
      .nodesIdNotificationsAuthorityDelete(id, authority);

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    String id = "test-node-id";
    String authority = "testUser";

    WebScriptRequest req = mockRequest(id, authority, "fr");
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        id,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    Status status = new Status();
    Cache cache = new Cache();
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(notificationsApi).nodesIdNotificationsAuthorityDelete(id, authority);
  }

  private WebScriptRequest mockRequest(
    String id,
    String authority,
    String language
  ) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    templateVars.put("authority", authority);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(language);

    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesAuthorityNotificationsDelete.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
