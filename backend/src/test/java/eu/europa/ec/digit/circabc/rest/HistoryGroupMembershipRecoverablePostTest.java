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

public class HistoryGroupMembershipRecoverablePostTest {

  private HistoryGroupMembershipRecoverablePost webScript;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private HistoryApi historyApi;

  @Before
  public void setUp() throws Exception {
    webScript = new HistoryGroupMembershipRecoverablePost();
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    historyApi = mock(HistoryApi.class);

    setField("currentUserPermissionCheckerService", permissionCheckerService);
    setField("historyApi", historyApi);
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenRecoversMembership()
    throws Exception {
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(true);

    WebScriptRequest req = mockRequest("user1", "group1", "profile1");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(historyApi).recoverMembershipFromGroup(
      "user1",
      "group1",
      "profile1"
    );
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenRecoversMembership()
    throws Exception {
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(false);
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(true);

    WebScriptRequest req = mockRequest("user1", "group1", "profile1");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(historyApi).recoverMembershipFromGroup(
      "user1",
      "group1",
      "profile1"
    );
  }

  @Test
  public void testExecuteImpl_whenIGDirAdmin_thenRecoversMembership()
    throws Exception {
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(false);
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(false);
    when(permissionCheckerService.isInterestGroupDirAdmin("group1")).thenReturn(
      true
    );

    WebScriptRequest req = mockRequest("user1", "group1", "profile1");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(historyApi).recoverMembershipFromGroup(
      "user1",
      "group1",
      "profile1"
    );
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(false);
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(false);
    when(permissionCheckerService.isInterestGroupDirAdmin("group1")).thenReturn(
      false
    );

    WebScriptRequest req = mockRequest("user1", "group1", "profile1");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(historyApi, never()).recoverMembershipFromGroup(
      anyString(),
      anyString(),
      anyString()
    );
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsServerError()
    throws Exception {
    when(permissionCheckerService.isAlfrescoAdmin()).thenReturn(true);
    doThrow(new RuntimeException("DB error"))
      .when(historyApi)
      .recoverMembershipFromGroup("user1", "group1", "profile1");

    WebScriptRequest req = mockRequest("user1", "group1", "profile1");
    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("DB error", status.getMessage());
  }

  private WebScriptRequest mockRequest(
    String userId,
    String groupId,
    String profileId
  ) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", userId);
    templateVars.put("groupId", groupId);
    templateVars.put("profileId", profileId);
    String template =
      "/circabc/history/{groupId}/members/{userId}/recover/{profileId}";
    Match match = new Match(template, templateVars, template);
    when(req.getServiceMatch()).thenReturn(match);
    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HistoryGroupMembershipRecoverablePost.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
