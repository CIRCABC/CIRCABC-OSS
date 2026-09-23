/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/**
 * Simple data model (DTO) that captures a single display toggle used to control
 * whether a given element or feature is shown in the CIRCABC user interface.
 *
 * <p>Instances are typically serialized to / deserialized from the JSON REST API
 * consumed by the Angular frontend. The single {@link #display} flag defaults to
 * {@code true}, meaning the associated element is visible unless explicitly hidden.
 *
 * @author beaurpi
 */
public class DisplayConfiguration {

  /** Whether the associated element is displayed; defaults to {@code true} (visible). */
  private Boolean display = true;

  /**
   * Returns the display flag.
   *
   * @return {@code true} if the associated element should be displayed,
   *         {@code false} if it should be hidden, or {@code null} if unset
   */
  public Boolean getDisplay() {
    return display;
  }

  /**
   * Sets the display flag.
   *
   * @param display {@code true} to display the associated element, {@code false}
   *                to hide it, or {@code null} to leave it unset
   */
  public void setDisplay(Boolean display) {
    this.display = display;
  }

  /**
   * Computes a hash code consistent with {@link #equals(Object)}, based on the
   * {@link #display} field.
   *
   * @return the hash code for this configuration
   */
  @Override
  public int hashCode() {
    return Objects.hash(display);
  }

  /**
   * Returns a human-readable, multi-line string representation of this configuration.
   *
   * @return a string describing the {@link #display} field
   */
  @Override
  public String toString() {
    return (
      "class DisplayConfiguration {\n" +
      "    display: " +
      toIndentedString(display) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to render; may be {@code null}
   * @return the indented string representation of {@code o}
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Compares this configuration with another object for equality. Two
   * {@code DisplayConfiguration} instances are equal when their {@link #display}
   * fields are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code DisplayConfiguration},
   *         {@code false} otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DisplayConfiguration that = (DisplayConfiguration) o;
    return Objects.equals(display, that.display);
  }
}
