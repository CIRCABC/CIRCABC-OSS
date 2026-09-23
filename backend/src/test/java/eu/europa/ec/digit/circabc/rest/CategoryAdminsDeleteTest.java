package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
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

public class CategoryAdminsDeleteTest {

  private CategoryAdminsDelete categoryAdminsDelete;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryAdminsDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryAdminsDelete, value);
  }

  @Before
  public void setUp() throws Exception {
    categoryAdminsDelete = new CategoryAdminsDelete();
    categoriesApi = mock(CategoriesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("categoriesApi", categoriesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-id-123");
    templateVars.put("userId", "user1");

    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenSuccess() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> result = categoryAdminsDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(categoriesApi).categoriesIdAdminsDelete("cat-id-123", "user1");
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenSuccess() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(
      currentUserPermissionCheckerService.isCategoryAdmin("cat-id-123")
    ).thenReturn(true);

    Map<String, Object> result = categoryAdminsDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(categoriesApi).categoriesIdAdminsDelete("cat-id-123", "user1");
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(
      currentUserPermissionCheckerService.isCategoryAdmin("cat-id-123")
    ).thenReturn(false);

    Map<String, Object> result = categoryAdminsDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "cat-id-123")
      )
    )
      .when(categoriesApi)
      .categoriesIdAdminsDelete("cat-id-123", "user1");

    Map<String, Object> result = categoryAdminsDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenInternalServerError() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    doThrow(new RuntimeException("unexpected"))
      .when(categoriesApi)
      .categoriesIdAdminsDelete("cat-id-123", "user1");

    Map<String, Object> result = categoryAdminsDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    when(req.getParameter("language")).thenReturn("fr");
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> result = categoryAdminsDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(categoriesApi).categoriesIdAdminsDelete("cat-id-123", "user1");
  }
}
