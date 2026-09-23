package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.model.User;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UserDBGetTest {

  private UserDBGet userDBGet;
  private UsersApi usersApi;
  private AuthenticationService authenticationService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    userDBGet = new UserDBGet();
    usersApi = mock(UsersApi.class);
    authenticationService = mock(AuthenticationService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("usersApi", usersApi);
    setField("authenticationService", authenticationService);
  }

  @Test
  public void testExecuteImpl_whenUserRequestsOwnData_thenReturnsUser()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    User user = new User();
    when(usersApi.usersUserIdGetFromLdap("testuser")).thenReturn(user);

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(user, result.get("user"));
  }

  @Test
  public void testExecuteImpl_whenAdminRequestsOtherUserData_thenReturnsUser()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "otheruser");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn("fr");
    when(authenticationService.getCurrentUserName()).thenReturn("admin");

    User user = new User();
    when(usersApi.usersUserIdGetFromLdap("otheruser")).thenReturn(user);

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(user, result.get("user"));
  }

  @Test
  public void testExecuteImpl_whenNonAdminRequestsOtherUserData_thenForbidden()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "otheruser");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUserIdIsNull_thenReturnsEmptyModel()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertFalse(result.containsKey("user"));
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(usersApi.usersUserIdGetFromLdap("testuser")).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      UserDBGet.class.getSuperclass().getDeclaredMethod(
        "executeImpl",
        WebScriptRequest.class,
        Status.class,
        Cache.class
      );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      userDBGet,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UserDBGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(userDBGet, value);
  }
}
