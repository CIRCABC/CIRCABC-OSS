package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ForumsDeleteTest {

  private ForumsDelete forumsDelete;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    forumsDelete = new ForumsDelete();
    forumsApi = mock(ForumsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("forumsApi", forumsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-forum-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenDeletesAndReturnsOk()
    throws Exception {
    when(
      permissionChecker.hasAlfrescoDeletePermission("test-forum-id")
    ).thenReturn(true);

    Map<String, Object> result = forumsDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(forumsApi).forumsIdDelete("test-forum-id");
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      permissionChecker.hasAlfrescoDeletePermission("test-forum-id")
    ).thenReturn(false);

    Map<String, Object> result = forumsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    assertEquals("Access denied", status.getMessage());
    assertTrue(status.getRedirect());
    verify(forumsApi, never()).forumsIdDelete(anyString());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      permissionChecker.hasAlfrescoDeletePermission("test-forum-id")
    ).thenReturn(true);
    doThrow(new InvalidNodeRefException("invalid", null))
      .when(forumsApi)
      .forumsIdDelete("test-forum-id");

    Map<String, Object> result = forumsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Bad request", status.getMessage());
    assertTrue(status.getRedirect());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ForumsDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(forumsDelete, value);
  }
}
