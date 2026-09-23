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

public class GetExternalRepositoryNodeLogTest {

  private GetExternalRepositoryNodeLog webscript;
  private AresBridgeApi aresBridgeApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() {
    webscript = new GetExternalRepositoryNodeLog();
    aresBridgeApi = mock(AresBridgeApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    webscript.setAresBridgeApi(aresBridgeApi);
    webscript.setCurrentUserPermissionCheckerService(permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "node-id-123");
    templateVars.put("repoId", "repo-1");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserHasPermission_thenReturnsLogs() {
    when(permissionChecker.hasAlfrescoReadPermission("node-id-123")).thenReturn(
      true
    );
    Collection<AresBridgeDAO> expectedLogs = Collections.singletonList(
      new AresBridgeDAO()
    );
    when(aresBridgeApi.nodeLog("node-id-123", "repo-1")).thenReturn(
      expectedLogs
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(expectedLogs, model.get("logs"));
  }

  @Test
  public void testExecuteImpl_whenUserLacksPermission_thenReturnsForbidden() {
    when(permissionChecker.hasAlfrescoReadPermission("node-id-123")).thenReturn(
      false
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsBadRequest() {
    when(permissionChecker.hasAlfrescoReadPermission("node-id-123")).thenReturn(
      true
    );
    when(aresBridgeApi.nodeLog("node-id-123", "repo-1")).thenThrow(
      new RuntimeException("API failure")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
