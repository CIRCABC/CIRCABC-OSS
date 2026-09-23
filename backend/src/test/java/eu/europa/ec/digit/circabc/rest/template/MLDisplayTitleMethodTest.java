package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.template.TemplateModelException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class MLDisplayTitleMethodTest {

  private MLDisplayTitleMethod method;
  private NodeService nodeService;
  private NodeRef nodeRef;

  @Before
  public void setUp() {
    method = new MLDisplayTitleMethod();
    nodeService = mock(NodeService.class);
    method.setNodeService(nodeService);
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  @Test
  public void testGetResult_whenTitleIsString_thenReturnsString()
    throws TemplateModelException {
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).thenReturn(
      "My Title"
    );

    String result = method.getResult(nodeRef);

    assertEquals("My Title", result);
  }

  @Test
  public void testGetResult_whenTitleIsMLText_thenReturnsDefaultValue()
    throws TemplateModelException {
    MLText mlText = new MLText("ML Title");
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).thenReturn(
      mlText
    );

    String result = method.getResult(nodeRef);

    assertEquals("ML Title", result);
  }

  @Test
  public void testGetResult_whenTitleIsNull_thenReturnsEmpty()
    throws TemplateModelException {
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );

    String result = method.getResult(nodeRef);

    assertEquals("", result);
  }

  @Test
  public void testGetResult_whenNodeRefIsNull_thenReturnsEmpty()
    throws TemplateModelException {
    String result = method.getResult(null);

    assertEquals("", result);
    verifyNoInteractions(nodeService);
  }

  @Test
  public void testGetResult_whenTitleIsOtherType_thenReturnsEmpty()
    throws TemplateModelException {
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).thenReturn(
      Integer.valueOf(42)
    );

    String result = method.getResult(nodeRef);

    assertEquals("", result);
  }
}
