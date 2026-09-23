package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.model.BulkImportUserData;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

public class BulkInviteUsersDigestFilePutTest {

  private BulkInviteUsersDigestFilePut webScript;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new BulkInviteUsersDigestFilePut();
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

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsUserData()
    throws Exception {
    String igId = "test-ig-id";
    when(req.getParameter("igId")).thenReturn(igId);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(igId)
    ).thenReturn(true);

    InputStream inputStream = new ByteArrayInputStream("test".getBytes());
    FormData formData = mock(FormData.class);
    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getName()).thenReturn("fileData");
    when(field.getIsFile()).thenReturn(true);
    when(field.getInputStream()).thenReturn(inputStream);
    when(field.getFilename()).thenReturn("users.xlsx");
    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });
    when(req.parseContent()).thenReturn(formData);

    List<BulkImportUserData> expectedData = new ArrayList<>();
    expectedData.add(new BulkImportUserData());
    when(
      usersApi.bulkInviteUsersDigestFile(
        eq(igId),
        any(InputStream.class),
        eq("users.xlsx")
      )
    ).thenReturn(expectedData);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(expectedData, model.get("userData"));
  }

  @Test
  public void testExecuteImpl_whenIgIdNull_thenReturnsNull() {
    when(req.getParameter("igId")).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIgIdEmpty_thenReturnsNull() {
    when(req.getParameter("igId")).thenReturn("   ");

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String igId = "test-ig-id";
    when(req.getParameter("igId")).thenReturn(igId);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(igId)
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenFormDataNull_thenReturnsNull() {
    String igId = "test-ig-id";
    when(req.getParameter("igId")).thenReturn(igId);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(igId)
    ).thenReturn(true);
    when(req.parseContent()).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenWrongNumberOfFields_thenReturnsNull() {
    String igId = "test-ig-id";
    when(req.getParameter("igId")).thenReturn(igId);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(igId)
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getFields()).thenReturn(new FormData.FormField[] {});
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = BulkInviteUsersDigestFilePut.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
