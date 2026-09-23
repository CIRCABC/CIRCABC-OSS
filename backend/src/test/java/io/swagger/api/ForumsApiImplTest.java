package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.newsgroup.ModerationService;
import io.swagger.model.I18nProperty;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.ModerationModel;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class ForumsApiImplTest {

  private ForumsApiImpl forumsApi;
  private NodeService nodeService;
  private NodesApi nodesApi;
  private FileFolderService fileFolderService;
  private ModerationService moderationService;
  private PersonService personService;

  @Before
  public void setUp() throws Exception {
    forumsApi = new ForumsApiImpl();
    nodeService = mock(NodeService.class);
    nodesApi = mock(NodesApi.class);
    fileFolderService = mock(FileFolderService.class);
    moderationService = mock(ModerationService.class);
    personService = mock(PersonService.class);

    setField("nodeService", nodeService);
    setField("nodesApi", nodesApi);
    setField("fileFolderService", fileFolderService);
    setField("moderationService", moderationService);
    setField("personService", personService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ForumsApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(forumsApi, value);
  }

  @Test
  public void testGetForumById_whenHasNewsgroupAspect_thenReturnsNodes() {
    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    when(
      nodeService.hasAspect(forumRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);

    FileInfo fileInfo = mock(FileInfo.class);
    when(fileInfo.getNodeRef()).thenReturn(childRef);

    List<FileInfo> fileInfoList = new ArrayList<>();
    fileInfoList.add(fileInfo);

    @SuppressWarnings("unchecked")
    PagingResults<FileInfo> pagingResults = mock(PagingResults.class);
    when(pagingResults.getPage()).thenReturn(fileInfoList);
    when(
      fileFolderService.list(
        eq(forumRef),
        eq(true),
        eq(true),
        isNull(),
        anyList(),
        any()
      )
    ).thenReturn(pagingResults);

    Node node = new Node();
    node.setName("test-node");
    when(nodesApi.getNode(childRef)).thenReturn(node);

    List<Node> result = forumsApi.getForumById("forum-id");

    assertEquals(1, result.size());
    assertEquals("test-node", result.get(0).getName());
  }

  @Test
  public void testGetForumById_whenNoNewsgroupAspect_thenReturnsEmptyList() {
    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum-id"
    );
    when(
      nodeService.hasAspect(forumRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);

    List<Node> result = forumsApi.getForumById("forum-id");

    assertTrue(result.isEmpty());
  }

  @Test
  public void testForumsIdDelete_whenForumExists_thenDeletesNode() {
    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum-id"
    );
    when(nodeService.exists(forumRef)).thenReturn(true);
    when(nodeService.getType(forumRef)).thenReturn(ForumModel.TYPE_FORUM);

    forumsApi.forumsIdDelete("forum-id");

    verify(nodeService).deleteNode(forumRef);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testForumsIdDelete_whenForumNotExists_thenThrows() {
    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "bad-id"
    );
    when(nodeService.exists(forumRef)).thenReturn(false);

    forumsApi.forumsIdDelete("bad-id");
  }

  @Test
  public void testUpdateForum_whenTitleAndDescription_thenUpdatesProperties() {
    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum-id"
    );
    when(nodeService.exists(forumRef)).thenReturn(true);
    when(nodeService.getType(forumRef)).thenReturn(ForumModel.TYPE_FORUM);

    Node forumNode = new Node();
    forumNode.setName("new-name");
    I18nProperty title = new I18nProperty();
    title.put("en", "New Title");
    forumNode.setTitle(title);
    I18nProperty description = new I18nProperty();
    description.put("en", "New Description");
    forumNode.setDescription(description);

    forumsApi.updateForum("forum-id", forumNode);

    verify(nodeService).setProperty(
      eq(forumRef),
      eq(ContentModel.PROP_TITLE),
      any()
    );
    verify(nodeService).setProperty(
      eq(forumRef),
      eq(ContentModel.PROP_DESCRIPTION),
      any()
    );
    verify(nodeService).setProperty(
      forumRef,
      ContentModel.PROP_NAME,
      "new-name"
    );
  }

  @Test
  public void testToggleModeration_whenEnableAndNotModerated_thenAppliesModeration() {
    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum-id"
    );
    when(
      nodeService.getProperty(forumRef, ModerationModel.PROP_IS_MODERATED)
    ).thenReturn(null);

    forumsApi.toggleModeration("forum-id", true, false);

    verify(moderationService).applyModeration(forumRef, false);
  }

  @Test
  public void testToggleModeration_whenDisableAndModerated_thenStopsModeration() {
    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum-id"
    );
    when(
      nodeService.getProperty(forumRef, ModerationModel.PROP_IS_MODERATED)
    ).thenReturn("true");

    forumsApi.toggleModeration("forum-id", false, true);

    verify(moderationService).stopModeration(forumRef, "accept");
  }

  @Test
  public void testToggleModeration_whenDisableAndNotAcceptAll_thenRefuses() {
    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum-id"
    );
    when(
      nodeService.getProperty(forumRef, ModerationModel.PROP_IS_MODERATED)
    ).thenReturn("true");

    forumsApi.toggleModeration("forum-id", false, false);

    verify(moderationService).stopModeration(forumRef, "refuse");
  }

  @Test
  public void testVerifyPost_whenApprove_thenAccepts() {
    NodeRef postRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "post-id"
    );

    forumsApi.verifyPost("post-id", true, null);

    verify(moderationService).accept(postRef);
    verify(moderationService, never()).reject(any(), anyString());
  }

  @Test
  public void testForumsIdSubforumsGet_whenHasAspect_thenReturnsForumChildren() {
    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "subforum-id"
    );

    when(
      nodeService.hasAspect(forumRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(childRef);

    List<ChildAssociationRef> children = new ArrayList<>();
    children.add(childAssoc);
    when(nodeService.getChildAssocs(forumRef)).thenReturn(children);
    when(nodeService.getType(childRef)).thenReturn(ForumModel.TYPE_FORUM);

    Node node = new Node();
    node.setName("subforum");
    when(nodesApi.getNode(childRef)).thenReturn(node);

    List<Node> result = forumsApi.forumsIdSubforumsGet("forum-id");

    assertEquals(1, result.size());
    assertEquals("subforum", result.get(0).getName());
  }

  @Test
  public void testForumsIdSubforumsGet_whenChildNotForum_thenExcluded() {
    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "topic-id"
    );

    when(
      nodeService.hasAspect(forumRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(childRef);

    List<ChildAssociationRef> children = new ArrayList<>();
    children.add(childAssoc);
    when(nodeService.getChildAssocs(forumRef)).thenReturn(children);
    when(nodeService.getType(childRef)).thenReturn(ForumModel.TYPE_TOPIC);

    List<Node> result = forumsApi.forumsIdSubforumsGet("forum-id");

    assertTrue(result.isEmpty());
  }

  @Test
  public void testRemoveAbuses_thenCallsSignalNotAbuse() {
    NodeRef postRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "post-id"
    );

    forumsApi.removeAbuses("post-id");

    verify(moderationService).signalNotAbuse(postRef);
  }

  @Test
  public void testGetSignaledAbuses_thenDelegatesToModerationService() {
    NodeRef postRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "post-id"
    );
    when(moderationService.getAbuses(postRef)).thenReturn(new ArrayList<>());

    forumsApi.getSignaledAbuses("post-id");

    verify(moderationService).getAbuses(postRef);
  }
}
