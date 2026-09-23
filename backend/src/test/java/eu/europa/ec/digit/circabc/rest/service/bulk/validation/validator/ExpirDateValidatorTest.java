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

public class ExpirDateValidatorTest {

  private ExpirDateValidator validator;
  private IndexRecord indexRecord;
  private List<ValidationMessage> messages;

  @Before
  public void setUp() {
    ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
    validator = new ExpirDateValidator(serviceRegistry);
    indexRecord = mock(IndexRecord.class);
    messages = new ArrayList<>();
  }

  @Test
  public void testValidate_whenExpirationDateNull_thenNoMessages() {
    when(indexRecord.getExpirationDate()).thenReturn(null);
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenExpirationDateEmpty_thenNoMessages() {
    when(indexRecord.getExpirationDate()).thenReturn("");
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenValidDate_thenNoMessages() {
    when(indexRecord.getExpirationDate()).thenReturn("25/12/2025");
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenInvalidDate_thenAddsWarning() {
    when(indexRecord.getExpirationDate()).thenReturn("invalid-date");
    when(indexRecord.getRowNumber()).thenReturn(3);
    when(indexRecord.getName()).thenReturn("testfile.pdf");

    validator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(ErrorType.Warning, messages.get(0).getErrorType());
    assertEquals(3, messages.get(0).getRowNumber());
    assertEquals("testfile.pdf", messages.get(0).getFileName());
  }

  @Test
  public void testValidate_whenWrongFormat_thenAddsWarning() {
    when(indexRecord.getExpirationDate()).thenReturn("2025-12-25");
    when(indexRecord.getRowNumber()).thenReturn(1);
    when(indexRecord.getName()).thenReturn("doc.txt");

    validator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(ErrorType.Warning, messages.get(0).getErrorType());
  }
}
