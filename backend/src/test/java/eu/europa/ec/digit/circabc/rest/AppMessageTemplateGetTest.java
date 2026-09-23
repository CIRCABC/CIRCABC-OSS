package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.model.AppMessage;
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

public class AppMessageTemplateGetTest {

  private AppMessageTemplateGet webscript;
  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new AppMessageTemplateGet();
    appMessageApi = mock(AppMessageApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("appMessageApi", appMessageApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Match match = new Match("", Map.of("id", "42"), "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenReturnsMessage()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    AppMessage message = new AppMessage();
    when(appMessageApi.getAppMessageTemplate(42)).thenReturn(message);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertSame(message, model.get("message"));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsMessage()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    AppMessage message = new AppMessage();
    when(appMessageApi.getAppMessageTemplate(42)).thenReturn(message);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertSame(message, model.get("message"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsServerError()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(appMessageApi.getAppMessageTemplate(42)).thenThrow(
      new RuntimeException("db error")
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      AppMessageTemplateGet.class.getSuperclass().getDeclaredMethod(
        "executeImpl",
        WebScriptRequest.class,
        Status.class,
        Cache.class
      );
    method.setAccessible(true);
    return (Map<String, Object>) method.invoke(webscript, req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppMessageTemplateGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
