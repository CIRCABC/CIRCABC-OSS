package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
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

public class UserAvatarDeleteTest {

  private UserAvatarDelete userAvatarDelete;
  private UsersApi usersApi;
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

    userAvatarDelete = new UserAvatarDelete();
    usersApi = mock(UsersApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("usersApi", usersApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenCurrentUser_thenSuccess() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    Map<String, Object> result = userAvatarDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(usersApi).removeAvatar("testuser");
  }

  @Test
  public void testExecuteImpl_whenAdmin_thenSuccess() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(false);
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );

    Map<String, Object> result = userAvatarDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(usersApi).removeAvatar("testuser");
  }

  @Test
  public void testExecuteImpl_whenNotAuthorized_thenForbidden() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(false);
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );

    Map<String, Object> result = userAvatarDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    doThrow(new InvalidNodeRefException("bad ref", null))
      .when(usersApi)
      .removeAvatar("testuser");

    Map<String, Object> result = userAvatarDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenInternalError() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    doThrow(new RuntimeException("unexpected"))
      .when(usersApi)
      .removeAvatar("testuser");

    Map<String, Object> result = userAvatarDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UserAvatarDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(userAvatarDelete, value);
  }
}
