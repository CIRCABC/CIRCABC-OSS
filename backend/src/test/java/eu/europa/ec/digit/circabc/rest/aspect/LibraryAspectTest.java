package eu.europa.ec.digit.circabc.rest.aspect;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class LibraryAspectTest {

  private LibraryAspect libraryAspect;
  private NodeService nodeService;
  private PolicyComponent policyComponent;

  @Before
  public void setUp() throws Exception {
    libraryAspect = new LibraryAspect();
    nodeService = mock(NodeService.class);
    policyComponent = mock(PolicyComponent.class);
    setField("nodeService", nodeService);
    setField("policyComponent", policyComponent);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = LibraryAspect.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(libraryAspect, value);
  }

  @Test
  public void testInitialise_registersPolicy() {
    libraryAspect.initialise();
    verify(policyComponent).bindClassBehaviour(
      any(QName.class),
      any(QName.class),
      any()
    );
  }

  @Test
  public void testBeforeDeleteNode_whenFolder_deletesEmptyTranslations() {
    NodeRef folderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(childRef);

    when(nodeService.getType(folderRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getChildAssocs(folderRef)).thenReturn(
      Arrays.asList(childAssoc)
    );
    when(
      nodeService.hasAspect(
        childRef,
        ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION
      )
    ).thenReturn(true);

    libraryAspect.beforeDeleteNode(folderRef);

    verify(nodeService).deleteNode(childRef);
  }

  @Test
  public void testBeforeDeleteNode_whenFolder_skipsNonEmptyTranslation() {
    NodeRef folderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(childRef);

    when(nodeService.getType(folderRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getChildAssocs(folderRef)).thenReturn(
      Arrays.asList(childAssoc)
    );
    when(
      nodeService.hasAspect(
        childRef,
        ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION
      )
    ).thenReturn(false);

    libraryAspect.beforeDeleteNode(folderRef);

    verify(nodeService, never()).deleteNode(any(NodeRef.class));
  }

  @Test
  public void testBeforeDeleteNode_whenNotFolder_doesNothing() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );

    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);

    libraryAspect.beforeDeleteNode(nodeRef);

    verify(nodeService, never()).getChildAssocs(any(NodeRef.class));
    verify(nodeService, never()).deleteNode(any(NodeRef.class));
  }

  @Test
  public void testBeforeDeleteNode_whenFolderEmpty_deletesNothing() {
    NodeRef folderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "empty-folder"
    );

    when(nodeService.getType(folderRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getChildAssocs(folderRef)).thenReturn(
      Collections.emptyList()
    );

    libraryAspect.beforeDeleteNode(folderRef);

    verify(nodeService, never()).deleteNode(any(NodeRef.class));
  }
}
