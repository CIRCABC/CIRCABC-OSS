package eu.europa.ec.digit.circabc.rest.exception;

/**
 * Runtime exception raised when the deletion of a user fails.
 *
 * <p>Thrown by the REST layer to signal that an attempt to remove a user could not be completed,
 * for example due to underlying repository errors or business-rule violations. As an unchecked
 * exception, it does not need to be declared in method signatures and typically propagates up to be
 * translated into an appropriate HTTP error response.
 */
public class UserDeletionException extends RuntimeException {

  /**
   * Creates a new exception with the given detail message.
   *
   * @param message the detail message describing why the user deletion failed
   */
  public UserDeletionException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with the given detail message and underlying cause.
   *
   * @param message the detail message describing why the user deletion failed
   * @param cause the underlying cause of the failure
   */
  public UserDeletionException(String message, Throwable cause) {
    super(message, cause);
  }
}
