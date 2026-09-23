package io.swagger.model;

import java.util.Objects;

/**
 * Lightweight data-transfer object that wraps a single identifier.
 *
 * <p>{@code SimpleId} is used across the REST API to carry a bare {@code id}
 * value in request or response bodies where a full domain model would be
 * unnecessary (for example, when referencing a node, group or user solely by
 * its identifier). It exposes standard bean-style accessors together with
 * value-based {@link #equals(Object)}, {@link #hashCode()} and
 * {@link #toString()} implementations.</p>
 */
public class SimpleId {

  /** The wrapped identifier value; {@code null} when unset. */
  private String id = null;

  /**
   * Get id
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the wrapped identifier value.
   *
   * @param id the identifier to store; may be {@code null}
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Compares this object with another for value equality based on the
   * wrapped {@code id}.
   *
   * @param o the object to compare against
   * @return {@code true} if {@code o} is a {@code SimpleId} with an equal
   *     identifier, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SimpleId simpleId = (SimpleId) o;
    return Objects.equals(this.id, simpleId.id);
  }

  /**
   * Returns a hash code derived from the wrapped {@code id}, consistent with
   * {@link #equals(Object)}.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  /**
   * Returns a human-readable, multi-line representation of this object.
   *
   * @return a string describing this {@code SimpleId} and its identifier
   */
  @Override
  public String toString() {
    return (
      "class SimpleId {\n" + "    id: " + toIndentedString(id) + "\n" + "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
