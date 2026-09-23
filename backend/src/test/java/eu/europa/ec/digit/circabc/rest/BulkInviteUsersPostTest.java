package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class BulkInviteUsersPostTest {

  private BulkInviteUsersPost webscript;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new BulkInviteUsersPost();
    usersApi = mock(UsersApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("usersApi", usersApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  @Test
  public void testExecuteImpl_whenIgIdNull_thenNotAcceptable() {
    when(req.getParameter("igId")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIgIdEmpty_thenNotAcceptable() {
    when(req.getParameter("igId")).thenReturn("  ");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenForbidden() {
    when(req.getParameter("igId")).thenReturn("ig-id");
    when(permissionChecker.hasAlfrescoReadPermission("ig-id")).thenReturn(
      false
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenValid_thenReturnsResult() throws Exception {
    when(req.getParameter("igId")).thenReturn("ig-id");
    when(req.getParameter("createNewProfiles")).thenReturn("true");
    when(req.getParameter("notifyUsers")).thenReturn("false");
    when(permissionChecker.hasAlfrescoReadPermission("ig-id")).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("{\"data\":\"test\"}");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(1, model.get("result"));
    verify(usersApi).bulkInviteUsers(
      "{\"data\":\"test\"}",
      "ig-id",
      true,
      false
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest() {
    when(req.getParameter("igId")).thenReturn("ig-id");
    when(permissionChecker.hasAlfrescoReadPermission("ig-id")).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id")
      )
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenNotAcceptable() {
    when(req.getParameter("igId")).thenReturn("ig-id");
    when(permissionChecker.hasAlfrescoReadPermission("ig-id")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = BulkInviteUsersPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
