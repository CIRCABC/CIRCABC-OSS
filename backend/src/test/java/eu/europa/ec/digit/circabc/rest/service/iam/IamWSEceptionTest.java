package eu.europa.ec.digit.circabc.rest.service.iam;

import static org.junit.Assert.*;

import org.junit.Test;

public class IamWSEceptionTest {

  @Test
  public void testConstructor_withMessage() {
    IamWSEception ex = new IamWSEception("error");
    assertEquals("error", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  public void testConstructor_withMessageAndCause() {
    Throwable cause = new RuntimeException("root");
    IamWSEception ex = new IamWSEception("error", cause);
    assertEquals("error", ex.getMessage());
    assertSame(cause, ex.getCause());
  }

  @Test
  public void testIsRuntimeException() {
    IamWSEception ex = new IamWSEception("test");
    assertTrue(ex instanceof RuntimeException);
  }
}
