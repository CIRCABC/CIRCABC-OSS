package io.swagger.exception;

/**
 * Unchecked exception thrown when an attempt is made to create a resource that
 * already exists.
 *
 * <p>Typically raised by the domain/API layer to signal a conflict (e.g. a
 * duplicate entity), allowing the REST layer to translate it into an
 * appropriate HTTP error response.
 */
public class AlreadyExistsException extends RuntimeException {

  /**
   * Creates a new exception with a message describing the resource that already
   * exists.
   *
   * @param message the detail message explaining what already exists
   */
  public AlreadyExistsException(String message) {
    super(message);
  }
}
