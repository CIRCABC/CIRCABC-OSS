package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.ApplicantAction;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
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

public class GroupsApplicantsPutTest {

  private GroupsApplicantsPut groupsApplicantsPut;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_IG_ID = "test-ig-id";
  private static final String VALID_JSON =
    "{\"username\":\"testuser\",\"action\":\"approve\",\"message\":\"Welcome\"}";

  @Before
  public void setUp() throws Exception {
    groupsApplicantsPut = new GroupsApplicantsPut();
    groupsApi = mock(GroupsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", TEST_IG_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("action")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenPermissionGranted_thenSuccess()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(TEST_IG_ID),
        any(DirectoryPermissions.class)
      )
    ).thenReturn(true);

    Map<String, Object> model = groupsApplicantsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    verify(groupsApi).groupsIdMembersApplicantsPut(
      eq(TEST_IG_ID),
      any(ApplicantAction.class)
    );
  }

  @Test
  public void testExecuteImpl_whenActionClean_thenSetsActionOnBody()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);
    when(req.getParameter("action")).thenReturn("clean");

    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(TEST_IG_ID),
        any(DirectoryPermissions.class)
      )
    ).thenReturn(true);

    Map<String, Object> model = groupsApplicantsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    verify(groupsApi).groupsIdMembersApplicantsPut(
      eq(TEST_IG_ID),
      argThat(body -> "clean".equals(body.getAction()))
    );
  }

  @Test
  public void testExecuteImpl_whenActionDecline_thenSetsActionOnBody()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);
    when(req.getParameter("action")).thenReturn("decline");

    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(TEST_IG_ID),
        any(DirectoryPermissions.class)
      )
    ).thenReturn(true);

    Map<String, Object> model = groupsApplicantsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    verify(groupsApi).groupsIdMembersApplicantsPut(
      eq(TEST_IG_ID),
      argThat(body -> "decline".equals(body.getAction()))
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidAction_thenActionIsEmpty()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);
    when(req.getParameter("action")).thenReturn("invalid");

    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(TEST_IG_ID),
        any(DirectoryPermissions.class)
      )
    ).thenReturn(true);

    Map<String, Object> model = groupsApplicantsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    verify(groupsApi).groupsIdMembersApplicantsPut(
      eq(TEST_IG_ID),
      argThat(body -> "".equals(body.getAction()))
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(TEST_IG_ID),
        any(DirectoryPermissions.class)
      )
    ).thenReturn(false);

    Map<String, Object> model = groupsApplicantsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(TEST_IG_ID),
        any(DirectoryPermissions.class)
      )
    ).thenReturn(true);

    Map<String, Object> model = groupsApplicantsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(TEST_IG_ID),
        any(DirectoryPermissions.class)
      )
    ).thenReturn(true);

    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TEST_IG_ID)
      )
    )
      .when(groupsApi)
      .groupsIdMembersApplicantsPut(eq(TEST_IG_ID), any(ApplicantAction.class));

    Map<String, Object> model = groupsApplicantsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);
    when(req.getParameter("language")).thenReturn("fr");

    when(
      permissionCheckerService.hasAnyOfDirectoryPermission(
        eq(TEST_IG_ID),
        any(DirectoryPermissions.class)
      )
    ).thenReturn(true);

    Map<String, Object> model = groupsApplicantsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    verify(groupsApi).groupsIdMembersApplicantsPut(
      eq(TEST_IG_ID),
      any(ApplicantAction.class)
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsApplicantsPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(groupsApplicantsPut, value);
  }
}
