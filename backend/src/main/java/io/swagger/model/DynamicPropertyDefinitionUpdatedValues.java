/**
 *
 */
package io.swagger.model;

/**
 * Data transfer object that captures the outcome of updating a single value of a dynamic property
 * definition.
 *
 * <p>It records the previous value, the newly assigned value and a status describing the result of
 * the update operation, so callers can report what changed and whether the change succeeded.
 *
 * @author beaurpi
 */
public class DynamicPropertyDefinitionUpdatedValues {

  /** The value held before the update was applied. */
  private String old;

  /** The value assigned by the update. */
  private String newValue;

  /** A status describing the outcome of the update (e.g. success or failure). */
  private String status;

  /**
   * Returns the value held before the update was applied.
   *
   * @return the previous value
   */
  public String getOld() {
    return old;
  }

  /**
   * Sets the value held before the update was applied.
   *
   * @param old the previous value
   */
  public void setOld(String old) {
    this.old = old;
  }

  /**
   * Returns the value assigned by the update.
   *
   * @return the new value
   */
  public String getNewValue() {
    return newValue;
  }

  /**
   * Sets the value assigned by the update.
   *
   * @param newValue the new value
   */
  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  /**
   * Returns the status describing the outcome of the update.
   *
   * @return the update status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status describing the outcome of the update.
   *
   * @param status the update status
   */
  public void setStatus(String status) {
    this.status = status;
  }
}
