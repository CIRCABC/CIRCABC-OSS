package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpArticle;
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

public class HelpArticleHighlightPutTest {

  private HelpArticleHighlightPut webscript;
  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    webscript = new HelpArticleHighlightPut();
    helpApi = mock(HelpApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("helpApi", helpApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    templateVars = new HashMap<>();
    templateVars.put("id", "article-123");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenAdminAndValidId_thenReturnsArticle() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    HelpArticle article = new HelpArticle();
    when(helpApi.toggleHighlightArticle("article-123")).thenReturn(article);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(article, model.get("article"));
    verify(helpApi).toggleHighlightArticle("article-123");
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsArticle() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    HelpArticle article = new HelpArticle();
    when(helpApi.toggleHighlightArticle("article-123")).thenReturn(article);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(article, model.get("article"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenEmptyId_thenBadRequest() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    templateVars.put("id", "");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenHelpApiThrows_thenInternalServerError() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(helpApi.toggleHighlightArticle("article-123")).thenThrow(
      new RuntimeException("db error")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("language")).thenReturn("fr");
    HelpArticle article = new HelpArticle();
    when(helpApi.toggleHighlightArticle("article-123")).thenReturn(article);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(article, model.get("article"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpArticleHighlightPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
