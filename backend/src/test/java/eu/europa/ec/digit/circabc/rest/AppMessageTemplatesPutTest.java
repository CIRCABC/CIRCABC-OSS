package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.model.AppMessage;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AppMessageTemplatesPutTest {

  private AppMessageTemplatesPut webScript;
  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Content content;

  @Before
  public void setUp() throws Exception {
    webScript = new AppMessageTemplatesPut();
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
    content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenUpdatesTemplate()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(content.getContent()).thenReturn(
      "{\"id\":1,\"content\":\"test\",\"level\":\"info\",\"enabled\":true,\"displayTime\":10}"
    );
    when(req.getParameter("notification")).thenReturn(null);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).updateAppMessageTemplate(any(AppMessage.class));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenUpdatesTemplate()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    when(content.getContent()).thenReturn(
      "{\"id\":2,\"content\":\"msg\",\"level\":\"warning\",\"enabled\":false,\"displayTime\":5}"
    );
    when(req.getParameter("notification")).thenReturn(null);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).updateAppMessageTemplate(any(AppMessage.class));
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
    verify(appMessageApi, never()).updateAppMessageTemplate(
      any(AppMessage.class)
    );
  }

  @Test
  public void testExecuteImpl_whenNotificationTrueAndEnabled_thenNotifies()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(content.getContent()).thenReturn(
      "{\"id\":1,\"content\":\"test\",\"level\":\"info\",\"enabled\":true,\"displayTime\":10}"
    );
    when(req.getParameter("notification")).thenReturn("true");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).updateAppMessageTemplate(any(AppMessage.class));
    verify(appMessageApi).notifyTemplate(any(AppMessage.class));
  }

  @Test
  public void testExecuteImpl_whenNotificationTrueButDisabled_thenNoNotify()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(content.getContent()).thenReturn(
      "{\"id\":1,\"content\":\"test\",\"level\":\"info\",\"enabled\":false,\"displayTime\":10}"
    );
    when(req.getParameter("notification")).thenReturn("true");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).updateAppMessageTemplate(any(AppMessage.class));
    verify(appMessageApi, never()).notifyTemplate(any(AppMessage.class));
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenInternalServerError()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(content.getContent()).thenReturn(
      "{\"id\":1,\"content\":\"test\",\"level\":\"info\",\"enabled\":true,\"displayTime\":10}"
    );
    when(req.getParameter("notification")).thenReturn(null);
    doThrow(new RuntimeException("DB error"))
      .when(appMessageApi)
      .updateAppMessageTemplate(any(AppMessage.class));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenBadJson_thenBadRequest() throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(content.getContent()).thenReturn("not valid json{{{");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppMessageTemplatesPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
