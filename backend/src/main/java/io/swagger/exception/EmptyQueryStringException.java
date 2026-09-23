package io.swagger.exception;

/**
 * Checked exception raised when a search or query operation is invoked with an
 * empty or missing query string.
 *
 * <p>This is typically thrown by search-related business logic to signal that
 * the caller did not provide the mandatory query text, allowing the REST layer
 * to translate it into an appropriate client error response.
 *
 * @author beaurpi
 */
public class EmptyQueryStringException extends Exception {

  /**
   * Serialization version identifier for this exception class.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new exception with the given detail message.
   *
   * @param text the detail message describing why the query string was
   *     considered empty or invalid
   */
  public EmptyQueryStringException(String text) {
    super(text);
  }
}
