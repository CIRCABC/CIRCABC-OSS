package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class SimpleDirectDowloadUrlMethodTest {

  private SimpleDirectDowloadUrlMethod method;
  private NodeService nodeService;

  @Before
  public void setUp() {
    nodeService = mock(NodeService.class);
    method = new SimpleDirectDowloadUrlMethod();
    method.setNodeService(nodeService);
  }

  @Test
  public void testGetResult_whenValidNodeRef_thenReturnsDownloadUrl() {
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
  public void testGetResult_whenNameHasSpaces_thenIncludesSpacesInUrl() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "def-456"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "my file.docx"
    );

    String result = method.getResult(nodeRef);

    assertEquals("/d/d/workspace/SpacesStore/def-456/my file.docx", result);
  }

  @Test(expected = NullPointerException.class)
  public void testGetResult_whenPropertyReturnsNull_thenThrowsNpe() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "no-name"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      null
    );

    method.getResult(nodeRef);
  }
}
