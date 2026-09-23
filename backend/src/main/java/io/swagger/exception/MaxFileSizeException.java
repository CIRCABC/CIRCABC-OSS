/**
 *
 */
package io.swagger.exception;

/**
 * Checked exception thrown when an uploaded file exceeds the maximum allowed file size.
 *
 * <p>Used by the REST layer to signal that a content upload has been rejected because its size is
 * over the configured limit.
 *
 * @author beaurpi
 */
public class MaxFileSizeException extends Exception {

  /** Serialization version identifier for this exception class. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new exception with the given detail message.
   *
   * @param text the detail message describing the file size violation
   */
  public MaxFileSizeException(String text) {
    super(text);
  }
}
