package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.config.CircabcConfig;
import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class ForumNodeLinkMethodTest {

  private ForumNodeLinkMethod method;
  private NodeService nodeService;
  private CircabcConfig circabcConfig;

  private static final String BASE_URL = "https://circabc.europa.eu";
  private static final String CONTEXT = "/ui/";

  @Before
  public void setUp() throws Exception {
    method = new ForumNodeLinkMethod();
    nodeService = mock(NodeService.class);
    circabcConfig = mock(CircabcConfig.class);

    method.setNodeService(nodeService);

    Field configField = ForumNodeLinkMethod.class.getDeclaredField(
      "circabcConfig"
    );
    configField.setAccessible(true);
    configField.set(method, circabcConfig);

    when(circabcConfig.getNewUiUrl()).thenReturn(BASE_URL);
    when(circabcConfig.getNewUiContext()).thenReturn(CONTEXT);
  }

  @Test
  public void testGetResult_whenPostInNewsgroup_thenReturnsTopicUrl() {
    // Hierarchy: groupRef -> forumRef -> topicRef -> postRef (nodeRef)
    NodeRef nodeRef = newNodeRef("post-id");
    NodeRef topicRef = newNodeRef("topic-id");
    NodeRef forumRef = newNodeRef("forum-id");
    NodeRef groupRef = newNodeRef("group-id");

    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(nodeService.getType(nodeRef)).thenReturn(ForumModel.TYPE_POST);

    // post -> topic
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(
      childAssoc(topicRef, nodeRef)
    );
    // topic -> forum (for findInterestGroupRoot traversal)
    when(nodeService.getPrimaryParent(topicRef)).thenReturn(
      childAssoc(forumRef, topicRef)
    );
    // forum -> group
    when(nodeService.getPrimaryParent(forumRef)).thenReturn(
      childAssoc(groupRef, forumRef)
    );

    when(
      nodeService.hasAspect(topicRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(forumRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(groupRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    String result = method.getResult(nodeRef);

    assertEquals(
      BASE_URL + CONTEXT + "group/group-id/forum/topic/topic-id",
      result
    );
  }

  @Test
  public void testGetResult_whenTopicInNewsgroup_thenReturnsForumUrl() {
    // Hierarchy: groupRef -> forumRef -> topicRef (nodeRef)
    NodeRef nodeRef = newNodeRef("topic-id");
    NodeRef forumRef = newNodeRef("forum-id");
    NodeRef groupRef = newNodeRef("group-id");

    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(nodeService.getType(nodeRef)).thenReturn(ForumModel.TYPE_TOPIC);

    // topic -> forum
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(
      childAssoc(forumRef, nodeRef)
    );
    // forum -> group
    when(nodeService.getPrimaryParent(forumRef)).thenReturn(
      childAssoc(groupRef, forumRef)
    );

    when(
      nodeService.hasAspect(forumRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(groupRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    String result = method.getResult(nodeRef);

    assertEquals(BASE_URL + CONTEXT + "group/group-id/forum/forum-id", result);
  }

  @Test
  public void testGetResult_whenForumInNewsgroup_thenReturnsForumNodeUrl() {
    // Hierarchy: groupRef -> forumRef (nodeRef)
    NodeRef nodeRef = newNodeRef("forum-id");
    NodeRef groupRef = newNodeRef("group-id");

    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(nodeService.getType(nodeRef)).thenReturn(ForumModel.TYPE_FORUM);

    // forum -> group
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(
      childAssoc(groupRef, nodeRef)
    );
    when(
      nodeService.hasAspect(groupRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    String result = method.getResult(nodeRef);

    assertEquals(BASE_URL + CONTEXT + "group/group-id/forum/forum-id", result);
  }

  @Test
  public void testGetResult_whenPostInLibrary_thenReturnsDocumentDetailsUrl() {
    // Hierarchy: groupRef -> documentRef -> discussionRef -> topicRef -> postRef (nodeRef)
    NodeRef nodeRef = newNodeRef("post-id");
    NodeRef topicRef = newNodeRef("topic-id");
    NodeRef discussionRef = newNodeRef("discussion-id");
    NodeRef documentRef = newNodeRef("document-id");
    NodeRef groupRef = newNodeRef("group-id");

    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(nodeService.getType(nodeRef)).thenReturn(ForumModel.TYPE_POST);

    // post -> topic
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(
      childAssoc(topicRef, nodeRef)
    );
    // topic -> discussion
    when(nodeService.getPrimaryParent(topicRef)).thenReturn(
      childAssoc(discussionRef, topicRef)
    );
    // discussion -> document
    when(nodeService.getPrimaryParent(discussionRef)).thenReturn(
      childAssoc(documentRef, discussionRef)
    );
    // document -> group (for findInterestGroupRoot)
    when(nodeService.getPrimaryParent(documentRef)).thenReturn(
      childAssoc(groupRef, documentRef)
    );

    when(
      nodeService.hasAspect(topicRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(discussionRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(documentRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(groupRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    String result = method.getResult(nodeRef);

    assertEquals(
      BASE_URL + CONTEXT + "group/group-id/library/document-id/details",
      result
    );
  }

  @Test
  public void testGetResult_whenTopicInLibrary_thenReturnsDocumentDetailsUrl() {
    // Hierarchy: groupRef -> documentRef -> discussionRef -> topicRef (nodeRef)
    NodeRef nodeRef = newNodeRef("topic-id");
    NodeRef discussionRef = newNodeRef("discussion-id");
    NodeRef documentRef = newNodeRef("document-id");
    NodeRef groupRef = newNodeRef("group-id");

    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(nodeService.getType(nodeRef)).thenReturn(ForumModel.TYPE_TOPIC);

    // topic -> discussion
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(
      childAssoc(discussionRef, nodeRef)
    );
    // discussion -> document
    when(nodeService.getPrimaryParent(discussionRef)).thenReturn(
      childAssoc(documentRef, discussionRef)
    );
    // document -> group (for findInterestGroupRoot)
    when(nodeService.getPrimaryParent(documentRef)).thenReturn(
      childAssoc(groupRef, documentRef)
    );

    when(
      nodeService.hasAspect(discussionRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(documentRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(groupRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    String result = method.getResult(nodeRef);

    assertEquals(
      BASE_URL + CONTEXT + "group/group-id/library/document-id/details",
      result
    );
  }

  @Test
  public void testGetResult_whenContextWithoutTrailingSlash_thenAddsSlash() {
    when(circabcConfig.getNewUiContext()).thenReturn("/ui");

    NodeRef nodeRef = newNodeRef("forum-id");
    NodeRef groupRef = newNodeRef("group-id");

    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(nodeService.getType(nodeRef)).thenReturn(ForumModel.TYPE_FORUM);

    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(
      childAssoc(groupRef, nodeRef)
    );
    when(
      nodeService.hasAspect(groupRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    String result = method.getResult(nodeRef);

    assertEquals(BASE_URL + "/ui/group/group-id/forum/forum-id", result);
  }

  private NodeRef newNodeRef(String id) {
    return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
  }

  private ChildAssociationRef childAssoc(NodeRef parent, NodeRef child) {
    return new ChildAssociationRef(
      QName.createQName("test", "assoc"),
      parent,
      QName.createQName("test", "child"),
      child
    );
  }
}
