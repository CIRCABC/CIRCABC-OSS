package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.model.AppMessage;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AppMessageTemplatesPostTest {

  private AppMessageTemplatesPost webScript;
  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new AppMessageTemplatesPost();
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

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(
      "{\"content\":\"test message\",\"level\":\"info\",\"enabled\":true,\"displayTime\":10}"
    );
    when(req.getContent()).thenReturn(content);
    when(req.getParameter("notification")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenAddsTemplate()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).addAppMessageTemplate(any(AppMessage.class));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenAddsTemplate()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).addAppMessageTemplate(any(AppMessage.class));
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
    verify(appMessageApi, never()).addAppMessageTemplate(any());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenBadRequest()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenBadJson_thenBadRequest() throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("not valid json {{{");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNotificationTrueAndEnabled_thenNotifies()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(req.getParameter("notification")).thenReturn("true");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).addAppMessageTemplate(any(AppMessage.class));
    verify(appMessageApi).notifyTemplate(any(AppMessage.class));
  }

  @Test
  public void testExecuteImpl_whenNotificationTrueButDisabled_thenNoNotify()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(req.getParameter("notification")).thenReturn("true");

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(
      "{\"content\":\"test\",\"level\":\"info\",\"enabled\":false,\"displayTime\":10}"
    );
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).addAppMessageTemplate(any(AppMessage.class));
    verify(appMessageApi, never()).notifyTemplate(any(AppMessage.class));
  }

  @Test
  public void testExecuteImpl_whenRuntimeException_thenInternalServerError()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    doThrow(new RuntimeException("DB error"))
      .when(appMessageApi)
      .addAppMessageTemplate(any(AppMessage.class));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppMessageTemplatesPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
