package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import org.springframework.extensions.webscripts.servlet.FormData;

public class ContentTranslationsEnhancedPostTest {

  private ContentTranslationsEnhancedPost webScript;
  private ContentApi contentApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    webScript = new ContentTranslationsEnhancedPost();
    contentApi = mock(ContentApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("contentApi", contentApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "node-123");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenValidFileUpload_thenReturnsNodeRef()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);

    FormData formData = mockFormDataWithFile(100L);
    when(req.parseContent()).thenReturn(formData);

    NodeRef resultRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "result-id"
    );
    when(
      contentApi.createContentTranslation(
        eq("node-123"),
        eq("test.pdf"),
        any(),
        any(),
        eq(""),
        eq(""),
        eq(""),
        eq(""),
        any(String[].class),
        any(),
        eq("application/pdf"),
        any(InputStream.class),
        eq("fr"),
        any()
      )
    ).thenReturn(resultRef);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("result-id", result.get("nodeRef"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(false);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(formData.getFields()).thenReturn(new FormData.FormField[] {});
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenFormNotMultipart_thenReturnsError() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(false);
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenFormNull_thenReturnsError() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);

    when(req.parseContent()).thenReturn(null);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenFileTooLarge_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);

    long oversizedFile = 1024L * 1024 * 301;
    FormData formData = mockFormDataWithFile(oversizedFile);
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);

    FormData formData = mockFormDataWithFile(100L);
    when(req.parseContent()).thenReturn(formData);

    when(
      contentApi.createContentTranslation(
        eq("node-123"),
        anyString(),
        any(),
        any(),
        anyString(),
        anyString(),
        anyString(),
        anyString(),
        any(String[].class),
        any(),
        anyString(),
        any(InputStream.class),
        anyString(),
        any()
      )
    ).thenThrow(new InvalidNodeRefException("bad ref", null));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private FormData mockFormDataWithFile(long fileSize) throws Exception {
    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField fileField = mock(FormData.FormField.class);
    when(fileField.getName()).thenReturn("file");
    when(fileField.getIsFile()).thenReturn(true);
    when(fileField.getFilename()).thenReturn("test.pdf");
    when(fileField.getMimetype()).thenReturn("application/pdf");
    when(fileField.getInputStream()).thenReturn(
      new ByteArrayInputStream("file content".getBytes())
    );

    Content content = mock(Content.class);
    when(content.getSize()).thenReturn(fileSize);
    when(fileField.getContent()).thenReturn(content);

    FormData.FormField langField = mock(FormData.FormField.class);
    when(langField.getName()).thenReturn("lang");
    when(langField.getValue()).thenReturn("fr");
    when(langField.getIsFile()).thenReturn(false);

    when(formData.getFields()).thenReturn(
      new FormData.FormField[] { langField, fileField }
    );

    return formData;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentTranslationsEnhancedPost.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, field.getType() == value.getClass() ? value : value);
  }
}
