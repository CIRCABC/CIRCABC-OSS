package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpArticle;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpCategoryArticlesGetTest {

  private HelpCategoryArticlesGet helpCategoryArticlesGet;
  private HelpApi helpApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    helpCategoryArticlesGet = new HelpCategoryArticlesGet();
    helpApi = mock(HelpApi.class);
    setField("helpApi", helpApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    templateVars = new HashMap<>();
    templateVars.put("id", "category-123");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidId_thenReturnsArticles() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("skipcontent")).thenReturn(null);
    List<HelpArticle> articles = List.of(new HelpArticle());
    when(helpApi.getCategoryArticles("category-123", true)).thenReturn(
      articles
    );

    Map<String, Object> result = helpCategoryArticlesGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertSame(articles, result.get("articles"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("skipcontent")).thenReturn(null);
    List<HelpArticle> articles = Collections.emptyList();
    when(helpApi.getCategoryArticles("category-123", true)).thenReturn(
      articles
    );

    Map<String, Object> result = helpCategoryArticlesGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertSame(articles, result.get("articles"));
  }

  @Test
  public void testExecuteImpl_whenSkipContentTrue_thenLoadContentFalse() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("skipcontent")).thenReturn("true");
    List<HelpArticle> articles = Collections.emptyList();
    when(helpApi.getCategoryArticles("category-123", false)).thenReturn(
      articles
    );

    Map<String, Object> result = helpCategoryArticlesGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(helpApi).getCategoryArticles("category-123", false);
  }

  @Test
  public void testExecuteImpl_whenEmptyId_thenReturnsBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    templateVars.put("id", "");

    Map<String, Object> result = helpCategoryArticlesGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Help category ID cannot be empty", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("skipcontent")).thenReturn(null);
    when(helpApi.getCategoryArticles("category-123", true)).thenThrow(
      new RuntimeException("DB error")
    );

    Map<String, Object> result = helpCategoryArticlesGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("Internal error", status.getMessage());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpCategoryArticlesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpCategoryArticlesGet, value);
  }
}
