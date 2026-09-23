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

public class NoContentValidatorTest {

  private NoContentValidator validator;
  private IndexRecord indexRecord;
  private List<ValidationMessage> messages;

  @Before
  public void setUp() {
    ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
    validator = new NoContentValidator(serviceRegistry);
    indexRecord = mock(IndexRecord.class);
    messages = new ArrayList<>();
    when(indexRecord.getRowNumber()).thenReturn(1);
    when(indexRecord.getName()).thenReturn("test.doc");
  }

  @Test
  public void testValidate_whenNoContentIsNull_thenNoError() {
    when(indexRecord.getNoContent()).thenReturn(null);
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenNoContentIsEmpty_thenNoError() {
    when(indexRecord.getNoContent()).thenReturn("");
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenNoContentIsY_thenNoError() {
    when(indexRecord.getNoContent()).thenReturn("Y");
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenNoContentIsN_thenNoError() {
    when(indexRecord.getNoContent()).thenReturn("N");
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenNoContentIsInvalid_thenAddsError() {
    when(indexRecord.getNoContent()).thenReturn("X");
    validator.validate(indexRecord, messages);
    assertEquals(1, messages.size());
    assertEquals(ErrorType.Fatal, messages.get(0).getErrorType());
    assertEquals(1, messages.get(0).getRowNumber());
    assertEquals("test.doc", messages.get(0).getFileName());
  }

  @Test
  public void testValidate_whenNoContentIsLowercaseY_thenAddsError() {
    when(indexRecord.getNoContent()).thenReturn("y");
    validator.validate(indexRecord, messages);
    assertEquals(1, messages.size());
    assertEquals(ErrorType.Fatal, messages.get(0).getErrorType());
  }
}
