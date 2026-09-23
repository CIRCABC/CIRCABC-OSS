package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpArticle;
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

public class HelpArticlePutTest {

  private HelpArticlePut webScript;
  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService permissionChecker;

  @Before
  public void setUp() throws Exception {
    webScript = new HelpArticlePut();
    helpApi = mock(HelpApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("helpApi", helpApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

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
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenUpdatesArticle()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);

    WebScriptRequest req = mockRequest("valid-id", null);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(helpApi).updateHelpArticle(eq("valid-id"), any(HelpArticle.class));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenUpdatesArticle()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);

    WebScriptRequest req = mockRequest("valid-id", null);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(helpApi).updateHelpArticle(eq("valid-id"), any(HelpArticle.class));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);

    WebScriptRequest req = mockRequest("valid-id", null);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(helpApi, never()).updateHelpArticle(
      anyString(),
      any(HelpArticle.class)
    );
  }

  @Test
  public void testExecuteImpl_whenEmptyId_thenReturnsBadRequest()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);

    WebScriptRequest req = mockRequest("", null);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    verify(helpApi, never()).updateHelpArticle(
      anyString(),
      any(HelpArticle.class)
    );
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);

    WebScriptRequest req = mockRequest("valid-id", "fr");

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(helpApi).updateHelpArticle(eq("valid-id"), any(HelpArticle.class));
  }

  @Test
  public void testExecuteImpl_whenHelpApiThrows_thenReturnsServerError()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    doThrow(new RuntimeException("DB error"))
      .when(helpApi)
      .updateHelpArticle(anyString(), any(HelpArticle.class));

    WebScriptRequest req = mockRequest("valid-id", null);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private WebScriptRequest mockRequest(String id, String language) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(language);

    Content content = mock(Content.class);
    try {
      when(content.getContent()).thenReturn(
        "{\"title\":{\"en\":\"Test\"},\"content\":{\"en\":\"Body\"}}"
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    when(req.getContent()).thenReturn(content);

    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpArticlePut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
