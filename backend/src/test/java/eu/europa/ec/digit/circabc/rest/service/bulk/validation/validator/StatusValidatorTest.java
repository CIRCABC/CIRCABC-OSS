package eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import io.swagger.model.alfresco.DocumentModel;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.junit.Before;
import org.junit.Test;

public class StatusValidatorTest {

  private StatusValidator validator;
  private IndexRecord indexRecord;
  private List<ValidationMessage> messages;

  @Before
  public void setUp() {
    ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
    validator = new StatusValidator(serviceRegistry);
    indexRecord = mock(IndexRecord.class);
    messages = new ArrayList<>();
  }

  @Test
  public void testValidate_whenEmptyStatusAndNoRelTrans_thenSetsDefaultAndAddsWarning() {
    when(indexRecord.getStatus()).thenReturn("");
    when(indexRecord.getRelTrans()).thenReturn(null);
    when(indexRecord.getRowNumber()).thenReturn(1);
    when(indexRecord.getName()).thenReturn("test.doc");

    validator.validate(indexRecord, messages);

    verify(indexRecord).setStatus(DocumentModel.STATUS_VALUES.get(0));
    assertEquals(1, messages.size());
    assertEquals(ErrorType.Warning, messages.get(0).getErrorType());
  }

  @Test
  public void testValidate_whenEmptyStatusAndHasRelTrans_thenNoWarning() {
    when(indexRecord.getStatus()).thenReturn("");
    when(indexRecord.getRelTrans()).thenReturn("someTrans");

    validator.validate(indexRecord, messages);

    verify(indexRecord, never()).setStatus(anyString());
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenStatusIsRELEASED_thenNormalizesToRELEASE() {
    when(indexRecord.getStatus()).thenReturn("RELEASED");

    validator.validate(indexRecord, messages);

    verify(indexRecord).setStatus(DocumentModel.STATUS_VALUE_RELEASE);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenStatusIsValid_thenNoMessages() {
    when(indexRecord.getStatus()).thenReturn("DRAFT");

    validator.validate(indexRecord, messages);

    verify(indexRecord, never()).setStatus(anyString());
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenStatusIsInvalid_thenAddsFatalMessage() {
    when(indexRecord.getStatus()).thenReturn("INVALID_STATUS");
    when(indexRecord.getRowNumber()).thenReturn(5);
    when(indexRecord.getName()).thenReturn("bad.doc");

    validator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(ErrorType.Fatal, messages.get(0).getErrorType());
    assertEquals(5, messages.get(0).getRowNumber());
    assertEquals("bad.doc", messages.get(0).getFileName());
  }
}
