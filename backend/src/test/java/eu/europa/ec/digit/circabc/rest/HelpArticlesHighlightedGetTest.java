package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpArticle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpArticlesHighlightedGetTest {

  private HelpArticlesHighlightedGet webscript;
  private HelpApi helpApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new HelpArticlesHighlightedGet();
    helpApi = mock(HelpApi.class);
    setField("helpApi", helpApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenNoLanguage_thenReturnsArticles() {
    when(req.getParameter("language")).thenReturn(null);
    List<HelpArticle> articles = new ArrayList<>();
    articles.add(new HelpArticle());
    when(helpApi.getHighlightedArticles()).thenReturn(articles);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(articles, result.get("articles"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsArticles() {
    when(req.getParameter("language")).thenReturn("fr");
    List<HelpArticle> articles = new ArrayList<>();
    when(helpApi.getHighlightedArticles()).thenReturn(articles);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(articles, result.get("articles"));
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(helpApi.getHighlightedArticles()).thenThrow(
      new RuntimeException("DB error")
    );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("Internal server error", status.getMessage());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpArticlesHighlightedGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
