package eu.europa.ec.digit.circabc.rest.exception;

/**
 * Checked exception raised when a mail-related operation in the CIRCABC REST layer fails.
 *
 * <p>Typical causes include failures while composing, sending or otherwise processing
 * notification or e-mail messages. Being a checked exception, callers are expected to handle
 * or propagate it explicitly.
 */
public class MailServiceException extends Exception {

  /**
   * Creates a new exception with the given detail message.
   *
   * @param message the detail message describing the mail failure
   */
  public MailServiceException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with the given detail message and underlying cause.
   *
   * @param message the detail message describing the mail failure
   * @param cause the underlying cause of this exception
   */
  public MailServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
