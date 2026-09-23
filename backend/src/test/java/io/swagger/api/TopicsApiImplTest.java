package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.attachment.AttachmentService;
import eu.europa.ec.digit.circabc.rest.service.newsgroup.ModerationService;
import eu.europa.ec.digit.circabc.rest.service.user.UserDetails;
import eu.europa.ec.digit.circabc.rest.service.user.UserDetailsService;
import io.swagger.config.CircabcConfig;
import io.swagger.model.Attachement;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class TopicsApiImplTest {

  private TopicsApiImpl topicsApi;

  private NodeService secureNodeService;
  private ContentService contentService;
  private FileFolderService fileFolderService;
  private PersonService personService;
  private UserDetailsService userDetailsService;
  private NodesApi nodesApi;
  private ModerationService moderationService;
  private AttachmentService attachmentService;
  private CircabcConfig circabcConfig;

  private static final String TEST_ID = "test-node-id";
  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );

  private void setField(String fieldName, Object value) throws Exception {
    Field field = TopicsApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(topicsApi, value);
  }

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    topicsApi = new TopicsApiImpl();

    secureNodeService = mock(NodeService.class);
    contentService = mock(ContentService.class);
    fileFolderService = mock(FileFolderService.class);
    personService = mock(PersonService.class);
    userDetailsService = mock(UserDetailsService.class);
    nodesApi = mock(NodesApi.class);
    moderationService = mock(ModerationService.class);
    attachmentService = mock(AttachmentService.class);
    circabcConfig = mock(CircabcConfig.class);

    setField("secureNodeService", secureNodeService);
    setField("contentService", contentService);
    setField("fileFolderService", fileFolderService);
    setField("personService", personService);
    setField("userDetailsService", userDetailsService);
    setField("nodesApi", nodesApi);
    setField("moderationService", moderationService);
    setField("attachmentService", attachmentService);
    setField("circabcConfig", circabcConfig);
  }

  @Test
  public void testGetTopicReplies_whenTopicHasNewsgroupAspect_thenReturnsPosts() {
    when(
      secureNodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);

    NodeRef postRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "post-1"
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(postRef);
    when(secureNodeService.getChildAssocs(TEST_NODE_REF)).thenReturn(
      Collections.singletonList(childAssoc)
    );
    when(secureNodeService.getType(postRef)).thenReturn(ForumModel.TYPE_POST);

    PostNode postNode = new PostNode();
    postNode.setProperties(new java.util.HashMap<>());
    postNode.getProperties().put("creator", "admin");
    when(nodesApi.getNode(eq(postRef), any(PostNode.class))).thenReturn(
      postNode
    );

    ContentReader reader = mock(ContentReader.class);
    when(reader.getContentString()).thenReturn("Hello");
    when(
      contentService.getReader(postRef, ContentModel.PROP_CONTENT)
    ).thenReturn(reader);

    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-1"
    );
    when(personService.getPerson("admin")).thenReturn(personRef);
    UserDetails userDetails = mock(UserDetails.class);
    NodeRef avatarRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "avatar-1"
    );
    when(userDetails.getAvatar()).thenReturn(avatarRef);
    when(userDetailsService.getUserDetails(personRef)).thenReturn(userDetails);

    when(moderationService.isWaitingForApproval(postRef)).thenReturn(false);
    when(attachmentService.getAttachements(postRef)).thenReturn(
      new ArrayList<>()
    );

    List<Node> result = topicsApi.getTopicReplies(TEST_ID);

    assertEquals(1, result.size());
  }

  @Test
  public void testGetTopicReplies_whenNoAspect_thenReturnsEmptyList() {
    when(
      secureNodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);
    when(
      secureNodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);

    List<Node> result = topicsApi.getTopicReplies(TEST_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testPostsIdDelete_whenNodeIsPost_thenDeletesNode() {
    when(secureNodeService.getType(TEST_NODE_REF)).thenReturn(
      ForumModel.TYPE_POST
    );

    topicsApi.postsIdDelete(TEST_ID);

    verify(secureNodeService).deleteNode(TEST_NODE_REF);
  }

  @Test
  public void testPostsIdDelete_whenNodeIsNotPost_thenDoesNotDelete() {
    when(secureNodeService.getType(TEST_NODE_REF)).thenReturn(
      ForumModel.TYPE_TOPIC
    );

    topicsApi.postsIdDelete(TEST_ID);

    verify(secureNodeService, never()).deleteNode(any(NodeRef.class));
  }

  @Test
  public void testTopicsIdDelete_deletesNode() {
    topicsApi.topicsIdDelete(TEST_ID);

    verify(secureNodeService).deleteNode(TEST_NODE_REF);
  }

  @Test
  public void testAddLinkAttachment_whenDestinationExists_thenAddsAttachment() {
    String destId = "dest-node-id";
    NodeRef destRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      destId
    );
    when(secureNodeService.exists(destRef)).thenReturn(true);

    topicsApi.addLinkAttachment(TEST_ID, destId);

    verify(attachmentService).addAttachement(TEST_NODE_REF, destRef);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddLinkAttachment_whenDestinationDoesNotExist_thenThrows() {
    String destId = "missing-node-id";
    NodeRef destRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      destId
    );
    when(secureNodeService.exists(destRef)).thenReturn(false);

    topicsApi.addLinkAttachment(TEST_ID, destId);
  }

  @Test
  public void testGetAttachmentsRemainingSize_whenNodeDoesNotExist_thenReturnsTotalSize() {
    when(circabcConfig.getPostsAllowedAttachmentSizeinBytes()).thenReturn(
      "10000"
    );
    when(secureNodeService.exists(TEST_NODE_REF)).thenReturn(false);

    long remaining = topicsApi.getAttachmentsRemainingSize(TEST_ID);

    assertEquals(10000L, remaining);
  }

  @Test
  public void testGetAttachmentsRemainingSize_whenAttachmentsExist_thenReturnsReducedSize() {
    when(circabcConfig.getPostsAllowedAttachmentSizeinBytes()).thenReturn(
      "10000"
    );
    when(secureNodeService.exists(TEST_NODE_REF)).thenReturn(true);

    NodeRef attachRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "attach-1"
    );
    Attachement attachment = mock(Attachement.class);
    when(attachment.geType()).thenReturn(
      Attachement.AttachementType.HIDDEN_FILE
    );
    when(attachment.getSize()).thenReturn(3000L);
    when(attachment.getNodeRef()).thenReturn(attachRef);

    when(attachmentService.getAttachements(TEST_NODE_REF)).thenReturn(
      Collections.singletonList(attachment)
    );

    ContentReader reader = mock(ContentReader.class);
    when(reader.getSize()).thenReturn(3000L);
    when(reader.getEncoding()).thenReturn("UTF-8");
    when(reader.getMimetype()).thenReturn("application/pdf");
    when(contentService.getReader(eq(attachRef), any(QName.class))).thenReturn(
      reader
    );

    long remaining = topicsApi.getAttachmentsRemainingSize(TEST_ID);

    assertEquals(7000L, remaining);
  }

  @Test
  public void testGetAttachmentsRemainingSize_whenExceedsLimit_thenReturnsZero() {
    when(circabcConfig.getPostsAllowedAttachmentSizeinBytes()).thenReturn(
      "1000"
    );
    when(secureNodeService.exists(TEST_NODE_REF)).thenReturn(true);

    NodeRef attachRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "attach-2"
    );
    Attachement attachment = mock(Attachement.class);
    when(attachment.geType()).thenReturn(
      Attachement.AttachementType.HIDDEN_FILE
    );
    when(attachment.getSize()).thenReturn(2000L);
    when(attachment.getNodeRef()).thenReturn(attachRef);

    when(attachmentService.getAttachements(TEST_NODE_REF)).thenReturn(
      Collections.singletonList(attachment)
    );

    ContentReader reader = mock(ContentReader.class);
    when(reader.getSize()).thenReturn(2000L);
    when(reader.getEncoding()).thenReturn("UTF-8");
    when(reader.getMimetype()).thenReturn("application/pdf");
    when(contentService.getReader(eq(attachRef), any(QName.class))).thenReturn(
      reader
    );

    long remaining = topicsApi.getAttachmentsRemainingSize(TEST_ID);

    assertEquals(0L, remaining);
  }
}
