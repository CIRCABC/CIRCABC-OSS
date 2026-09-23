package eu.europa.ec.digit.circabc.rest.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class MailServiceExceptionTest {

  @Test
  public void testConstructor_withMessage_thenMessageIsSet() {
    MailServiceException ex = new MailServiceException("test error");
    assertEquals("test error", ex.getMessage());
  }

  @Test
  public void testConstructor_withMessageAndCause_thenBothAreSet() {
    Throwable cause = new RuntimeException("root cause");
    MailServiceException ex = new MailServiceException("test error", cause);
    assertEquals("test error", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testIsInstanceOfException() {
    MailServiceException ex = new MailServiceException("msg");
    assertTrue(ex instanceof Exception);
  }
}
