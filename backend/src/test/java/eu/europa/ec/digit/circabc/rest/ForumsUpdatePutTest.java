package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ForumsUpdatePutTest {

  private ForumsUpdatePut forumsUpdatePut;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-node-id";
  private static final String VALID_JSON =
    "{\"name\":\"Updated Forum\",\"title\":{\"en\":\"English Title\"},\"description\":{\"en\":\"English Desc\"}}";

  @Before
  public void setUp() throws Exception {
    forumsUpdatePut = new ForumsUpdatePut();
    forumsApi = mock(ForumsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("forumsApi", forumsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenPermissionGranted_thenUpdatesSuccessfully()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    Map<String, Object> model = forumsUpdatePut.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(forumsApi).updateForum(eq(TEST_ID), any());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(false);

    Map<String, Object> model = forumsUpdatePut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsServerError()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("not valid json");
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    Map<String, Object> model = forumsUpdatePut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TEST_ID)
      )
    )
      .when(forumsApi)
      .updateForum(eq(TEST_ID), any());

    Map<String, Object> model = forumsUpdatePut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNameOnly_thenUpdatesSuccessfully()
    throws Exception {
    String nameOnlyJson = "{\"name\":\"Simple Forum\"}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(nameOnlyJson);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    Map<String, Object> model = forumsUpdatePut.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(forumsApi).updateForum(eq(TEST_ID), any());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ForumsUpdatePut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(forumsUpdatePut, value);
  }
}
