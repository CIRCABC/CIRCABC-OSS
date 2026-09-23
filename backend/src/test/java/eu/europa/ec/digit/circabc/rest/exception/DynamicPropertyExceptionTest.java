package eu.europa.ec.digit.circabc.rest.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class DynamicPropertyExceptionTest {

  @Test
  public void testConstructor_withMessage_thenMessageIsSet() {
    DynamicPropertyException ex = new DynamicPropertyException("test error");
    assertEquals("test error", ex.getMessage());
  }

  @Test
  public void testConstructor_withMessageAndCause_thenBothAreSet() {
    Throwable cause = new IllegalArgumentException("root cause");
    DynamicPropertyException ex = new DynamicPropertyException(
      "test error",
      cause
    );
    assertEquals("test error", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testIsRuntimeException() {
    DynamicPropertyException ex = new DynamicPropertyException("msg");
    assertTrue(ex instanceof RuntimeException);
  }

  @Test(expected = DynamicPropertyException.class)
  public void testCanBeThrown() {
    throw new DynamicPropertyException("thrown");
  }
}
