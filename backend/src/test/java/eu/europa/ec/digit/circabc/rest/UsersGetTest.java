package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UsersGetTest {

  private UsersGet usersGet;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    usersGet = new UsersGet();
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
    Field field = UsersGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(usersGet, value);
  }

  @Test
  public void testExecuteImpl_whenValidQuery_thenReturnsUsers()
    throws Exception {
    when(req.getParameter("query")).thenReturn("john");
    when(req.getParameter("filter")).thenReturn(null);
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(
      false
    );
    List<User> expectedUsers = Collections.singletonList(new User());
    when(usersApi.usersGet("john", true, false)).thenReturn(expectedUsers);

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(expectedUsers, result.get("users"));
  }

  @Test
  public void testExecuteImpl_whenFilterFalse_thenPassesFilterFalse()
    throws Exception {
    when(req.getParameter("query")).thenReturn("john");
    when(req.getParameter("filter")).thenReturn("false");
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(
      false
    );
    when(usersApi.usersGet("john", false, false)).thenReturn(
      Collections.<User>emptyList()
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    verify(usersApi).usersGet("john", false, false);
  }

  @Test
  public void testExecuteImpl_whenQueryNull_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("query")).thenReturn(null);
    when(req.getParameter("filter")).thenReturn(null);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenQueryEmpty_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("query")).thenReturn("");
    when(req.getParameter("filter")).thenReturn(null);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenGuest_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("query")).thenReturn("john");
    when(req.getParameter("filter")).thenReturn(null);
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(true);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExternalUserWithInvalidEmail_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("query")).thenReturn("notanemail");
    when(req.getParameter("filter")).thenReturn(null);
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(true);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExternalUserWithValidEmail_thenReturnsUsers()
    throws Exception {
    when(req.getParameter("query")).thenReturn("user@example.com");
    when(req.getParameter("filter")).thenReturn(null);
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(true);
    List<User> expectedUsers = Collections.singletonList(new User());
    when(usersApi.usersGet("user@example.com", true, true)).thenReturn(
      expectedUsers
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(expectedUsers, result.get("users"));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method = org.springframework.extensions.webscripts
      .DeclarativeWebScript.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    return (Map<String, Object>) method.invoke(usersGet, req, status, cache);
  }
}
