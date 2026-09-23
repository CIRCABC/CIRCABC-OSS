package eu.europa.ec.digit.circabc.rest.model;

import static org.mockito.Mockito.*;

import io.swagger.exception.CircabcRuntimeException;
import io.swagger.model.alfresco.DossierModel;
import java.lang.reflect.Field;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class DossierModelTypeTest {

  private DossierModelType dossierModelType;
  private NodeService nodeService;

  @Before
  public void setUp() throws Exception {
    dossierModelType = new DossierModelType();
    nodeService = mock(NodeService.class);
    setField("nodeService", nodeService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = DossierModelType.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(dossierModelType, value);
  }

  @Test
  public void testBeforeCreateNode_whenFileLinkInDossier_thenAllowed() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(nodeService.getType(parentRef)).thenReturn(
      DossierModel.TYPE_DOSSIER_SPACE
    );

    dossierModelType.beforeCreateNode(
      parentRef,
      ContentModel.ASSOC_CONTAINS,
      ContentModel.ASSOC_CONTAINS,
      ApplicationModel.TYPE_FILELINK
    );
  }

  @Test
  public void testBeforeCreateNode_whenFolderLinkInDossier_thenAllowed() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(nodeService.getType(parentRef)).thenReturn(
      DossierModel.TYPE_DOSSIER_SPACE
    );

    dossierModelType.beforeCreateNode(
      parentRef,
      ContentModel.ASSOC_CONTAINS,
      ContentModel.ASSOC_CONTAINS,
      ApplicationModel.TYPE_FOLDERLINK
    );
  }

  @Test
  public void testBeforeCreateNode_whenForumInDossier_thenAllowed() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(nodeService.getType(parentRef)).thenReturn(
      DossierModel.TYPE_DOSSIER_SPACE
    );

    dossierModelType.beforeCreateNode(
      parentRef,
      ContentModel.ASSOC_CONTAINS,
      ContentModel.ASSOC_CONTAINS,
      ForumModel.TYPE_FORUM
    );
  }

  @Test(expected = CircabcRuntimeException.class)
  public void testBeforeCreateNode_whenContentInDossier_thenThrows() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(nodeService.getType(parentRef)).thenReturn(
      DossierModel.TYPE_DOSSIER_SPACE
    );

    dossierModelType.beforeCreateNode(
      parentRef,
      ContentModel.ASSOC_CONTAINS,
      ContentModel.ASSOC_CONTAINS,
      ContentModel.TYPE_CONTENT
    );
  }

  @Test
  public void testBeforeCreateNode_whenParentNotDossier_thenAllowed() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(nodeService.getType(parentRef)).thenReturn(ContentModel.TYPE_FOLDER);

    dossierModelType.beforeCreateNode(
      parentRef,
      ContentModel.ASSOC_CONTAINS,
      ContentModel.ASSOC_CONTAINS,
      ContentModel.TYPE_CONTENT
    );
  }

  @Test(expected = CircabcRuntimeException.class)
  public void testOnMoveNode_whenContentMovedToDossier_thenThrows() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    ChildAssociationRef oldAssoc = mock(ChildAssociationRef.class);
    ChildAssociationRef newAssoc = mock(ChildAssociationRef.class);
    when(newAssoc.getParentRef()).thenReturn(parentRef);
    when(newAssoc.getChildRef()).thenReturn(childRef);
    when(nodeService.getType(parentRef)).thenReturn(
      DossierModel.TYPE_DOSSIER_SPACE
    );
    when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_CONTENT);

    dossierModelType.onMoveNode(oldAssoc, newAssoc);
  }

  @Test
  public void testOnMoveNode_whenFileLinkMovedToDossier_thenAllowed() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    ChildAssociationRef oldAssoc = mock(ChildAssociationRef.class);
    ChildAssociationRef newAssoc = mock(ChildAssociationRef.class);
    when(newAssoc.getParentRef()).thenReturn(parentRef);
    when(newAssoc.getChildRef()).thenReturn(childRef);
    when(nodeService.getType(parentRef)).thenReturn(
      DossierModel.TYPE_DOSSIER_SPACE
    );
    when(nodeService.getType(childRef)).thenReturn(
      ApplicationModel.TYPE_FILELINK
    );

    dossierModelType.onMoveNode(oldAssoc, newAssoc);
  }
}
