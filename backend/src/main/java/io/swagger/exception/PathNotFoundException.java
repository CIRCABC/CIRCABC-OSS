package io.swagger.exception;

/**
 * Checked exception thrown when a requested path cannot be resolved to an
 * existing node or resource in the repository.
 *
 * <p>Typically raised while navigating or looking up content by its path when
 * one or more path segments do not correspond to an existing item.
 */
public class PathNotFoundException extends Exception {

  /**
   * Creates a new exception describing the path that could not be found.
   *
   * @param message a detail message explaining which path was not found or why
   *     the lookup failed
   */
  public PathNotFoundException(String message) {
    super(message);
  }
}
