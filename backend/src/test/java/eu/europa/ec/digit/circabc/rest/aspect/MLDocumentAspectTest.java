package eu.europa.ec.digit.circabc.rest.aspect;

import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.DocumentModel;
import java.lang.reflect.Field;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class MLDocumentAspectTest {

  private MLDocumentAspect aspect;
  private PolicyComponent policyComponent;
  private NodeService nodeService;

  private void setField(String fieldName, Object value) throws Exception {
    Field field = MLDocumentAspect.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(aspect, value);
  }

  @Before
  public void setUp() throws Exception {
    aspect = new MLDocumentAspect();
    policyComponent = mock(PolicyComponent.class);
    nodeService = mock(NodeService.class);
    setField("policyComponent", policyComponent);
    setField("nodeService", nodeService);
  }

  @Test
  public void testInitialise_bindsClassBehaviours() {
    aspect.initialise();

    verify(policyComponent, times(2)).bindClassBehaviour(
      any(QName.class),
      any(QName.class),
      any()
    );
  }

  @Test
  public void testOnAddAspect_whenNodeExists_removesAndAddsAspects() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.exists(nodeRef)).thenReturn(true);

    aspect.onAddAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);

    verify(nodeService).removeAspect(nodeRef, DocumentModel.ASPECT_BPROPERTIES);
    verify(nodeService).addAspect(
      nodeRef,
      DocumentModel.ASPECT_CPROPERTIES,
      null
    );
  }

  @Test
  public void testOnAddAspect_whenNodeDoesNotExist_doesNothing() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "missing-id"
    );
    when(nodeService.exists(nodeRef)).thenReturn(false);

    aspect.onAddAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);

    verify(nodeService, never()).removeAspect(any(), any());
    verify(nodeService, never()).addAspect(any(), any(), any());
  }

  @Test
  public void testBeforeDeleteNode_addsTemporaryAspect() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "delete-id"
    );

    aspect.beforeDeleteNode(nodeRef);

    verify(nodeService).addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
  }
}
