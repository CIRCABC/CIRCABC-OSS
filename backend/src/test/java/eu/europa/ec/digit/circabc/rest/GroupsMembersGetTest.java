package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.PagedUserProfile;
import io.swagger.model.UserProfile;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsMembersGetTest {

  private GroupsMembersGet webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionChecker;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsMembersGet();
    groupsApi = mock(GroupsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsMembers()
    throws Exception {
    WebScriptRequest req = mockRequest(
      "test-ig-id",
      null,
      null,
      null,
      null,
      null,
      null
    );
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("test-ig-id"),
        eq(DirectoryPermissions.DIRACCESS)
      )
    ).thenReturn(true);

    List<UserProfile> users = new ArrayList<>();
    UserProfile up = new UserProfile();
    users.add(up);
    PagedUserProfile pagedResult = new PagedUserProfile();
    pagedResult.setData(users);
    pagedResult.setTotal(1);

    when(
      groupsApi.groupsIdMembersGet(
        eq("test-ig-id"),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull()
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = invokeExecuteImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(users, model.get("data"));
    assertEquals(1, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    WebScriptRequest req = mockRequest(
      "test-ig-id",
      null,
      null,
      null,
      null,
      null,
      null
    );
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("test-ig-id"),
        eq(DirectoryPermissions.DIRACCESS)
      )
    ).thenReturn(false);

    Map<String, Object> model = invokeExecuteImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLimitAndPageProvided_thenPassesToApi()
    throws Exception {
    WebScriptRequest req = mockRequest(
      "ig1",
      "en",
      "10",
      "2",
      "lastName",
      "admin",
      "john"
    );
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("ig1"),
        eq(DirectoryPermissions.DIRACCESS)
      )
    ).thenReturn(true);

    PagedUserProfile pagedResult = new PagedUserProfile();
    pagedResult.setData(new ArrayList<>());
    pagedResult.setTotal(0);

    List<String> expectedProfile = new ArrayList<>();
    expectedProfile.add("admin");

    when(
      groupsApi.groupsIdMembersGet(
        eq("ig1"),
        eq(expectedProfile),
        eq("en"),
        eq(10),
        eq(2),
        eq("lastName"),
        eq("john")
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = invokeExecuteImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(0, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenEmptyLimit_thenDefaults25() throws Exception {
    WebScriptRequest req = mockRequest("ig1", null, "", null, null, null, null);
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("ig1"),
        eq(DirectoryPermissions.DIRACCESS)
      )
    ).thenReturn(true);

    PagedUserProfile pagedResult = new PagedUserProfile();
    pagedResult.setData(new ArrayList<>());
    pagedResult.setTotal(0);

    when(
      groupsApi.groupsIdMembersGet(
        eq("ig1"),
        isNull(),
        isNull(),
        eq(25),
        isNull(),
        isNull(),
        isNull()
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = invokeExecuteImpl(req, status, cache);

    assertNotNull(model);
    verify(groupsApi).groupsIdMembersGet(
      eq("ig1"),
      isNull(),
      isNull(),
      eq(25),
      isNull(),
      isNull(),
      isNull()
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    WebScriptRequest req = mockRequest(
      "bad-id",
      null,
      null,
      null,
      null,
      null,
      null
    );
    Status status = new Status();
    Cache cache = new Cache();

    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        eq("bad-id"),
        eq(DirectoryPermissions.DIRACCESS)
      )
    ).thenReturn(true);

    when(
      groupsApi.groupsIdMembersGet(
        eq("bad-id"),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull(),
        isNull()
      )
    ).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> model = invokeExecuteImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private WebScriptRequest mockRequest(
    String igId,
    String language,
    String limit,
    String page,
    String order,
    String profile,
    String searchQuery
  ) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", igId);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(language);
    when(req.getParameter("limit")).thenReturn(limit);
    when(req.getParameter("page")).thenReturn(page);
    when(req.getParameter("order")).thenReturn(order);
    when(req.getParameter("profile")).thenReturn(profile);
    when(req.getParameter("searchQuery")).thenReturn(searchQuery);

    return req;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> invokeExecuteImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) throws Exception {
    Method method = GroupsMembersGet.class.getSuperclass().getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    return (Map<String, Object>) method.invoke(webScript, req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsMembersGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
