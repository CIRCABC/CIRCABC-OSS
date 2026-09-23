package eu.europa.ec.digit.circabc.rest.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class LdapAccessExceptionTest {

  @Test
  public void testConstructor_withMessage_thenMessageIsSet() {
    LdapAccessException ex = new LdapAccessException("ldap error");
    assertEquals("ldap error", ex.getMessage());
  }

  @Test
  public void testConstructor_withMessageAndCause_thenBothAreSet() {
    Throwable cause = new RuntimeException("root cause");
    LdapAccessException ex = new LdapAccessException("ldap error", cause);
    assertEquals("ldap error", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testIsRuntimeException() {
    LdapAccessException ex = new LdapAccessException("test");
    assertTrue(ex instanceof RuntimeException);
  }
}
