package io.swagger.exception;

/**
 * Checked exception thrown when an email address fails validation.
 *
 * <p>Raised by the business/service layer when a supplied email address is
 * malformed or otherwise does not meet the expected format, so that callers
 * can surface a meaningful error to the client.
 *
 * @author beaurpi
 */
public class InvalidEmailException extends Exception {

  /**
   * Serialization version identifier for this exception type.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@code InvalidEmailException} with the given detail message.
   *
   * @param message a human-readable description explaining why the email
   *     address was considered invalid
   */
  public InvalidEmailException(String message) {
    super(message);
  }
}
