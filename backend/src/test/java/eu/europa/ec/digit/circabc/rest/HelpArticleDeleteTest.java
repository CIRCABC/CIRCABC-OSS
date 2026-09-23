package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.*;

public class HelpArticleDeleteTest {

  private HelpArticleDelete helpArticleDelete;
  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    helpArticleDelete = new HelpArticleDelete();
    helpApi = mock(HelpApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("helpApi", helpApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-article-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenAdmin_thenDeletesArticle() throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );

    Map<String, Object> result = helpArticleDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(helpApi).deleteHelpArticle("test-article-id");
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenDeletesArticle()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> result = helpArticleDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(helpApi).deleteHelpArticle("test-article-id");
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );

    Map<String, Object> result = helpArticleDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenEmptyId_thenBadRequest() throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    Map<String, Object> result = helpArticleDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenInternalServerError()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    doThrow(new RuntimeException("unexpected"))
      .when(helpApi)
      .deleteHelpArticle("test-article-id");

    Map<String, Object> result = helpArticleDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn("fr");

    Map<String, Object> result = helpArticleDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(helpApi).deleteHelpArticle("test-article-id");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpArticleDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpArticleDelete, value);
  }
}
