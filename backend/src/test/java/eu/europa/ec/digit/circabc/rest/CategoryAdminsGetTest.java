package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryAdminsGetTest {

  private CategoryAdminsGet categoryAdminsGet;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;

  @Before
  public void setUp() throws Exception {
    categoryAdminsGet = new CategoryAdminsGet();
    categoriesApi = mock(CategoriesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();

    setField("categoriesApi", categoriesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-id-123");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsAdmins()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    User user = new User();
    user.setUserId("admin1");
    when(categoriesApi.categoriesIdAdminsGet("cat-id-123")).thenReturn(
      List.of(user)
    );

    Map<String, Object> model = categoryAdminsGet.executeImpl(
      req,
      status,
      null
    );

    assertNotNull(model);
    List<?> admins = (List<?>) model.get("admins");
    assertEquals(1, admins.size());
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenReturnsAdmins()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(true);
    when(categoriesApi.categoriesIdAdminsGet("cat-id-123")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = categoryAdminsGet.executeImpl(
      req,
      status,
      null
    );

    assertNotNull(model);
    List<?> admins = (List<?>) model.get("admins");
    assertTrue(admins.isEmpty());
  }

  @Test
  public void testExecuteImpl_whenNotAllowed_thenReturnsForbidden()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(false);

    Map<String, Object> model = categoryAdminsGet.executeImpl(
      req,
      status,
      null
    );

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(categoriesApi.categoriesIdAdminsGet("cat-id-123")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = categoryAdminsGet.executeImpl(
      req,
      status,
      null
    );

    assertNotNull(model);
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsServerError()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(categoriesApi.categoriesIdAdminsGet("cat-id-123")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = categoryAdminsGet.executeImpl(
      req,
      status,
      null
    );

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryAdminsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryAdminsGet, value);
  }
}
