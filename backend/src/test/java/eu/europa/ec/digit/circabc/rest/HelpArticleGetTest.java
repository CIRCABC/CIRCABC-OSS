package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpArticle;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpArticleGetTest {

  private HelpArticleGet helpArticleGet;
  private HelpApi helpApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    helpArticleGet = new HelpArticleGet();
    helpApi = mock(HelpApi.class);
    setField("helpApi", helpApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    templateVars = new HashMap<>();
    templateVars.put("id", "article-123");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidId_thenReturnsArticle() {
    when(req.getParameter("language")).thenReturn(null);
    HelpArticle article = new HelpArticle();
    when(helpApi.getHelpArticle("article-123")).thenReturn(article);

    Map<String, Object> result = helpArticleGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(article, result.get("article"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocaleAndReturnsArticle() {
    when(req.getParameter("language")).thenReturn("fr");
    HelpArticle article = new HelpArticle();
    when(helpApi.getHelpArticle("article-123")).thenReturn(article);

    Map<String, Object> result = helpArticleGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(article, result.get("article"));
  }

  @Test
  public void testExecuteImpl_whenEmptyId_thenReturnsBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    templateVars.put("id", "");

    Map<String, Object> result = helpArticleGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(helpApi.getHelpArticle("article-123")).thenThrow(
      new RuntimeException("DB error")
    );

    Map<String, Object> result = helpArticleGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("Internal server error", status.getMessage());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpArticleGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpArticleGet, value);
  }
}
