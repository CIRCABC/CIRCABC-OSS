/**
 *
 */
package io.swagger.exception;

/**
 * Checked exception raised when an operation encounters an empty file where non-empty content is
 * required (for example, when uploading or processing a document whose payload contains no bytes).
 *
 * @author beaurpi
 */
public class EmptyFileException extends Exception {

  /** Serialization version identifier for this exception class. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@code EmptyFileException} with the given detail message.
   *
   * @param text the detail message describing why the file was considered empty
   */
  public EmptyFileException(String text) {
    super(text);
  }
}
