package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AutoUploadApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AutoUploadPostTest {

  private AutoUploadPost autoUploadPost;
  private AutoUploadApi autoUploadApi;
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

    autoUploadPost = new AutoUploadPost();
    autoUploadApi = mock(AutoUploadApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("autoUploadApi", autoUploadApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "ig-123");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenAdminAndValidBody_thenReturnsResult()
    throws Exception {
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("{\"key\":\"value\"}");

    Map<String, Object> result = autoUploadPost.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(1, result.get("result"));
    verify(autoUploadApi).addAutoUploadEntry("ig-123", "{\"key\":\"value\"}");
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden()
    throws Exception {
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      false
    );

    Map<String, Object> result = autoUploadPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsNotAcceptable()
    throws Exception {
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("body");
    doThrow(new RuntimeException("fail"))
      .when(autoUploadApi)
      .addAutoUploadEntry("ig-123", "body");

    Map<String, Object> result = autoUploadPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("body");
    doThrow(new InvalidNodeRefException("bad ref", null))
      .when(autoUploadApi)
      .addAutoUploadEntry("ig-123", "body");

    Map<String, Object> result = autoUploadPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AutoUploadPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(autoUploadPost, value);
  }
}
