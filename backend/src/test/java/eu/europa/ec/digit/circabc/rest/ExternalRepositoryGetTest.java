package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AresBridgeApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ExternalRepositoryGetTest {

  private ExternalRepositoryGet externalRepositoryGet;
  private AresBridgeApi aresBridgeApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    externalRepositoryGet = new ExternalRepositoryGet();
    aresBridgeApi = mock(AresBridgeApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("aresBridgeApi", aresBridgeApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  @Test
  public void testExecuteImpl_whenUserIsGuest_thenReturnsForbidden() {
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(true);

    Map<String, Object> result = externalRepositoryGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenValidUser_thenReturnsRepos() {
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    Collection<String> repos = Arrays.asList("ARES", "HERMES");
    when(aresBridgeApi.getAvailableExternalRepositories()).thenReturn(repos);

    Map<String, Object> result = externalRepositoryGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(repos, result.get("repos"));
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsBadRequest() {
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(aresBridgeApi.getAvailableExternalRepositories()).thenThrow(
      new RuntimeException("connection error")
    );

    Map<String, Object> result = externalRepositoryGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ExternalRepositoryGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(externalRepositoryGet, value);
  }
}
