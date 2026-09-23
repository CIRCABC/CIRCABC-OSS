package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.Category;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryPostTest {

  private CategoryPost categoryPost;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Content content;

  @Before
  public void setUp() throws Exception {
    categoryPost = new CategoryPost();
    categoriesApi = mock(CategoriesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
    content = mock(Content.class);

    setField("categoriesApi", categoriesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    when(req.getContent()).thenReturn(content);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryPost, value);
  }

  private void mockTemplateVars(Map<String, String> vars) {
    Match match = new Match("", vars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsCategory()
    throws IOException {
    String headerId = "header-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", headerId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(content.getContent()).thenReturn(
      "{\"name\":\"TestCat\",\"title\":{\"en\":\"English Title\"}}"
    );

    Category expected = new Category("cat-id", "TestCat");
    when(
      categoriesApi.headersIdCategoryPost(eq(headerId), any(Category.class))
    ).thenReturn(expected);

    Map<String, Object> result = categoryPost.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(expected, result.get("category"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsCategory()
    throws IOException {
    String headerId = "header-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", headerId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn("fr");
    when(content.getContent()).thenReturn("{\"name\":\"CatFR\"}");

    Category expected = new Category("cat-id", "CatFR");
    when(
      categoriesApi.headersIdCategoryPost(eq(headerId), any(Category.class))
    ).thenReturn(expected);

    Map<String, Object> result = categoryPost.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(expected, result.get("category"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", "header-id");
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.throwIfNotCircabcAdmin()
    ).thenThrow(new AccessDeniedException("not admin"));

    Map<String, Object> result = categoryPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws IOException {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", "header-id");
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(content.getContent()).thenThrow(new IOException("read error"));

    Map<String, Object> result = categoryPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError()
    throws IOException {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", "header-id");
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(content.getContent()).thenReturn("{\"name\":\"Test\"}");
    when(
      categoriesApi.headersIdCategoryPost(anyString(), any(Category.class))
    ).thenThrow(new RuntimeException("unexpected"));

    Map<String, Object> result = categoryPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
