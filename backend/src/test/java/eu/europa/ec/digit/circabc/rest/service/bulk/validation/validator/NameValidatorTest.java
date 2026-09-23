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

public class NameValidatorTest {

  private NameValidator nameValidator;
  private IndexRecord indexRecord;
  private List<ValidationMessage> messages;

  @Before
  public void setUp() {
    ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
    nameValidator = new NameValidator(serviceRegistry);
    indexRecord = mock(IndexRecord.class);
    messages = new ArrayList<>();
  }

  @Test
  public void testValidate_whenNameIsNull_andNoContentIsN_thenAddsFatalMessage() {
    when(indexRecord.getName()).thenReturn(null);
    when(indexRecord.getNoContent()).thenReturn("N");
    when(indexRecord.getRowNumber()).thenReturn(1);

    nameValidator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(ErrorType.Fatal, messages.get(0).getErrorType());
  }

  @Test
  public void testValidate_whenNameIsEmpty_andNoContentIsN_thenAddsFatalMessage() {
    when(indexRecord.getName()).thenReturn("");
    when(indexRecord.getNoContent()).thenReturn("N");
    when(indexRecord.getRowNumber()).thenReturn(2);

    nameValidator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(ErrorType.Fatal, messages.get(0).getErrorType());
  }

  @Test
  public void testValidate_whenNameIsNull_andNoContentIsNull_thenAddsFatalMessage() {
    when(indexRecord.getName()).thenReturn(null);
    when(indexRecord.getNoContent()).thenReturn(null);
    when(indexRecord.getRowNumber()).thenReturn(3);

    nameValidator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
  }

  @Test
  public void testValidate_whenNameIsNull_andNoContentIsY_thenNoMessage() {
    when(indexRecord.getName()).thenReturn(null);
    when(indexRecord.getNoContent()).thenReturn("Y");

    nameValidator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenNameStartsWithDoubleDot_thenAddsFatalMessage() {
    when(indexRecord.getName()).thenReturn("..hidden");
    when(indexRecord.getRowNumber()).thenReturn(4);

    nameValidator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(ErrorType.Fatal, messages.get(0).getErrorType());
  }

  @Test
  public void testValidate_whenNameIsValid_thenNoMessage() {
    when(indexRecord.getName()).thenReturn("document.pdf");

    nameValidator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenNameStartsWithSingleDot_thenNoMessage() {
    when(indexRecord.getName()).thenReturn(".gitignore");

    nameValidator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }
}
