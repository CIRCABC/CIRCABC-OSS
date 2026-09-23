package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.PermissionsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesPermissionsDeleteTest {

  private NodesPermissionsDelete webScript;
  private PermissionsApi permissionsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
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

    webScript = new NodesPermissionsDelete();
    permissionsApi = mock(PermissionsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("permissionsApi", permissionsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    templateVars.put("authority", "GROUP_EVERYONE");
    templateVars.put("permission", "Read");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenUserHasLibAdminPermission_thenDeletesPermission() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("test-node-id"),
        any()
      )
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(permissionsApi).nodeIdPermissionsDelete(
      "test-node-id",
      "GROUP_EVERYONE",
      "Read"
    );
  }

  @Test
  public void testExecuteImpl_whenUserHasNewsGroupAdminPermission_thenDeletesPermission() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("test-node-id"),
        any()
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("test-node-id"),
        any()
      )
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(permissionsApi).nodeIdPermissionsDelete(
      "test-node-id",
      "GROUP_EVERYONE",
      "Read"
    );
  }

  @Test
  public void testExecuteImpl_whenUserHasNoPermission_thenReturnsForbidden() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("test-node-id"),
        any()
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("test-node-id"),
        any()
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(permissionsApi, never()).nodeIdPermissionsDelete(
      anyString(),
      anyString(),
      anyString()
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("test-node-id"),
        any()
      )
    ).thenReturn(true);
    doThrow(new InvalidNodeRefException("invalid", null))
      .when(permissionsApi)
      .nodeIdPermissionsDelete("test-node-id", "GROUP_EVERYONE", "Read");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalServerError() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("test-node-id"),
        any()
      )
    ).thenReturn(true);
    doThrow(new RuntimeException("unexpected"))
      .when(permissionsApi)
      .nodeIdPermissionsDelete("test-node-id", "GROUP_EVERYONE", "Read");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesPermissionsDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
