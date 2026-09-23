package eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message;

import static org.junit.Assert.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import org.junit.Test;

public class ValidationMessageTest {

  @Test
  public void testGetters_whenConstructedWithValues_thenReturnsCorrectValues() {
    ValidationMessage msg = new ValidationMessageImpl(
      5,
      "file.txt",
      "some error",
      ErrorType.Fatal
    );

    assertEquals(5, msg.getRowNumber());
    assertEquals("file.txt", msg.getFileName());
    assertEquals("some error", msg.getErrorDescription());
    assertEquals(ErrorType.Fatal, msg.getErrorType());
  }

  @Test
  public void testGetters_whenWarningType_thenReturnsWarning() {
    ValidationMessage msg = new ValidationMessageImpl(
      0,
      "doc.pdf",
      "minor issue",
      ErrorType.Warning
    );

    assertEquals(0, msg.getRowNumber());
    assertEquals("doc.pdf", msg.getFileName());
    assertEquals("minor issue", msg.getErrorDescription());
    assertEquals(ErrorType.Warning, msg.getErrorType());
  }

  @Test
  public void testGetters_whenNullValues_thenReturnsNull() {
    ValidationMessage msg = new ValidationMessageImpl(1, null, null, null);

    assertEquals(1, msg.getRowNumber());
    assertNull(msg.getFileName());
    assertNull(msg.getErrorDescription());
    assertNull(msg.getErrorType());
  }
}
