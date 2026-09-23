package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import io.swagger.model.Node;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
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

public class ContentTranslationsPostTest {

  private ContentTranslationsPost webscript;
  private ContentApi contentApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new ContentTranslationsPost();
    contentApi = mock(ContentApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("contentApi", contentApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenValidMultipartWithLang_thenReturnsTranslationSet()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);

    InputStream fileStream = new ByteArrayInputStream("content".getBytes());
    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField fileField = mock(FormData.FormField.class);
    when(fileField.getIsFile()).thenReturn(true);
    when(fileField.getMimetype()).thenReturn("application/pdf");
    when(fileField.getFilename()).thenReturn("doc.pdf");
    when(fileField.getInputStream()).thenReturn(fileStream);
    when(fileField.getName()).thenReturn("file");

    FormData.FormField langField = mock(FormData.FormField.class);
    when(langField.getIsFile()).thenReturn(false);
    when(langField.getName()).thenReturn("lang");
    when(langField.getValue()).thenReturn("fr");

    when(formData.getFields()).thenReturn(
      new FormData.FormField[] { fileField, langField }
    );
    when(req.parseContent()).thenReturn(formData);

    Node node = new Node();
    when(
      contentApi.contentIdTranslationsPost(
        "test-node-id",
        "fr",
        fileStream,
        "application/pdf",
        "doc.pdf"
      )
    ).thenReturn(node);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertSame(node, model.get("translationSet"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(false);

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenFormIsNull_thenThrowsIllegalArgument()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(req.parseContent()).thenReturn(null);

    try {
      callExecuteImpl();
      fail("Expected IllegalArgumentException");
    } catch (Exception e) {
      assertTrue(
        e.getCause() instanceof IllegalArgumentException ||
          e instanceof IllegalArgumentException
      );
    }
  }

  @Test
  public void testExecuteImpl_whenLangEmpty_thenThrowsIllegalArgument()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField langField = mock(FormData.FormField.class);
    when(langField.getIsFile()).thenReturn(false);
    when(langField.getName()).thenReturn("lang");
    when(langField.getValue()).thenReturn("");

    when(formData.getFields()).thenReturn(
      new FormData.FormField[] { langField }
    );
    when(req.parseContent()).thenReturn(formData);

    try {
      callExecuteImpl();
      fail("Expected IllegalArgumentException");
    } catch (Exception e) {
      assertTrue(
        e.getCause() instanceof IllegalArgumentException ||
          e instanceof IllegalArgumentException
      );
    }
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      permissionChecker.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField langField = mock(FormData.FormField.class);
    when(langField.getIsFile()).thenReturn(false);
    when(langField.getName()).thenReturn("lang");
    when(langField.getValue()).thenReturn("de");

    when(formData.getFields()).thenReturn(
      new FormData.FormField[] { langField }
    );
    when(req.parseContent()).thenReturn(formData);

    when(
      contentApi.contentIdTranslationsPost(
        eq("test-node-id"),
        eq("de"),
        any(),
        eq(""),
        eq("translation")
      )
    ).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id")
      )
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method = org.springframework.extensions.webscripts
      .DeclarativeWebScript.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      webscript,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentTranslationsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
