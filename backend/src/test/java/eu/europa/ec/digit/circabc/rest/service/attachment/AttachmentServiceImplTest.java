package eu.europa.ec.digit.circabc.rest.service.attachment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.helper.ContentManager;
import eu.europa.ec.digit.circabc.rest.service.helper.TemporaryFileManager;
import io.swagger.model.Attachement;
import io.swagger.model.alfresco.DocumentModel;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

public class AttachmentServiceImplTest {

  private AttachmentServiceImpl service;
  private NodeService nodeService;
  private ContentManager contentManager;
  private TemporaryFileManager temporaryFileManager;

  private NodeRef referer;
  private NodeRef refered;

  @Before
  public void setUp() throws Exception {
    service = new AttachmentServiceImpl();
    nodeService = mock(NodeService.class);
    contentManager = mock(ContentManager.class);
    temporaryFileManager = mock(TemporaryFileManager.class);

    setField("nodeService", nodeService);
    setField("contentManager", contentManager);
    setField("temporaryFileManager", temporaryFileManager);

    referer = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "referer-id"
    );
    refered = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "refered-id"
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AttachmentServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testGetAttachementsNodes_whenNoAspect_thenReturnsEmptyList() {
    when(
      nodeService.hasAspect(referer, DocumentModel.ASPECT_ATTACHABLE)
    ).thenReturn(false);

    List<NodeRef> result = service.getAttachementsNodes(referer);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetAttachementsNodes_whenHasAspect_thenReturnsBothAssocTypes() {
    when(
      nodeService.hasAspect(referer, DocumentModel.ASPECT_ATTACHABLE)
    ).thenReturn(true);

    AssociationRef externalAssoc = mock(AssociationRef.class);
    NodeRef externalTarget = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ext-id"
    );
    when(externalAssoc.getTargetRef()).thenReturn(externalTarget);
    when(
      nodeService.getTargetAssocs(
        referer,
        DocumentModel.ASSOC_EXTERNAL_REFERENCES
      )
    ).thenReturn(Collections.singletonList(externalAssoc));

    ChildAssociationRef hiddenAssoc = mock(ChildAssociationRef.class);
    NodeRef hiddenChild = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "hidden-id"
    );
    when(hiddenAssoc.getChildRef()).thenReturn(hiddenChild);
    when(
      nodeService.getChildAssocs(
        referer,
        DocumentModel.ASSOC_HIDDEN_REFERENCES,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(hiddenAssoc));

    List<NodeRef> result = service.getAttachementsNodes(referer);

    assertEquals(2, result.size());
    assertTrue(result.contains(externalTarget));
    assertTrue(result.contains(hiddenChild));
  }

  @Test
  public void testRemoveAttachement_whenHiddenAttachment_thenDeletesNode() {
    when(
      nodeService.hasAspect(referer, DocumentModel.ASPECT_ATTACHABLE)
    ).thenReturn(true);
    when(
      nodeService.getTargetAssocs(
        referer,
        DocumentModel.ASSOC_EXTERNAL_REFERENCES
      )
    ).thenReturn(Collections.emptyList());

    ChildAssociationRef hiddenAssoc = mock(ChildAssociationRef.class);
    when(hiddenAssoc.getChildRef()).thenReturn(refered);
    when(
      nodeService.getChildAssocs(
        referer,
        DocumentModel.ASSOC_HIDDEN_REFERENCES,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(hiddenAssoc));

    when(nodeService.getType(refered)).thenReturn(
      DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT
    );

    service.removeAttachement(referer, refered);

    verify(nodeService).deleteNode(refered);
  }

  @Test
  public void testRemoveAttachement_whenExternalAttachment_thenRemovesAssociation() {
    when(
      nodeService.hasAspect(referer, DocumentModel.ASPECT_ATTACHABLE)
    ).thenReturn(true);

    AssociationRef externalAssoc = mock(AssociationRef.class);
    when(externalAssoc.getTargetRef()).thenReturn(refered);
    when(
      nodeService.getTargetAssocs(
        referer,
        DocumentModel.ASSOC_EXTERNAL_REFERENCES
      )
    ).thenReturn(Collections.singletonList(externalAssoc));
    when(
      nodeService.getChildAssocs(
        referer,
        DocumentModel.ASSOC_HIDDEN_REFERENCES,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.emptyList());

    when(nodeService.getType(refered)).thenReturn(
      QName.createQName("cm", "content")
    );

    service.removeAttachement(referer, refered);

    verify(nodeService).removeAssociation(
      referer,
      refered,
      DocumentModel.ASSOC_EXTERNAL_REFERENCES
    );
    verify(nodeService, never()).deleteNode(refered);
  }

  @Test
  public void testRemoveAttachement_whenNotAttached_thenDoesNothing() {
    when(
      nodeService.hasAspect(referer, DocumentModel.ASPECT_ATTACHABLE)
    ).thenReturn(false);

    service.removeAttachement(referer, refered);

    verify(nodeService, never()).deleteNode(any());
    verify(nodeService, never()).removeAssociation(any(), any(), any());
  }

  @Test
  public void testAddAttachement_whenTempFile_thenMovesNode() {
    when(temporaryFileManager.isTempFile(refered)).thenReturn(true);

    ChildAssociationRef moveResult = mock(ChildAssociationRef.class);
    NodeRef movedChild = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "moved-id"
    );
    when(moveResult.getChildRef()).thenReturn(movedChild);
    when(
      nodeService.moveNode(
        refered,
        referer,
        DocumentModel.ASSOC_HIDDEN_REFERENCES,
        DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT
      )
    ).thenReturn(moveResult);

    NodeRef result = service.addAttachement(referer, refered);

    assertEquals(movedChild, result);
    verify(nodeService).addAspect(
      referer,
      DocumentModel.ASPECT_ATTACHABLE,
      null
    );
  }

  @Test
  public void testAddAttachement_whenNotTempFile_thenCreatesAssociation() {
    when(temporaryFileManager.isTempFile(refered)).thenReturn(false);
    when(
      nodeService.hasAspect(referer, DocumentModel.ASPECT_ATTACHABLE)
    ).thenReturn(false);

    NodeRef result = service.addAttachement(referer, refered);

    assertEquals(refered, result);
    verify(nodeService).addAspect(
      referer,
      DocumentModel.ASPECT_ATTACHABLE,
      null
    );
    verify(nodeService).createAssociation(
      referer,
      refered,
      DocumentModel.ASSOC_EXTERNAL_REFERENCES
    );
  }

  @Test
  public void testAddAttachement_withFile_thenDelegatesToContentManager() {
    File file = new File("/tmp/test.txt");
    NodeRef created = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "created-id"
    );

    when(
      contentManager.createContent(
        referer,
        "test.txt",
        DocumentModel.ASSOC_HIDDEN_REFERENCES,
        DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT,
        file,
        false
      )
    ).thenReturn(created);

    NodeRef result = service.addAttachement(referer, "test.txt", file);

    assertEquals(created, result);
    verify(nodeService).addAspect(
      referer,
      DocumentModel.ASPECT_ATTACHABLE,
      null
    );
  }

  @Test
  public void testIsHiddenAttachement_whenHiddenType_thenReturnsTrue() {
    when(nodeService.getType(refered)).thenReturn(
      DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT
    );

    assertTrue(service.isHiddenAttachement(refered));
  }

  @Test
  public void testIsHiddenAttachement_whenOtherType_thenReturnsFalse() {
    when(nodeService.getType(refered)).thenReturn(
      QName.createQName("cm", "content")
    );

    assertFalse(service.isHiddenAttachement(refered));
  }
}
