package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AresBridgeApi;
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

public class ExternalRepositoryIdDeleteTest {

  private ExternalRepositoryIdDelete webScript;
  private AresBridgeApi aresBridgeApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new ExternalRepositoryIdDelete();
    aresBridgeApi = mock(AresBridgeApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("aresBridgeApi", aresBridgeApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "group-id");
    templateVars.put("repoId", "repo-id");

    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenAuthorized_thenDeletesRepository() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id")
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(aresBridgeApi).deleteExternalRepository("group-id", "repo-id");
  }

  @Test
  public void testExecuteImpl_whenNotAuthorized_thenReturnsForbidden() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id")
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsBadRequest() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id")
    ).thenReturn(true);
    doThrow(new RuntimeException("fail"))
      .when(aresBridgeApi)
      .deleteExternalRepository("group-id", "repo-id");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIdNull_thenSkipsDelete() {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);
    templateVars.put("repoId", "repo-id");

    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(currentUserPermissionCheckerService.isGroupAdmin(null)).thenReturn(
      true
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verifyNoInteractions(aresBridgeApi);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ExternalRepositoryIdDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
