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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsImportedProfilesPostTest {

  private GroupsImportedProfilesPost webscript;
  private ProfilesApi profilesApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_IG_ID = "test-ig-id";
  private static final String VALID_JSON =
    "{\"title\":{\"en\":\"Imported Profile\"},\"imported\":true}";

  @Before
  public void setUp() throws Exception {
    webscript = new GroupsImportedProfilesPost();
    profilesApi = mock(ProfilesApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("profilesApi", profilesApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", TEST_IG_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenPermissionGranted_thenReturnsProfile()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        TEST_IG_ID,
        DirectoryPermissions.DIRADMIN
      )
    ).thenReturn(true);

    Profile resultProfile = new Profile();
    resultProfile.setName("Imported Profile");
    NodeRef expectedNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_IG_ID
    );
    when(
      profilesApi.groupsIdImportedProfilesPost(
        eq(expectedNodeRef),
        any(Profile.class)
      )
    ).thenReturn(resultProfile);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultProfile, model.get("profile"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        TEST_IG_ID,
        DirectoryPermissions.DIRADMIN
      )
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsBadRequest()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("not json");
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        TEST_IG_ID,
        DirectoryPermissions.DIRADMIN
      )
    ).thenReturn(true);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        TEST_IG_ID,
        DirectoryPermissions.DIRADMIN
      )
    ).thenReturn(true);

    Profile resultProfile = new Profile();
    NodeRef expectedNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_IG_ID
    );
    when(
      profilesApi.groupsIdImportedProfilesPost(
        eq(expectedNodeRef),
        any(Profile.class)
      )
    ).thenReturn(resultProfile);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultProfile, model.get("profile"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsImportedProfilesPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
