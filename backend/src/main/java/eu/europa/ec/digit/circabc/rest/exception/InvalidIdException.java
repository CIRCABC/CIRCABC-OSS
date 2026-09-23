package eu.europa.ec.digit.circabc.rest.exception;

/**
 * Runtime exception raised in the REST layer when an identifier supplied to an
 * endpoint is missing, empty or otherwise invalid.
 *
 * <p>Being an unchecked exception, it can be thrown during request handling
 * (for example while validating path or query parameters) without being
 * declared in method signatures, and is typically translated into an
 * appropriate HTTP error response by the webscript error handling.
 */
public class InvalidIdException extends RuntimeException {

  /**
   * Creates the exception with a default message indicating that an invalid or
   * empty ID was provided.
   */
  public InvalidIdException() {
    super("Invalid or empty ID provided");
  }

  /**
   * Creates the exception with a custom detail message.
   *
   * @param message the detail message describing why the ID is considered
   *     invalid
   */
  public InvalidIdException(String message) {
    super(message);
  }
}
