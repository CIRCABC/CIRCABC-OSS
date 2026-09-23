package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateModelException;
import java.util.Arrays;
import java.util.Collections;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class NodeRefBaseTemplateProcessorExtensionTest {

  private NodeRefBaseTemplateProcessorExtension extension;
  private NodeService nodeService;

  @Before
  public void setUp() {
    nodeService = mock(NodeService.class);
    extension = new NodeRefBaseTemplateProcessorExtension() {
      @Override
      public String getResult(NodeRef nodeRef) throws TemplateModelException {
        return nodeRef.toString();
      }
    };
    extension.setNodeService(nodeService);
  }

  @Test
  public void testExec_whenArgIsNodeRef_thenReturnsResult()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(nodeRef);

    Object result = extension.exec(Arrays.asList(beanModel));

    assertEquals(nodeRef.toString(), result);
  }

  @Test
  public void testExec_whenArgIsTemplateNode_thenReturnsResult()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    TemplateNode templateNode = mock(TemplateNode.class);
    when(templateNode.getNodeRef()).thenReturn(nodeRef);

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(templateNode);

    Object result = extension.exec(Arrays.asList(beanModel));

    assertEquals(nodeRef.toString(), result);
  }

  @Test
  public void testExec_whenEmptyArgs_thenReturnsEmptyString()
    throws TemplateModelException {
    Object result = extension.exec(Collections.emptyList());

    assertEquals("", result);
  }

  @Test
  public void testExec_whenMultipleArgs_thenReturnsEmptyString()
    throws TemplateModelException {
    BeanModel arg1 = mock(BeanModel.class);
    BeanModel arg2 = mock(BeanModel.class);

    Object result = extension.exec(Arrays.asList(arg1, arg2));

    assertEquals("", result);
  }

  @Test
  public void testExec_whenWrappedObjectIsUnknownType_thenReturnsEmptyString()
    throws TemplateModelException {
    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn("some-string");

    Object result = extension.exec(Arrays.asList(beanModel));

    assertEquals("", result);
  }

  @Test
  public void testGetNodeService_returnsSetNodeService() {
    assertSame(nodeService, extension.getNodeService());
  }
}
