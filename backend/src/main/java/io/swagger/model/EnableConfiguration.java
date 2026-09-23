/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/**
 * Simple data transfer object that carries a single boolean flag used to enable or disable a
 * configuration option.
 *
 * <p>Instances are typically deserialized from a JSON request body (or serialized into a response)
 * where the sole {@code enable} property indicates whether the associated feature or setting should
 * be turned on. The flag defaults to {@code true}.
 *
 * @author beaurpi
 */
public class EnableConfiguration {

  /** Flag indicating whether the configuration is enabled; defaults to {@code true}. */
  private Boolean enable = true;

  /**
   * Returns the current value of the enable flag.
   *
   * @return the enable flag, or {@code null} if it has been explicitly unset
   */
  public Boolean getEnable() {
    return enable;
  }

  /**
   * Sets the enable flag.
   *
   * @param enable the new value for the enable flag
   */
  public void setEnable(Boolean enable) {
    this.enable = enable;
  }

  /**
   * Computes a hash code based on the {@code enable} flag.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(enable);
  }

  /**
   * Returns a human-readable, indented string representation of this configuration.
   *
   * @return a string representation of this {@code EnableConfiguration}
   */
  @Override
  public String toString() {
    return (
      "class EnableConfiguration {\n" +
      "    enable: " +
      toIndentedString(enable) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert, may be {@code null}
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Compares this configuration to another object for equality based on the {@code enable} flag.
   *
   * @param o the object to compare with
   * @return {@code true} if the other object is an {@code EnableConfiguration} with the same enable
   *     value, {@code false} otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EnableConfiguration that = (EnableConfiguration) o;
    return Objects.equals(enable, that.enable);
  }
}
