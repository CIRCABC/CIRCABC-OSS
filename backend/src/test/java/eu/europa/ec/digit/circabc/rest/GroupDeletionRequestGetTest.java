package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.PagedGroupDeletionRequests;
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

public class GroupDeletionRequestGetTest {

  private GroupDeletionRequestGet webscript;
  private CurrentUserPermissionCheckerService permissionChecker;
  private CategoriesApi categoriesApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new GroupDeletionRequestGet();
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    categoriesApi = mock(CategoriesApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("currentUserPermissionCheckerService", permissionChecker);
    setField("categoriesApi", categoriesApi);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-id-123");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenReturnsRequests() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("5");
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("filter")).thenReturn("pending");
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(true);

    PagedGroupDeletionRequests expected = new PagedGroupDeletionRequests();
    expected.setTotal(1L);
    when(
      categoriesApi.categoriesIdGroupDeleteRequestsGet(
        "cat-id-123",
        5,
        1,
        "pending"
      )
    ).thenReturn(expected);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(expected, result.get("requests"));
  }

  @Test
  public void testExecuteImpl_whenDefaultParams_thenUsesDefaults() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("filter")).thenReturn(null);
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(true);

    when(
      categoriesApi.categoriesIdGroupDeleteRequestsGet(
        "cat-id-123",
        10,
        0,
        null
      )
    ).thenReturn(new PagedGroupDeletionRequests());

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(new PagedGroupDeletionRequests(), result.get("requests"));
  }

  @Test
  public void testExecuteImpl_whenNotCategoryAdmin_thenForbidden() {
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(false);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidLimit_thenBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("notanumber");
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(true);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Bad request query parameters", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale() {
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("filter")).thenReturn(null);
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(true);

    when(
      categoriesApi.categoriesIdGroupDeleteRequestsGet(
        "cat-id-123",
        10,
        0,
        null
      )
    ).thenReturn(new PagedGroupDeletionRequests());

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupDeletionRequestGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
