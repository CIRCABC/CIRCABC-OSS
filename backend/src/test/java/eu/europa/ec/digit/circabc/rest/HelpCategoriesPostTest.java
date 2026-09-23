package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpCategory;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpCategoriesPostTest {

  private HelpCategoriesPost helpCategoriesPost;
  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    helpCategoriesPost = new HelpCategoriesPost();
    helpApi = mock(HelpApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("helpApi", helpApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteImpl_whenAdminWithValidCategory_thenReturnsCategory()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );

    HelpCategory category = new HelpCategory();
    category.setId("cat-1");

    JSONObject json = new JSONObject();
    json.put("id", "cat-1");
    JSONObject titles = new JSONObject();
    titles.put("en", "Test Category");
    json.put("title", titles);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json.toJSONString());
    when(req.getContent()).thenReturn(content);
    when(req.getParameter("language")).thenReturn(null);
    when(helpApi.createHelpCategory(any(HelpCategory.class))).thenReturn(
      category
    );

    Map<String, Object> model = helpCategoriesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(category, model.get("category"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> model = helpCategoriesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = helpCategoriesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteImpl_whenLanguageParam_thenSetsLocale()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );

    JSONObject json = new JSONObject();
    json.put("id", "cat-2");

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json.toJSONString());
    when(req.getContent()).thenReturn(content);
    when(req.getParameter("language")).thenReturn("fr");

    HelpCategory category = new HelpCategory();
    when(helpApi.createHelpCategory(any(HelpCategory.class))).thenReturn(
      category
    );

    Map<String, Object> model = helpCategoriesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(category, model.get("category"));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenAllowed() throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);

    JSONObject json = new JSONObject();
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json.toJSONString());
    when(req.getContent()).thenReturn(content);

    HelpCategory category = new HelpCategory();
    when(helpApi.createHelpCategory(any(HelpCategory.class))).thenReturn(
      category
    );

    Map<String, Object> model = helpCategoriesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(category, model.get("category"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpCategoriesPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpCategoriesPost, value);
  }
}
