package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CircabcApi;
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

public class CircabcAdminsDeleteTest {

  private CircabcAdminsDelete classUnderTest;
  private CircabcApi circabcApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new CircabcAdminsDelete();
    circabcApi = mock(CircabcApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
    templateVars = new HashMap<>();
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    setField("circabcApi", circabcApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CircabcAdminsDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(classUnderTest, value);
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenDeletesUser() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    templateVars.put("userId", "user1");

    Map<String, Object> result = classUnderTest.executeImpl(req, status, cache);

    assertEquals(Status.STATUS_OK, status.getCode());
    assertEquals("ok", result.get("result"));
    verify(circabcApi).circabcAdminsUserIdDelete("user1");
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenDeletesUser() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    templateVars.put("userId", "user2");

    Map<String, Object> result = classUnderTest.executeImpl(req, status, cache);

    assertEquals(Status.STATUS_OK, status.getCode());
    assertEquals("ok", result.get("result"));
    verify(circabcApi).circabcAdminsUserIdDelete("user2");
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );

    Map<String, Object> result = classUnderTest.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(circabcApi);
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenInternalServerError() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    templateVars.put("userId", "user1");
    doThrow(new RuntimeException("fail"))
      .when(circabcApi)
      .circabcAdminsUserIdDelete("user1");

    Map<String, Object> result = classUnderTest.executeImpl(req, status, cache);

    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertNotNull(result);
    assertFalse(result.containsKey("result"));
  }
}
