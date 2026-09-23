package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AresBridgeApi;
import io.swagger.model.db.AresBridgeDAO;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GetExternalRepositoryGroupLogTest {

  private GetExternalRepositoryGroupLog webScript;
  private AresBridgeApi aresBridgeApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() {
    webScript = new GetExternalRepositoryGroupLog();
    aresBridgeApi = mock(AresBridgeApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    webScript.setAresBridgeApi(aresBridgeApi);
    webScript.setCurrentUserPermissionCheckerService(permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "group-id");
    templateVars.put("repoId", "repo-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserIsAdmin_thenReturnsLogs() {
    when(permissionChecker.isGroupAdmin("group-id")).thenReturn(true);
    Collection<AresBridgeDAO> logs = Collections.singletonList(
      new AresBridgeDAO()
    );
    when(aresBridgeApi.groupLog("group-id", "repo-id")).thenReturn(logs);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(logs, model.get("logs"));
  }

  @Test
  public void testExecuteImpl_whenUserIsNotAdmin_thenReturnsForbidden() {
    when(permissionChecker.isGroupAdmin("group-id")).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsBadRequest() {
    when(permissionChecker.isGroupAdmin("group-id")).thenReturn(true);
    when(aresBridgeApi.groupLog("group-id", "repo-id")).thenThrow(
      new RuntimeException("fail")
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
