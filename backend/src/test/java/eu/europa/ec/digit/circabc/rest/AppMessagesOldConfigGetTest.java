package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.model.EnableConfiguration;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AppMessagesOldConfigGetTest {

  private AppMessagesOldConfigGet webscript;
  private AppMessageApi appMessageApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new AppMessagesOldConfigGet();
    appMessageApi = mock(AppMessageApi.class);
    setField("appMessageApi", appMessageApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenSuccess_thenReturnsConfig() {
    EnableConfiguration config = new EnableConfiguration();
    config.setEnable(true);
    when(appMessageApi.getEnableOldMessage()).thenReturn(config);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(config, model.get("config"));
  }

  @Test
  public void testExecuteImpl_whenDisabled_thenReturnsConfig() {
    EnableConfiguration config = new EnableConfiguration();
    config.setEnable(false);
    when(appMessageApi.getEnableOldMessage()).thenReturn(config);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(config, model.get("config"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(appMessageApi.getEnableOldMessage()).thenThrow(
      new AccessDeniedException("denied")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenException_thenReturnsInternalError() {
    when(appMessageApi.getEnableOldMessage()).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppMessagesOldConfigGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
