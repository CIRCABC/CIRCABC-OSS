package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.template.TemplateModelException;
import io.swagger.model.alfresco.CircabcModel;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class NodePathMethodTest {

  private NodePathMethod nodePathMethod;
  private NodeService nodeService;

  @Before
  public void setUp() {
    nodePathMethod = new NodePathMethod();
    nodeService = mock(NodeService.class);
    nodePathMethod.setNodeService(nodeService);
  }

  @Test
  public void testGetResult_whenNodeIsCircabcRoot_thenReturnsSlashName()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root-id"
    );

    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_CIRCABC_ROOT);

    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getAspects(nodeRef)).thenReturn(aspects);
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "CircaBC"
    );

    String result = nodePathMethod.getResult(nodeRef);

    assertEquals("/CircaBC", result);
  }

  @Test
  public void testGetResult_whenNodeHasParent_thenReturnsFullPath()
    throws TemplateModelException {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    Set<QName> parentAspects = new HashSet<>();
    parentAspects.add(CircabcModel.ASPECT_CIRCABC_ROOT);

    Set<QName> childAspects = new HashSet<>();

    when(nodeService.getType(parentRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getAspects(parentRef)).thenReturn(parentAspects);
    when(nodeService.getProperty(parentRef, ContentModel.PROP_NAME)).thenReturn(
      "Root"
    );

    when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getAspects(childRef)).thenReturn(childAspects);
    when(nodeService.getPrimaryParent(childRef)).thenReturn(
      new ChildAssociationRef(
        ContentModel.ASSOC_CONTAINS,
        parentRef,
        ContentModel.PROP_NAME,
        childRef
      )
    );
    when(nodeService.getProperty(childRef, ContentModel.PROP_NAME)).thenReturn(
      "MyFolder"
    );

    String result = nodePathMethod.getResult(childRef);

    assertEquals("/Root/MyFolder", result);
  }

  @Test(expected = TemplateModelException.class)
  public void testGetResult_whenPathNotFound_thenThrowsTemplateModelException()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "orphan-id"
    );

    Set<QName> aspects = new HashSet<>();

    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getAspects(nodeRef)).thenReturn(aspects);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(null);

    nodePathMethod.getResult(nodeRef);
  }
}
