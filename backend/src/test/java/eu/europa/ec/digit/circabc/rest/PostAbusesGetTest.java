package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
import io.swagger.model.AbuseReport;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class PostAbusesGetTest {

  private PostAbusesGet webScript;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new PostAbusesGet();
    forumsApi = mock(ForumsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("forumsApi", forumsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenUserHasPermission_thenReturnsAbuses() {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-node-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(true);

    List<AbuseReport> abuses = new ArrayList<>();
    abuses.add(mock(AbuseReport.class));
    when(forumsApi.getSignaledAbuses("test-node-id")).thenReturn(abuses);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(abuses, result.get("abuses"));
  }

  @Test
  public void testExecuteImpl_whenUserLacksPermission_thenReturnsForbidden() {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-node-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenForumsApiThrowsException_thenReturnsBadRequest() {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-node-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(true);
    when(forumsApi.getSignaledAbuses("test-node-id")).thenThrow(
      new RuntimeException("error")
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenEmptyAbuses_thenReturnsEmptyList() {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-node-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(true);
    when(forumsApi.getSignaledAbuses("test-node-id")).thenReturn(
      new ArrayList<>()
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertTrue(((List<?>) result.get("abuses")).isEmpty());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PostAbusesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
