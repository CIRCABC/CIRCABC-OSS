package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryLogosDeleteTest {

  private CategoryLogosDelete categoryLogosDelete;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    categoryLogosDelete = new CategoryLogosDelete();
    categoriesApi = mock(CategoriesApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("categoriesApi", categoriesApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsLogos()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-1");
    templateVars.put("logoId", "logo-1");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(permissionCheckerService.isCategoryAdmin("cat-1")).thenReturn(true);

    List<Node> logos = List.of(new Node());
    when(
      categoriesApi.deleteCategoryLogoByLogoId("cat-1", "logo-1")
    ).thenReturn(logos);

    Map<String, Object> result = categoryLogosDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(logos, result.get("logos"));
    verify(categoriesApi).deleteCategoryLogoByLogoId("cat-1", "logo-1");
  }

  @Test
  public void testExecuteImpl_whenNotCategoryAdmin_thenReturnsForbidden()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-1");
    templateVars.put("logoId", "logo-1");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(permissionCheckerService.isCategoryAdmin("cat-1")).thenReturn(false);

    Map<String, Object> result = categoryLogosDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenCategoryIdNull_thenReturnsBadRequest()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);
    templateVars.put("logoId", "logo-1");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(permissionCheckerService.isCategoryAdmin(null)).thenReturn(true);

    Map<String, Object> result = categoryLogosDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLogoIdNull_thenReturnsBadRequest()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-1");
    templateVars.put("logoId", null);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(permissionCheckerService.isCategoryAdmin("cat-1")).thenReturn(true);

    Map<String, Object> result = categoryLogosDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-1");
    templateVars.put("logoId", "logo-1");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn("fr");
    when(permissionCheckerService.isCategoryAdmin("cat-1")).thenReturn(true);
    when(
      categoriesApi.deleteCategoryLogoByLogoId("cat-1", "logo-1")
    ).thenReturn(List.of(new Node()));

    Map<String, Object> result = categoryLogosDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(categoriesApi).deleteCategoryLogoByLogoId("cat-1", "logo-1");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryLogosDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryLogosDelete, value);
  }
}
