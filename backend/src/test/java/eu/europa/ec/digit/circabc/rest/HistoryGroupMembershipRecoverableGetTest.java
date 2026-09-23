package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HistoryApi;
import io.swagger.model.UserRecoveryOption;
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

public class HistoryGroupMembershipRecoverableGetTest {

  private HistoryGroupMembershipRecoverableGet webscript;
  private CurrentUserPermissionCheckerService permissionChecker;
  private HistoryApi historyApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new HistoryGroupMembershipRecoverableGet();
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    historyApi = mock(HistoryApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("currentUserPermissionCheckerService", permissionChecker);
    setField("historyApi", historyApi);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "user1");
    templateVars.put("groupId", "group1");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HistoryGroupMembershipRecoverableGet.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenReturnsRecoveryOption()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    UserRecoveryOption option = new UserRecoveryOption();
    option.setRecoverable(true);
    when(historyApi.isRecoverableFromGroup("user1", "group1")).thenReturn(
      option
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(option, model.get("recoveryOption"));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsRecoveryOption()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    UserRecoveryOption option = new UserRecoveryOption();
    option.setRecoverable(false);
    when(historyApi.isRecoverableFromGroup("user1", "group1")).thenReturn(
      option
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(option, model.get("recoveryOption"));
  }

  @Test
  public void testExecuteImpl_whenDirAdmin_thenReturnsRecoveryOption()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isInterestGroupDirAdmin("group1")).thenReturn(true);
    UserRecoveryOption option = new UserRecoveryOption();
    option.setRecoverable(true);
    when(historyApi.isRecoverableFromGroup("user1", "group1")).thenReturn(
      option
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(option, model.get("recoveryOption"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isInterestGroupDirAdmin("group1")).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenHistoryApiThrows_thenReturnsNotAcceptable()
    throws Exception {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(historyApi.isRecoverableFromGroup("user1", "group1")).thenThrow(
      new RuntimeException("db error")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("db error", status.getMessage());
  }
}
