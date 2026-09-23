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
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GetPostAttachmentRemainingSizeTest {

  private GetPostAttachmentRemainingSize webScript;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GetPostAttachmentRemainingSize();
    topicsApi = mock(TopicsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("topicsApi", topicsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GetPostAttachmentRemainingSize.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsSize()
    throws Exception {
    mockTemplateVars("some-id");
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        "some-id",
        NewsGroupPermissions.NWSACCESS
      )
    ).thenReturn(true);
    when(topicsApi.getAttachmentsRemainingSize("some-id")).thenReturn(1024L);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(1024L, model.get("size"));
  }

  @Test
  public void testExecuteImpl_whenIdIsNull_thenSkipsPermissionCheck()
    throws Exception {
    mockTemplateVars("null");
    when(topicsApi.getAttachmentsRemainingSize("null")).thenReturn(500L);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(500L, model.get("size"));
    verifyNoInteractions(permissionCheckerService);
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    mockTemplateVars("some-id");
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        "some-id",
        NewsGroupPermissions.NWSACCESS
      )
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidTypeException_thenReturnsBadRequest()
    throws Exception {
    mockTemplateVars("some-id");
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        "some-id",
        NewsGroupPermissions.NWSACCESS
      )
    ).thenReturn(true);
    when(topicsApi.getAttachmentsRemainingSize("some-id")).thenThrow(
      new InvalidTypeException("bad type", null)
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Bad noderef type", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsBadRequest()
    throws Exception {
    mockTemplateVars("some-id");
    when(
      permissionCheckerService.hasAnyOfNewsGroupPermission(
        "some-id",
        NewsGroupPermissions.NWSACCESS
      )
    ).thenReturn(true);
    when(topicsApi.getAttachmentsRemainingSize("some-id")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Bad request", status.getMessage());
  }
}
