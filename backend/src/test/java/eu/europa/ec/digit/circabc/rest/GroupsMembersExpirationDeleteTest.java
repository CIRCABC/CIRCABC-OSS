package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsMembersExpirationDeleteTest {

  private GroupsMembersExpirationDelete webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsMembersExpirationDelete();
    groupsApi = mock(GroupsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsMembersExpirationDelete.class.getDeclaredField(
      fieldName
    );
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
  public void testExecuteImpl_whenAuthorized_thenDeletesExpirationAndReturnsModel() {
    mockTemplateVars("ig-123", "user-456");
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "ig-123",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(groupsApi).groupsIdMembersUserIdExpirationDelete(
      "ig-123",
      "user-456"
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    mockTemplateVars("ig-123", "user-456");
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "ig-123",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    assertEquals("Access denied", status.getMessage());
    assertTrue(status.getRedirect());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    mockTemplateVars("ig-123", "user-456");
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "ig-123",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    )
      .when(groupsApi)
      .groupsIdMembersUserIdExpirationDelete("ig-123", "user-456");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Bad request", status.getMessage());
    assertTrue(status.getRedirect());
  }
}
