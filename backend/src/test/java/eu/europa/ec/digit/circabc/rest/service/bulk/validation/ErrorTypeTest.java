package eu.europa.ec.digit.circabc.rest.service.bulk.validation;

import static org.junit.Assert.*;

import org.junit.Test;

public class ErrorTypeTest {

  @Test
  public void testValues_returnsAllEnumConstants() {
    ErrorType[] values = ErrorType.values();
    assertEquals(2, values.length);
    assertEquals(ErrorType.Fatal, values[0]);
    assertEquals(ErrorType.Warning, values[1]);
  }

  @Test
  public void testValueOf_fatal() {
    assertEquals(ErrorType.Fatal, ErrorType.valueOf("Fatal"));
  }

  @Test
  public void testValueOf_warning() {
    assertEquals(ErrorType.Warning, ErrorType.valueOf("Warning"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueOf_invalidName_throwsException() {
    ErrorType.valueOf("INVALID");
  }
}
