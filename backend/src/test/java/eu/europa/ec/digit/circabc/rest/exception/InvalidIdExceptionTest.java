package eu.europa.ec.digit.circabc.rest.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class InvalidIdExceptionTest {

  @Test
  public void testDefaultConstructor_thenHasDefaultMessage() {
    InvalidIdException ex = new InvalidIdException();
    assertEquals("Invalid or empty ID provided", ex.getMessage());
  }

  @Test
  public void testCustomMessage_thenHasCustomMessage() {
    InvalidIdException ex = new InvalidIdException("custom error");
    assertEquals("custom error", ex.getMessage());
  }

  @Test
  public void testIsRuntimeException() {
    InvalidIdException ex = new InvalidIdException();
    assertTrue(ex instanceof RuntimeException);
  }
}
