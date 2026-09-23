package eu.europa.ec.digit.circabc.rest.service.bulk.validation.validator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.junit.Before;
import org.junit.Test;

public class AbstractIndexValidatorTest {

  private ServiceRegistry serviceRegistry;
  private TestIndexValidator validator;

  @Before
  public void setUp() {
    serviceRegistry = mock(ServiceRegistry.class);
    validator = new TestIndexValidator(serviceRegistry);
  }

  @Test
  public void testConstructor_whenServiceRegistryProvided_thenFieldIsSet() {
    assertSame(serviceRegistry, validator.serviceRegistry);
  }

  @Test
  public void testConstructor_whenNullServiceRegistry_thenFieldIsNull() {
    TestIndexValidator nullValidator = new TestIndexValidator(null);
    assertNull(nullValidator.serviceRegistry);
  }

  @Test
  public void testImplementsIndexValidator() {
    assertTrue(validator instanceof IndexValidator);
  }

  private static class TestIndexValidator extends AbstractIndexValidator {

    TestIndexValidator(ServiceRegistry serviceRegistry) {
      super(serviceRegistry);
    }

    @Override
    public void validate(
      IndexRecord indexRecord,
      List<ValidationMessage> messages
    ) {
      // no-op for testing
    }
  }
}
