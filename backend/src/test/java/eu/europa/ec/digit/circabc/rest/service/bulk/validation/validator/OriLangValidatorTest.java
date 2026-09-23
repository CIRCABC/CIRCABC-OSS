package eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.junit.Before;
import org.junit.Test;

public class OriLangValidatorTest {

  private OriLangValidator validator;
  private IndexRecord indexRecord;
  private List<ValidationMessage> messages;

  @Before
  public void setUp() {
    ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
    validator = new OriLangValidator(serviceRegistry);
    indexRecord = mock(IndexRecord.class);
    messages = new ArrayList<>();
  }

  @Test
  public void testValidate_whenOriLangIsNull_thenNoError() {
    when(indexRecord.getOriLang()).thenReturn(null);

    validator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenOriLangIsEmpty_thenNoError() {
    when(indexRecord.getOriLang()).thenReturn("");

    validator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenOriLangIsY_andDocLangSet_thenNoError() {
    when(indexRecord.getOriLang()).thenReturn("Y");
    when(indexRecord.getDocLang()).thenReturn("EN");

    validator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenOriLangIsN_thenNoError() {
    when(indexRecord.getOriLang()).thenReturn("N");

    validator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenOriLangIsInvalid_thenFatalError() {
    when(indexRecord.getOriLang()).thenReturn("X");
    when(indexRecord.getRowNumber()).thenReturn(5);
    when(indexRecord.getName()).thenReturn("test.pdf");

    validator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(5, messages.get(0).getRowNumber());
    assertEquals("test.pdf", messages.get(0).getFileName());
  }

  @Test
  public void testValidate_whenOriLangIsY_andDocLangIsNull_thenFatalError() {
    when(indexRecord.getOriLang()).thenReturn("Y");
    when(indexRecord.getDocLang()).thenReturn(null);
    when(indexRecord.getRowNumber()).thenReturn(3);
    when(indexRecord.getName()).thenReturn("doc.pdf");

    validator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(3, messages.get(0).getRowNumber());
    assertEquals("doc.pdf", messages.get(0).getFileName());
  }
}
