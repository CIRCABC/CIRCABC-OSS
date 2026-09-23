/**
 *
 */
package io.swagger.model;

import java.util.Objects;
import org.joda.time.DateTime;

/**
 * Data transfer object representing a single help article in the CIRCABC help system.
 *
 * <p>A help article carries internationalized (i18n) title and content, together with
 * metadata such as its author, the last update timestamp, the identifier of its parent
 * help category, a flag indicating whether it should be highlighted, and a counter of how
 * many times it has been visited. Instances are serialized to/from JSON when exchanged
 * through the REST API.
 *
 * @author beaurpi
 */
public class HelpArticle {

  /** Unique identifier of the help article. */
  private String id = null;

  /** Internationalized title of the help article. */
  private I18nProperty title = new I18nProperty();

  /** Internationalized body content of the help article. */
  private I18nProperty content = new I18nProperty();

  /** Timestamp of the last modification of the help article. */
  private DateTime lastUpdate;

  /** Identifier or name of the author of the help article. */
  private String author = null;

  /** Identifier of the parent help category this article belongs to. */
  private String parentId = null;

  /** Flag indicating whether the article should be highlighted/featured. */
  private Boolean highlighted = null;

  /** Number of times the help article has been visited. */
  private Integer visitCounter = null;

  /**
   * Compares this help article with another object for equality. Two help articles are
   * considered equal when all of their fields (id, title, content, last update, author,
   * parent id, highlighted flag and visit counter) are equal.
   *
   * @param o the object to compare with this help article
   * @return {@code true} if the given object is a {@code HelpArticle} with equal fields,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HelpArticle helpCategory = (HelpArticle) o;
    return (
      Objects.equals(this.id, helpCategory.id) &&
      Objects.equals(this.title, helpCategory.title) &&
      Objects.equals(this.content, helpCategory.content) &&
      Objects.equals(this.lastUpdate, helpCategory.lastUpdate) &&
      Objects.equals(this.author, helpCategory.author) &&
      Objects.equals(this.parentId, helpCategory.parentId) &&
      Objects.equals(this.highlighted, helpCategory.highlighted) &&
      Objects.equals(this.visitCounter, helpCategory.visitCounter)
    );
  }

  /**
   * Returns a hash code for this help article, derived from its id, title and content.
   *
   * @return the hash code value for this help article
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, title, content);
  }

  /**
   * Returns a human-readable, multi-line string representation of this help article listing
   * all of its fields.
   *
   * @return a string representation of this help article
   */
  @Override
  public String toString() {
    return (
      "class HelpArticle {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    content: " +
      toIndentedString(content) +
      "\n" +
      "    lastUpdate: " +
      toIndentedString(lastUpdate) +
      "\n" +
      "    author: " +
      toIndentedString(author) +
      "\n" +
      "    parentId: " +
      toIndentedString(parentId) +
      "\n" +
      "    highlighted: " +
      toIndentedString(highlighted) +
      "\n" +
      "    visitCounter: " +
      toIndentedString(visitCounter) +
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

  /**
   * Returns the unique identifier of this help article.
   *
   * @return the article id, or {@code null} if not set
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier of this help article.
   *
   * @param id the article id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the internationalized title of this help article.
   *
   * @return the title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * Sets the internationalized title of this help article.
   *
   * @param title the title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Returns the internationalized content of this help article.
   *
   * @return the content
   */
  public I18nProperty getContent() {
    return content;
  }

  /**
   * Sets the internationalized content of this help article.
   *
   * @param content the content to set
   */
  public void setContent(I18nProperty content) {
    this.content = content;
  }

  /**
   * Returns the timestamp of the last update of this help article.
   *
   * @return the last update timestamp, or {@code null} if not set
   */
  public DateTime getLastUpdate() {
    return lastUpdate;
  }

  /**
   * Sets the timestamp of the last update of this help article.
   *
   * @param lastUpdate the last update timestamp to set
   */
  public void setLastUpdate(DateTime lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  /**
   * Returns the author of this help article.
   *
   * @return the author, or {@code null} if not set
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Sets the author of this help article.
   *
   * @param author the author to set
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * Returns the identifier of the parent help category of this article.
   *
   * @return the parent id, or {@code null} if not set
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * Sets the identifier of the parent help category of this article.
   *
   * @param parentId the parent id to set
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  /**
   * Returns whether this help article is highlighted/featured.
   *
   * @return {@code true} if highlighted, {@code false} or {@code null} otherwise
   */
  public Boolean getHighlighted() {
    return highlighted;
  }

  /**
   * Sets whether this help article is highlighted/featured.
   *
   * @param highlighted the highlighted flag to set
   */
  public void setHighlighted(Boolean highlighted) {
    this.highlighted = highlighted;
  }

  /**
   * Returns the number of times this help article has been visited.
   *
   * @return the visit counter, or {@code null} if not set
   */
  public Integer getVisitCounter() {
    return visitCounter;
  }

  /**
   * Sets the number of times this help article has been visited.
   *
   * @param visitCounter the visit counter to set
   */
  public void setVisitCounter(Integer visitCounter) {
    this.visitCounter = visitCounter;
  }
}
