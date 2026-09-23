package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AresBridgeApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ExternalRepositoryIdGetTest {

  private ExternalRepositoryIdGet webscript;
  private AresBridgeApi aresBridgeApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new ExternalRepositoryIdGet();
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
    templateVars.put("id", "test-group-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserIsAdmin_thenReturnsRepos()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);
    when(aresBridgeApi.getExternalRepositories("test-group-id")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(Collections.emptyList(), result.get("repos"));
  }

  @Test
  public void testExecuteImpl_whenUserIsNotAdmin_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(false);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);
    when(aresBridgeApi.getExternalRepositories("test-group-id")).thenThrow(
      new RuntimeException("connection error")
    );

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      ExternalRepositoryIdGet.class.getDeclaredMethod(
        "executeImpl",
        WebScriptRequest.class,
        Status.class,
        Cache.class
      );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      webscript,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ExternalRepositoryIdGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
