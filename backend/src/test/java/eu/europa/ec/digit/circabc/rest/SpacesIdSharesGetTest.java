package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SpacesApi;
import io.swagger.model.PagedShares;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpacesIdSharesGetTest {

  private SpacesIdSharesGet webscript;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new SpacesIdSharesGet();
    spacesApi = mock(SpacesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("spacesApi", spacesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenHappyPath_thenReturnsSharesAndTotal()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-space-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-space-id",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");

    PagedShares pagedShares = new PagedShares(Collections.emptyList(), 5L);
    when(spacesApi.getInvitedInterestGroups("test-space-id", 0, 10)).thenReturn(
      pagedShares
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("shares"));
    assertEquals(5L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-space-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-space-id",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(false);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPageIsZero_thenReturnsError()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-space-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-space-id",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn("0");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNegativeLimit_thenReturnsError()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-space-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-space-id",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("-1");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPageAndLimitNull_thenUsesDefaults()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-space-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-space-id",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);

    PagedShares pagedShares = new PagedShares(Collections.emptyList(), 0L);
    when(spacesApi.getInvitedInterestGroups("test-space-id", 0, 0)).thenReturn(
      pagedShares
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(0L, model.get("total"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpacesIdSharesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
