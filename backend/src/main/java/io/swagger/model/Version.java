package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object describing a single version of a content item of any kind.
 *
 * <p>A version pairs the versioned {@link Node} with a human-readable version label
 * (e.g. {@code "1.0"}) and optional free-text notes describing the change. Instances
 * are serialized to JSON as part of the CIRCABC REST API responses.
 */
public class Version {

  /** The content node this version refers to. */
  private Node node = null;

  /** Optional free-text notes describing the changes introduced by this version. */
  private String notes = "";

  /** The human-readable version label (e.g. {@code "1.0"}). */
  private String versionLabel = null;

  /**
   * Returns the content node this version refers to.
   *
   * @return the versioned node
   */
  public Node getNode() {
    return node;
  }

  /**
   * Sets the content node this version refers to.
   *
   * @param node the versioned node to set
   */
  public void setNode(Node node) {
    this.node = node;
  }

  /**
   * Returns the human-readable version label.
   *
   * @return the version label (e.g. {@code "1.0"})
   */
  public String getVersionLabel() {
    return versionLabel;
  }

  /**
   * Sets the human-readable version label.
   *
   * @param versionLabel the version label to set (e.g. {@code "1.0"})
   */
  public void setVersionLabel(String versionLabel) {
    this.versionLabel = versionLabel;
  }

  /**
   * Compares this version with another object for equality.
   *
   * <p>Two versions are considered equal when their {@code node} and
   * {@code versionLabel} are equal. The {@code notes} field is not taken into account.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code Version}, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Version version = (Version) o;
    return (
      Objects.equals(this.node, version.node) &&
      Objects.equals(this.versionLabel, version.versionLabel)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from
   * {@code node} and {@code versionLabel}.
   *
   * @return the hash code for this version
   */
  @Override
  public int hashCode() {
    return Objects.hash(node, versionLabel);
  }

  /**
   * Returns a multi-line string representation of this version for debugging purposes.
   *
   * @return a human-readable representation of this version
   */
  @Override
  public String toString() {
    return (
      "class Version {\n" +
      "    node: " +
      toIndentedString(node) +
      "\n" +
      "    versionLabel: " +
      toIndentedString(versionLabel) +
      "\n" +
      "}"
    );
  }

  /**
   * Converts the given object to a string with each line indented by 4 spaces
   * (except the first line).
   *
   * @param o the object to convert; may be {@code null}
   * @return the indented string representation
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Returns the free-text notes describing the changes introduced by this version.
   *
   * @return the notes
   */
  public String getNotes() {
    return notes;
  }

  /**
   * Sets the free-text notes describing the changes introduced by this version.
   *
   * @param notes the notes to set
   */
  public void setNotes(String notes) {
    this.notes = notes;
  }
}
