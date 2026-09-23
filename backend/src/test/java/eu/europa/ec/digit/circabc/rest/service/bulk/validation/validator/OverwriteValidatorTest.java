package eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.junit.Before;
import org.junit.Test;

public class OverwriteValidatorTest {

  private OverwriteValidator validator;
  private IndexRecord indexRecord;
  private List<ValidationMessage> messages;

  @Before
  public void setUp() {
    ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
    validator = new OverwriteValidator(serviceRegistry);
    indexRecord = mock(IndexRecord.class);
    messages = new ArrayList<>();
  }

  @Test
  public void validate_whenOverwriteIsY_thenNoError() {
    when(indexRecord.getOverwrite()).thenReturn("Y");

    validator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void validate_whenOverwriteIsN_thenNoError() {
    when(indexRecord.getOverwrite()).thenReturn("N");

    validator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void validate_whenOverwriteIsNull_thenNoError() {
    when(indexRecord.getOverwrite()).thenReturn(null);

    validator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void validate_whenOverwriteIsEmpty_thenNoError() {
    when(indexRecord.getOverwrite()).thenReturn("");

    validator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void validate_whenOverwriteIsInvalid_thenAddsError() {
    when(indexRecord.getOverwrite()).thenReturn("X");
    when(indexRecord.getRowNumber()).thenReturn(5);
    when(indexRecord.getName()).thenReturn("testfile.pdf");

    validator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    ValidationMessage msg = messages.get(0);
    assertEquals(5, msg.getRowNumber());
    assertEquals("testfile.pdf", msg.getFileName());
    assertEquals(ErrorType.Fatal, msg.getErrorType());
  }

  @Test
  public void validate_whenOverwriteIsLowercaseY_thenAddsError() {
    when(indexRecord.getOverwrite()).thenReturn("y");
    when(indexRecord.getRowNumber()).thenReturn(1);
    when(indexRecord.getName()).thenReturn("doc.txt");

    validator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(ErrorType.Fatal, messages.get(0).getErrorType());
  }
}
