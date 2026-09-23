package eu.europa.ec.digit.circabc.rest.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserDeletionExceptionTest {

  @Test
  public void testConstructor_withMessage() {
    UserDeletionException ex = new UserDeletionException("user not found");
    assertEquals("user not found", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  public void testConstructor_withMessageAndCause() {
    Throwable cause = new RuntimeException("root cause");
    UserDeletionException ex = new UserDeletionException(
      "deletion failed",
      cause
    );
    assertEquals("deletion failed", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testIsRuntimeException() {
    UserDeletionException ex = new UserDeletionException("test");
    assertTrue(ex instanceof RuntimeException);
  }
}
