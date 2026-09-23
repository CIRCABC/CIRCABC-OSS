package eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class IndexValidatorTest {

  private IndexValidator validator;
  private IndexRecord indexRecord;
  private List<ValidationMessage> messages;

  @Before
  public void setUp() {
    validator = mock(IndexValidator.class);
    indexRecord = mock(IndexRecord.class);
    messages = new ArrayList<>();
  }

  @Test
  public void testValidate_whenCalled_thenNoException() {
    doNothing().when(validator).validate(indexRecord, messages);

    validator.validate(indexRecord, messages);

    verify(validator).validate(indexRecord, messages);
  }

  @Test
  public void testValidate_whenCalledWithNullRecord_thenNoException() {
    doNothing().when(validator).validate(null, messages);

    validator.validate(null, messages);

    verify(validator).validate(null, messages);
  }

  @Test
  public void testValidate_whenCalledWithNullMessages_thenNoException() {
    doNothing().when(validator).validate(indexRecord, null);

    validator.validate(indexRecord, null);

    verify(validator).validate(indexRecord, null);
  }

  @Test
  public void testValidate_whenCalledMultipleTimes_thenEachCallIsRecorded() {
    doNothing().when(validator).validate(any(), any());

    validator.validate(indexRecord, messages);
    validator.validate(indexRecord, messages);

    verify(validator, times(2)).validate(indexRecord, messages);
  }

  @Test
  public void testValidate_whenImplementedInline_thenAddsMessage() {
    ValidationMessage message = mock(ValidationMessage.class);

    IndexValidator inlineValidator = (record, msgs) -> {
      if (record.getName() == null) {
        msgs.add(message);
      }
    };

    when(indexRecord.getName()).thenReturn(null);

    inlineValidator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertSame(message, messages.get(0));
  }

  @Test
  public void testValidate_whenImplementedInline_andNamePresent_thenNoMessage() {
    IndexValidator inlineValidator = (record, msgs) -> {
      if (record.getName() == null) {
        msgs.add(mock(ValidationMessage.class));
      }
    };

    when(indexRecord.getName()).thenReturn("document.pdf");

    inlineValidator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }
}
