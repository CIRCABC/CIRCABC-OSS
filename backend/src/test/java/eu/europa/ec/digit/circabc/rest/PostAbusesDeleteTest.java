package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
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

public class PostAbusesDeleteTest {

  private PostAbusesDelete webScript;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new PostAbusesDelete();
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
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserHasPermission_thenRemovesAbuses() {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "test-node-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(forumsApi).removeAbuses("test-node-id");
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
    doThrow(new RuntimeException("error"))
      .when(forumsApi)
      .removeAbuses("test-node-id");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PostAbusesDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
