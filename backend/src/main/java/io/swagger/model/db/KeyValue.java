package io.swagger.model.db;

/**
 * Simple data holder that associates a string key with a numeric (long) value.
 *
 * <p>This is a lightweight value object typically used to carry key/value pairs
 * retrieved from the database layer, for example aggregated counts or statistics
 * keyed by a label. It defines {@link #equals(Object)}, {@link #hashCode()} and
 * {@link #toString()} based on both the {@code key} and {@code value} fields.
 */
public class KeyValue {

  /** The identifying label of this pair. */
  String key;

  /** The numeric value associated with the {@link #key}. */
  Long value;

  /**
   * Computes a hash code based on both the {@code key} and {@code value} fields.
   *
   * @return the hash code for this key/value pair
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /**
   * Returns a human-readable representation of this pair.
   *
   * @return a string containing the {@code key} and {@code value}
   */
  @Override
  public String toString() {
    return "KeyValue [key=" + key + ", value=" + value + "]";
  }

  /**
   * Compares this pair with another object for equality.
   *
   * <p>Two {@code KeyValue} instances are considered equal when they are of the
   * same class and both their {@code key} and {@code value} fields are equal.
   *
   * @param obj the object to compare with
   * @return {@code true} if the given object is an equal {@code KeyValue},
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    KeyValue other = (KeyValue) obj;
    if (key == null) {
      if (other.key != null) {
        return false;
      }
    } else if (!key.equals(other.key)) {
      return false;
    }
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  /**
   * Returns the identifying label of this pair.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the identifying label of this pair.
   *
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Returns the numeric value associated with the key.
   *
   * @return the value
   */
  public Long getValue() {
    return value;
  }

  /**
   * Sets the numeric value associated with the key.
   *
   * @param value the value to set
   */
  public void setValue(Long value) {
    this.value = value;
  }
}
