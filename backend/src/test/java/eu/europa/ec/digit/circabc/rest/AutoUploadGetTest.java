package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AutoUploadApi;
import io.swagger.model.Configuration;
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

public class AutoUploadGetTest {

  private AutoUploadGet autoUploadGet;
  private AutoUploadApi autoUploadApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    autoUploadGet = new AutoUploadGet();
    autoUploadApi = mock(AutoUploadApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("autoUploadApi", autoUploadApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    templateVars = new HashMap<>();
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AutoUploadGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(autoUploadGet, value);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsConfiguration() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("nodeId")).thenReturn("node-456");

    Configuration config = new Configuration();
    config.setDateRestriction("0 0 14 * * 3");
    when(autoUploadApi.getAutoUploadEntry("ig-123", "node-456")).thenReturn(
      config
    );

    Map<String, Object> model = autoUploadGet.executeImpl(req, status, cache);

    assertNotNull(model);
    assertSame(config, model.get("autoupload"));
    assertEquals(3, model.get("dayChoice"));
    assertEquals(14, model.get("hourChoice"));
  }

  @Test
  public void testExecuteImpl_whenConfigurationNull_thenNoDayHourChoice() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("nodeId")).thenReturn("node-456");
    when(autoUploadApi.getAutoUploadEntry("ig-123", "node-456")).thenReturn(
      null
    );

    Map<String, Object> model = autoUploadGet.executeImpl(req, status, cache);

    assertNotNull(model);
    assertNull(model.get("autoupload"));
    assertFalse(model.containsKey("dayChoice"));
    assertFalse(model.containsKey("hourChoice"));
  }

  @Test
  public void testExecuteImpl_whenWildcardCron_thenReturnsMinusOne() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("nodeId")).thenReturn("node-456");

    Configuration config = new Configuration();
    config.setDateRestriction("0 0 * * * *");
    when(autoUploadApi.getAutoUploadEntry("ig-123", "node-456")).thenReturn(
      config
    );

    Map<String, Object> model = autoUploadGet.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(-1, model.get("dayChoice"));
    assertEquals(-1, model.get("hourChoice"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      false
    );

    Map<String, Object> model = autoUploadGet.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenInternalError() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("nodeId")).thenReturn("node-456");
    when(autoUploadApi.getAutoUploadEntry("ig-123", "node-456")).thenThrow(
      new RuntimeException("DB error")
    );

    Map<String, Object> model = autoUploadGet.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
