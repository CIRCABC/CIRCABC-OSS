package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ValidateHelperTest {

  private ValidateHelperImpl validateHelper;

  @Before
  public void setUp() {
    validateHelper = new ValidateHelperImpl();
  }

  @Test
  public void testValidate_whenEmptyIndexRecords_thenNoValidationMessages() {
    IndexHeaders index = mock(IndexHeaders.class);
    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();

    validateHelper.validate(index, messages);

    assertTrue(messages.isEmpty());
    verifyNoInteractions(index);
  }

  @Test
  public void testPrivateValidate_whenAllValidatorsPass_thenNoMessages()
    throws Exception {
    IndexEntry indexRecord = mock(IndexEntry.class);
    HeaderValidator validator = mock(HeaderValidator.class);
    when(validator.validate(indexRecord)).thenReturn(true);

    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();
    invokePrivateValidate(indexRecord, Arrays.asList(validator), messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testPrivateValidate_whenValidatorFails_thenMessageAdded()
    throws Exception {
    IndexEntry indexRecord = mock(IndexEntry.class);
    HeaderValidator validator = mock(HeaderValidator.class);
    ValidationMessage validationMessage = mock(ValidationMessage.class);
    when(validator.validate(indexRecord)).thenReturn(false);
    when(validator.getValidationMessage()).thenReturn(validationMessage);

    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();
    invokePrivateValidate(indexRecord, Arrays.asList(validator), messages);

    assertTrue(messages.containsKey(indexRecord));
    assertEquals(1, messages.get(indexRecord).size());
    assertSame(validationMessage, messages.get(indexRecord).get(0));
  }

  @Test
  public void testPrivateValidate_whenMultipleValidatorsFail_thenAllMessagesAdded()
    throws Exception {
    IndexEntry indexRecord = mock(IndexEntry.class);
    HeaderValidator validator1 = mock(HeaderValidator.class);
    HeaderValidator validator2 = mock(HeaderValidator.class);
    ValidationMessage msg1 = mock(ValidationMessage.class);
    ValidationMessage msg2 = mock(ValidationMessage.class);
    when(validator1.validate(indexRecord)).thenReturn(false);
    when(validator1.getValidationMessage()).thenReturn(msg1);
    when(validator2.validate(indexRecord)).thenReturn(false);
    when(validator2.getValidationMessage()).thenReturn(msg2);

    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();
    invokePrivateValidate(
      indexRecord,
      Arrays.asList(validator1, validator2),
      messages
    );

    assertEquals(2, messages.get(indexRecord).size());
    assertSame(msg1, messages.get(indexRecord).get(0));
    assertSame(msg2, messages.get(indexRecord).get(1));
  }

  @Test
  public void testPrivateValidate_whenExistingMessages_thenAppendsToList()
    throws Exception {
    IndexEntry indexRecord = mock(IndexEntry.class);
    HeaderValidator validator = mock(HeaderValidator.class);
    ValidationMessage existingMsg = mock(ValidationMessage.class);
    ValidationMessage newMsg = mock(ValidationMessage.class);
    when(validator.validate(indexRecord)).thenReturn(false);
    when(validator.getValidationMessage()).thenReturn(newMsg);

    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();
    List<ValidationMessage> existingList = new ArrayList<>();
    existingList.add(existingMsg);
    messages.put(indexRecord, existingList);

    invokePrivateValidate(indexRecord, Arrays.asList(validator), messages);

    assertEquals(2, messages.get(indexRecord).size());
    assertSame(existingMsg, messages.get(indexRecord).get(0));
    assertSame(newMsg, messages.get(indexRecord).get(1));
  }

  private void invokePrivateValidate(
    IndexEntry indexRecord,
    List<HeaderValidator> validators,
    Map<IndexEntry, List<ValidationMessage>> messages
  ) throws Exception {
    Method method = ValidateHelperImpl.class.getDeclaredMethod(
      "validate",
      IndexEntry.class,
      List.class,
      Map.class
    );
    method.setAccessible(true);
    method.invoke(validateHelper, indexRecord, validators, messages);
  }
}
