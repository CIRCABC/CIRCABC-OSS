package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpCategory;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpCategoryPutTest {

  private HelpCategoryPut webScript;
  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new HelpCategoryPut();
    helpApi = mock(HelpApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("helpApi", helpApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    mockTemplateVars("cat-123");
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteImpl_whenAdminUpdatesCategory_thenReturnsUpdatedCategory()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);

    JSONObject json = new JSONObject();
    json.put("id", "cat-123");
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json.toJSONString());
    when(req.getContent()).thenReturn(content);

    HelpCategory updated = new HelpCategory();
    updated.setId("cat-123");
    when(
      helpApi.updateHelpCategory(eq("cat-123"), any(HelpCategory.class))
    ).thenReturn(updated);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(updated, result.get("category"));
    verify(helpApi).updateHelpCategory(eq("cat-123"), any(HelpCategory.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteImpl_whenCircabcAdmin_thenAllowed() throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);

    JSONObject json = new JSONObject();
    json.put("id", "cat-123");
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json.toJSONString());
    when(req.getContent()).thenReturn(content);

    HelpCategory updated = new HelpCategory();
    when(
      helpApi.updateHelpCategory(eq("cat-123"), any(HelpCategory.class))
    ).thenReturn(updated);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(updated, result.get("category"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(helpApi, never()).updateHelpCategory(
      anyString(),
      any(HelpCategory.class)
    );
  }

  @Test(expected = InvalidArgumentException.class)
  public void testExecuteImpl_whenEmptyId_thenThrowsInvalidArgument()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    mockTemplateVars("");

    webScript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteImpl_whenLanguageParam_thenSetsLocale()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("language")).thenReturn("fr");

    JSONObject json = new JSONObject();
    json.put("id", "cat-123");
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json.toJSONString());
    when(req.getContent()).thenReturn(content);

    HelpCategory updated = new HelpCategory();
    when(
      helpApi.updateHelpCategory(eq("cat-123"), any(HelpCategory.class))
    ).thenReturn(updated);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(updated, result.get("category"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpCategoryPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }
}
