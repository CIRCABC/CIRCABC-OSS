package eu.europa.ec.digit.circabc.rest.exception;

/**
 * Runtime exception raised when an error occurs while handling dynamic (custom) properties.
 *
 * <p>Dynamic properties are user-defined metadata attached to nodes in the CIRCABC repository. This
 * unchecked exception signals failures encountered while reading, validating, converting or
 * persisting such properties, allowing callers to propagate the error without declaring it in their
 * method signatures.
 */
public class DynamicPropertyException extends RuntimeException {

  /**
   * Creates a new exception with the given detail message.
   *
   * @param message a human-readable description of the error
   */
  public DynamicPropertyException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with the given detail message and underlying cause.
   *
   * @param message a human-readable description of the error
   * @param cause the underlying throwable that triggered this exception
   */
  public DynamicPropertyException(String message, Throwable cause) {
    super(message, cause);
  }
}
