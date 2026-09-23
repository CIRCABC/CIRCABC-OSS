package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.model.AppMessage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AppMessagesGetTest {

  private AppMessagesGet webscript;
  private AppMessageApi appMessageApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new AppMessagesGet();
    appMessageApi = mock(AppMessageApi.class);
    setField("appMessageApi", appMessageApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenSuccess_thenReturnsMessages()
    throws Exception {
    List<AppMessage> messages = new ArrayList<>();
    messages.add(new AppMessage());
    when(appMessageApi.getAppMessages()).thenReturn(messages);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(messages, model.get("messages"));
  }

  @Test
  public void testExecuteImpl_whenEmptyList_thenReturnsEmptyMessages()
    throws Exception {
    when(appMessageApi.getAppMessages()).thenReturn(new ArrayList<>());

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertTrue(((List<?>) model.get("messages")).isEmpty());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(appMessageApi.getAppMessages()).thenThrow(
      new AccessDeniedException("denied")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenException_thenReturnsInternalError()
    throws Exception {
    when(appMessageApi.getAppMessages()).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppMessagesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
