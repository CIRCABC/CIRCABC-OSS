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

public class ExternalRepositoryIdPostTest {

  private ExternalRepositoryIdPost webScript;
  private AresBridgeApi aresBridgeApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new ExternalRepositoryIdPost();
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
  }

  @Test
  public void testExecuteImpl_whenUserIsAdmin_thenAddsExternalRepository()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "group-id");
    templateVars.put("repoId", "repo-id");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id")
    ).thenReturn(true);

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    verify(aresBridgeApi).addExternalRepositories("group-id", "repo-id");
  }

  @Test
  public void testExecuteImpl_whenUserIsNotAdmin_thenReturnsForbidden()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "group-id");
    templateVars.put("repoId", "repo-id");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id")
    ).thenReturn(false);

    Map<String, Object> result = invokeExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(aresBridgeApi);
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsBadRequest()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "group-id");
    templateVars.put("repoId", "repo-id");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id")
    ).thenReturn(true);
    doThrow(new RuntimeException("error"))
      .when(aresBridgeApi)
      .addExternalRepositories("group-id", "repo-id");

    Map<String, Object> result = invokeExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIdIsNull_thenSkipsApiCall() throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);
    templateVars.put("repoId", "repo-id");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(currentUserPermissionCheckerService.isGroupAdmin(null)).thenReturn(
      true
    );

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    verifyNoInteractions(aresBridgeApi);
  }

  private Map<String, Object> invokeExecuteImpl() {
    return webScript.executeImpl(req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ExternalRepositoryIdPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
