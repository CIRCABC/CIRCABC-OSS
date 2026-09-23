package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.TopicsApi;
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

public class PostAttachmentDeleteTest {

  private PostAttachmentDelete postAttachmentDelete;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PostAttachmentDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(postAttachmentDelete, value);
  }

  @Before
  public void setUp() throws Exception {
    postAttachmentDelete = new PostAttachmentDelete();
    topicsApi = mock(TopicsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    setField("topicsApi", topicsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "post-id");
    templateVars.put("attachmentId", "attachment-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenSuccess() {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        "post-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(true);

    Map<String, Object> result = postAttachmentDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(topicsApi).removeAttachment("post-id", "attachment-id");
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenForbidden() {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        "post-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(false);

    Map<String, Object> result = postAttachmentDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(topicsApi, never()).removeAttachment(anyString(), anyString());
  }

  @Test
  public void testExecuteImpl_whenRemoveAttachmentThrowsException_thenBadRequest() {
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        "post-id",
        NewsGroupPermissions.NWSMODERATE
      )
    ).thenReturn(true);
    doThrow(new RuntimeException("error"))
      .when(topicsApi)
      .removeAttachment("post-id", "attachment-id");

    Map<String, Object> result = postAttachmentDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
