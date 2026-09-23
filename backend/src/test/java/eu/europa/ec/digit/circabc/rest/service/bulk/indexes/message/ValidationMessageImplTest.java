package eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message;

import static org.junit.Assert.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import org.junit.Before;
import org.junit.Test;

public class ValidationMessageImplTest {

  private ValidationMessageImpl message;

  @Before
  public void setUp() {
    message = new ValidationMessageImpl(
      5,
      "test.csv",
      "Column missing",
      ErrorType.Fatal
    );
  }

  @Test
  public void testGetRowNumber_whenConstructed_thenReturnsValue() {
    assertEquals(5, message.getRowNumber());
  }

  @Test
  public void testGetFileName_whenConstructed_thenReturnsValue() {
    assertEquals("test.csv", message.getFileName());
  }

  @Test
  public void testGetErrorDescription_whenConstructed_thenReturnsValue() {
    assertEquals("Column missing", message.getErrorDescription());
  }

  @Test
  public void testGetErrorType_whenConstructed_thenReturnsValue() {
    assertEquals(ErrorType.Fatal, message.getErrorType());
  }

  @Test
  public void testConstructor_whenNullValues_thenStoresNulls() {
    ValidationMessageImpl nullMessage = new ValidationMessageImpl(
      0,
      null,
      null,
      null
    );
    assertEquals(0, nullMessage.getRowNumber());
    assertNull(nullMessage.getFileName());
    assertNull(nullMessage.getErrorDescription());
    assertNull(nullMessage.getErrorType());
  }

  @Test
  public void testGetErrorType_whenWarning_thenReturnsWarning() {
    ValidationMessageImpl warning = new ValidationMessageImpl(
      1,
      "file.xml",
      "Minor issue",
      ErrorType.Warning
    );
    assertEquals(ErrorType.Warning, warning.getErrorType());
  }
}
