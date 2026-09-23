package eu.europa.ec.digit.circabc.rest.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProfilePersistenceExceptionTest {

  @Test
  public void testConstructor_withMessage_thenMessageIsSet() {
    ProfilePersistenceException ex = new ProfilePersistenceException(
      "test error"
    );
    assertEquals("test error", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  public void testConstructor_withMessageAndCause_thenBothAreSet() {
    Throwable cause = new RuntimeException("root cause");
    ProfilePersistenceException ex = new ProfilePersistenceException(
      "test error",
      cause
    );
    assertEquals("test error", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testIsInstanceOfException() {
    ProfilePersistenceException ex = new ProfilePersistenceException("msg");
    assertTrue(ex instanceof Exception);
  }
}
