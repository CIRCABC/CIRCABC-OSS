package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AresBridgeApi;
import io.swagger.api.AresBridgeApiImpl;
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

public class ExternalRepositoryTicketPostTest {

  private ExternalRepositoryTicketPost webscript;
  private AresBridgeApi aresBridgeApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new ExternalRepositoryTicketPost();
    aresBridgeApi = mock(AresBridgeApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("aresBridgeApi", aresBridgeApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenGuestUser_thenForbidden() {
    when(permissionChecker.isGuest()).thenReturn(true);
    when(permissionChecker.isExternalUser()).thenReturn(false);
    mockTemplateVars("AresBridge");

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExternalUser_thenForbidden() {
    when(permissionChecker.isGuest()).thenReturn(false);
    when(permissionChecker.isExternalUser()).thenReturn(true);
    mockTemplateVars("AresBridge");

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAresBridgeId_thenReturnsTicket()
    throws Exception {
    when(permissionChecker.isGuest()).thenReturn(false);
    when(permissionChecker.isExternalUser()).thenReturn(false);
    mockTemplateVars(AresBridgeApiImpl.ARES_BRIDGE);
    mockRequestBody(
      "{\"requestDate\":\"2026-01-01\",\"httpVerb\":\"GET\",\"path\":\"/test\"}"
    );
    when(aresBridgeApi.getTicket("2026-01-01", "GET", "/test")).thenReturn(
      "ticket-123"
    );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ticket-123", result.get("ticket"));
  }

  @Test
  public void testExecuteImpl_whenIdIsNull_thenReturnsEmptyModel() {
    when(permissionChecker.isGuest()).thenReturn(false);
    when(permissionChecker.isExternalUser()).thenReturn(false);
    mockTemplateVars(null);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertFalse(result.containsKey("ticket"));
  }

  @Test
  public void testExecuteImpl_whenNonAresBridgeId_thenNoTicket()
    throws Exception {
    when(permissionChecker.isGuest()).thenReturn(false);
    when(permissionChecker.isExternalUser()).thenReturn(false);
    mockTemplateVars("OtherRepo");
    mockRequestBody(
      "{\"requestDate\":\"2026-01-01\",\"httpVerb\":\"GET\",\"path\":\"/test\"}"
    );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertFalse(result.containsKey("ticket"));
  }

  @Test
  public void testExecuteImpl_whenIOException_thenBadRequest()
    throws Exception {
    when(permissionChecker.isGuest()).thenReturn(false);
    when(permissionChecker.isExternalUser()).thenReturn(false);
    mockTemplateVars(AresBridgeApiImpl.ARES_BRIDGE);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new IOException("read error"));

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void mockTemplateVars(String id) {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void mockRequestBody(String json) throws IOException {
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(json);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ExternalRepositoryTicketPost.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
