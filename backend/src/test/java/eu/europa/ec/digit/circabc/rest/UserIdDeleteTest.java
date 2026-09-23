package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UserIdDeleteTest {

  private UserIdDelete userIdDelete;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    userIdDelete = new UserIdDelete();
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
    Field field = UserIdDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(userIdDelete, value);
  }

  private void mockTemplateVars(Map<String, String> vars) {
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenDeletesUser() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    Map<String, String> vars = new HashMap<>();
    vars.put("userId", "user1");
    mockTemplateVars(vars);

    Map<String, Object> result = userIdDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(usersApi).usersUserIdDelete("user1");
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenDeletesUser() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    Map<String, String> vars = new HashMap<>();
    vars.put("userId", "user2");
    mockTemplateVars(vars);

    Map<String, Object> result = userIdDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(usersApi).usersUserIdDelete("user2");
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    Map<String, String> vars = new HashMap<>();
    vars.put("userId", "user1");
    mockTemplateVars(vars);

    Map<String, Object> result = userIdDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUserIdNull_thenNoDelete() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    Map<String, String> vars = new HashMap<>();
    mockTemplateVars(vars);

    Map<String, Object> result = userIdDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    verifyNoInteractions(usersApi);
  }

  @Test
  public void testExecuteImpl_whenIllegalArgument_thenNotFound() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    Map<String, String> vars = new HashMap<>();
    vars.put("userId", "baduser");
    mockTemplateVars(vars);
    doThrow(new IllegalArgumentException("no such user"))
      .when(usersApi)
      .usersUserIdDelete("baduser");

    Map<String, Object> result = userIdDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_FOUND, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenInternalError() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    Map<String, String> vars = new HashMap<>();
    vars.put("userId", "user1");
    mockTemplateVars(vars);
    doThrow(new RuntimeException("unexpected"))
      .when(usersApi)
      .usersUserIdDelete("user1");

    Map<String, Object> result = userIdDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
