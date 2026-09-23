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
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpCategoryDeleteTest {

  private HelpCategoryDelete webScript;
  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new HelpCategoryDelete();
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
  public void testExecuteImpl_whenAdminDeletesCategory_thenSuccess()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(helpApi).deleteHelpCategory("cat-123");
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenSuccess() throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(helpApi).deleteHelpCategory("cat-123");
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(helpApi, never()).deleteHelpCategory(anyString());
  }

  @Test
  public void testExecuteImpl_whenEmptyId_thenInternalServerError()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);

    mockTemplateVars("");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    verify(helpApi, never()).deleteHelpCategory(anyString());
  }

  @Test
  public void testExecuteImpl_whenHelpApiThrows_thenInternalServerError()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    doThrow(new RuntimeException("db error"))
      .when(helpApi)
      .deleteHelpCategory("cat-123");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpCategoryDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }
}
