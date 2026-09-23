package eu.europa.ec.digit.circabc.rest.service.helper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class ContentManagerTest {

  private ContentManager contentManager;
  private NodeService nodeService;
  private ContentService contentService;
  private OwnableService ownableService;
  private MetadataManager metadataManager;

  @Before
  public void setUp() throws Exception {
    contentManager = new ContentManager();
    nodeService = mock(NodeService.class);
    contentService = mock(ContentService.class);
    ownableService = mock(OwnableService.class);
    metadataManager = mock(MetadataManager.class);

    setField("nodeService", nodeService);
    setField("contentService", contentService);
    setField("ownableService", ownableService);
    setField("metadataManager", metadataManager);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentManager.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(contentManager, value);
  }

  @Test
  public void testGetTargetRef_whenNull_thenReturnsNull() {
    assertNull(contentManager.getTargetRef(null));
  }

  @Test
  public void testGetTargetRef_whenNotALink_thenReturnsSameRef() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION)
    ).thenReturn(null);

    NodeRef result = contentManager.getTargetRef(nodeRef);

    assertEquals(nodeRef, result);
  }

  @Test
  public void testGetTargetRef_whenIsLink_thenReturnsTarget() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "link-id"
    );
    NodeRef targetRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "target-id"
    );
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION)
    ).thenReturn(targetRef);

    NodeRef result = contentManager.getTargetRef(nodeRef);

    assertEquals(targetRef, result);
  }

  @Test
  public void testComputeMimeType_whenValidFileName_thenReturnsMimetype() {
    when(metadataManager.guessMimetype("document.pdf")).thenReturn(
      MimetypeMap.MIMETYPE_PDF
    );

    String result = contentManager.computeMimeType("document.pdf");

    assertEquals(MimetypeMap.MIMETYPE_PDF, result);
  }

  @Test
  public void testComputeMimeType_whenExceptionThrown_thenReturnsBinary() {
    when(metadataManager.guessMimetype("bad.xyz")).thenThrow(
      new RuntimeException("unknown")
    );

    String result = contentManager.computeMimeType("bad.xyz");

    assertEquals(MimetypeMap.MIMETYPE_BINARY, result);
  }

  @Test
  public void testComputeEncoding_whenFileIsNull_thenReturnsDefaultCharset() {
    String result = contentManager.computeEncoding(null, "text/plain");

    assertEquals(java.nio.charset.Charset.defaultCharset().name(), result);
  }

  @Test
  public void testComputeEncoding_whenFileExists_thenReturnsGuessedEncoding()
    throws Exception {
    File tempFile = File.createTempFile("test", ".txt");
    tempFile.deleteOnExit();

    when(metadataManager.guessEncoding(any(), eq("text/plain"))).thenReturn(
      "ISO-8859-1"
    );

    String result = contentManager.computeEncoding(tempFile, "text/plain");

    assertEquals("ISO-8859-1", result);
  }

  @Test
  public void testComputeEncoding_whenExceptionThrown_thenReturnsDefaultCharset()
    throws Exception {
    File tempFile = File.createTempFile("test", ".txt");
    tempFile.deleteOnExit();

    when(metadataManager.guessEncoding(any(), eq("text/plain"))).thenThrow(
      new RuntimeException("error")
    );

    String result = contentManager.computeEncoding(tempFile, "text/plain");

    assertEquals(java.nio.charset.Charset.defaultCharset().name(), result);
  }

  @Test
  public void testCreateContent_withFile_thenReturnsNodeRef() {
    NodeRef parent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );
    File tempFile;
    try {
      tempFile = File.createTempFile("test", ".pdf");
      tempFile.deleteOnExit();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    when(
      metadataManager.getValidUniqueName(
        eq(parent),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("test.pdf")
      )
    ).thenReturn("test.pdf");
    when(metadataManager.guessMimetype("test.pdf")).thenReturn(
      MimetypeMap.MIMETYPE_PDF
    );
    when(
      metadataManager.guessEncoding(any(), eq(MimetypeMap.MIMETYPE_PDF))
    ).thenReturn("UTF-8");

    ChildAssociationRef assocRef = mock(ChildAssociationRef.class);
    when(assocRef.getChildRef()).thenReturn(childRef);
    when(
      nodeService.createNode(
        eq(parent),
        eq(ContentModel.ASSOC_CONTAINS),
        any(QName.class),
        eq(ContentModel.TYPE_CONTENT),
        anyMap()
      )
    ).thenReturn(assocRef);
    when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_CONTENT);

    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(eq(childRef), any(QName.class), eq(true))
    ).thenReturn(writer);

    when(
      metadataManager.extractContentMetadata(any(ContentReader.class))
    ).thenReturn(new HashMap<>());
    when(metadataManager.isInlineEditable(MimetypeMap.MIMETYPE_PDF)).thenReturn(
      false
    );

    NodeRef result = contentManager.createContent(
      parent,
      "test.pdf",
      null,
      null,
      tempFile,
      false
    );

    assertEquals(childRef, result);
    verify(writer).putContent(tempFile);
    verify(nodeService).addAspect(
      eq(childRef),
      eq(ContentModel.ASPECT_AUTHOR),
      anyMap()
    );
    verify(ownableService).takeOwnership(childRef);
    verify(nodeService).addAspect(
      eq(childRef),
      eq(ContentModel.ASPECT_TITLED),
      anyMap()
    );
  }
}
