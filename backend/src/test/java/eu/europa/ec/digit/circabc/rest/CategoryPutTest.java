package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.Category;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryPutTest {

  private CategoryPut categoryPut;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    categoryPut = new CategoryPut();
    categoriesApi = mock(CategoriesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("categoriesApi", categoriesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-id-123");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryPut, value);
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenSuccess() throws Exception {
    when(permissionChecker.throwIfNotCategoryAdmin("cat-id-123")).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"name\":\"Test\",\"id\":\"cat-id-123\"}"
    );

    Category updatedCategory = new Category();
    updatedCategory.setName("Test");
    when(
      categoriesApi.categoriesIdPut(eq("cat-id-123"), any(Category.class))
    ).thenReturn(updatedCategory);

    Map<String, Object> result = categoryPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(updatedCategory, result.get("category"));
    verify(categoriesApi).categoriesIdPut(
      eq("cat-id-123"),
      any(Category.class)
    );
  }

  @Test
  public void testExecuteImpl_whenLanguageSpecified_thenSuccess()
    throws Exception {
    when(permissionChecker.throwIfNotCategoryAdmin("cat-id-123")).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn("fr");

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"name\":\"Test\",\"id\":\"cat-id-123\"}"
    );

    Category updatedCategory = new Category();
    when(
      categoriesApi.categoriesIdPut(eq("cat-id-123"), any(Category.class))
    ).thenReturn(updatedCategory);

    Map<String, Object> result = categoryPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(updatedCategory, result.get("category"));
  }

  @Test
  public void testExecuteImpl_whenNotCategoryAdmin_thenForbidden()
    throws Exception {
    when(permissionChecker.throwIfNotCategoryAdmin("cat-id-123")).thenReturn(
      false
    );
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> result = categoryPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenBadRequest()
    throws Exception {
    when(permissionChecker.throwIfNotCategoryAdmin("cat-id-123")).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("not valid json");

    Map<String, Object> result = categoryPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenBadRequest()
    throws Exception {
    when(permissionChecker.throwIfNotCategoryAdmin("cat-id-123")).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new java.io.IOException("read error"));

    Map<String, Object> result = categoryPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
