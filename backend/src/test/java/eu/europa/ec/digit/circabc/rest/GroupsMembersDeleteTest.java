package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.api.NotificationsApi;
import io.swagger.model.InterestGroup;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsMembersDeleteTest {

  private GroupsMembersDelete webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private NotificationsApi notificationsApi;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsMembersDelete();
    groupsApi = mock(GroupsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    notificationsApi = mock(NotificationsApi.class);

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
    setField("notificationsApi", notificationsApi);

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
  public void testExecuteImpl_whenAuthorized_thenDeletesMemberAndRemovesNotifications()
    throws Exception {
    WebScriptRequest req = mockRequest("ig-123", "user1");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "ig-123",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);

    InterestGroup ig = new InterestGroup();
    ig.setNewsgroupId("newsgroup-1");
    ig.setLibraryId("library-1");
    when(groupsApi.getInterestGroup("ig-123")).thenReturn(ig);

    when(
      notificationsApi.isUsersubscribedForNotification("newsgroup-1", "user1")
    ).thenReturn(true);
    when(
      notificationsApi.isUsersubscribedForNotification("library-1", "user1")
    ).thenReturn(true);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(notificationsApi).removeNotification("newsgroup-1", "user1");
    verify(notificationsApi).removeNotification("library-1", "user1");
    verify(groupsApi).groupsIdMembersUserIdDelete("ig-123", "user1");
  }

  @Test
  public void testExecuteImpl_whenCurrentUser_thenDeletesOwnMembership()
    throws Exception {
    WebScriptRequest req = mockRequest("ig-123", "user1");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "ig-123",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(false);
    when(permissionChecker.isCurrentUserEqualTo("user1")).thenReturn(true);

    InterestGroup ig = new InterestGroup();
    ig.setNewsgroupId("newsgroup-1");
    ig.setLibraryId("library-1");
    when(groupsApi.getInterestGroup("ig-123")).thenReturn(ig);

    when(
      notificationsApi.isUsersubscribedForNotification("newsgroup-1", "user1")
    ).thenReturn(false);
    when(
      notificationsApi.isUsersubscribedForNotification("library-1", "user1")
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(notificationsApi, never()).removeNotification(
      anyString(),
      anyString()
    );
    verify(groupsApi).groupsIdMembersUserIdDelete("ig-123", "user1");
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    WebScriptRequest req = mockRequest("ig-123", "user1");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "ig-123",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(false);
    when(permissionChecker.isCurrentUserEqualTo("user1")).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNoNotificationSubscriptions_thenSkipsRemoval()
    throws Exception {
    WebScriptRequest req = mockRequest("ig-123", "user1");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "ig-123",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);

    InterestGroup ig = new InterestGroup();
    ig.setNewsgroupId("newsgroup-1");
    ig.setLibraryId("library-1");
    when(groupsApi.getInterestGroup("ig-123")).thenReturn(ig);

    when(
      notificationsApi.isUsersubscribedForNotification("newsgroup-1", "user1")
    ).thenReturn(false);
    when(
      notificationsApi.isUsersubscribedForNotification("library-1", "user1")
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(notificationsApi, never()).removeNotification(
      anyString(),
      anyString()
    );
    verify(groupsApi).groupsIdMembersUserIdDelete("ig-123", "user1");
  }

  private WebScriptRequest mockRequest(String igId, String userId) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", igId);
    templateVars.put("userId", userId);
    String template = "/circabc/groups/{igId}/members/{userId}";
    Match match = new Match(template, templateVars, template);
    when(req.getServiceMatch()).thenReturn(match);
    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsMembersDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
