package io.swagger.model;

import java.util.List;
import java.util.Objects;

/**
 * JSON data-transfer object representing a CIRCABC Header.
 *
 * <p>In the CIRCABC domain model a Header is the top-level organisational
 * entity that groups {@link Category} instances (which in turn contain Interest
 * Groups). This class is a simple bean used to serialise/deserialise a header
 * to and from the JSON payloads exchanged with the REST API and the Angular
 * frontend. It carries no business logic beyond value semantics
 * ({@link #equals(Object)}, {@link #hashCode()} and {@link #toString()}).
 */
public class Header {

  /** Unique identifier of the header (typically the Alfresco node reference). */
  private String id = null;

  /** Human-readable display name of the header. */
  private String name = null;

  /** Localised (i18n) description of the header. */
  private I18nProperty description = null;

  /** Categories that belong to this header; may be {@code null} when not loaded. */
  private List<Category> categories = null;

  /**
   * Returns the unique identifier of this header.
   *
   * @return the header id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier of this header.
   *
   * @param id the header id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the display name of this header.
   *
   * @return the header name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the display name of this header.
   *
   * @param name the header name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the localised description of this header.
   *
   * @return the description
   */
  public I18nProperty getDescription() {
    return description;
  }

  /**
   * Sets the localised description of this header.
   *
   * @param description the description to set
   */
  public void setDescription(I18nProperty description) {
    this.description = description;
  }

  /**
   * @return the categories
   */
  public List<Category> getCategories() {
    return categories;
  }

  /**
   * @param categories the categories to set
   */
  public void setCategories(List<Category> categories) {
    this.categories = categories;
  }

  /**
   * Compares this header with another object for equality. Two headers are
   * considered equal when their id, name, description and categories are all
   * equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code Header} with equal
   *     field values, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Header header = (Header) o;
    return (
      Objects.equals(this.id, header.id) &&
      Objects.equals(this.name, header.name) &&
      Objects.equals(this.description, header.description) &&
      Objects.equals(this.categories, header.categories)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from
   * the header's id, name, description and categories.
   *
   * @return the hash code value for this header
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, categories);
  }

  /**
   * Returns a human-readable, multi-line string representation of this header,
   * primarily intended for logging and debugging.
   *
   * @return a string representation of this header
   */
  @Override
  public String toString() {
    return (
      "class Header {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    name: " +
      toIndentedString(name) +
      "\n" +
      "    description: " +
      toIndentedString(description) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
