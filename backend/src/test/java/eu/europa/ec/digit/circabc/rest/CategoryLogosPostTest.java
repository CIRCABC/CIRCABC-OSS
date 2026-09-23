package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

public class CategoryLogosPostTest {

  private CategoryLogosPost webScript;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new CategoryLogosPost();
    categoriesApi = mock(CategoriesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("categoriesApi", categoriesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryLogosPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(Map<String, String> vars) {
    Match match = new Match("", vars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenValidFileUpload_thenReturnsLogos()
    throws Exception {
    String categoryId = "test-category-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);

    InputStream inputStream = new ByteArrayInputStream("logo".getBytes());
    FormData formData = mock(FormData.class);
    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getIsFile()).thenReturn(true);
    when(field.getInputStream()).thenReturn(inputStream);
    when(field.getFilename()).thenReturn("logo.png");
    when(formData.getIsMultiPart()).thenReturn(true);
    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });
    when(req.parseContent()).thenReturn(formData);

    List<Node> logos = Collections.singletonList(new Node());
    when(categoriesApi.getCategoryLogoByCategoryId(categoryId)).thenReturn(
      logos
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(logos, result.get("logos"));
    verify(categoriesApi).postCategoryLogoByCategoryId(
      eq(categoryId),
      any(InputStream.class),
      eq("logo.png")
    );
  }

  @Test
  public void testExecuteImpl_whenCategoryIdNull_thenReturnsNullLogos() {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", null);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn("en");
    when(currentUserPermissionCheckerService.isCategoryAdmin(null)).thenReturn(
      true
    );

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(formData.getFields()).thenReturn(new FormData.FormField[] {});
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertNull(result.get("logos"));
  }

  @Test
  public void testExecuteImpl_whenNotCategoryAdmin_thenReturnsForbidden() {
    String categoryId = "restricted-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenFormDataNull_thenThrowsIllegalArgument() {
    String categoryId = "test-category-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);
    when(req.parseContent()).thenReturn(null);

    try {
      webScript.executeImpl(req, status, cache);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Not a multipart request.", e.getMessage());
    }
  }

  @Test
  public void testExecuteImpl_whenNotMultipart_thenThrowsIllegalArgument() {
    String categoryId = "test-category-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(false);
    when(req.parseContent()).thenReturn(formData);

    try {
      webScript.executeImpl(req, status, cache);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Not a multipart request.", e.getMessage());
    }
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    String categoryId = "invalid-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getIsFile()).thenReturn(true);
    when(field.getInputStream()).thenReturn(
      new ByteArrayInputStream("data".getBytes())
    );
    when(field.getFilename()).thenReturn("logo.png");
    when(formData.getIsMultiPart()).thenReturn(true);
    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });
    when(req.parseContent()).thenReturn(formData);

    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, categoryId)
      )
    )
      .when(categoriesApi)
      .postCategoryLogoByCategoryId(
        eq(categoryId),
        any(InputStream.class),
        eq("logo.png")
      );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
