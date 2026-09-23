package eu.europa.ec.digit.circabc.rest.exception;

/**
 * Checked exception raised when a user profile cannot be persisted or retrieved
 * from the underlying store.
 *
 * <p>This exception signals failures in the profile persistence layer (for
 * example, when reading, creating or updating a profile fails) and allows the
 * originating cause to be propagated to the caller for diagnostics.
 */
public class ProfilePersistenceException extends Exception {

  /**
   * Creates a new exception with the given detail message.
   *
   * @param message the detail message describing the persistence failure
   */
  public ProfilePersistenceException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with the given detail message and underlying cause.
   *
   * @param message the detail message describing the persistence failure
   * @param cause the underlying cause of the failure
   */
  public ProfilePersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
