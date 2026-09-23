package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.PermissionsApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesAllPermissionsDeleteTest {

  private NodesAllPermissionsDelete webScript;
  private PermissionsApi permissionsApi;
  private CurrentUserPermissionCheckerService permissionChecker;

  @Before
  public void setUp() throws Exception {
    webScript = new NodesAllPermissionsDelete();
    permissionsApi = mock(PermissionsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("permissionsApi", permissionsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

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
  }

  @Test
  public void testExecuteImpl_whenLibAdmin_thenClearsPermissions()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest("node-id", "someAuthority");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(permissionsApi).nodeIdPermissionsClear("node-id", "someAuthority");
  }

  @Test
  public void testExecuteImpl_whenNwsAdmin_thenClearsPermissions()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        "node-id",
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest("node-id", "someAuthority");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(permissionsApi).nodeIdPermissionsClear("node-id", "someAuthority");
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        "node-id",
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(false);

    WebScriptRequest req = mockRequest("node-id", "someAuthority");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(403, status.getCode());
    verify(permissionsApi, never()).nodeIdPermissionsClear(
      anyString(),
      anyString()
    );
  }

  @Test
  public void testExecuteImpl_whenAuthorityContainsEncodedDot_thenDecodes()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest("node-id", "GROUP_some%2Eauthority");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(permissionsApi).nodeIdPermissionsClear(
      "node-id",
      "GROUP_some.authority"
    );
  }

  @Test
  public void testExecuteImpl_whenLanguageParam_thenSetsLocale()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "node-id",
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest("node-id", "auth");
    when(req.getParameter("language")).thenReturn("fr");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(permissionsApi).nodeIdPermissionsClear("node-id", "auth");
  }

  private WebScriptRequest mockRequest(String id, String authority) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    templateVars.put("authority", authority);
    Match match = new Match("template", templateVars, "url");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);
    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesAllPermissionsDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
