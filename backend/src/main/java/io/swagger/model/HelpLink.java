/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/**
 * Domain model representing a single help link exposed by the CIRCABC REST API.
 *
 * <p>A help link associates a stable identifier with a localized (i18n) title and a target URL. It
 * is typically serialized to JSON and returned to the Angular frontend so that context-sensitive
 * help resources can be displayed to users.
 *
 * @author beaurpi
 */
public class HelpLink {

  /** Unique identifier of the help link. */
  private String id = null;

  /** Localized, human-readable title of the help link. */
  private I18nProperty title = new I18nProperty();

  /** Target URL (hyperlink reference) the help link points to. */
  private String href;

  /**
   * Indicates whether another object is a {@link HelpLink} with equal id, title and href.
   *
   * @param o the reference object with which to compare
   * @return {@code true} if this object is equal to {@code o}; {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HelpLink helpLink = (HelpLink) o;
    return (
      Objects.equals(this.id, helpLink.id) &&
      Objects.equals(this.title, helpLink.title) &&
      Objects.equals(this.href, helpLink.href)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}, derived from the id,
   * title and href.
   *
   * @return the hash code value for this help link
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, title, href);
  }

  /**
   * Returns a human-readable string representation of this help link, primarily intended for
   * debugging and logging.
   *
   * @return a formatted string describing the id, title and href
   */
  @Override
  public String toString() {
    return (
      "class HelpLink {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    href: " +
      toIndentedString(href) +
      "\n }"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Returns the unique identifier of this help link.
   *
   * @return the id, or {@code null} if not set
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier of this help link.
   *
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the localized title of this help link.
   *
   * @return the i18n title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * Sets the localized title of this help link.
   *
   * @param title the i18n title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Returns the target URL this help link points to.
   *
   * @return the hyperlink reference (href), or {@code null} if not set
   */
  public String getHref() {
    return href;
  }

  /**
   * Sets the target URL this help link points to.
   *
   * @param href the hyperlink reference (href) to set
   */
  public void setHref(String href) {
    this.href = href;
  }
}
