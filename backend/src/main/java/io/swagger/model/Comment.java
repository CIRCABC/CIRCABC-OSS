package io.swagger.model;

import java.util.Objects;

/**
 * Domain model representing a comment.
 *
 * <p>A simple data transfer object that carries the textual content of a
 * comment exchanged through the CIRCABC REST API. It exposes standard
 * accessor/mutator methods for its {@code text} property and value-based
 * {@link #equals(Object)} / {@link #hashCode()} implementations.
 */
public class Comment {

  /** The textual content of the comment. */
  private String text = null;

  /**
   * Returns the textual content of the comment.
   *
   * @return the comment text, or {@code null} if not set
   */
  public String getText() {
    return text;
  }

  /**
   * Sets the textual content of the comment.
   *
   * @param text the comment text to set
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Compares this comment with another object for equality.
   *
   * <p>Two {@code Comment} instances are considered equal when their
   * {@code text} values are equal.
   *
   * @param o the object to compare with this comment
   * @return {@code true} if the given object is a {@code Comment} with an equal
   *     {@code text} value, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Comment comment = (Comment) o;
    return Objects.equals(this.text, comment.text);
  }

  /**
   * Returns a hash code value for this comment, derived from its {@code text}.
   *
   * @return the hash code for this comment
   */
  @Override
  public int hashCode() {
    return Objects.hash(text);
  }
}
