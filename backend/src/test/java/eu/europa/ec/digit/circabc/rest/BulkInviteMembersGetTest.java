package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.model.BulkImportUserData;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class BulkInviteMembersGetTest {

  private BulkInviteMembersGet webScript;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new BulkInviteMembersGet();
    usersApi = mock(UsersApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("usersApi", usersApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = BulkInviteMembersGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Test
  public void testExecuteImpl_whenValidParams_thenReturnsMembers()
    throws Exception {
    when(req.getParameterValues("igIds")).thenReturn(
      new String[] { "ig1", "ig2" }
    );
    when(req.getParameter("destinationIGId")).thenReturn("destIg");
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission("ig1")
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission("ig2")
    ).thenReturn(true);

    List<BulkImportUserData> expected = new ArrayList<>();
    expected.add(new BulkImportUserData());
    when(
      usersApi.getBulkInviteMembers(Arrays.asList("ig1", "ig2"), "destIg")
    ).thenReturn(expected);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(expected, model.get("members"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenIgIdsNull_thenThrowsIllegalArgument()
    throws Exception {
    when(req.getParameterValues("igIds")).thenReturn(null);

    webScript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenIgIdsEmpty_thenThrowsIllegalArgument()
    throws Exception {
    when(req.getParameterValues("igIds")).thenReturn(new String[] {});

    webScript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenDestinationIGIdNull_thenThrowsIllegalArgument()
    throws Exception {
    when(req.getParameterValues("igIds")).thenReturn(new String[] { "ig1" });
    when(req.getParameter("destinationIGId")).thenReturn(null);

    webScript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenDestinationIGIdBlank_thenThrowsIllegalArgument()
    throws Exception {
    when(req.getParameterValues("igIds")).thenReturn(new String[] { "ig1" });
    when(req.getParameter("destinationIGId")).thenReturn("   ");

    webScript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameterValues("igIds")).thenReturn(new String[] { "ig1" });
    when(req.getParameter("destinationIGId")).thenReturn("destIg");
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission("ig1")
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameterValues("igIds")).thenReturn(new String[] { "ig1" });
    when(req.getParameter("destinationIGId")).thenReturn("destIg");
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission("ig1")
    ).thenReturn(true);
    when(usersApi.getBulkInviteMembers(anyList(), eq("destIg"))).thenThrow(
      new InvalidNodeRefException(
        "bad ref",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad")
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError()
    throws Exception {
    when(req.getParameterValues("igIds")).thenReturn(new String[] { "ig1" });
    when(req.getParameter("destinationIGId")).thenReturn("destIg");
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission("ig1")
    ).thenReturn(true);
    when(usersApi.getBulkInviteMembers(anyList(), eq("destIg"))).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIgIdsContainBlanks_thenSkipsThem()
    throws Exception {
    when(req.getParameterValues("igIds")).thenReturn(
      new String[] { "ig1", "", "  ", null }
    );
    when(req.getParameter("destinationIGId")).thenReturn("destIg");
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission("ig1")
    ).thenReturn(true);

    List<BulkImportUserData> expected = new ArrayList<>();
    when(
      usersApi.getBulkInviteMembers(Arrays.asList("ig1"), "destIg")
    ).thenReturn(expected);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(expected, model.get("members"));
    verify(
      currentUserPermissionCheckerService,
      times(1)
    ).hasAlfrescoReadPermission(anyString());
  }
}
