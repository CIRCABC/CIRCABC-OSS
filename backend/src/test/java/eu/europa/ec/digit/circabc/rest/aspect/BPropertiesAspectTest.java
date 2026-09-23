package eu.europa.ec.digit.circabc.rest.aspect;

import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.DocumentModel;
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

public class BPropertiesAspectTest {

  private BPropertiesAspect aspect;
  private NodeService nodeService;
  private PolicyComponent policyComponent;

  private void setField(String fieldName, Object value) throws Exception {
    Field field = BPropertiesAspect.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(aspect, value);
  }

  @Before
  public void setUp() throws Exception {
    aspect = new BPropertiesAspect();
    nodeService = mock(NodeService.class);
    policyComponent = mock(PolicyComponent.class);
    setField("nodeService", nodeService);
    setField("policyComponent", policyComponent);
  }

  @Test
  public void testOnAddAspect_whenNodeDoesNotExist_thenReturns() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.exists(nodeRef)).thenReturn(false);

    aspect.onAddAspect(nodeRef, DocumentModel.ASPECT_BPROPERTIES);

    verify(nodeService, never()).getType(any(NodeRef.class));
  }

  @Test
  public void testOnAddAspect_whenNotMultilingualContainer_thenDoesNotRemoveAspect() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    QName nonMatchingType = ContentModel.TYPE_CONTENT;
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(nodeService.getType(nodeRef)).thenReturn(nonMatchingType);

    aspect.onAddAspect(nodeRef, DocumentModel.ASPECT_BPROPERTIES);

    verify(nodeService, never()).getChildAssocs(nodeRef);
  }

  @Test
  public void testOnAddAspect_whenMultilingualContainerWithBProperties_thenRemovesAspectFromChildren() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);

    when(nodeService.exists(parentRef)).thenReturn(true);
    when(nodeService.getType(parentRef)).thenReturn(
      ContentModel.TYPE_MULTILINGUAL_CONTAINER
    );
    when(nodeService.getChildAssocs(parentRef)).thenReturn(
      Arrays.asList(childAssoc)
    );
    when(childAssoc.getChildRef()).thenReturn(childRef);
    when(
      nodeService.hasAspect(childRef, DocumentModel.ASPECT_BPROPERTIES)
    ).thenReturn(true);

    aspect.onAddAspect(parentRef, DocumentModel.ASPECT_BPROPERTIES);

    verify(nodeService).removeAspect(
      childRef,
      DocumentModel.ASPECT_BPROPERTIES
    );
  }

  @Test
  public void testOnAddAspect_whenChildDoesNotHaveAspect_thenDoesNotRemove() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);

    when(nodeService.exists(parentRef)).thenReturn(true);
    when(nodeService.getType(parentRef)).thenReturn(
      ContentModel.TYPE_MULTILINGUAL_CONTAINER
    );
    when(nodeService.getChildAssocs(parentRef)).thenReturn(
      Arrays.asList(childAssoc)
    );
    when(childAssoc.getChildRef()).thenReturn(childRef);
    when(
      nodeService.hasAspect(childRef, DocumentModel.ASPECT_BPROPERTIES)
    ).thenReturn(false);

    aspect.onAddAspect(parentRef, DocumentModel.ASPECT_BPROPERTIES);

    verify(nodeService, never()).removeAspect(eq(childRef), any(QName.class));
  }

  @Test
  public void testOnAddAspect_whenNoChildren_thenNothingRemoved() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );

    when(nodeService.exists(parentRef)).thenReturn(true);
    when(nodeService.getType(parentRef)).thenReturn(
      ContentModel.TYPE_MULTILINGUAL_CONTAINER
    );
    when(nodeService.getChildAssocs(parentRef)).thenReturn(
      Collections.emptyList()
    );

    aspect.onAddAspect(parentRef, DocumentModel.ASPECT_BPROPERTIES);

    verify(nodeService, never()).removeAspect(any(NodeRef.class), any());
  }

  @Test
  public void testOnCreateNode_addsAspectToChild() {
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );
    ChildAssociationRef ref = mock(ChildAssociationRef.class);
    when(ref.getChildRef()).thenReturn(childRef);

    aspect.onCreateNode(ref);

    verify(nodeService).addAspect(
      childRef,
      DocumentModel.ASPECT_BPROPERTIES,
      null
    );
  }
}
