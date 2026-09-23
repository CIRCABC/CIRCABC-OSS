package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.helper.AspectManager;
import io.swagger.api.TopicsApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

public class TopicRepliesPostTest {

  private TopicRepliesPost topicRepliesPost;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private AspectManager aspectManager;
  private NodeService nodeService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TOPIC_ID = "test-topic-id";

  @Before
  public void setUp() throws Exception {
    topicRepliesPost = new TopicRepliesPost();
    topicsApi = mock(TopicsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    aspectManager = mock(AspectManager.class);
    nodeService = mock(NodeService.class);

    setField("topicsApi", topicsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("aspectManager", aspectManager);
    setField("nodeService", nodeService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TOPIC_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("notify")).thenReturn("true");
  }

  @Test
  public void testExecuteImpl_whenValidCommentPost_thenReturnsPost()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq(TOPIC_ID),
        any()
      )
    ).thenReturn(true);

    FormData formData = mockFormDataWithComment("{\"text\":\"Hello world\"}");
    when(req.parseContent()).thenReturn(formData);

    Node resultNode = new Node();
    resultNode.setId("reply-id");
    when(
      topicsApi.topicsIdRepliesPost(eq(TOPIC_ID), any(), any(), any())
    ).thenReturn(resultNode);

    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TOPIC_ID
    );
    when(aspectManager.isLibraryNode(parentRef)).thenReturn(false);
    when(aspectManager.isNewsgroupNode(parentRef)).thenReturn(true);

    Map<String, Object> model = topicRepliesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(resultNode, model.get("post"));
    verify(topicsApi).topicsIdRepliesPost(eq(TOPIC_ID), any(), any(), any());
    verify(aspectManager).addNewsgroupAspect(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "reply-id")
    );
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq(TOPIC_ID),
        any()
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq(TOPIC_ID),
        any()
      )
    ).thenReturn(false);

    FormData formData = mockFormDataWithComment("{\"text\":\"Hello\"}");
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> model = topicRepliesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullFormData_thenReturnsServerError()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq(TOPIC_ID),
        any()
      )
    ).thenReturn(true);

    when(req.parseContent()).thenReturn(null);

    Map<String, Object> model = topicRepliesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNotMultipart_thenReturnsServerError()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq(TOPIC_ID),
        any()
      )
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(false);
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> model = topicRepliesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNotifyFalse_thenNoNotifyAspect()
    throws Exception {
    when(req.getParameter("notify")).thenReturn("false");
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq(TOPIC_ID),
        any()
      )
    ).thenReturn(true);

    FormData formData = mockFormDataWithComment("{\"text\":\"No notify\"}");
    when(req.parseContent()).thenReturn(formData);

    Node resultNode = new Node();
    resultNode.setId("reply-id-2");
    when(
      topicsApi.topicsIdRepliesPost(eq(TOPIC_ID), any(), any(), any())
    ).thenReturn(resultNode);

    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TOPIC_ID
    );
    when(aspectManager.isLibraryNode(parentRef)).thenReturn(false);
    when(aspectManager.isNewsgroupNode(parentRef)).thenReturn(false);

    Map<String, Object> model = topicRepliesPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(resultNode, model.get("post"));
    verify(nodeService, never()).addAspect(any(), any(), any());
  }

  private FormData mockFormDataWithComment(String json) throws Exception {
    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField commentField = mock(FormData.FormField.class);
    when(commentField.getName()).thenReturn("comment");
    when(commentField.getIsFile()).thenReturn(false);
    when(commentField.getValue()).thenReturn(json);

    FormData.FormField[] fields = new FormData.FormField[] { commentField };
    when(formData.getFields()).thenReturn(fields);

    return formData;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = TopicRepliesPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(topicRepliesPost, value);
  }
}
