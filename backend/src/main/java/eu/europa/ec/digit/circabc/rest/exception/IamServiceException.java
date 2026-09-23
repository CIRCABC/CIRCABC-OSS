package eu.europa.ec.digit.circabc.rest.exception;

/**
 * Unchecked exception raised when an interaction with the IAM (Identity and Access
 * Management) service fails.
 *
 * <p>Being a {@link RuntimeException}, it does not need to be declared in method
 * signatures and typically propagates up to the REST layer where it is translated
 * into an appropriate error response.
 */
public class IamServiceException extends RuntimeException {

  /**
   * Creates a new exception with the given detail message.
   *
   * @param message the detail message describing the IAM failure
   */
  public IamServiceException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with the given detail message and underlying cause.
   *
   * @param message the detail message describing the IAM failure
   * @param cause the underlying cause of this exception
   */
  public IamServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
