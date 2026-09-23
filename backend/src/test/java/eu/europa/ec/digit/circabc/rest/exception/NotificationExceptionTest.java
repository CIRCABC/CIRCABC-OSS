package eu.europa.ec.digit.circabc.rest.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class NotificationExceptionTest {

  @Test
  public void testConstructor_withMessage() {
    NotificationException ex = new NotificationException("test error");
    assertEquals("test error", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  public void testConstructor_withMessageAndCause() {
    Throwable cause = new RuntimeException("root cause");
    NotificationException ex = new NotificationException("test error", cause);
    assertEquals("test error", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testIsException() {
    NotificationException ex = new NotificationException("msg");
    assertTrue(ex instanceof Exception);
  }
}
