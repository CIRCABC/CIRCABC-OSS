package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ProfilesApi;
import io.swagger.model.Profile;
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
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsProfilesPostTest {

  private GroupsProfilesPost webScript;
  private ProfilesApi profilesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsProfilesPost();
    profilesApi = mock(ProfilesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("profilesApi", profilesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

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

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenAuthorized_thenReturnsProfile()
    throws Exception {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRADMIN
      )
    ).thenReturn(true);

    String json =
      "{\"title\":{\"en\":\"Test Profile\"},\"permissions\":{\"library\":\"LibAdmin\"}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json);
    when(req.getContent()).thenReturn(content);
    when(req.getParameter("language")).thenReturn(null);

    Profile expectedProfile = new Profile();
    expectedProfile.setName("Test Profile");
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    when(
      profilesApi.groupsIdProfilesPost(eq(groupRef), any(Profile.class))
    ).thenReturn(expectedProfile);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(expectedProfile, result.get("profile"));
  }

  @Test
  public void testExecuteImpl_whenNotAuthorized_thenReturnsForbidden()
    throws Exception {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRADMIN
      )
    ).thenReturn(false);
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale()
    throws Exception {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRADMIN
      )
    ).thenReturn(true);
    when(req.getParameter("language")).thenReturn("fr");

    String json = "{\"title\":{\"fr\":\"Profil Test\"}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json);
    when(req.getContent()).thenReturn(content);

    Profile expectedProfile = new Profile();
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    when(
      profilesApi.groupsIdProfilesPost(eq(groupRef), any(Profile.class))
    ).thenReturn(expectedProfile);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(expectedProfile, result.get("profile"));
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsBadRequest()
    throws Exception {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRADMIN
      )
    ).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);

    String json = "not valid json";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json);
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRADMIN
      )
    ).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new java.io.IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsProfilesPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
