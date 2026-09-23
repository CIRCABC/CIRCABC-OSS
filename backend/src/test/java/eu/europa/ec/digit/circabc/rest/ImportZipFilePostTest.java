package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

public class ImportZipFilePostTest {

  private ImportZipFilePost webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new ImportZipFilePost();
    groupsApi = mock(GroupsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ImportZipFilePost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String folderId) {
    Map<String, String> vars = new HashMap<>();
    vars.put("folderId", folderId);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  private void mockFormData(
    String fileName,
    String mimeType,
    InputStream inputStream
  ) {
    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getIsFile()).thenReturn(true);
    when(field.getFilename()).thenReturn(fileName);
    when(field.getMimetype()).thenReturn(mimeType);
    when(field.getInputStream()).thenReturn(inputStream);

    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });
    when(req.parseContent()).thenReturn(formData);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsOk()
    throws Exception {
    String folderId = "test-folder-id";
    mockTemplateVars(folderId);
    when(
      permissionChecker.hasAlfrescoAddChildrenPermission(folderId)
    ).thenReturn(true);
    when(req.getParameter("notifyUser")).thenReturn("true");
    when(req.getParameter("deleteFile")).thenReturn("false");
    when(req.getParameter("disableNotification")).thenReturn("false");
    when(req.getParameter("encoding")).thenReturn("UTF-8");

    InputStream is = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
    mockFormData("test.zip", "application/zip", is);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("ok", model.get("message"));
    verify(groupsApi).importZipFile(
      eq(folderId),
      eq(is),
      eq("test.zip"),
      eq("application/zip"),
      eq(true),
      eq(false),
      eq(false),
      eq("UTF-8")
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String folderId = "test-folder-id";
    mockTemplateVars(folderId);
    when(
      permissionChecker.hasAlfrescoAddChildrenPermission(folderId)
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidEncoding_thenReturnsError() {
    String folderId = "test-folder-id";
    mockTemplateVars(folderId);
    when(
      permissionChecker.hasAlfrescoAddChildrenPermission(folderId)
    ).thenReturn(true);
    when(req.getParameter("notifyUser")).thenReturn("false");
    when(req.getParameter("deleteFile")).thenReturn("false");
    when(req.getParameter("disableNotification")).thenReturn("false");
    when(req.getParameter("encoding")).thenReturn("ISO-8859-1");

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNotMultipart_thenReturnsError() {
    String folderId = "test-folder-id";
    mockTemplateVars(folderId);
    when(
      permissionChecker.hasAlfrescoAddChildrenPermission(folderId)
    ).thenReturn(true);
    when(req.getParameter("notifyUser")).thenReturn("false");
    when(req.getParameter("deleteFile")).thenReturn("false");
    when(req.getParameter("disableNotification")).thenReturn("false");
    when(req.getParameter("encoding")).thenReturn("CP437");
    when(req.parseContent()).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    String folderId = "test-folder-id";
    mockTemplateVars(folderId);
    when(
      permissionChecker.hasAlfrescoAddChildrenPermission(folderId)
    ).thenReturn(true);
    when(req.getParameter("notifyUser")).thenReturn("false");
    when(req.getParameter("deleteFile")).thenReturn("false");
    when(req.getParameter("disableNotification")).thenReturn("false");
    when(req.getParameter("encoding")).thenReturn("UTF-8");

    InputStream is = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
    mockFormData("test.zip", "application/zip", is);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      folderId
    );
    doThrow(new InvalidNodeRefException(nodeRef))
      .when(groupsApi)
      .importZipFile(
        anyString(),
        any(InputStream.class),
        anyString(),
        anyString(),
        anyBoolean(),
        anyBoolean(),
        anyBoolean(),
        anyString()
      );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
