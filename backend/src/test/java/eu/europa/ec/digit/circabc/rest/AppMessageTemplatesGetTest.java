package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.model.PagedAppMessages;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AppMessageTemplatesGetTest {

  private AppMessageTemplatesGet webscript;
  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new AppMessageTemplatesGet();
    appMessageApi = mock(AppMessageApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("appMessageApi", appMessageApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenReturnsMessages()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("2");
    when(req.getParameter("limit")).thenReturn("10");

    PagedAppMessages messages = new PagedAppMessages();
    messages.setTotal(5L);
    when(appMessageApi.getAppMessageTemplates(2, 10)).thenReturn(messages);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertSame(messages, model.get("messages"));
    assertEquals(5L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsMessages()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);

    PagedAppMessages messages = new PagedAppMessages();
    messages.setTotal(0L);
    when(appMessageApi.getAppMessageTemplates(1, 25)).thenReturn(messages);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertSame(messages, model.get("messages"));
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
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("25");
    when(appMessageApi.getAppMessageTemplates(1, 25)).thenThrow(
      new RuntimeException("db error")
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidParams_thenUsesDefaults()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("abc");
    when(req.getParameter("limit")).thenReturn("");

    PagedAppMessages messages = new PagedAppMessages();
    messages.setTotal(3L);
    when(appMessageApi.getAppMessageTemplates(1, 25)).thenReturn(messages);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertSame(messages, model.get("messages"));
    verify(appMessageApi).getAppMessageTemplates(1, 25);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      AppMessageTemplatesGet.class.getSuperclass().getDeclaredMethod(
        "executeImpl",
        WebScriptRequest.class,
        Status.class,
        Cache.class
      );
    method.setAccessible(true);
    return (Map<String, Object>) method.invoke(webscript, req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppMessageTemplatesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
