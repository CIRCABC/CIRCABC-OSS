package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryRequestGroupPutTest {

  private CategoryRequestGroupPut categoryRequestGroupPut;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Content content;

  @Before
  public void setUp() throws Exception {
    categoryRequestGroupPut = new CategoryRequestGroupPut();
    categoriesApi = mock(CategoriesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    content = mock(Content.class);
    status = new Status();
    cache = new Cache();

    setField("categoriesApi", categoriesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryRequestGroupPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryRequestGroupPut, value);
  }

  private void mockTemplateVars(String categoryId, String requestId) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    vars.put("requestId", requestId);
    Match match = new Match("", vars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  private void mockRequestBody(String json) throws IOException {
    when(content.getContent()).thenReturn(json);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenUpdatesRequest()
    throws Exception {
    String categoryId = "cat-id";
    String requestId = "req-id";
    mockTemplateVars(categoryId, requestId);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);

    String json =
      "{\"id\":1,\"proposedName\":\"TestGroup\",\"categoryRef\":\"cat-ref\",\"justification\":\"reason\"}";
    mockRequestBody(json);

    Map<String, Object> result = categoryRequestGroupPut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(categoriesApi).categoriesGroupRequestPut(
      eq(categoryId),
      eq(requestId),
      any()
    );
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    String categoryId = "cat-id";
    String requestId = "req-id";
    mockTemplateVars(categoryId, requestId);
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);

    String json =
      "{\"id\":2,\"proposedName\":\"Groupe\",\"categoryRef\":\"cat-ref\",\"justification\":\"raison\"}";
    mockRequestBody(json);

    Map<String, Object> result = categoryRequestGroupPut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(categoriesApi).categoriesGroupRequestPut(
      eq(categoryId),
      eq(requestId),
      any()
    );
  }

  @Test
  public void testExecuteImpl_whenNotCategoryAdmin_thenReturnsForbidden()
    throws Exception {
    String categoryId = "cat-id";
    String requestId = "req-id";
    mockTemplateVars(categoryId, requestId);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(false);

    Map<String, Object> result = categoryRequestGroupPut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(categoriesApi, never()).categoriesGroupRequestPut(
      anyString(),
      anyString(),
      any()
    );
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    String categoryId = "cat-id";
    String requestId = "req-id";
    mockTemplateVars(categoryId, requestId);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new IOException("read error"));

    Map<String, Object> result = categoryRequestGroupPut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsBadRequest()
    throws Exception {
    String categoryId = "cat-id";
    String requestId = "req-id";
    mockTemplateVars(categoryId, requestId);
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);
    mockRequestBody("not valid json");

    Map<String, Object> result = categoryRequestGroupPut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
