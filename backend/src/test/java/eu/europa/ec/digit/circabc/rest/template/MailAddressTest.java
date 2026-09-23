package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.template.TemplateModelException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class MailAddressTest {

  private MailAddress mailAddress;
  private NodeService nodeService;
  private NodeRef nodeRef;

  @Before
  public void setUp() {
    mailAddress = new MailAddress();
    nodeService = mock(NodeService.class);
    mailAddress.setNodeService(nodeService);
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  @Test
  public void testGetResult_whenEmailExists_thenReturnsEmail()
    throws TemplateModelException {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_EMAIL, "user@example.com");
    when(nodeService.getProperties(nodeRef)).thenReturn(props);

    String result = mailAddress.getResult(nodeRef);

    assertEquals("user@example.com", result);
  }

  @Test
  public void testGetResult_whenEmailIsNull_thenReturnsNull()
    throws TemplateModelException {
    Map<QName, Serializable> props = new HashMap<>();
    when(nodeService.getProperties(nodeRef)).thenReturn(props);

    String result = mailAddress.getResult(nodeRef);

    assertNull(result);
  }
}
