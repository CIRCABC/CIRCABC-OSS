package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.FavouritesApi;
import io.swagger.model.SimpleId;
import io.swagger.util.CurrentUserPermissionCheckerService;
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

public class UsersFavouritesPostTest {

  private UsersFavouritesPost webScript;
  private FavouritesApi favouritesApi;
  private CurrentUserPermissionCheckerService permissionChecker;

  @Before
  public void setUp() throws Exception {
    webScript = new UsersFavouritesPost();
    favouritesApi = mock(FavouritesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("favouritesApi", favouritesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  @Test
  public void testExecuteImpl_whenValidUser_thenCallsFavouritesApi()
    throws Exception {
    String userId = "testuser";
    when(permissionChecker.isCurrentUserEqualTo(userId)).thenReturn(true);

    WebScriptRequest req = mockRequest(userId, "{\"id\":\"node-123\"}");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(favouritesApi).usersUserIdFavouritesPost(
      eq(userId),
      any(SimpleId.class)
    );
  }

  @Test
  public void testExecuteImpl_whenDifferentUser_thenForbidden()
    throws Exception {
    String userId = "otheruser";
    when(permissionChecker.isCurrentUserEqualTo(userId)).thenReturn(false);

    WebScriptRequest req = mockRequest(userId, "{\"id\":\"node-123\"}");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(favouritesApi, never()).usersUserIdFavouritesPost(
      anyString(),
      any(SimpleId.class)
    );
  }

  @Test
  public void testExecuteImpl_whenNullUserId_thenDoesNotCallApi()
    throws Exception {
    when(permissionChecker.isCurrentUserEqualTo(null)).thenReturn(true);

    WebScriptRequest req = mockRequest(null, "{\"id\":\"node-123\"}");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(favouritesApi, never()).usersUserIdFavouritesPost(
      anyString(),
      any(SimpleId.class)
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenBadRequest()
    throws Exception {
    String userId = "testuser";
    when(permissionChecker.isCurrentUserEqualTo(userId)).thenReturn(true);

    WebScriptRequest req = mockRequest(userId, "not valid json");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private WebScriptRequest mockRequest(String userId, String body)
    throws Exception {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", userId);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(body);

    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UsersFavouritesPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
