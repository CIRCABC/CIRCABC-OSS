package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
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

public class CategoryAdminsPostTest {

  private CategoryAdminsPost categoryAdminsPost;
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

    categoryAdminsPost = new CategoryAdminsPost();
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
    Field field = CategoryAdminsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryAdminsPost, value);
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenSuccess() throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("[\"user1\",\"user2\"]");

    Map<String, Object> result = categoryAdminsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(categoriesApi).categoriesIdAdminsPost(eq("cat-id-123"), anyList());
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenSuccess() throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(true);
    when(req.getParameter("language")).thenReturn("en");

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("[\"user1\"]");

    Map<String, Object> result = categoryAdminsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(categoriesApi).categoriesIdAdminsPost(eq("cat-id-123"), anyList());
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isCategoryAdmin("cat-id-123")).thenReturn(false);
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> result = categoryAdminsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenBadRequest()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("not valid json");

    Map<String, Object> result = categoryAdminsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenInternalError()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("[\"user1\"]");
    doThrow(new RuntimeException("unexpected"))
      .when(categoriesApi)
      .categoriesIdAdminsPost(anyString(), anyList());

    Map<String, Object> result = categoryAdminsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
