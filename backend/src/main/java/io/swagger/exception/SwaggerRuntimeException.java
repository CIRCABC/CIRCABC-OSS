package io.swagger.exception;

/**
 * Unchecked exception used across the Swagger-generated API layer to signal
 * runtime failures that should abort the current operation.
 *
 * <p>As a {@link RuntimeException} it does not need to be declared in method
 * signatures, allowing it to propagate up through the API and REST layers where
 * it is typically translated into an appropriate HTTP error response. It wraps
 * either a descriptive message, an underlying cause, or both.
 */
public class SwaggerRuntimeException extends RuntimeException {

  /**
   * Creates an exception with the given detail message.
   *
   * @param s the detail message describing the failure
   */
  public SwaggerRuntimeException(String s) {
    super(s);
  }

  /**
   * Creates an exception wrapping the given underlying cause.
   *
   * @param e the exception that caused this failure
   */
  public SwaggerRuntimeException(Exception e) {
    super(e);
  }

  /**
   * Creates an exception with the given detail message and underlying cause.
   *
   * @param s the detail message describing the failure
   * @param e the exception that caused this failure
   */
  public SwaggerRuntimeException(String s, Exception e) {
    super(s, e);
  }
}
