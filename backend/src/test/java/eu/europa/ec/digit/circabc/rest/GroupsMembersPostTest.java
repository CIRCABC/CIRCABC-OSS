package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.MembershipPostDefinition;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsMembersPostTest {

  private GroupsMembersPost webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsMembersPost();
    groupsApi = mock(GroupsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

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
  public void testExecuteImpl_whenValidBody_thenCallsGroupsApi()
    throws Exception {
    String igId = "test-ig-id";
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(igId),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest(igId, buildValidBody(), null, null);

    Status status = new Status();
    Map<String, Object> result = webScript.executeImpl(
      req,
      status,
      new Cache()
    );

    assertNotNull(result);
    ArgumentCaptor<NodeRef> nodeRefCaptor = ArgumentCaptor.forClass(
      NodeRef.class
    );
    ArgumentCaptor<MembershipPostDefinition> bodyCaptor =
      ArgumentCaptor.forClass(MembershipPostDefinition.class);
    verify(groupsApi).groupsIdMembersPost(
      nodeRefCaptor.capture(),
      bodyCaptor.capture()
    );

    assertEquals(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, igId),
      nodeRefCaptor.getValue()
    );
    MembershipPostDefinition posted = bodyCaptor.getValue();
    assertTrue(posted.getAdminNotifications());
    assertFalse(posted.getUserNotifications());
    assertEquals(1, posted.getMemberships().size());
    assertEquals("user1", posted.getMemberships().get(0).getUser().getUserId());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    String igId = "test-ig-id";
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(igId),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(false);

    WebScriptRequest req = mockRequest(igId, buildValidBody(), null, null);
    Status status = new Status();

    Map<String, Object> result = webScript.executeImpl(
      req,
      status,
      new Cache()
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(groupsApi, never()).groupsIdMembersPost(
      any(NodeRef.class),
      any(MembershipPostDefinition.class)
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsNotAcceptable()
    throws Exception {
    String igId = "test-ig-id";
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(igId),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest(igId, "not valid json", null, null);
    Status status = new Status();

    Map<String, Object> result = webScript.executeImpl(
      req,
      status,
      new Cache()
    );

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageParam_thenSetsLocale()
    throws Exception {
    String igId = "test-ig-id";
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(igId),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest(igId, buildValidBody(), "fr", null);
    Status status = new Status();

    Map<String, Object> result = webScript.executeImpl(
      req,
      status,
      new Cache()
    );

    assertNotNull(result);
    verify(groupsApi).groupsIdMembersPost(
      any(NodeRef.class),
      any(MembershipPostDefinition.class)
    );
  }

  @Test
  public void testExecuteImpl_whenExpirationDate_thenSetsOnBody()
    throws Exception {
    String igId = "test-ig-id";
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(igId),
        eq(DirectoryPermissions.DIRMANAGEMEMBERS)
      )
    ).thenReturn(true);

    WebScriptRequest req = mockRequest(
      igId,
      buildValidBody(),
      null,
      "2026-12-31T00:00:00.000Z"
    );
    Status status = new Status();

    Map<String, Object> result = webScript.executeImpl(
      req,
      status,
      new Cache()
    );

    assertNotNull(result);
    ArgumentCaptor<MembershipPostDefinition> bodyCaptor =
      ArgumentCaptor.forClass(MembershipPostDefinition.class);
    verify(groupsApi).groupsIdMembersPost(
      any(NodeRef.class),
      bodyCaptor.capture()
    );
    assertNotNull(bodyCaptor.getValue().getExpirationDate());
  }

  private WebScriptRequest mockRequest(
    String igId,
    String body,
    String language,
    String expirationDate
  ) throws Exception {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", igId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(language);
    when(req.getParameter("expirationDate")).thenReturn(expirationDate);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(body);
    when(req.getContent()).thenReturn(content);

    return req;
  }

  private String buildValidBody() {
    return (
      "{" +
      "\"adminNotifications\": true," +
      "\"userNotifications\": false," +
      "\"notifyText\": \"Welcome!\"," +
      "\"memberships\": [{" +
      "  \"user\": {" +
      "    \"userId\": \"user1\"," +
      "    \"email\": \"user1@test.com\"," +
      "    \"firstname\": \"John\"," +
      "    \"lastname\": \"Doe\"" +
      "  }," +
      "  \"profile\": {" +
      "    \"id\": \"prof1\"," +
      "    \"name\": \"ACCESS\"," +
      "    \"groupName\": \"GROUP_ACCESS\"" +
      "  }" +
      "}]" +
      "}"
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsMembersPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
