package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class PostAbuseSignalPostTest {

  private PostAbuseSignalPost webScript;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new PostAbuseSignalPost();
    forumsApi = mock(ForumsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("forumsApi", forumsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PostAbuseSignalPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Test
  public void testExecuteImpl_whenHasPermissionAndAbuseText_thenSuccess()
    throws Exception {
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        eq("test-node-id"),
        eq(NewsGroupPermissions.NWSACCESS),
        eq(NewsGroupPermissions.NWSPOST)
      )
    ).thenReturn(true);
    when(req.getParameter("abuseText")).thenReturn("offensive content");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(forumsApi).signalAbuse("test-node-id", "offensive content");
  }

  @Test
  public void testExecuteImpl_whenAbuseTextNull_thenPassesEmptyString()
    throws Exception {
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        eq("test-node-id"),
        eq(NewsGroupPermissions.NWSACCESS),
        eq(NewsGroupPermissions.NWSPOST)
      )
    ).thenReturn(true);
    when(req.getParameter("abuseText")).thenReturn(null);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(forumsApi).signalAbuse("test-node-id", "");
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden()
    throws Exception {
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        eq("test-node-id"),
        eq(NewsGroupPermissions.NWSACCESS),
        eq(NewsGroupPermissions.NWSPOST)
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(forumsApi);
  }

  @Test
  public void testExecuteImpl_whenInvalidTypeException_thenBadRequest()
    throws Exception {
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        eq("test-node-id"),
        eq(NewsGroupPermissions.NWSACCESS),
        eq(NewsGroupPermissions.NWSPOST)
      )
    ).thenReturn(true);
    when(req.getParameter("abuseText")).thenReturn("text");
    doThrow(new InvalidTypeException("bad type", null))
      .when(forumsApi)
      .signalAbuse(anyString(), anyString());

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Bad noderef type", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenGenericException_thenBadRequest()
    throws Exception {
    when(
      permissionChecker.hasAnyOfNewsGroupPermission(
        eq("test-node-id"),
        eq(NewsGroupPermissions.NWSACCESS),
        eq(NewsGroupPermissions.NWSPOST)
      )
    ).thenReturn(true);
    when(req.getParameter("abuseText")).thenReturn("text");
    doThrow(new RuntimeException("unexpected"))
      .when(forumsApi)
      .signalAbuse(anyString(), anyString());

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Bad request", status.getMessage());
  }
}
