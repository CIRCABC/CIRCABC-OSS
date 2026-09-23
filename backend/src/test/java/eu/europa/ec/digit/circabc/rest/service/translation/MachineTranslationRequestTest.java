package eu.europa.ec.digit.circabc.rest.service.translation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MachineTranslationRequestTest {

  private MachineTranslationRequest request;

  @Before
  public void setUp() {
    request = new MachineTranslationRequest();
  }

  @Test
  public void testDefaultValues_whenNewInstance_thenStringsNullAndPriorityZero() {
    assertNull(request.getApplicationName());
    assertNull(request.getSourceLanguage());
    assertNull(request.getTargetLanguage());
    assertNull(request.getTextToTranslate());
    assertEquals(0, request.getPriority());
  }

  @Test
  public void testSettersAndGetters_whenValuesSet_thenReturnedCorrectly() {
    request.setApplicationName("CIRCABC");
    request.setDepartmentNumber("DIGIT");
    request.setDocumentToTranslate("base64content");
    request.setDomains("GEN");
    request.setErrorCallback("http://error");
    request.setExternalReference("ref-123");
    request.setInstitution("EC");
    request.setOriginalFileName("doc.pdf");
    request.setOutputFormat("xlf");
    request.setPriority(5);
    request.setRequesterCallback("http://callback");
    request.setRequestType("txt");
    request.setSourceLanguage("EN");
    request.setTargetLanguage("FR");
    request.setTargetTranslationPath("/translations");
    request.setTextToTranslate("Hello world");
    request.setUsername("testuser");

    assertEquals("CIRCABC", request.getApplicationName());
    assertEquals("DIGIT", request.getDepartmentNumber());
    assertEquals("base64content", request.getDocumentToTranslate());
    assertEquals("GEN", request.getDomains());
    assertEquals("http://error", request.getErrorCallback());
    assertEquals("ref-123", request.getExternalReference());
    assertEquals("EC", request.getInstitution());
    assertEquals("doc.pdf", request.getOriginalFileName());
    assertEquals("xlf", request.getOutputFormat());
    assertEquals(5, request.getPriority());
    assertEquals("http://callback", request.getRequesterCallback());
    assertEquals("txt", request.getRequestType());
    assertEquals("EN", request.getSourceLanguage());
    assertEquals("FR", request.getTargetLanguage());
    assertEquals("/translations", request.getTargetTranslationPath());
    assertEquals("Hello world", request.getTextToTranslate());
    assertEquals("testuser", request.getUsername());
  }

  @Test
  public void testSetNull_whenNullSet_thenGetterReturnsNull() {
    request.setApplicationName("value");
    request.setApplicationName(null);
    assertNull(request.getApplicationName());
  }

  @Test
  public void testPriority_whenNegativeValue_thenStoredAsIs() {
    request.setPriority(-1);
    assertEquals(-1, request.getPriority());
  }
}
