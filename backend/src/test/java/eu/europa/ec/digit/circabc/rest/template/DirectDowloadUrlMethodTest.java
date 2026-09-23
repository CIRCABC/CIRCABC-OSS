package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateModelException;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class DirectDowloadUrlMethodTest {

  private DirectDowloadUrlMethod method;
  private NodeService nodeService;

  @Before
  public void setUp() {
    method = new DirectDowloadUrlMethod();
    nodeService = mock(NodeService.class);
    method.setNodeService(nodeService);
  }

  @Test
  public void testGetResult_whenValidNodeRef_thenReturnsDirectDownloadUrl() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "abc-123"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "document.pdf"
    );

    String result = method.getResult(nodeRef);

    assertEquals("/d/d/workspace/SpacesStore/abc-123/document.pdf", result);
  }

  @Test
  public void testExec_whenArgIsNodeRef_thenReturnsUrl()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "file.txt"
    );

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(nodeRef);

    @SuppressWarnings("unchecked")
    List args = Collections.singletonList(beanModel);

    Object result = method.exec(args);

    assertEquals("/d/d/workspace/SpacesStore/node-id/file.txt", result);
  }

  @Test
  public void testExec_whenArgIsTemplateNode_thenReturnsUrl()
    throws TemplateModelException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "tmpl-id"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "report.docx"
    );

    TemplateNode templateNode = mock(TemplateNode.class);
    when(templateNode.getNodeRef()).thenReturn(nodeRef);

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(templateNode);

    @SuppressWarnings("unchecked")
    List args = Collections.singletonList(beanModel);

    Object result = method.exec(args);

    assertEquals("/d/d/workspace/SpacesStore/tmpl-id/report.docx", result);
  }

  @Test
  public void testExec_whenNoArgs_thenReturnsEmptyString()
    throws TemplateModelException {
    @SuppressWarnings("unchecked")
    List args = Collections.emptyList();

    Object result = method.exec(args);

    assertEquals("", result);
  }
}
