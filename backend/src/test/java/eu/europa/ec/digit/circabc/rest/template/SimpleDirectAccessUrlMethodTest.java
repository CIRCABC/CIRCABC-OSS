package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateModelException;
import java.util.Arrays;
import java.util.Collections;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class SimpleDirectAccessUrlMethodTest {

  private SimpleDirectAccessUrlMethod method;

  @Before
  public void setUp() {
    method = new SimpleDirectAccessUrlMethod();
  }

  @Test
  public void testGetResult_whenValidNodeRef_thenReturnsBrowseUrl() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "abc-123"
    );
    String result = method.getResult(nodeRef);
    assertEquals("/w/browse/abc-123", result);
  }

  @Test
  public void testExec_whenArgIsNodeRef_thenReturnsBrowseUrl()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id-1"
    );
    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(nodeRef);

    Object result = method.exec(Collections.singletonList(beanModel));
    assertEquals("/w/browse/node-id-1", result);
  }

  @Test
  public void testExec_whenArgIsTemplateNode_thenReturnsBrowseUrl()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "tmpl-node-id"
    );
    TemplateNode templateNode = mock(TemplateNode.class);
    when(templateNode.getNodeRef()).thenReturn(nodeRef);

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(templateNode);

    Object result = method.exec(Collections.singletonList(beanModel));
    assertEquals("/w/browse/tmpl-node-id", result);
  }

  @Test
  public void testExec_whenNoArgs_thenReturnsEmptyString()
    throws TemplateModelException {
    Object result = method.exec(Collections.emptyList());
    assertEquals("", result);
  }

  @Test
  public void testExec_whenTooManyArgs_thenReturnsEmptyString()
    throws TemplateModelException {
    BeanModel beanModel = mock(BeanModel.class);
    Object result = method.exec(Arrays.asList(beanModel, beanModel));
    assertEquals("", result);
  }
}
