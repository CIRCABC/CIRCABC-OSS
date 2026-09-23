package eu.europa.ec.digit.circabc.rest.model;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.newsgroup.ModerationService;
import java.lang.reflect.Field;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class ModerationAspectTest {

  private ModerationAspect aspect;
  private ModerationService moderationService;
  private NodeService nodeService;
  private DictionaryService dictionaryService;

  @Before
  public void setUp() throws Exception {
    aspect = new ModerationAspect();
    moderationService = mock(ModerationService.class);
    nodeService = mock(NodeService.class);
    dictionaryService = mock(DictionaryService.class);

    setField("moderationService", moderationService);
    setField("nodeService", nodeService);
    setField("dictionaryService", dictionaryService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ModerationAspect.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(aspect, value);
  }

  @Test
  public void testOnContentUpdate_whenRejected_thenWaitForApproval() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(moderationService.isRejected(nodeRef)).thenReturn(true);

    aspect.onContentUpdate(nodeRef, false);

    verify(moderationService).waitForApproval(nodeRef);
  }

  @Test
  public void testOnContentUpdate_whenNotRejected_thenNoAction() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(moderationService.isRejected(nodeRef)).thenReturn(false);

    aspect.onContentUpdate(nodeRef, false);

    verify(moderationService, never()).waitForApproval(nodeRef);
  }

  @Test(expected = IllegalStateException.class)
  public void testOnCreateReplyAssociation_whenWaitingForApproval_thenThrows() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child"
    );
    ChildAssociationRef assocRef = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentRef,
      ContentModel.ASPECT_REFERENCING,
      childRef
    );

    when(moderationService.isWaitingForApproval(parentRef)).thenReturn(true);

    aspect.onCreateReplyAssociation(assocRef, true);
  }

  @Test(expected = IllegalStateException.class)
  public void testOnCreateReplyAssociation_whenRejected_thenThrows() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child"
    );
    ChildAssociationRef assocRef = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentRef,
      ContentModel.ASPECT_REFERENCING,
      childRef
    );

    when(moderationService.isWaitingForApproval(parentRef)).thenReturn(false);
    when(moderationService.isRejected(parentRef)).thenReturn(true);

    aspect.onCreateReplyAssociation(assocRef, true);
  }

  @Test
  public void testOnCreateReplyAssociation_whenNotReferencingQName_thenNoAction() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child"
    );
    QName otherQName = QName.createQName("http://other", "other");
    ChildAssociationRef assocRef = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentRef,
      otherQName,
      childRef
    );

    aspect.onCreateReplyAssociation(assocRef, true);

    verifyNoInteractions(moderationService);
  }

  @Test
  public void testOnCreateChildAssociation_whenContainerModerated_andChildIsContainer_thenApplyModeration() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child"
    );
    ChildAssociationRef assocRef = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentRef,
      ContentModel.ASSOC_CONTAINS,
      childRef
    );

    when(moderationService.isContainerModerated(parentRef)).thenReturn(true);
    when(nodeService.getType(childRef)).thenReturn(ForumModel.TYPE_FORUM);

    aspect.onCreateChildAssociation(assocRef, true);

    verify(moderationService).applyModeration(childRef, false);
  }

  @Test
  public void testOnCreateChildAssociation_whenContainerModerated_andChildIsPost_thenWaitForApproval() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child"
    );
    ChildAssociationRef assocRef = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentRef,
      ContentModel.ASSOC_CONTAINS,
      childRef
    );

    when(moderationService.isContainerModerated(parentRef)).thenReturn(true);
    when(nodeService.getType(childRef)).thenReturn(ForumModel.TYPE_POST);
    when(
      dictionaryService.isSubClass(
        ForumModel.TYPE_POST,
        ContentModel.TYPE_FOLDER
      )
    ).thenReturn(false);

    aspect.onCreateChildAssociation(assocRef, true);

    verify(moderationService).waitForApproval(childRef);
  }

  @Test
  public void testOnCreateChildAssociation_whenNotModerated_thenNoAction() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child"
    );
    ChildAssociationRef assocRef = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentRef,
      ContentModel.ASSOC_CONTAINS,
      childRef
    );

    when(moderationService.isContainerModerated(parentRef)).thenReturn(false);

    aspect.onCreateChildAssociation(assocRef, true);

    verify(moderationService, never()).applyModeration(any(), anyBoolean());
    verify(moderationService, never()).waitForApproval(any());
  }
}
