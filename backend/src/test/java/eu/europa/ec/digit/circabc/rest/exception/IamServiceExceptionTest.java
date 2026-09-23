package eu.europa.ec.digit.circabc.rest.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class IamServiceExceptionTest {

  @Test
  public void testConstructor_withMessage_thenMessageIsSet() {
    IamServiceException ex = new IamServiceException("test error");
    assertEquals("test error", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  public void testConstructor_withMessageAndCause_thenBothAreSet() {
    Throwable cause = new RuntimeException("root cause");
    IamServiceException ex = new IamServiceException("test error", cause);
    assertEquals("test error", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testIsRuntimeException() {
    IamServiceException ex = new IamServiceException("msg");
    assertTrue(ex instanceof RuntimeException);
  }
}
