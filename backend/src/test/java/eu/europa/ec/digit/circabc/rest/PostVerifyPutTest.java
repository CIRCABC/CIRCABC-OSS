package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ForumsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class PostVerifyPutTest {

  private PostVerifyPut postVerifyPut;
  private ForumsApi forumsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private NodeService nodeService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PostVerifyPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(postVerifyPut, value);
  }

  @Before
  public void setUp() throws Exception {
    postVerifyPut = new PostVerifyPut();
    forumsApi = mock(ForumsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    nodeService = mock(NodeService.class);

    setField("forumsApi", forumsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("nodeService", nodeService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  private void setupRequest(String id, String approve, String rejectReason) {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("approve")).thenReturn(approve);
    when(req.getParameter("rejectReason")).thenReturn(rejectReason);
  }

  private void setupNodeService(String id, String topicId) {
    NodeRef postRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    NodeRef topicRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      topicId
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(topicRef);
    when(nodeService.getPrimaryParent(postRef)).thenReturn(childAssoc);
  }

  @Test
  public void testExecuteImpl_whenApproveTrue_thenVerifiesPostApproved() {
    String postId = "post-id-123";
    String topicId = "topic-id-456";

    setupRequest(postId, "true", null);
    setupNodeService(postId, topicId);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq(topicId),
        any()
      )
    ).thenReturn(true);

    Map<String, Object> result = postVerifyPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(forumsApi).verifyPost(postId, true, "");
  }

  @Test
  public void testExecuteImpl_whenApproveFalse_thenVerifiesPostRejected() {
    String postId = "post-id-123";
    String topicId = "topic-id-456";

    setupRequest(postId, "false", "spam content");
    setupNodeService(postId, topicId);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq(topicId),
        any()
      )
    ).thenReturn(true);

    Map<String, Object> result = postVerifyPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(forumsApi).verifyPost(postId, false, "spam content");
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden() {
    String postId = "post-id-123";
    String topicId = "topic-id-456";

    setupRequest(postId, "true", null);
    setupNodeService(postId, topicId);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq(topicId),
        any()
      )
    ).thenReturn(false);

    Map<String, Object> result = postVerifyPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(forumsApi, never()).verifyPost(
      anyString(),
      anyBoolean(),
      anyString()
    );
  }

  @Test
  public void testExecuteImpl_whenRejectReasonNull_thenPassesEmptyString() {
    String postId = "post-id-123";
    String topicId = "topic-id-456";

    setupRequest(postId, "false", null);
    setupNodeService(postId, topicId);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq(topicId),
        any()
      )
    ).thenReturn(true);

    Map<String, Object> result = postVerifyPut.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(forumsApi).verifyPost(postId, false, "");
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsBadRequest() {
    String postId = "post-id-123";
    String topicId = "topic-id-456";

    setupRequest(postId, "true", null);
    setupNodeService(postId, topicId);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq(topicId),
        any()
      )
    ).thenReturn(true);
    doThrow(new RuntimeException("unexpected"))
      .when(forumsApi)
      .verifyPost(anyString(), anyBoolean(), anyString());

    Map<String, Object> result = postVerifyPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
