package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.DashboardApi;
import io.swagger.model.UserActionLog;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UsersDashboardDownloadsGetTest {

  private UsersDashboardDownloadsGet webscript;
  private DashboardApi dashboardApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new UsersDashboardDownloadsGet();
    dashboardApi = mock(DashboardApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("dashboardApi", dashboardApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = Map.of("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenSuccess_thenReturnsDownloads() {
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);

    UserActionLog log = new UserActionLog();
    List<UserActionLog> downloads = List.of(log);
    when(dashboardApi.usersUserIdDashboardDownloadsGet("testuser")).thenReturn(
      downloads
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(downloads, model.get("downloads"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsDownloads() {
    when(req.getParameter("language")).thenReturn("fr");
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    when(dashboardApi.usersUserIdDashboardDownloadsGet("testuser")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("downloads"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    when(dashboardApi.usersUserIdDashboardDownloadsGet("testuser")).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    when(dashboardApi.usersUserIdDashboardDownloadsGet("testuser")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UsersDashboardDownloadsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
