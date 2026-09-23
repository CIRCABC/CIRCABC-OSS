package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.TopicsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

public class PostAttachmentFileAddTest {

  private PostAttachmentFileAdd webscript;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new PostAttachmentFileAdd();
    topicsApi = mock(TopicsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("topicsApi", topicsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-post-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("name")).thenReturn("file.pdf");
  }

  @Test
  public void testExecuteImpl_whenValidFile_thenReturnsModel()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("test-post-id"),
        eq(NewsGroupPermissions.NWSPOST)
      )
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getIsFile()).thenReturn(true);
    InputStream inputStream = new ByteArrayInputStream("content".getBytes());
    when(field.getInputStream()).thenReturn(inputStream);

    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(topicsApi).addFileAttachment(
      eq("test-post-id"),
      eq("file.pdf"),
      any(InputStream.class)
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("test-post-id"),
        eq(NewsGroupPermissions.NWSPOST)
      )
    ).thenReturn(false);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenFormNotMultipart_thenReturnsError()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("test-post-id"),
        eq(NewsGroupPermissions.NWSPOST)
      )
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(false);
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNoFileField_thenReturnsError()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("test-post-id"),
        eq(NewsGroupPermissions.NWSPOST)
      )
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getIsFile()).thenReturn(false);
    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullForm_thenReturnsError() throws Exception {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("test-post-id"),
        eq(NewsGroupPermissions.NWSPOST)
      )
    ).thenReturn(true);

    when(req.parseContent()).thenReturn(null);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PostAttachmentFileAdd.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
