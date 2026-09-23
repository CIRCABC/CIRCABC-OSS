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

public class IssueDateValidatorTest {

  private IssueDateValidator validator;
  private IndexRecord indexRecord;
  private List<ValidationMessage> messages;

  @Before
  public void setUp() {
    ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
    validator = new IssueDateValidator(serviceRegistry);
    indexRecord = mock(IndexRecord.class);
    messages = new ArrayList<>();
  }

  @Test
  public void testValidate_whenIssueDateNull_thenNoMessage() {
    when(indexRecord.getIssueDate()).thenReturn(null);
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenIssueDateEmpty_thenNoMessage() {
    when(indexRecord.getIssueDate()).thenReturn("");
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenIssueDateValid_thenNoMessage() {
    when(indexRecord.getIssueDate()).thenReturn("15/03/2024");
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenIssueDateInvalid_thenAddsWarning() {
    when(indexRecord.getIssueDate()).thenReturn("not-a-date");
    when(indexRecord.getRowNumber()).thenReturn(5);
    when(indexRecord.getName()).thenReturn("testfile.pdf");

    validator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    ValidationMessage msg = messages.get(0);
    assertEquals(5, msg.getRowNumber());
    assertEquals("testfile.pdf", msg.getFileName());
    assertEquals(ErrorType.Warning, msg.getErrorType());
  }

  @Test
  public void testValidate_whenIssueDateWrongFormat_thenAddsWarning() {
    when(indexRecord.getIssueDate()).thenReturn("2024-03-15");
    when(indexRecord.getRowNumber()).thenReturn(1);
    when(indexRecord.getName()).thenReturn("doc.pdf");

    validator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(ErrorType.Warning, messages.get(0).getErrorType());
  }
}
