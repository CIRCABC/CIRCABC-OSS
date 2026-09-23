package io.swagger.model;

import java.util.Objects;

/**
 * Simple data transfer object representing a subscription flag.
 *
 * <p>Wraps a single {@link Boolean} value indicating whether a given entity
 * (for example a user with respect to a node, service or interest group) is
 * subscribed. It is typically serialized to / deserialized from JSON as part
 * of the CIRCABC REST API.
 */
public class Subscribed {

  /**
   * The subscription flag. {@code true} when subscribed, {@code false} when not
   * subscribed and {@code null} when the value has not been set.
   */
  private Boolean value = null;

  /**
   * Returns the subscription flag.
   *
   * @return {@code true} if subscribed, {@code false} if not, or {@code null}
   *     if the value has not been set
   */
  public Boolean getSubscribed() {
    return value;
  }

  /**
   * Sets the subscription flag.
   *
   * @param subscribed {@code true} to mark as subscribed, {@code false} to mark
   *     as not subscribed, or {@code null} to leave it unset
   */
  public void setSubscribed(Boolean subscribed) {
    this.value = subscribed;
  }

  /**
   * Compares this object with another for equality based on the subscription
   * flag.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code Subscribed} with an
   *     equal value, {@code false} otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Subscribed that = (Subscribed) o;
    return Objects.equals(value, that.value);
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code derived from the subscription flag
   */
  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  /**
   * Returns a human-readable representation of this object.
   *
   * @return a string containing the subscription flag
   */
  @Override
  public String toString() {
    return "Subscribed{" + "subscribed=" + value + '}';
  }
}
