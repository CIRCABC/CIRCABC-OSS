package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing all the translations attached to a single node.
 *
 * <p>This describes the translations of a node's <em>content</em> (i.e. sibling nodes that are
 * alternative language versions of the same content item), not the translations of each of the
 * node's individual properties.
 *
 * <p>A {@code Translations} instance is composed of:
 *
 * <ul>
 *   <li>a {@link #getPivot() pivot} node, which is the reference/original node the translations
 *       relate to; and
 *   <li>a list of {@link #getTranslations() translation} nodes representing the available
 *       translated versions.
 * </ul>
 */
public class Translations {

  /** The reference (original) node that the translations relate to. */
  private Node pivot = null;

  /** The list of nodes that are translations of the pivot node's content. */
  private List<Node> items = new ArrayList<>();

  /**
   * Get pivot
   *
   * @return pivot
   */
  public Node getPivot() {
    return pivot;
  }

  /**
   * Sets the reference (original) node that the translations relate to.
   *
   * @param pivot the pivot node to set
   */
  public void setPivot(Node pivot) {
    this.pivot = pivot;
  }

  /**
   * Adds a single translation node to this instance's list of translations.
   *
   * @param translationsItem the translation node to add
   * @return this {@code Translations} instance, to allow method chaining
   */
  public Translations addTranslationsItem(Node translationsItem) {
    this.items.add(translationsItem);
    return this;
  }

  /**
   * Get translations
   *
   * @return translations
   */
  public List<Node> getTranslations() {
    return items;
  }

  /**
   * Sets the list of nodes that are translations of the pivot node's content.
   *
   * @param translations the list of translation nodes to set
   */
  public void setTranslations(List<Node> translations) {
    this.items = translations;
  }

  /**
   * Compares this instance with another object for equality. Two {@code Translations} instances are
   * equal when their pivot nodes and their lists of translation nodes are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is equal to this instance, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Translations objectTranslations = (Translations) o;
    return (
      Objects.equals(this.pivot, objectTranslations.pivot) &&
      Objects.equals(this.items, objectTranslations.items)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}, derived from the pivot
   * node and the list of translation nodes.
   *
   * @return the hash code for this instance
   */
  @Override
  public int hashCode() {
    return Objects.hash(pivot, items);
  }

  /**
   * Returns a human-readable, multi-line string representation of this instance, including the
   * pivot node and the list of translation nodes.
   *
   * @return a string representation of this instance
   */
  @Override
  public String toString() {
    return (
      "class Translations {\n" +
      "    pivot: " +
      toIndentedString(pivot) +
      "\n" +
      "    translations: " +
      toIndentedString(items) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert; may be {@code null}
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
