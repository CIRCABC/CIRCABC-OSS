package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SpacesApi;
import io.swagger.model.permissions.LibraryPermissions;
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

public class SpacesIdSharePostTest {

  private SpacesIdSharePost webScript;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new SpacesIdSharePost();
    spacesApi = mock(SpacesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("spacesApi", spacesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-space-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenHasPermissionAndValidBody_thenReturnsOk()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        eq("test-space-id"),
        eq(LibraryPermissions.LIBMANAGEOWN)
      )
    ).thenReturn(true);
    when(req.getParameter("notifyLeaders")).thenReturn("false");

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(
      "{\"igId\":\"ig-123\",\"permission\":\"LibAccess\"}"
    );
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("ok", model.get("message"));
    verify(spacesApi).addShare(eq("test-space-id"), any(), eq(false));
  }

  @Test
  public void testExecuteImpl_whenNotifyLeadersTrue_thenPassesTrue()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        eq("test-space-id"),
        eq(LibraryPermissions.LIBMANAGEOWN)
      )
    ).thenReturn(true);
    when(req.getParameter("notifyLeaders")).thenReturn("true");

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(
      "{\"igId\":\"ig-456\",\"permission\":\"LibManageOwn\"}"
    );
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(spacesApi).addShare(eq("test-space-id"), any(), eq(true));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        eq("test-space-id"),
        eq(LibraryPermissions.LIBMANAGEOWN)
      )
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsNotAcceptable()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        eq("test-space-id"),
        eq(LibraryPermissions.LIBMANAGEOWN)
      )
    ).thenReturn(true);
    when(req.getParameter("notifyLeaders")).thenReturn(null);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("invalid json {{{");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpacesIdSharePost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
