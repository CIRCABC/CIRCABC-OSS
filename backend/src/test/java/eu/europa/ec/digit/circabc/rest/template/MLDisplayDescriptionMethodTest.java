package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.template.TemplateModelException;
import java.util.Locale;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class MLDisplayDescriptionMethodTest {

  private MLDisplayDescriptionMethod method;
  private NodeService nodeService;
  private NodeRef nodeRef;

  @Before
  public void setUp() {
    method = new MLDisplayDescriptionMethod();
    nodeService = mock(NodeService.class);
    method.setNodeService(nodeService);
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  @Test
  public void testGetResult_whenPropertyIsString_thenReturnsString()
    throws TemplateModelException {
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("A description");

    String result = method.getResult(nodeRef);

    assertEquals("A description", result);
  }

  @Test
  public void testGetResult_whenPropertyIsMLText_thenReturnsDefaultValue()
    throws TemplateModelException {
    MLText mlText = new MLText();
    mlText.put(Locale.ENGLISH, "English description");

    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION)
    ).thenReturn(mlText);

    String result = method.getResult(nodeRef);

    assertEquals("English description", result);
  }

  @Test
  public void testGetResult_whenPropertyIsNull_thenReturnsEmpty()
    throws TemplateModelException {
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION)
    ).thenReturn(null);

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
}
