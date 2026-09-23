package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

public class NodesContentUploadPostTest {

  private NodesContentUploadPost webscript;
  private CurrentUserPermissionCheckerService permissionChecker;
  private ContentApi contentApi;
  private ActionService actionService;
  private WebScriptRequest req;
  private Status status;

  @Before
  public void setUp() throws Exception {
    webscript = new NodesContentUploadPost();
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    contentApi = mock(ContentApi.class);
    actionService = mock(ActionService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();

    setField("currentUserPermissionCheckerService", permissionChecker);
    setField("contentApi", contentApi);
    setField("actionService", actionService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesContentUploadPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }

  @Test
  public void testExecuteImpl_whenValidUpload_thenReturnsNodeRef()
    throws Exception {
    String parentId = "parent-node-id";
    setupRequest(parentId);
    when(
      permissionChecker.verifyMemberPermission(eq(parentId), anyString())
    ).thenReturn(true);

    FormData formData = mockFormDataWithFile(1024L);
    when(req.parseContent()).thenReturn(formData);

    NodeRef createdRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "created-id"
    );
    when(
      contentApi.createContent(
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(String[].class),
        any(),
        any(),
        any(),
        anyBoolean(),
        any(),
        any()
      )
    ).thenReturn(createdRef);

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNotNull(result);
    assertEquals("created-id", result.get("nodeRef"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden() {
    String parentId = "parent-node-id";
    setupRequest(parentId);
    when(
      permissionChecker.verifyMemberPermission(eq(parentId), anyString())
    ).thenReturn(false);

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNotMultipart_thenInternalServerError() {
    String parentId = "parent-node-id";
    setupRequest(parentId);
    when(
      permissionChecker.verifyMemberPermission(eq(parentId), anyString())
    ).thenReturn(true);
    when(req.parseContent()).thenReturn(null);

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenFileTooLarge_thenBadRequest() {
    String parentId = "parent-node-id";
    setupRequest(parentId);
    when(
      permissionChecker.verifyMemberPermission(eq(parentId), anyString())
    ).thenReturn(true);

    long tooLarge = 1024L * 1024L * 301L;
    FormData formData = mockFormDataWithFile(tooLarge);
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenCustomName_thenUsesCustomName()
    throws Exception {
    String parentId = "parent-node-id";
    setupRequest(parentId);
    when(
      permissionChecker.verifyMemberPermission(eq(parentId), anyString())
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField nameField = mockTextField("name", "custom-name.pdf");
    FormData.FormField fileField = mockFileField(
      "file",
      "original.pdf",
      "application/pdf",
      512L
    );

    when(formData.getFields()).thenReturn(
      new FormData.FormField[] { nameField, fileField }
    );
    when(req.parseContent()).thenReturn(formData);

    NodeRef createdRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-id"
    );
    when(
      contentApi.createContent(
        eq(parentId),
        eq("custom-name.pdf"),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(String[].class),
        any(),
        any(),
        any(),
        anyBoolean(),
        any(),
        any()
      )
    ).thenReturn(createdRef);

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNotNull(result);
    assertEquals("new-id", result.get("nodeRef"));
    verify(contentApi).createContent(
      eq(parentId),
      eq("custom-name.pdf"),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any(String[].class),
      any(),
      any(),
      any(),
      anyBoolean(),
      any(),
      any()
    );
  }

  @Test
  public void testExecuteImpl_whenContentApiThrows_thenInternalServerError()
    throws Exception {
    String parentId = "parent-node-id";
    setupRequest(parentId);
    when(
      permissionChecker.verifyMemberPermission(eq(parentId), anyString())
    ).thenReturn(true);

    FormData formData = mockFormDataWithFile(1024L);
    when(req.parseContent()).thenReturn(formData);

    when(
      contentApi.createContent(
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(String[].class),
        any(),
        any(),
        any(),
        anyBoolean(),
        any(),
        any()
      )
    ).thenThrow(new RuntimeException("Repo error"));

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setupRequest(String parentId) {
    when(req.getParameter("language")).thenReturn("en");
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", parentId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private FormData mockFormDataWithFile(long fileSize) {
    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField fileField = mockFileField(
      "file",
      "test.pdf",
      "application/pdf",
      fileSize
    );

    when(formData.getFields()).thenReturn(
      new FormData.FormField[] { fileField }
    );
    return formData;
  }

  private FormData.FormField mockFileField(
    String name,
    String filename,
    String mimetype,
    long size
  ) {
    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getName()).thenReturn(name);
    when(field.getIsFile()).thenReturn(true);
    when(field.getFilename()).thenReturn(filename);
    when(field.getMimetype()).thenReturn(mimetype);
    when(field.getInputStream()).thenReturn(
      new ByteArrayInputStream("file data".getBytes())
    );

    Content content = mock(Content.class);
    when(content.getSize()).thenReturn(size);
    when(field.getContent()).thenReturn(content);

    return field;
  }

  private FormData.FormField mockTextField(String name, String value) {
    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getName()).thenReturn(name);
    when(field.getValue()).thenReturn(value);
    when(field.getIsFile()).thenReturn(false);
    return field;
  }
}
