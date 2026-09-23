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

public class GroupsMembersExpirationPostTest {

  private GroupsMembersExpirationPost webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionChecker;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsMembersExpirationPost();
    groupsApi = mock(GroupsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsModel()
    throws Exception {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("ig-id"),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest(
      "ig-id",
      "user-id",
      "2099-01-01T00:00:00.000Z",
      "profile1",
      "GROUP_alfGroup"
    );
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(groupsApi).groupsIdMembersUserIdExpirationPost(
      eq("ig-id"),
      eq("user-id"),
      any(),
      eq("profile1"),
      eq("GROUP_alfGroup")
    );
  }

  @Test
  public void testExecuteImpl_whenNoExpirationDate_thenBadRequest() {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("ig-id"),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest("ig-id", "user-id", null, "p1", "g1");
    Status status = new Status();

    Map<String, Object> result = webScript.executeImpl(
      req,
      status,
      new Cache()
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExpirationDateInPast_thenBadRequest() {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("ig-id"),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest(
      "ig-id",
      "user-id",
      "2000-01-01T00:00:00.000Z",
      "p1",
      "g1"
    );
    Status status = new Status();

    Map<String, Object> result = webScript.executeImpl(
      req,
      status,
      new Cache()
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Expiration date must be in the future", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenMissingProfileId_thenBadRequest() {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("ig-id"),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest(
      "ig-id",
      "user-id",
      "2099-01-01T00:00:00.000Z",
      null,
      "g1"
    );
    Status status = new Status();

    Map<String, Object> result = webScript.executeImpl(
      req,
      status,
      new Cache()
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenForbidden() {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("ig-id"),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(false);

    WebScriptRequest req = mockRequest(
      "ig-id",
      "user-id",
      "2099-01-01T00:00:00.000Z",
      "p1",
      "g1"
    );
    Status status = new Status();

    Map<String, Object> result = webScript.executeImpl(
      req,
      status,
      new Cache()
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidDateFormat_thenNotAcceptable() {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("ig-id"),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest(
      "ig-id",
      "user-id",
      "not-a-date",
      "p1",
      "g1"
    );
    Status status = new Status();

    Map<String, Object> result = webScript.executeImpl(
      req,
      status,
      new Cache()
    );

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  private WebScriptRequest mockRequest(
    String igId,
    String userId,
    String expirationDate,
    String profileId,
    String alfrescoGroup
  ) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", igId);
    templateVars.put("userId", userId);

    String template = "/circabc/groups/{igId}/members/{userId}/expiration";
    Match match = new Match(template, templateVars, template);
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("expirationDate")).thenReturn(expirationDate);
    when(req.getParameter("profileId")).thenReturn(profileId);
    when(req.getParameter("alfrescoGroup")).thenReturn(alfrescoGroup);

    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsMembersExpirationPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
