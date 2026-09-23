package io.swagger.model.db;

/**
 * Simple mutable key/value pair holder where both the key and the value are
 * {@link String}s.
 *
 * <p>Used as a lightweight data model for representing string-based key/value
 * associations (for example configuration or property entries) within the
 * persistence/database layer.
 */
public class KeyValueString {

  /** The string key of this pair. */
  String key;

  /** The string value associated with {@link #key}. */
  String value;

  /**
   * Computes a hash code based on the {@code key} and {@code value} fields,
   * consistent with {@link #equals(Object)}.
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
   * Returns a string representation of this key/value pair for debugging and
   * logging purposes.
   *
   * @return a string containing the key and value
   */
  @Override
  public String toString() {
    return "KeyValue [key=" + key + ", value=" + value + "]";
  }

  /**
   * Compares this key/value pair with another object for equality. Two
   * {@code KeyValueString} instances are considered equal when both their
   * {@code key} and {@code value} fields are equal.
   *
   * @param obj the object to compare with this instance
   * @return {@code true} if the given object is a {@code KeyValueString} with
   *     equal key and value, {@code false} otherwise
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
    KeyValueString other = (KeyValueString) obj;
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
   * Returns the key of this pair.
   *
   * @return the key, or {@code null} if not set
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the key of this pair.
   *
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Returns the value of this pair.
   *
   * @return the value, or {@code null} if not set
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value of this pair.
   *
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }
}
