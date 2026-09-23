package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HistoryApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HistoryMembershipsRevocationPostTest {

  private HistoryMembershipsRevocationPost webScript;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private HistoryApi historyApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new HistoryMembershipsRevocationPost();
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    historyApi = mock(HistoryApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("currentUserPermissionCheckerService", permissionCheckerService);
    setField("historyApi", historyApi);

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
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(false);
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenSuccess() throws Exception {
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"userIds\":[\"user1\"],\"revocationDate\":\"2026-01-01\",\"action\":\"revoke\"}"
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(Status.STATUS_OK, status.getCode());
    verify(historyApi).registerRevocation(any());
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenSuccess() throws Exception {
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(false);
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"userIds\":[\"user1\"],\"revocationDate\":\"2026-01-01\",\"action\":\"revoke\"}"
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(Status.STATUS_OK, status.getCode());
    verify(historyApi).registerRevocation(any());
  }

  @Test
  public void testExecuteImpl_whenParsingFails_thenInternalServerError()
    throws Exception {
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("invalid json");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenContentThrowsException_thenInternalServerError()
    throws Exception {
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(true);
    when(req.getContent()).thenThrow(new RuntimeException("IO error"));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HistoryMembershipsRevocationPost.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
