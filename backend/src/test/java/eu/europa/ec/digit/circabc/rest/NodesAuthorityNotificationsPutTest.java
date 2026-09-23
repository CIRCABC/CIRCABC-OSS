package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NotificationsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesAuthorityNotificationsPutTest {

  private NodesAuthorityNotificationsPut webscript;
  private NotificationsApi notificationsApi;
  private AuthenticationService authenticationService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new NodesAuthorityNotificationsPut();
    notificationsApi = mock(NotificationsApi.class);
    authenticationService = mock(AuthenticationService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("notificationsApi", notificationsApi);
    setField("authenticationService", authenticationService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    templateVars.put("authority", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenCurrentUserMatchesAuthority_thenSuccess()
    throws Exception {
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("\"ALLOWED\"");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(notificationsApi).nodesIdNotificationsAuthorityPut(
      "test-node-id",
      "testuser",
      "ALLOWED"
    );
  }

  @Test
  public void testExecuteImpl_whenLibAdmin_thenSuccess() throws Exception {
    when(authenticationService.getCurrentUserName()).thenReturn("adminuser");
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("test-node-id"),
        any()
      )
    ).thenReturn(true);
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("\"ALLOWED\"");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(notificationsApi).nodesIdNotificationsAuthorityPut(
      "test-node-id",
      "testuser",
      "ALLOWED"
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(authenticationService.getCurrentUserName()).thenReturn("otheruser");
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        anyString(),
        any()
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        anyString(),
        any()
      )
    ).thenReturn(false);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenProcessesWithLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("\"INHIBITED\"");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(notificationsApi).nodesIdNotificationsAuthorityPut(
      "test-node-id",
      "testuser",
      "INHIBITED"
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesAuthorityNotificationsPut.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
