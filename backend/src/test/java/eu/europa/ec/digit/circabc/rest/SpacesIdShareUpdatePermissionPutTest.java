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
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpacesIdShareUpdatePermissionPutTest {

  private SpacesIdShareUpdatePermissionPut webscript;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new SpacesIdShareUpdatePermissionPut();
    spacesApi = mock(SpacesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("spacesApi", spacesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "space-123");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpacesIdShareUpdatePermissionPut.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenSuccess() throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "space-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(req.getParameter("igId")).thenReturn("ig-456");
    when(req.getParameter("permission")).thenReturn("LibAdmin");
    when(req.getParameter("notifyLeaders")).thenReturn("true");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("ok", model.get("message"));
    verify(spacesApi).changeSharePermission(
      "space-123",
      "ig-456",
      "LibAdmin",
      true
    );
  }

  @Test
  public void testExecuteImpl_whenNotifyLeadersFalse_thenPassesFalse()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "space-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(req.getParameter("igId")).thenReturn("ig-456");
    when(req.getParameter("permission")).thenReturn("LibAccess");
    when(req.getParameter("notifyLeaders")).thenReturn("false");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(spacesApi).changeSharePermission(
      "space-123",
      "ig-456",
      "LibAccess",
      false
    );
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "space-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenNotAcceptable()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "space-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(req.getParameter("igId")).thenReturn("ig-456");
    when(req.getParameter("permission")).thenReturn("LibAdmin");
    when(req.getParameter("notifyLeaders")).thenReturn("true");
    doThrow(new RuntimeException("Share error"))
      .when(spacesApi)
      .changeSharePermission("space-123", "ig-456", "LibAdmin", true);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("Share error", status.getMessage());
  }
}
