package io.swagger.exception;

/**
 * Checked exception signalling that a referenced node could not be found in the
 * Alfresco repository.
 *
 * <p>Thrown when an operation attempts to resolve or act upon a node (document,
 * folder, topic, etc.) whose identifier does not correspond to any existing node,
 * allowing callers to translate the condition into an appropriate REST error
 * response.
 *
 * @author beaurpi
 */
public class NonExistingNodeException extends Exception {

  /** Serialization version identifier for this exception type. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new exception describing the missing node.
   *
   * @param message the detail message explaining which node could not be found
   */
  public NonExistingNodeException(String message) {
    super(message);
  }
}
