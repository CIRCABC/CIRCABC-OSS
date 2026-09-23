package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpArticle;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpCategoryArticlesPostTest {

  private HelpCategoryArticlesPost helpCategoryArticlesPost;
  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    helpCategoryArticlesPost = new HelpCategoryArticlesPost();
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

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-1");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteImpl_whenAdminWithValidArticle_thenReturnsArticle()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn(null);

    JSONObject json = new JSONObject();
    json.put("id", "art-1");
    JSONObject titles = new JSONObject();
    titles.put("en", "Test Article");
    json.put("title", titles);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json.toJSONString());
    when(req.getContent()).thenReturn(content);

    HelpArticle article = new HelpArticle();
    article.setId("art-1");
    when(
      helpApi.createHelpArticle(eq("cat-1"), any(HelpArticle.class))
    ).thenReturn(article);

    Map<String, Object> model = helpCategoryArticlesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(article, model.get("article"));
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

    Map<String, Object> model = helpCategoryArticlesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  @SuppressWarnings("unchecked")
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

    HelpArticle article = new HelpArticle();
    when(
      helpApi.createHelpArticle(eq("cat-1"), any(HelpArticle.class))
    ).thenReturn(article);

    Map<String, Object> model = helpCategoryArticlesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(article, model.get("article"));
  }

  @Test
  public void testExecuteImpl_whenEmptyId_thenReturnsInternalServerError()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn(null);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    Map<String, Object> model = helpCategoryArticlesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsInternalServerError()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = helpCategoryArticlesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteImpl_whenLanguageParam_thenSetsLocale()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn("fr");

    JSONObject json = new JSONObject();
    json.put("id", "art-2");

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json.toJSONString());
    when(req.getContent()).thenReturn(content);

    HelpArticle article = new HelpArticle();
    when(
      helpApi.createHelpArticle(eq("cat-1"), any(HelpArticle.class))
    ).thenReturn(article);

    Map<String, Object> model = helpCategoryArticlesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(article, model.get("article"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpCategoryArticlesPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpCategoryArticlesPost, value);
  }
}
