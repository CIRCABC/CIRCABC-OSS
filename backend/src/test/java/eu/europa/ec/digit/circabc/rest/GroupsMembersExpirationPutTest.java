package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.permissions.DirectoryPermissions;
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

public class GroupsMembersExpirationPutTest {

  private GroupsMembersExpirationPut webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsMembersExpirationPut();
    groupsApi = mock(GroupsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsMembersExpirationPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String igId, String userId) {
    Map<String, String> vars = new HashMap<>();
    vars.put("igId", igId);
    vars.put("userId", userId);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidFutureDate_thenUpdatesExpiration() {
    String igId = "test-ig-id";
    String userId = "test-user";
    mockTemplateVars(igId, userId);
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        igId,
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(req.getParameter("expirationDate")).thenReturn(
      "2099-12-31T23:59:59.000Z"
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(groupsApi).groupsIdMembersUserIdExpirationPut(
      eq(igId),
      eq(userId),
      any()
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String igId = "test-ig-id";
    String userId = "test-user";
    mockTemplateVars(igId, userId);
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        igId,
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(groupsApi);
  }

  @Test
  public void testExecuteImpl_whenNoExpirationDateParam_thenReturnsBadRequest() {
    String igId = "test-ig-id";
    String userId = "test-user";
    mockTemplateVars(igId, userId);
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        igId,
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(req.getParameter("expirationDate")).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    verifyNoInteractions(groupsApi);
  }

  @Test
  public void testExecuteImpl_whenDateInPast_thenReturnsBadRequest() {
    String igId = "test-ig-id";
    String userId = "test-user";
    mockTemplateVars(igId, userId);
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        igId,
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(req.getParameter("expirationDate")).thenReturn(
      "2000-01-01T00:00:00.000Z"
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    verifyNoInteractions(groupsApi);
  }

  @Test
  public void testExecuteImpl_whenInvalidDateFormat_thenReturnsNotAcceptable() {
    String igId = "test-ig-id";
    String userId = "test-user";
    mockTemplateVars(igId, userId);
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        igId,
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(req.getParameter("expirationDate")).thenReturn("not-a-date");

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    verifyNoInteractions(groupsApi);
  }
}
