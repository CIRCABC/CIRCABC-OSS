package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EmailApi;
import io.swagger.model.permissions.DirectoryPermissions;
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

public class GroupsEmailPostTest {

  private GroupsEmailPost webscript;
  private EmailApi emailApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new GroupsEmailPost();
    emailApi = mock(EmailApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("emailApi", emailApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    Map<String, String> vars = new HashMap<>();
    vars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRACCESS
      )
    ).thenReturn(false);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    assertEquals("Access denied", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenParseThrowsException_thenReturnsNotAcceptable()
    throws Exception {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRACCESS
      )
    ).thenReturn(true);
    when(req.getContent()).thenThrow(new RuntimeException("parse error"));

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("parse error", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenSuccess_thenReturnsModel() throws Exception {
    when(
      permissionChecker.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRACCESS
      )
    ).thenReturn(true);

    org.springframework.extensions.surf.util.Content content = mock(
      org.springframework.extensions.surf.util.Content.class
    );
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"subject\":\"test\",\"content\":\"body\"}"
    );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(Status.STATUS_OK, status.getCode());
    verify(emailApi).groupsIdEmailPost(eq("test-ig-id"), any());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsEmailPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
