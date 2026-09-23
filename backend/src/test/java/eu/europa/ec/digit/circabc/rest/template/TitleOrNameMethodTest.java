package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateModelException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class TitleOrNameMethodTest {

  private TitleOrNameMethod method;
  private NodeService nodeService;
  private NodeRef nodeRef;

  @Before
  public void setUp() {
    method = new TitleOrNameMethod();
    nodeService = mock(NodeService.class);
    method.setNodeService(nodeService);
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  @Test
  public void testGetResult_whenTitleIsMLTextWithValue_thenReturnsTitle()
    throws TemplateModelException {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, "filename.txt");
    MLText mlTitle = new MLText("My Document Title");
    props.put(ContentModel.PROP_TITLE, mlTitle);
    when(nodeService.getProperties(nodeRef)).thenReturn(props);

    String result = method.getResult(nodeRef);

    assertEquals("My Document Title", result);
  }

  @Test
  public void testGetResult_whenTitleIsMLTextEmpty_thenReturnsName()
    throws TemplateModelException {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, "filename.txt");
    MLText mlTitle = new MLText("");
    props.put(ContentModel.PROP_TITLE, mlTitle);
    when(nodeService.getProperties(nodeRef)).thenReturn(props);

    String result = method.getResult(nodeRef);

    assertEquals("filename.txt", result);
  }

  @Test
  public void testGetResult_whenTitleIsString_thenReturnsTitle()
    throws TemplateModelException {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, "filename.txt");
    props.put(ContentModel.PROP_TITLE, "String Title");
    when(nodeService.getProperties(nodeRef)).thenReturn(props);

    String result = method.getResult(nodeRef);

    assertEquals("String Title", result);
  }

  @Test
  public void testGetResult_whenTitleIsEmptyString_thenReturnsName()
    throws TemplateModelException {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, "filename.txt");
    props.put(ContentModel.PROP_TITLE, "");
    when(nodeService.getProperties(nodeRef)).thenReturn(props);

    String result = method.getResult(nodeRef);

    assertEquals("filename.txt", result);
  }

  @Test
  public void testGetResult_whenTitleIsNull_thenReturnsName()
    throws TemplateModelException {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, "filename.txt");
    props.put(ContentModel.PROP_TITLE, null);
    when(nodeService.getProperties(nodeRef)).thenReturn(props);

    String result = method.getResult(nodeRef);

    assertEquals("filename.txt", result);
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testExec_whenArgIsNodeRef_thenReturnsResult()
    throws TemplateModelException {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, "test.txt");
    props.put(ContentModel.PROP_TITLE, "Exec Title");
    when(nodeService.getProperties(nodeRef)).thenReturn(props);

    BeanModel beanModel = mock(BeanModel.class);
    when(beanModel.getWrappedObject()).thenReturn(nodeRef);
    List args = new ArrayList();
    args.add(beanModel);

    Object result = method.exec(args);

    assertEquals("Exec Title", result);
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testExec_whenNoArgs_thenReturnsEmptyString()
    throws TemplateModelException {
    List args = new ArrayList();

    Object result = method.exec(args);

    assertEquals("", result);
  }
}
