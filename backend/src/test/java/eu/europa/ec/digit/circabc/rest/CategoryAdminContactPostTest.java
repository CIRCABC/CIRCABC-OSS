package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.AdminContactRequest;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryAdminContactPostTest {

  private CategoryAdminContactPost webscript;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new CategoryAdminContactPost();
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
    templateVars.put("id", "cat-123");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenGuestUser_thenForbidden() {
    when(req.getParameter("language")).thenReturn(null);
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(true);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenSuccess() throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    org.springframework.extensions.surf.util.Content content = mock(
      org.springframework.extensions.surf.util.Content.class
    );
    when(content.getContent()).thenReturn(
      "{\"content\":\"hello\",\"sendCopy\":true}"
    );
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(Status.STATUS_OK, status.getCode());
    verify(categoriesApi).categoriesIdAdminContactPost(
      eq("cat-123"),
      any(AdminContactRequest.class)
    );
  }

  @Test
  public void testExecuteImpl_whenIOException_thenBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    org.springframework.extensions.surf.util.Content content = mock(
      org.springframework.extensions.surf.util.Content.class
    );
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenInternalServerError()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    org.springframework.extensions.surf.util.Content content = mock(
      org.springframework.extensions.surf.util.Content.class
    );
    when(content.getContent()).thenThrow(new RuntimeException("unexpected"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    org.springframework.extensions.surf.util.Content content = mock(
      org.springframework.extensions.surf.util.Content.class
    );
    when(content.getContent()).thenReturn(
      "{\"content\":\"bonjour\",\"sendCopy\":false}"
    );
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(categoriesApi).categoriesIdAdminContactPost(
      eq("cat-123"),
      any(AdminContactRequest.class)
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryAdminContactPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
