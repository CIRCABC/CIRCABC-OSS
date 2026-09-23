package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.lang.reflect.Method;
import java.util.*;
import org.junit.Before;
import org.junit.Test;

public class ValidateHelperImplTest {

  private ValidateHelperImpl validateHelper;

  @Before
  public void setUp() {
    validateHelper = new ValidateHelperImpl();
  }

  @Test
  public void testValidate_whenEmptyIndexRecords_thenNoMessages() {
    IndexHeaders index = mock(IndexHeaders.class);
    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();

    validateHelper.validate(index, messages);

    assertTrue(messages.isEmpty());
    verifyNoInteractions(index);
  }

  @Test
  public void testPrivateValidate_whenAllValidatorsPass_thenNoMessages()
    throws Exception {
    IndexEntry entry = mock(IndexEntry.class);
    HeaderValidator validator = mock(HeaderValidator.class);
    when(validator.validate(entry)).thenReturn(true);

    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();

    invokePrivateValidate(entry, Arrays.asList(validator), messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testPrivateValidate_whenValidatorFails_thenMessageAdded()
    throws Exception {
    IndexEntry entry = mock(IndexEntry.class);
    HeaderValidator validator = mock(HeaderValidator.class);
    ValidationMessage validationMessage = mock(ValidationMessage.class);
    when(validator.validate(entry)).thenReturn(false);
    when(validator.getValidationMessage()).thenReturn(validationMessage);

    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();

    invokePrivateValidate(entry, Arrays.asList(validator), messages);

    assertTrue(messages.containsKey(entry));
    assertEquals(1, messages.get(entry).size());
    assertSame(validationMessage, messages.get(entry).get(0));
  }

  @Test
  public void testPrivateValidate_whenMultipleValidatorsFail_thenAllMessagesAdded()
    throws Exception {
    IndexEntry entry = mock(IndexEntry.class);
    HeaderValidator validator1 = mock(HeaderValidator.class);
    HeaderValidator validator2 = mock(HeaderValidator.class);
    ValidationMessage msg1 = mock(ValidationMessage.class);
    ValidationMessage msg2 = mock(ValidationMessage.class);
    when(validator1.validate(entry)).thenReturn(false);
    when(validator1.getValidationMessage()).thenReturn(msg1);
    when(validator2.validate(entry)).thenReturn(false);
    when(validator2.getValidationMessage()).thenReturn(msg2);

    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();

    invokePrivateValidate(
      entry,
      Arrays.asList(validator1, validator2),
      messages
    );

    assertEquals(2, messages.get(entry).size());
    assertSame(msg1, messages.get(entry).get(0));
    assertSame(msg2, messages.get(entry).get(1));
  }

  @Test
  public void testPrivateValidate_whenEntryAlreadyInMap_thenAppendsMessage()
    throws Exception {
    IndexEntry entry = mock(IndexEntry.class);
    HeaderValidator validator = mock(HeaderValidator.class);
    ValidationMessage existingMsg = mock(ValidationMessage.class);
    ValidationMessage newMsg = mock(ValidationMessage.class);
    when(validator.validate(entry)).thenReturn(false);
    when(validator.getValidationMessage()).thenReturn(newMsg);

    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();
    List<ValidationMessage> existing = new ArrayList<>();
    existing.add(existingMsg);
    messages.put(entry, existing);

    invokePrivateValidate(entry, Arrays.asList(validator), messages);

    assertEquals(2, messages.get(entry).size());
    assertSame(existingMsg, messages.get(entry).get(0));
    assertSame(newMsg, messages.get(entry).get(1));
  }

  @Test
  public void testPrivateValidate_whenEmptyValidatorList_thenNoMessages()
    throws Exception {
    IndexEntry entry = mock(IndexEntry.class);
    Map<IndexEntry, List<ValidationMessage>> messages = new HashMap<>();

    invokePrivateValidate(entry, Collections.emptyList(), messages);

    assertTrue(messages.isEmpty());
  }

  private void invokePrivateValidate(
    IndexEntry entry,
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
    method.invoke(validateHelper, entry, validators, messages);
  }
}
