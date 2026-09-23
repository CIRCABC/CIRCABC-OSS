package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
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

public class ForumModerationTogglePutTest {

  private ForumModerationTogglePut webScript;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Before
  public void setUp() throws Exception {
    webScript = new ForumModerationTogglePut();
    forumsApi = mock(ForumsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("forumsApi", forumsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

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
  public void testExecuteImpl_whenEnableModeration_thenCallsToggleModeration() {
    WebScriptRequest req = mockRequest("test-id", "true", "false");
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(true);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("ok", model.get("message"));
    verify(forumsApi).toggleModeration("test-id", true, false);
  }

  @Test
  public void testExecuteImpl_whenDisableModerationAcceptAll_thenCallsToggleModeration() {
    WebScriptRequest req = mockRequest("test-id", "false", "true");
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(true);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("ok", model.get("message"));
    verify(forumsApi).toggleModeration("test-id", false, true);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    WebScriptRequest req = mockRequest("test-id", "true", "false");
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(false);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullEnableParam_thenPassesFalse() {
    WebScriptRequest req = mockRequest("test-id", null, null);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(true);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(forumsApi).toggleModeration("test-id", false, false);
  }

  private WebScriptRequest mockRequest(
    String id,
    String enable,
    String acceptAll
  ) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("enable")).thenReturn(enable);
    when(req.getParameter("acceptAll")).thenReturn(acceptAll);
    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ForumModerationTogglePut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, field.getType().cast(value));
  }
}
