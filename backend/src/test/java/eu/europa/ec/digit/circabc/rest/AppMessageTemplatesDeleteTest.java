package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
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

public class AppMessageTemplatesDeleteTest {

  private AppMessageTemplatesDelete webScript;
  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    webScript = new AppMessageTemplatesDelete();
    appMessageApi = mock(AppMessageApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("appMessageApi", appMessageApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    templateVars = new HashMap<>();
    templateVars.put("id", "42");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenDeletesTemplate()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).deleteAppMessageTemplate(42);
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenDeletesTemplate()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).deleteAppMessageTemplate(42);
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIdIsNull_thenDeletesWithMinusOne()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );

    templateVars.clear();
    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).deleteAppMessageTemplate(-1);
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenInternalServerError()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    doThrow(new RuntimeException("DB error"))
      .when(appMessageApi)
      .deleteAppMessageTemplate(42);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppMessageTemplatesDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
