package eu.europa.ec.digit.circabc.rest.exception;

/**
 * Checked exception raised when a notification-related operation fails within the
 * CIRCABC REST layer.
 *
 * <p>Typical causes include failures while composing, sending, or otherwise processing
 * user or group notifications (e.g. mail delivery problems or invalid notification
 * configuration). Being a checked exception, callers are expected to handle or
 * propagate it explicitly.
 */
public class NotificationException extends Exception {

  /**
   * Creates a new exception with a descriptive message.
   *
   * @param message a human-readable description of the notification failure
   */
  public NotificationException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with a descriptive message and an underlying cause.
   *
   * @param message a human-readable description of the notification failure
   * @param cause the underlying throwable that triggered this exception
   */
  public NotificationException(String message, Throwable cause) {
    super(message, cause);
  }
}
