package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.Category;
import io.swagger.model.InterestGroup;
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

public class CategoryGroupsGetTest {

  private CategoryGroupsGet categoryGroupsGet;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    categoryGroupsGet = new CategoryGroupsGet();
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
    Field field = CategoryGroupsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryGroupsGet, value);
  }

  private void mockTemplateVars(Map<String, String> vars) {
    Match match = new Match("", vars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenCategoryIdNull_thenReturnsCategories() {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", null);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);

    List<Category> categories = Collections.singletonList(new Category());
    when(categoriesApi.getCategories()).thenReturn(categories);

    Map<String, Object> result = categoryGroupsGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(categories, result.get("categories"));
  }

  @Test
  public void testExecuteImpl_whenCategoryIdProvided_thenReturnsGroups() {
    String categoryId = "test-category-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn("en");
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(categoryId)
    ).thenReturn(true);

    List<InterestGroup> groups = Collections.singletonList(new InterestGroup());
    when(categoriesApi.getInterestGroupByCategoryId(categoryId)).thenReturn(
      groups
    );

    Map<String, Object> result = categoryGroupsGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(categoryId, result.get("id"));
    assertEquals(groups, result.get("groups"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String categoryId = "restricted-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(categoryId)
    ).thenReturn(false);

    Map<String, Object> result = categoryGroupsGet.executeImpl(
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
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(categoryId)
    ).thenReturn(true);
    when(categoriesApi.getInterestGroupByCategoryId(categoryId)).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, categoryId)
      )
    );

    Map<String, Object> result = categoryGroupsGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError() {
    String categoryId = "error-id";
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    mockTemplateVars(vars);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(categoryId)
    ).thenReturn(true);
    when(categoriesApi.getInterestGroupByCategoryId(categoryId)).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> result = categoryGroupsGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
