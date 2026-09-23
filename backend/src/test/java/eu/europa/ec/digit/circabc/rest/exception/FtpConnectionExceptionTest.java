package eu.europa.ec.digit.circabc.rest.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class FtpConnectionExceptionTest {

  @Test
  public void testConstructor_withMessage_thenMessageIsSet() {
    FtpConnectionException ex = new FtpConnectionException("connection failed");
    assertEquals("connection failed", ex.getMessage());
  }

  @Test
  public void testConstructor_withMessageAndCause_thenBothAreSet() {
    Throwable cause = new RuntimeException("timeout");
    FtpConnectionException ex = new FtpConnectionException(
      "connection failed",
      cause
    );
    assertEquals("connection failed", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testIsRuntimeException() {
    FtpConnectionException ex = new FtpConnectionException("test");
    assertTrue(ex instanceof RuntimeException);
  }
}
