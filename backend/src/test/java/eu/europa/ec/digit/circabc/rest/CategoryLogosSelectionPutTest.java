package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
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

public class CategoryLogosSelectionPutTest {

  private CategoryLogosSelectionPut webScript;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new CategoryLogosSelectionPut();
    categoriesApi = mock(CategoriesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("categoriesApi", categoriesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-id");
    templateVars.put("logoId", "logo-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenSelectsLogo()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isCategoryAdmin("cat-id")).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(categoriesApi).selectCategoryLogoByLogoId("cat-id", "logo-id");
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSelectsLogo()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(permissionChecker.isCategoryAdmin("cat-id")).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(categoriesApi).selectCategoryLogoByLogoId("cat-id", "logo-id");
  }

  @Test
  public void testExecuteImpl_whenNotCategoryAdmin_thenForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isCategoryAdmin("cat-id")).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isCategoryAdmin("cat-id")).thenReturn(true);
    doThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(
        "bad",
        null
      )
    )
      .when(categoriesApi)
      .selectCategoryLogoByLogoId("cat-id", "logo-id");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryLogosSelectionPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
