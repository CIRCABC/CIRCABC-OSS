package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.PagedGroupCreationRequests;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryRequestGroupsGetTest {

  private CategoryRequestGroupsGet webscript;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new CategoryRequestGroupsGet();
    categoriesApi = mock(CategoriesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("categoriesApi", categoriesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-id-123");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenReturnsRequests() {
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("5");
    when(req.getParameter("page")).thenReturn("2");
    when(req.getParameter("filter")).thenReturn("pending");

    PagedGroupCreationRequests expected = new PagedGroupCreationRequests();
    expected.setTotal(10L);
    when(
      categoriesApi.categoriesIdGroupRequestsGet("cat-id-123", 5, 2, "pending")
    ).thenReturn(expected);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(expected, model.get("requests"));
  }

  @Test
  public void testExecuteImpl_whenDefaultParams_thenUsesDefaults() {
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("filter")).thenReturn(null);

    PagedGroupCreationRequests expected = new PagedGroupCreationRequests();
    when(
      categoriesApi.categoriesIdGroupRequestsGet("cat-id-123", 10, 0, null)
    ).thenReturn(expected);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(expected, model.get("requests"));
  }

  @Test
  public void testExecuteImpl_whenNotCategoryAdmin_thenForbidden() {
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(false);
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidLimit_thenBadRequest() {
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("notanumber");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Bad request query parameters", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale() {
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(true);
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("filter")).thenReturn(null);

    PagedGroupCreationRequests expected = new PagedGroupCreationRequests();
    when(
      categoriesApi.categoriesIdGroupRequestsGet("cat-id-123", 10, 0, null)
    ).thenReturn(expected);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(expected, model.get("requests"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryRequestGroupsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
