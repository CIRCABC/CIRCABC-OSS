package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.TopicsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class PostAttachmentLinkAddTest {

  private PostAttachmentLinkAdd webScript;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new PostAttachmentLinkAdd();
    topicsApi = mock(TopicsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("topicsApi", topicsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "post-id");
    templateVars.put("destinationId", "dest-id");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserHasPermission_thenAddsLinkAttachment()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "post-id",
        NewsGroupPermissions.NWSPOST
      )
    ).thenReturn(true);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    verify(topicsApi).addLinkAttachment("post-id", "dest-id");
  }

  @Test
  public void testExecuteImpl_whenUserLacksPermission_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "post-id",
        NewsGroupPermissions.NWSPOST
      )
    ).thenReturn(false);

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(topicsApi, never()).addLinkAttachment(anyString(), anyString());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsNotAcceptable()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        "post-id",
        NewsGroupPermissions.NWSPOST
      )
    ).thenReturn(true);
    doThrow(new RuntimeException("something failed"))
      .when(topicsApi)
      .addLinkAttachment("post-id", "dest-id");

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("something failed", status.getMessage());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method = org.springframework.extensions.webscripts
      .DeclarativeWebScript.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    return (Map<String, Object>) method.invoke(webScript, req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PostAttachmentLinkAdd.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
