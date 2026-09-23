package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.MembershipPostDefinition;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsMembersPutTest {

  private static final String VALID_JSON =
    "{\"adminNotifications\":true,\"userNotifications\":false,\"memberships\":[" +
    "{\"user\":{\"userId\":\"user1\",\"email\":\"u@e.com\",\"firstname\":\"John\",\"lastname\":\"Doe\"}," +
    "\"profile\":{\"id\":\"prof1\",\"name\":\"Access\",\"groupName\":\"GROUP_ACCESS\"}}]}";

  private GroupsMembersPut webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Content content;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsMembersPut();
    groupsApi = mock(GroupsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
    content = mock(Content.class);

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("expirationDate")).thenReturn(null);
    when(req.getContent()).thenReturn(content);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsMembersPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenSuccess() throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(content.getContent()).thenReturn(VALID_JSON);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    NodeRef expectedRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    verify(groupsApi).groupsIdMembersPut(
      eq(expectedRef),
      any(MembershipPostDefinition.class)
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(false);

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
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(content.getContent()).thenReturn(VALID_JSON);
    NodeRef expectedRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    doThrow(new InvalidNodeRefException(expectedRef))
      .when(groupsApi)
      .groupsIdMembersPut(eq(expectedRef), any(MembershipPostDefinition.class));

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsNotAcceptable()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(content.getContent()).thenThrow(new IOException("read error"));

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenParseException_thenReturnsNotAcceptable()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(content.getContent()).thenReturn("not valid json {{{");

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExpirationDateProvided_thenSetsOnBody()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getParameter("expirationDate")).thenReturn(
      "2026-12-31T00:00:00.000Z"
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(groupsApi).groupsIdMembersPut(
      any(NodeRef.class),
      any(MembershipPostDefinition.class)
    );
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getParameter("language")).thenReturn("fr");

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(groupsApi).groupsIdMembersPut(
      any(NodeRef.class),
      any(MembershipPostDefinition.class)
    );
  }
}
