package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object used when enabling the multilingual aspect on a document.
 *
 * <p>It carries the metadata required to mark a node as multilingual: the pivot
 * (reference) language of the content and the author associated with it. Instances
 * are typically deserialized from the JSON request body of the corresponding REST
 * endpoint and passed to the service layer.
 */
public class MultilingualAspectMetadata {

  /** ISO language code of the pivot (reference) language for the multilingual content. */
  private String pivotLang = null;

  /** Author associated with the multilingual content. */
  private String author = null;

  /**
   * Returns the pivot (reference) language of the multilingual content.
   *
   * @return the pivot language code, or {@code null} if not set
   */
  public String getPivotLang() {
    return pivotLang;
  }

  /**
   * Sets the pivot (reference) language of the multilingual content.
   *
   * @param pivotLang the pivot language code to set
   */
  public void setPivotLang(String pivotLang) {
    this.pivotLang = pivotLang;
  }

  /**
   * Returns the author associated with the multilingual content.
   *
   * @return the author, or {@code null} if not set
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Sets the author associated with the multilingual content.
   *
   * @param author the author to set
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * Compares this metadata to another object for equality. Two instances are equal
   * when their pivot language and author values are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code MultilingualAspectMetadata}
   *     with the same pivot language and author, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MultilingualAspectMetadata multilingualAspectMetadata =
      (MultilingualAspectMetadata) o;
    return (
      Objects.equals(this.pivotLang, multilingualAspectMetadata.pivotLang) &&
      Objects.equals(this.author, multilingualAspectMetadata.author)
    );
  }

  /**
   * Returns a hash code derived from the pivot language and author.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(pivotLang, author);
  }

  /**
   * Returns a human-readable string representation of this metadata, listing the
   * pivot language and author.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (
      "class MultilingualAspectMetadata {\n" +
      "    pivotLang: " +
      toIndentedString(pivotLang) +
      "\n" +
      "    author: " +
      toIndentedString(author) +
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
