/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/**
 * Domain model representing a category of help articles in CIRCABC.
 *
 * <p>A help category groups together related help articles under a
 * localized title. It carries the category's unique identifier, its
 * internationalized title and the number of articles it currently
 * contains. Instances are typically serialized to JSON and returned by
 * the help-related REST endpoints.
 *
 * @author beaurpi
 */
public class HelpCategory {

  /** Unique identifier of the help category. */
  private String id = null;

  /** Localized (internationalized) title of the help category. */
  private I18nProperty title = new I18nProperty();

  /** Number of help articles contained in this category. */
  private Integer numberOfArticles = 0;

  /**
   * Compares this help category with another object for equality.
   *
   * <p>Two help categories are considered equal when their {@code id},
   * {@code title} and {@code numberOfArticles} are all equal.
   *
   * @param o the object to compare with this instance
   * @return {@code true} if the given object represents an equivalent
   *     help category, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HelpCategory helpCategory = (HelpCategory) o;
    return (
      Objects.equals(this.id, helpCategory.id) &&
      Objects.equals(this.title, helpCategory.title) &&
      Objects.equals(this.numberOfArticles, helpCategory.numberOfArticles)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, based on
   * the category's identifier and title.
   *
   * @return the hash code for this help category
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, title);
  }

  /**
   * Returns a human-readable, multi-line string representation of this
   * help category, primarily intended for debugging and logging.
   *
   * @return a string representation of this help category
   */
  @Override
  public String toString() {
    return (
      "class HelpCategory {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    numberOfArticles: " +
      toIndentedString(numberOfArticles) +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert and indent
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Returns the unique identifier of this help category.
   *
   * @return the category identifier, or {@code null} if not set
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier of this help category.
   *
   * @param id the category identifier to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the localized title of this help category.
   *
   * @return the internationalized title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * Sets the localized title of this help category.
   *
   * @param title the internationalized title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Returns the number of help articles contained in this category.
   *
   * @return the number of articles
   */
  public Integer getNumberOfArticles() {
    return numberOfArticles;
  }

  /**
   * Sets the number of help articles contained in this category.
   *
   * @param numberOfArticles the number of articles to set
   */
  public void setNumberOfArticles(Integer numberOfArticles) {
    this.numberOfArticles = numberOfArticles;
  }
}
