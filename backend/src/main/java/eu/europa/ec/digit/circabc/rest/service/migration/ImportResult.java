package eu.europa.ec.digit.circabc.rest.service.migration;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable accumulator that captures the outcome of a migration import operation.
 *
 * <p>An instance is typically created at the start of an import and populated
 * incrementally as nodes are processed: successfully created nodes are tallied
 * via {@link #addNodes(int)} while any problems encountered are recorded as
 * human-readable messages via {@link #addError(String)}. Once the import
 * completes, callers can inspect the total number of created nodes and the list
 * of collected errors to report the result.
 *
 * <p>This class is not thread-safe; concurrent access must be synchronized
 * externally.
 */
public class ImportResult {

  /** Running total of nodes successfully created during the import. */
  private int nodesCreated;

  /** Human-readable error messages collected during the import, in the order they occurred. */
  private final List<String> errors = new ArrayList<>();

  /**
   * Returns the total number of nodes created so far during the import.
   *
   * @return the accumulated count of successfully created nodes
   */
  public int getNodesCreated() {
    return nodesCreated;
  }

  /**
   * Increments the count of created nodes by the given amount.
   *
   * @param count the number of newly created nodes to add to the running total
   */
  public void addNodes(int count) {
    nodesCreated += count;
  }

  /**
   * Returns the list of error messages collected during the import.
   *
   * <p>The returned list is the live backing list; modifications to it affect
   * this result.
   *
   * @return the collected error messages, in the order they were added
   */
  public List<String> getErrors() {
    return errors;
  }

  /**
   * Records an error message encountered during the import.
   *
   * @param error the human-readable error message to add
   */
  public void addError(String error) {
    errors.add(error);
  }
}
