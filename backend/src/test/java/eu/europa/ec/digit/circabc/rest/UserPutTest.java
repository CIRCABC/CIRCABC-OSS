package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UserPutTest {

  private UserPut userPut;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Content content;

  private static final String VALID_JSON =
    "{\"userId\":\"testuser\",\"firstname\":\"John\",\"lastname\":\"Doe\"," +
    "\"email\":\"john@example.com\",\"phone\":\"123\",\"uiLang\":\"en\"," +
    "\"contentFilterLang\":\"en\",\"avatar\":\"av.png\",\"visibility\":true," +
    "\"properties\":{\"title\":\"Mr\",\"organisation\":\"EC\",\"fax\":\"456\"," +
    "\"signature\":\"sig\"}}";

  @Before
  public void setUp() throws Exception {
    userPut = new UserPut();
    usersApi = mock(UsersApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
    content = mock(Content.class);

    setField("usersApi", usersApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getContent()).thenReturn(content);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UserPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(userPut, value);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenUpdatesUser()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    when(content.getContent()).thenReturn(VALID_JSON);

    User returnedUser = new User();
    returnedUser.setUserId("testuser");
    when(usersApi.usersUserIdPut(eq("testuser"), any(User.class))).thenReturn(
      returnedUser
    );

    Map<String, Object> result = userPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(returnedUser, result.get("user"));
    verify(usersApi).usersUserIdPut(eq("testuser"), any(User.class));
  }

  @Test
  public void testExecuteImpl_whenAdminUser_thenSuccess() throws Exception {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(false);
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(content.getContent()).thenReturn(VALID_JSON);

    User returnedUser = new User();
    when(usersApi.usersUserIdPut(eq("testuser"), any(User.class))).thenReturn(
      returnedUser
    );

    Map<String, Object> result = userPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(returnedUser, result.get("user"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenForbidden() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(false);
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );

    Map<String, Object> result = userPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenInternalServerError()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    when(content.getContent()).thenThrow(new IOException("read error"));

    Map<String, Object> result = userPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenInternalServerError()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    when(content.getContent()).thenReturn("not valid json");

    Map<String, Object> result = userPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageParam_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    when(content.getContent()).thenReturn(VALID_JSON);

    User returnedUser = new User();
    when(usersApi.usersUserIdPut(eq("testuser"), any(User.class))).thenReturn(
      returnedUser
    );

    Map<String, Object> result = userPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(returnedUser, result.get("user"));
  }
}
