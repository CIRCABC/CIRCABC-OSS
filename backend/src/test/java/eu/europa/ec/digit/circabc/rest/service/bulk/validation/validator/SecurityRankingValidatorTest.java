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

public class SecurityRankingValidatorTest {

  private SecurityRankingValidator validator;
  private IndexRecord indexRecord;
  private List<ValidationMessage> messages;

  @Before
  public void setUp() {
    ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
    validator = new SecurityRankingValidator(serviceRegistry);
    indexRecord = mock(IndexRecord.class);
    messages = new ArrayList<>();
  }

  @Test
  public void testValidate_whenSecurityRankingIsNull_thenNoMessage() {
    when(indexRecord.getSecurityRanking()).thenReturn(null);
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenSecurityRankingIsEmpty_thenNoMessage() {
    when(indexRecord.getSecurityRanking()).thenReturn("");
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenSecurityRankingIsValid_thenNoMessage() {
    when(indexRecord.getSecurityRanking()).thenReturn("NORMAL");
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenSecurityRankingIsSensitive_thenNoMessage() {
    when(indexRecord.getSecurityRanking()).thenReturn("SENSITIVE");
    validator.validate(indexRecord, messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenSecurityRankingIsInvalid_thenAddsMessage() {
    when(indexRecord.getSecurityRanking()).thenReturn("INVALID_RANKING");
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
  public void testValidate_whenSecurityRankingIsCaseMismatch_thenAddsMessage() {
    when(indexRecord.getSecurityRanking()).thenReturn("normal");
    when(indexRecord.getRowNumber()).thenReturn(1);
    when(indexRecord.getName()).thenReturn("doc.txt");

    validator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    assertEquals(ErrorType.Fatal, messages.get(0).getErrorType());
  }
}
