package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessageImpl;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import org.junit.Before;
import org.junit.Test;

public class HeaderValidatorTest {

  private HeaderValidator headerValidator;
  private IndexEntry indexEntry;

  @Before
  public void setUp() {
    headerValidator = mock(HeaderValidator.class);
    indexEntry = mock(IndexEntry.class);
  }

  @Test
  public void testValidate_whenValid_thenReturnsTrue() {
    when(headerValidator.validate(indexEntry)).thenReturn(true);

    assertTrue(headerValidator.validate(indexEntry));
    verify(headerValidator).validate(indexEntry);
  }

  @Test
  public void testValidate_whenInvalid_thenReturnsFalse() {
    when(headerValidator.validate(indexEntry)).thenReturn(false);

    assertFalse(headerValidator.validate(indexEntry));
  }

  @Test
  public void testGetValidationMessage_whenValidationFails_thenReturnsMessage() {
    ValidationMessage message = new ValidationMessageImpl(
      1,
      "test.csv",
      "Invalid value",
      ErrorType.Fatal
    );
    when(headerValidator.validate(indexEntry)).thenReturn(false);
    when(headerValidator.getValidationMessage()).thenReturn(message);

    assertFalse(headerValidator.validate(indexEntry));

    ValidationMessage result = headerValidator.getValidationMessage();
    assertNotNull(result);
    assertEquals(1, result.getRowNumber());
    assertEquals("test.csv", result.getFileName());
    assertEquals("Invalid value", result.getErrorDescription());
    assertEquals(ErrorType.Fatal, result.getErrorType());
  }

  @Test
  public void testGetValidationMessage_whenValidationPasses_thenMessageNotAccessed() {
    when(headerValidator.validate(indexEntry)).thenReturn(true);

    assertTrue(headerValidator.validate(indexEntry));
    verify(headerValidator, never()).getValidationMessage();
  }

  @Test
  public void testValidate_withNullIndexEntry_thenReturnsFalse() {
    when(headerValidator.validate(null)).thenReturn(false);

    assertFalse(headerValidator.validate(null));
  }
}
