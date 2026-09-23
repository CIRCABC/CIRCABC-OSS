package eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.junit.Before;
import org.junit.Test;

public class LangValidatorTest {

  private LangValidator langValidator;
  private ServiceRegistry serviceRegistry;
  private ContentFilterLanguagesService contentFilterLanguagesService;
  private IndexRecord indexRecord;

  @Before
  public void setUp() {
    serviceRegistry = mock(ServiceRegistry.class);
    contentFilterLanguagesService = mock(ContentFilterLanguagesService.class);
    when(serviceRegistry.getContentFilterLanguagesService()).thenReturn(
      contentFilterLanguagesService
    );
    when(contentFilterLanguagesService.getFilterLanguages()).thenReturn(
      Arrays.asList("en", "fr", "de")
    );

    indexRecord = mock(IndexRecord.class);
    langValidator = new LangValidator(serviceRegistry);
  }

  @Test
  public void testValidate_whenDocLangIsNull_thenNoError() {
    when(indexRecord.getDocLang()).thenReturn(null);
    List<ValidationMessage> messages = new ArrayList<>();

    langValidator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenDocLangIsEmpty_thenNoError() {
    when(indexRecord.getDocLang()).thenReturn("");
    List<ValidationMessage> messages = new ArrayList<>();

    langValidator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenDocLangIsValid_thenNoError() {
    when(indexRecord.getDocLang()).thenReturn("en");
    List<ValidationMessage> messages = new ArrayList<>();

    langValidator.validate(indexRecord, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidate_whenDocLangIsInvalid_thenFatalError() {
    when(indexRecord.getDocLang()).thenReturn("xx");
    when(indexRecord.getRowNumber()).thenReturn(5);
    when(indexRecord.getName()).thenReturn("testfile.pdf");
    List<ValidationMessage> messages = new ArrayList<>();

    langValidator.validate(indexRecord, messages);

    assertEquals(1, messages.size());
    ValidationMessage msg = messages.get(0);
    assertEquals(5, msg.getRowNumber());
    assertEquals("testfile.pdf", msg.getFileName());
    assertEquals(ErrorType.Fatal, msg.getErrorType());
  }
}
