package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.PagedUserProfile;
import io.swagger.model.UserProfile;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.*;

public class GroupsMembersFilterGetTest {

  private GroupsMembersFilterGet webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsMembersFilterGet();
    groupsApi = mock(GroupsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsMembersFilterGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsData()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRACCESS
      )
    ).thenReturn(true);

    PagedUserProfile pagedResult = new PagedUserProfile();
    List<UserProfile> data = new ArrayList<>();
    pagedResult.setData(data);
    pagedResult.setTotal(0);

    when(
      groupsApi.groupsIdMembersGet(
        eq("test-ig-id"),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull()
      )
    ).thenReturn(pagedResult);

    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("firstName")).thenReturn(null);
    when(req.getParameter("lastName")).thenReturn(null);
    when(req.getParameter("email")).thenReturn(null);
    when(req.getParameter("profile")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(data, model.get("data"));
    assertEquals(0, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenWithParameters_thenPassesThemToApi()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRACCESS
      )
    ).thenReturn(true);

    PagedUserProfile pagedResult = new PagedUserProfile();
    pagedResult.setData(new ArrayList<>());
    pagedResult.setTotal(5);

    List<String> expectedProfile = new ArrayList<>();
    expectedProfile.add("admin");

    when(
      groupsApi.groupsIdMembersGet(
        eq("test-ig-id"),
        eq(expectedProfile),
        eq("en"),
        eq(10),
        eq(2),
        eq("name_ASC"),
        eq("John"),
        eq("Doe"),
        eq("john@test.com")
      )
    ).thenReturn(pagedResult);

    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("firstName")).thenReturn("John");
    when(req.getParameter("lastName")).thenReturn("Doe");
    when(req.getParameter("email")).thenReturn("john@test.com");
    when(req.getParameter("profile")).thenReturn("admin");
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("page")).thenReturn("2");
    when(req.getParameter("order")).thenReturn("name_ASC");

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(5, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRACCESS
      )
    ).thenReturn(false);

    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("firstName")).thenReturn(null);
    when(req.getParameter("lastName")).thenReturn(null);
    when(req.getParameter("email")).thenReturn(null);
    when(req.getParameter("profile")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRACCESS
      )
    ).thenReturn(true);

    when(
      groupsApi.groupsIdMembersGet(
        anyString(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any()
      )
    ).thenThrow(new InvalidNodeRefException("bad ref", null));

    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("firstName")).thenReturn(null);
    when(req.getParameter("lastName")).thenReturn(null);
    when(req.getParameter("email")).thenReturn(null);
    when(req.getParameter("profile")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenEmptyLimitAndPage_thenUsesDefaults()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRACCESS
      )
    ).thenReturn(true);

    PagedUserProfile pagedResult = new PagedUserProfile();
    pagedResult.setData(new ArrayList<>());
    pagedResult.setTotal(0);

    when(
      groupsApi.groupsIdMembersGet(
        eq("test-ig-id"),
        isNull(),
        isNull(),
        eq(25),
        eq(1),
        isNull(),
        isNull(),
        isNull(),
        isNull()
      )
    ).thenReturn(pagedResult);

    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("firstName")).thenReturn(null);
    when(req.getParameter("lastName")).thenReturn(null);
    when(req.getParameter("email")).thenReturn(null);
    when(req.getParameter("profile")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("");
    when(req.getParameter("page")).thenReturn("");
    when(req.getParameter("order")).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(groupsApi).groupsIdMembersGet(
      eq("test-ig-id"),
      isNull(),
      isNull(),
      eq(25),
      eq(1),
      isNull(),
      isNull(),
      isNull(),
      isNull()
    );
  }
}
