package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.model.DisplayConfiguration;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AppMessagesConfigGetTest {

  private AppMessagesConfigGet webscript;
  private AppMessageApi appMessageApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new AppMessagesConfigGet();
    appMessageApi = mock(AppMessageApi.class);

    Field field = AppMessagesConfigGet.class.getDeclaredField("appMessageApi");
    field.setAccessible(true);
    field.set(webscript, appMessageApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenSuccess_thenReturnsConfig() {
    DisplayConfiguration config = new DisplayConfiguration();
    when(appMessageApi.getDisplayOldMessage()).thenReturn(config);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertSame(config, model.get("config"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(appMessageApi.getDisplayOldMessage()).thenThrow(
      new AccessDeniedException("denied")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    assertEquals("Access denied", status.getMessage());
    assertTrue(status.getRedirect());
  }

  @Test
  public void testExecuteImpl_whenException_thenReturnsInternalError() {
    when(appMessageApi.getDisplayOldMessage()).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("Internal server error", status.getMessage());
    assertTrue(status.getRedirect());
  }
}
