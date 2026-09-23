package io.swagger.model;

import java.util.Objects;

/**
 * Domain model describing a keyword definition used to tag or classify content within CIRCABC.
 *
 * <p>A keyword definition carries a stable identifier, a machine-readable name and an
 * internationalized, human-readable title. Instances are typically serialized to JSON as part
 * of REST responses and deserialized from request payloads.
 */
public class KeywordDefinition {

  /** Unique, stable identifier of the keyword definition. */
  private String id = null;

  /** Machine-readable name of the keyword. */
  private String name = null;

  /** Internationalized, human-readable title of the keyword. */
  private I18nProperty title = new I18nProperty();

  /**
   * Returns the unique identifier of the keyword definition.
   *
   * @return the keyword identifier, or {@code null} if not set
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the keyword definition.
   *
   * @param id the keyword identifier to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the machine-readable name of the keyword.
   *
   * @return the keyword name, or {@code null} if not set
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the machine-readable name of the keyword.
   *
   * @param name the keyword name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the internationalized, human-readable title of the keyword.
   *
   * @return the keyword title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * Sets the internationalized, human-readable title of the keyword.
   *
   * @param title the keyword title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Compares this keyword definition with another object for equality.
   *
   * <p>Two instances are considered equal when their {@code id}, {@code name} and {@code title}
   * fields are all equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code KeywordDefinition}, {@code false}
   *     otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeywordDefinition keywordDefinition = (KeywordDefinition) o;
    return (
      Objects.equals(this.id, keywordDefinition.id) &&
      Objects.equals(this.name, keywordDefinition.name) &&
      Objects.equals(this.title, keywordDefinition.title)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}.
   *
   * @return a hash code derived from the {@code id}, {@code name} and {@code title} fields
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, title);
  }

  /**
   * Returns a human-readable string representation of this keyword definition.
   *
   * @return a formatted string listing the field values
   */
  @Override
  public String toString() {
    return (
      "class KeywordDefinition {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    name: " +
      toIndentedString(name) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to render; may be {@code null}
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
