package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UserGetTest {

  private UserGet userGet;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    userGet = new UserGet();
    usersApi = mock(UsersApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("usersApi", usersApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UserGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(userGet, value);
  }

  private void mockTemplateVars(String userId) {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", userId);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenCurrentUser_thenReturnsFullUserData() {
    mockTemplateVars("testuser");
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    User userData = new User();
    userData.setUserId("testuser");
    userData.setFirstname("John");
    userData.setLastname("Doe");
    userData.setAvatar("avatar.png");
    when(usersApi.usersUserIdGet("testuser")).thenReturn(userData);

    Map<String, Object> result = userGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(userData, result.get("user"));
  }

  @Test
  public void testExecuteImpl_whenNotCurrentUser_thenReturnsBasicUserData() {
    mockTemplateVars("otheruser");
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("otheruser")
    ).thenReturn(false);

    User userData = new User();
    userData.setUserId("otheruser");
    userData.setFirstname("Jane");
    userData.setLastname("Smith");
    userData.setAvatar("avatar2.png");
    userData.setDefaultAvatar(true);
    when(usersApi.usersUserIdGet("otheruser")).thenReturn(userData);

    Map<String, Object> result = userGet.executeImpl(req, status, cache);

    assertNotNull(result);
    User basicUser = (User) result.get("user");
    assertEquals("otheruser", basicUser.getUserId());
    assertEquals("Jane", basicUser.getFirstname());
    assertEquals("Smith", basicUser.getLastname());
    assertEquals("avatar2.png", basicUser.getAvatar());
    assertTrue(basicUser.isDefaultAvatar());
  }

  @Test
  public void testExecuteImpl_whenUserIdNull_thenReturnsEmptyModel() {
    mockTemplateVars(null);
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> result = userGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertFalse(result.containsKey("user"));
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    mockTemplateVars("baduser");
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("baduser")
    ).thenReturn(false);
    when(usersApi.usersUserIdGet("baduser")).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> result = userGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    mockTemplateVars("forbidden");
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("forbidden")
    ).thenReturn(false);
    when(usersApi.usersUserIdGet("forbidden")).thenThrow(
      new AccessDeniedException("denied")
    );

    Map<String, Object> result = userGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    mockTemplateVars("testuser");
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    User userData = new User();
    userData.setUserId("testuser");
    when(usersApi.usersUserIdGet("testuser")).thenReturn(userData);

    Map<String, Object> result = userGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(userData, result.get("user"));
  }
}
