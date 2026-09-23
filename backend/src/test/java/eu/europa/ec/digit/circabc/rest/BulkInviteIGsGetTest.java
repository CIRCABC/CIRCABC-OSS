package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.model.db.IGData;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class BulkInviteIGsGetTest {

  private BulkInviteIGsGet webscript;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new BulkInviteIGsGet();
    usersApi = mock(UsersApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("usersApi", usersApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenCategoryIdNull_thenThrows() {
    when(req.getParameter("categoryId")).thenReturn(null);
    webscript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenCategoryIdEmpty_thenThrows() {
    when(req.getParameter("categoryId")).thenReturn("  ");
    webscript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenIgIdNull_thenThrows() {
    when(req.getParameter("categoryId")).thenReturn("cat-id");
    when(req.getParameter("igId")).thenReturn(null);
    webscript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenIgIdEmpty_thenThrows() {
    when(req.getParameter("categoryId")).thenReturn("cat-id");
    when(req.getParameter("igId")).thenReturn("");
    webscript.executeImpl(req, status, cache);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteImpl_whenValid_thenReturnsIGs() {
    when(req.getParameter("categoryId")).thenReturn("cat-id");
    when(req.getParameter("igId")).thenReturn("ig-id");
    when(permissionChecker.hasAlfrescoReadPermission("cat-id")).thenReturn(
      true
    );
    when(permissionChecker.hasAlfrescoReadPermission("ig-id")).thenReturn(true);

    IGData ig1 = new IGData();
    IGData ig2 = new IGData();
    List<IGData> igs = Arrays.asList(ig1, ig2);
    when(usersApi.getBulkInviteIGs("cat-id", "ig-id")).thenReturn(igs);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(igs, model.get("igs"));
  }

  @Test
  public void testExecuteImpl_whenAccessDeniedOnCategory_thenForbidden() {
    when(req.getParameter("categoryId")).thenReturn("cat-id");
    when(req.getParameter("igId")).thenReturn("ig-id");
    when(permissionChecker.hasAlfrescoReadPermission("cat-id")).thenReturn(
      false
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAccessDeniedOnIg_thenForbidden() {
    when(req.getParameter("categoryId")).thenReturn("cat-id");
    when(req.getParameter("igId")).thenReturn("ig-id");
    when(permissionChecker.hasAlfrescoReadPermission("cat-id")).thenReturn(
      true
    );
    when(permissionChecker.hasAlfrescoReadPermission("ig-id")).thenReturn(
      false
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest() {
    when(req.getParameter("categoryId")).thenReturn("cat-id");
    when(req.getParameter("igId")).thenReturn("ig-id");
    when(permissionChecker.hasAlfrescoReadPermission("cat-id")).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "cat-id")
      )
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenInternalError() {
    when(req.getParameter("categoryId")).thenReturn("cat-id");
    when(req.getParameter("igId")).thenReturn("ig-id");
    when(permissionChecker.hasAlfrescoReadPermission("cat-id")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = BulkInviteIGsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
