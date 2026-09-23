package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HistoryApi;
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

public class HistoryMembershipsLogsDeleteTest {

  private HistoryMembershipsLogsDelete webScript;
  private CurrentUserPermissionCheckerService permissionChecker;
  private HistoryApi historyApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new HistoryMembershipsLogsDelete();
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    historyApi = mock(HistoryApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("currentUserPermissionCheckerService", permissionChecker);
    setField("historyApi", historyApi);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("groupId", "group1");
    templateVars.put("userId", "user1");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HistoryMembershipsLogsDelete.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenSuccess() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(historyApi).cleanMembershipsLogs("group1", "user1");
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenSuccess() {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(historyApi).cleanMembershipsLogs("group1", "user1");
  }

  @Test
  public void testExecuteImpl_whenGroupAdmin_thenSuccess() {
    when(permissionChecker.isGroupAdmin("group1")).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(historyApi).cleanMembershipsLogs("group1", "user1");
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isGroupAdmin("group1")).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(historyApi, never()).cleanMembershipsLogs(anyString(), anyString());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenInternalServerError() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    doThrow(new RuntimeException("DB error"))
      .when(historyApi)
      .cleanMembershipsLogs("group1", "user1");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("DB error", status.getMessage());
  }
}
