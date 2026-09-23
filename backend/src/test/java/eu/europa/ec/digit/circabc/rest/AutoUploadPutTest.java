package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AutoUploadApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AutoUploadPutTest {

  private AutoUploadPut autoUploadPut;
  private AutoUploadApi autoUploadApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    autoUploadPut = new AutoUploadPut();
    autoUploadApi = mock(AutoUploadApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("autoUploadApi", autoUploadApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    templateVars = new HashMap<>();
    templateVars.put("id", "ig-123");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenAdminAndValidParams_thenReturnsResult()
    throws Exception {
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("configurationId")).thenReturn("42");
    when(req.getParameter("enable")).thenReturn("true");

    Map<String, Object> result = autoUploadPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(1, result.get("result"));
    verify(autoUploadApi).toggleAutoUploadEntry("ig-123", 42, true);
  }

  @Test
  public void testExecuteImpl_whenEnableFalse_thenCallsToggleWithFalse()
    throws Exception {
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("configurationId")).thenReturn("7");
    when(req.getParameter("enable")).thenReturn("false");

    Map<String, Object> result = autoUploadPut.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(autoUploadApi).toggleAutoUploadEntry("ig-123", 7, false);
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden()
    throws Exception {
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      false
    );

    Map<String, Object> result = autoUploadPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidConfigurationId_thenReturnsInternalServerError()
    throws Exception {
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("configurationId")).thenReturn("notANumber");
    when(req.getParameter("enable")).thenReturn("true");

    Map<String, Object> result = autoUploadPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("configurationId")).thenReturn("1");
    when(req.getParameter("enable")).thenReturn("true");
    doThrow(new InvalidNodeRefException("bad ref", null))
      .when(autoUploadApi)
      .toggleAutoUploadEntry("ig-123", 1, true);

    Map<String, Object> result = autoUploadPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AutoUploadPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(autoUploadPut, value);
  }
}
