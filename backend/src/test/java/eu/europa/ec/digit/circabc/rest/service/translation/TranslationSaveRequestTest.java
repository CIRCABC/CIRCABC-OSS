package eu.europa.ec.digit.circabc.rest.service.translation;

import static org.junit.Assert.*;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class TranslationSaveRequestTest {

  private TranslationSaveRequest request;

  @Before
  public void setUp() {
    request = new TranslationSaveRequest();
  }

  @Test
  public void testNodeRef_whenSet_thenReturnsValue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    request.setNodeRef(nodeRef);
    assertEquals(nodeRef, request.getNodeRef());
  }

  @Test
  public void testProperty_whenSet_thenReturnsValue() {
    QName property = QName.createQName("http://test", "prop");
    request.setProperty(property);
    assertEquals(property, request.getProperty());
  }

  @Test
  public void testStringFields_whenSet_thenReturnValues() {
    request.setDocumentToTranslate("doc.pdf");
    request.setSourceLanguage("EN");
    request.setExternalReference("REF-001");
    request.setUsername("testuser");
    request.setTextToTranslate("Hello");
    request.setTargetTranslationPath("/path/to/target");
    request.setTargetLanguage("FR");
    request.setEmail("test@example.com");

    assertEquals("doc.pdf", request.getDocumentToTranslate());
    assertEquals("EN", request.getSourceLanguage());
    assertEquals("REF-001", request.getExternalReference());
    assertEquals("testuser", request.getUsername());
    assertEquals("Hello", request.getTextToTranslate());
    assertEquals("/path/to/target", request.getTargetTranslationPath());
    assertEquals("FR", request.getTargetLanguage());
    assertEquals("test@example.com", request.getEmail());
  }

  @Test
  public void testNotify_whenSetTrue_thenReturnsTrue() {
    request.setNotify(true);
    assertTrue(request.isNotify());
  }

  @Test
  public void testNotify_whenDefault_thenReturnsFalse() {
    assertFalse(request.isNotify());
  }

  @Test
  public void testFields_whenNotSet_thenReturnNull() {
    assertNull(request.getNodeRef());
    assertNull(request.getProperty());
    assertNull(request.getDocumentToTranslate());
    assertNull(request.getSourceLanguage());
    assertNull(request.getUsername());
    assertNull(request.getEmail());
  }
}
