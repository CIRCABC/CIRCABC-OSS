package eu.europa.ec.digit.circabc.rest.service.newsgroup;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import io.swagger.model.AbuseReport;
import io.swagger.model.alfresco.ModerationModel;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class ModerationServiceImplTest {

  private ModerationServiceImpl service;
  private NodeService nodeService;
  private DictionaryService dictionaryService;
  private PermissionService permissionService;
  private ContentService contentService;
  private BehaviourFilter policyBehaviourFilter;

  private NodeRef contentNode;
  private NodeRef containerNode;

  @Before
  public void setUp() throws Exception {
    // Initialize AuthenticationUtil
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

    service = new ModerationServiceImpl();
    nodeService = mock(NodeService.class);
    dictionaryService = mock(DictionaryService.class);
    permissionService = mock(PermissionService.class);
    contentService = mock(ContentService.class);
    policyBehaviourFilter = mock(BehaviourFilter.class);

    setField("nodeService", nodeService);
    setField("dictionaryService", dictionaryService);
    setField("permissionService", permissionService);
    setField("contentService", contentService);
    setField("policyBehaviourFilter", policyBehaviourFilter);

    contentNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "content-id"
    );
    containerNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container-id"
    );

    // Default: contentNode is not a container (type is cm:content)
    when(nodeService.getType(contentNode)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        ContentModel.TYPE_FOLDER
      )
    ).thenReturn(false);

    // Default: containerNode is a forum container
    when(nodeService.getType(containerNode)).thenReturn(ForumModel.TYPE_FORUM);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ModerationServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  // --- accept tests ---

  @Test
  public void testAccept_whenWaitingForApproval_thenApproved() {
    when(
      nodeService.hasAspect(
        contentNode,
        ModerationModel.ASPECT_WAITING_APPROVAL
      )
    ).thenReturn(true);

    service.accept(contentNode);

    verify(permissionService).setInheritParentPermissions(contentNode, true);
    verify(nodeService).addAspect(
      eq(contentNode),
      eq(ModerationModel.ASPECT_APPROVED),
      anyMap()
    );
    verify(nodeService).removeAspect(
      contentNode,
      ModerationModel.ASPECT_WAITING_APPROVAL
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAccept_whenContainer_thenThrows() {
    service.accept(containerNode);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAccept_whenNotWaiting_thenThrows() {
    when(
      nodeService.hasAspect(
        contentNode,
        ModerationModel.ASPECT_WAITING_APPROVAL
      )
    ).thenReturn(false);

    service.accept(contentNode);
  }

  // --- reject tests ---

  @Test
  public void testReject_whenWaitingForApproval_thenRejected() {
    when(
      nodeService.hasAspect(
        contentNode,
        ModerationModel.ASPECT_WAITING_APPROVAL
      )
    ).thenReturn(true);
    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(contentNode, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);

    service.reject(contentNode, "bad content");

    verify(nodeService).addAspect(
      eq(contentNode),
      eq(ModerationModel.ASPECT_REJECTED),
      anyMap()
    );
    verify(writer).putContent("");
    verify(nodeService).removeAspect(
      contentNode,
      ModerationModel.ASPECT_WAITING_APPROVAL
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReject_whenContainer_thenThrows() {
    service.reject(containerNode, "msg");
  }

  // --- waitForApproval tests ---

  @Test
  public void testWaitForApproval_whenContent_thenAspectAdded() {
    when(
      nodeService.hasAspect(contentNode, ModerationModel.ASPECT_REJECTED)
    ).thenReturn(false);

    service.waitForApproval(contentNode);

    verify(nodeService).addAspect(
      contentNode,
      ModerationModel.ASPECT_WAITING_APPROVAL,
      null
    );
    verify(permissionService).setInheritParentPermissions(contentNode, false);
  }

  @Test
  public void testWaitForApproval_whenRejected_thenRejectedAspectRemoved() {
    when(
      nodeService.hasAspect(contentNode, ModerationModel.ASPECT_REJECTED)
    ).thenReturn(true);

    service.waitForApproval(contentNode);

    verify(nodeService).removeAspect(
      contentNode,
      ModerationModel.ASPECT_REJECTED
    );
    verify(nodeService).addAspect(
      contentNode,
      ModerationModel.ASPECT_WAITING_APPROVAL,
      null
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWaitForApproval_whenContainer_thenThrows() {
    service.waitForApproval(containerNode);
  }

  // --- signalAbuse tests ---

  @Test
  public void testSignalAbuse_whenFirstAbuse_thenAspectAdded() {
    when(
      nodeService.hasAspect(contentNode, ModerationModel.ASPECT_ABUSE_SIGNALED)
    ).thenReturn(false);

    AbuseReport result = service.signalAbuse(contentNode, "offensive");

    assertNotNull(result);
    verify(nodeService).addAspect(
      eq(contentNode),
      eq(ModerationModel.ASPECT_ABUSE_SIGNALED),
      anyMap()
    );
  }

  @Test
  public void testSignalAbuse_whenAlreadySignaled_thenPropertyUpdated() {
    when(
      nodeService.hasAspect(contentNode, ModerationModel.ASPECT_ABUSE_SIGNALED)
    ).thenReturn(true);
    when(
      nodeService.getProperty(contentNode, ModerationModel.PROP_ABUSE_MESSAGES)
    ).thenReturn(null);

    AbuseReport result = service.signalAbuse(contentNode, "spam");

    assertNotNull(result);
    verify(nodeService).setProperty(
      eq(contentNode),
      eq(ModerationModel.PROP_ABUSE_MESSAGES),
      any(Serializable.class)
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSignalAbuse_whenContainer_thenThrows() {
    service.signalAbuse(containerNode, "msg");
  }

  // --- signalNotAbuse tests ---

  @Test
  public void testSignalNotAbuse_whenAbuseSignaled_thenAspectRemoved() {
    when(
      nodeService.hasAspect(contentNode, ModerationModel.ASPECT_ABUSE_SIGNALED)
    ).thenReturn(true);

    service.signalNotAbuse(contentNode);

    verify(nodeService).removeAspect(
      contentNode,
      ModerationModel.ASPECT_ABUSE_SIGNALED
    );
    verify(nodeService).removeProperty(
      contentNode,
      ModerationModel.PROP_ABUSE_MESSAGES
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSignalNotAbuse_whenNoAbuse_thenThrows() {
    when(
      nodeService.hasAspect(contentNode, ModerationModel.ASPECT_ABUSE_SIGNALED)
    ).thenReturn(false);

    service.signalNotAbuse(contentNode);
  }

  // --- applyModeration tests ---

  @Test
  public void testApplyModeration_whenContainer_thenAspectApplied() {
    when(
      nodeService.hasAspect(containerNode, ModerationModel.ASPECT_MODERATED)
    ).thenReturn(false);
    when(nodeService.getChildAssocs(containerNode)).thenReturn(
      Collections.emptyList()
    );

    service.applyModeration(containerNode, false);

    verify(nodeService).addAspect(
      eq(containerNode),
      eq(ModerationModel.ASPECT_MODERATED),
      anyMap()
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testApplyModeration_whenNotContainer_thenThrows() {
    service.applyModeration(contentNode, false);
  }

  // --- stopModeration tests ---

  @Test(expected = IllegalArgumentException.class)
  public void testStopModeration_whenInvalidAction_thenThrows() {
    service.stopModeration(containerNode, "invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStopModeration_whenNotContainer_thenThrows() {
    service.stopModeration(contentNode, "accept");
  }

  // --- isContainerModerated tests ---

  @Test
  public void testIsContainerModerated_whenModerated_thenTrue() {
    when(
      nodeService.hasAspect(containerNode, ModerationModel.ASPECT_MODERATED)
    ).thenReturn(true);
    when(
      nodeService.getProperty(containerNode, ModerationModel.PROP_IS_MODERATED)
    ).thenReturn(Boolean.TRUE);

    assertTrue(service.isContainerModerated(containerNode));
  }

  @Test
  public void testIsContainerModerated_whenNotModerated_thenFalse() {
    when(
      nodeService.hasAspect(containerNode, ModerationModel.ASPECT_MODERATED)
    ).thenReturn(false);

    assertFalse(service.isContainerModerated(containerNode));
  }

  // --- isWaitingForApproval / isApproved / isRejected ---

  @Test
  public void testIsWaitingForApproval_whenHasAspect_thenTrue() {
    when(
      nodeService.hasAspect(
        contentNode,
        ModerationModel.ASPECT_WAITING_APPROVAL
      )
    ).thenReturn(true);

    assertTrue(service.isWaitingForApproval(contentNode));
  }

  @Test
  public void testIsApproved_whenHasAspect_thenTrue() {
    when(
      nodeService.hasAspect(contentNode, ModerationModel.ASPECT_APPROVED)
    ).thenReturn(true);

    assertTrue(service.isApproved(contentNode));
  }

  @Test
  public void testIsRejected_whenHasAspect_thenTrue() {
    when(
      nodeService.hasAspect(contentNode, ModerationModel.ASPECT_REJECTED)
    ).thenReturn(true);

    assertTrue(service.isRejected(contentNode));
  }
}
