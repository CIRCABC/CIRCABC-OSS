package eu.europa.ec.digit.circabc.rest.exception;

/**
 * Runtime exception raised when an FTP connection cannot be established or
 * maintained while the CIRCABC REST layer interacts with an FTP endpoint.
 *
 * <p>As an unchecked {@link RuntimeException}, it signals an unrecoverable
 * FTP connectivity failure that callers are generally not expected to
 * recover from, allowing the error to propagate up the REST request
 * handling chain.
 */
public class FtpConnectionException extends RuntimeException {

  /**
   * Creates a new exception with the given detail message.
   *
   * @param message a human-readable description of the FTP connection failure
   */
  public FtpConnectionException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with the given detail message and underlying cause.
   *
   * @param message a human-readable description of the FTP connection failure
   * @param cause the underlying throwable that triggered this exception
   */
  public FtpConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
