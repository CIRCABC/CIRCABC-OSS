package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AutoUploadApi;
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

public class AutoUploadDeleteTest {

  private AutoUploadDelete autoUploadDelete;
  private AutoUploadApi autoUploadApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    autoUploadDelete = new AutoUploadDelete();
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
    Field field = AutoUploadDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(autoUploadDelete, value);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenDeletesEntry() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("configurationId")).thenReturn("42");

    Map<String, Object> model = autoUploadDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(1, model.get("result"));
    verify(autoUploadApi).removeAutoUploadEntry("ig-123", 42L);
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      false
    );

    Map<String, Object> model = autoUploadDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidConfigurationId_thenInternalError() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("configurationId")).thenReturn("not-a-number");

    Map<String, Object> model = autoUploadDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenInternalError() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("configurationId")).thenReturn("42");
    doThrow(new RuntimeException("DB error"))
      .when(autoUploadApi)
      .removeAutoUploadEntry("ig-123", 42L);

    Map<String, Object> model = autoUploadDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
