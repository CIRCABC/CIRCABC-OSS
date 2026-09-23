package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ProfilesApi;
import io.swagger.model.Profile;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsProfilesGetTest {

  private GroupsProfilesGet webScript;
  private ProfilesApi profilesApi;
  private StubPermissionChecker permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsProfilesGet();
    profilesApi = mock(ProfilesApi.class);
    permissionChecker = new StubPermissionChecker();
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("profilesApi", profilesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsProfiles()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("searchQuery")).thenReturn(null);
    when(req.getParameter("nonEmptyProfiles")).thenReturn(null);
    permissionChecker.setResult(true);

    List<Profile> profiles = List.of(new Profile());
    when(profilesApi.groupsIdProfilesGet("test-ig-id", null, false)).thenReturn(
      profiles
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(profiles, result.get("profiles"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("searchQuery")).thenReturn(null);
    when(req.getParameter("nonEmptyProfiles")).thenReturn(null);
    permissionChecker.setResult(false);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("searchQuery")).thenReturn("admin");
    when(req.getParameter("nonEmptyProfiles")).thenReturn("true");
    permissionChecker.setResult(true);
    when(
      profilesApi.groupsIdProfilesGet("test-ig-id", "admin", true)
    ).thenReturn(Collections.emptyList());

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(Collections.emptyList(), result.get("profiles"));
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("searchQuery")).thenReturn(null);
    when(req.getParameter("nonEmptyProfiles")).thenReturn(null);
    permissionChecker.setResult(true);
    when(profilesApi.groupsIdProfilesGet("test-ig-id", null, false)).thenThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(
        "bad ref",
        null
      )
    );

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method = org.springframework.extensions.webscripts
      .DeclarativeWebScript.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      webScript,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsProfilesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private static class StubPermissionChecker
    extends CurrentUserPermissionCheckerService
  {

    private boolean result;

    void setResult(boolean result) {
      this.result = result;
    }

    @Override
    public boolean hasAnyOfDirectoryPermission(
      String nodeId,
      DirectoryPermissions... directoryPermissions
    ) {
      return result;
    }
  }
}
