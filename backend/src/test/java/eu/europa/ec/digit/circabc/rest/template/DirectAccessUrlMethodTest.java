package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class DirectAccessUrlMethodTest {

  private DirectAccessUrlMethod method;

  @Before
  public void setUp() {
    method = new DirectAccessUrlMethod();
  }

  @Test
  public void testGetResult_whenValidNodeRef_thenReturnsExpectedUrl()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "abc-123"
    );
    String result = method.getResult(nodeRef);
    assertEquals("/w/browse/abc-123", result);
  }

  @Test
  public void testExec_whenArgIsNodeRef_thenReturnsUrl()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id-1"
    );
    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(nodeRef);

    List args = new ArrayList();
    args.add(beanModel);

    Object result = method.exec(args);
    assertEquals("/w/browse/node-id-1", result);
  }

  @Test
  public void testExec_whenArgIsTemplateNode_thenReturnsUrl()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "tmpl-node-id"
    );
    TemplateNode templateNode = mock(TemplateNode.class);
    when(templateNode.getNodeRef()).thenReturn(nodeRef);

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(templateNode);

    List args = new ArrayList();
    args.add(beanModel);

    Object result = method.exec(args);
    assertEquals("/w/browse/tmpl-node-id", result);
  }

  @Test
  public void testExec_whenNoArgs_thenReturnsEmptyString()
    throws TemplateModelException {
    List args = new ArrayList();
    Object result = method.exec(args);
    assertEquals("", result);
  }

  @Test
  public void testExec_whenWrappedObjectIsUnknownType_thenReturnsEmptyString()
    throws TemplateModelException {
    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn("some-string");

    List args = new ArrayList();
    args.add(beanModel);

    Object result = method.exec(args);
    assertEquals("", result);
  }
}
