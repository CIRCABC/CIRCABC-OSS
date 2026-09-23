package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;

import org.junit.Test;

public class LdapLimitExceededExceptionTest {

  @Test
  public void testConstructor_withMessage() {
    LdapLimitExceededException ex = new LdapLimitExceededException("limit");
    assertEquals("limit", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  public void testConstructor_noArgs() {
    LdapLimitExceededException ex = new LdapLimitExceededException();
    assertNull(ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  public void testConstructor_withMessageAndCause() {
    Throwable cause = new RuntimeException("root");
    LdapLimitExceededException ex = new LdapLimitExceededException(
      "limit",
      cause
    );
    assertEquals("limit", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testConstructor_withCause() {
    Throwable cause = new RuntimeException("root");
    LdapLimitExceededException ex = new LdapLimitExceededException(cause);
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testIsRuntimeException() {
    LdapLimitExceededException ex = new LdapLimitExceededException();
    assertTrue(ex instanceof RuntimeException);
  }
}
