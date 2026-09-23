package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

public class CategoryLogosGetTest {

  private CategoryLogosGet categoryLogosGet;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    categoryLogosGet = new CategoryLogosGet();
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
    Field field = CategoryLogosGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryLogosGet, value);
  }

  private void mockTemplateVars(Map<String, String> vars) {
    Match match = new Match("", vars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenReturnsLogos() {
    String categoryId = "test-category-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);

    List<Node> logos = Collections.singletonList(new Node());
    when(categoriesApi.getCategoryLogoByCategoryId(categoryId)).thenReturn(
      logos
    );

    Map<String, Object> result = categoryLogosGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(logos, result.get("logos"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    String categoryId = "test-category-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);

    List<Node> logos = Collections.singletonList(new Node());
    when(categoriesApi.getCategoryLogoByCategoryId(categoryId)).thenReturn(
      logos
    );

    Map<String, Object> result = categoryLogosGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(logos, result.get("logos"));
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

    Map<String, Object> result = categoryLogosGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
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
    when(categoriesApi.getCategoryLogoByCategoryId(categoryId)).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, categoryId)
      )
    );

    Map<String, Object> result = categoryLogosGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenCategoryIdNull_thenReturnsEmptyModel() {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", null);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(currentUserPermissionCheckerService.isCategoryAdmin(null)).thenReturn(
      true
    );

    Map<String, Object> result = categoryLogosGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertNull(result.get("logos"));
  }
}
