package io.swagger.model;

import java.util.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Domain/transfer model representing a single news item published within a CIRCABC
 * Interest Group's Information service.
 *
 * <p>A news item carries its localized title, textual content and publication date,
 * together with presentation metadata (a display {@link PatternEnum pattern} and a
 * {@link LayoutEnum layout}), an optional display {@code size} and any attached
 * {@link Node files}. It also holds auditing information (creator/modifier and the
 * corresponding timestamps), per-node {@code permissions}, arbitrary Alfresco node
 * {@code properties} and the resource {@code url}.
 *
 * <p>Instances of this class are serialized to JSON in REST responses and populated
 * from JSON in REST requests. This is a plain data holder with no business logic.
 */
public class News {

  /** Unique identifier (Alfresco node reference) of the news item. */
  private String id = null;

  /** Localized (i18n) title of the news item. */
  private I18nProperty title = null;

  /** Main textual body/content of the news item. */
  private String content = null;

  /** Publication date of the news item. */
  private LocalDate date = null;

  /** Per-node permissions, keyed by principal/authority. */
  private Map<String, String> permissions = new HashMap<>();

  /** Additional Alfresco node properties, keyed by property name. */
  private Map<String, String> properties = new HashMap<>();

  /** Display pattern controlling how the news content is rendered. */
  private PatternEnum pattern = null;

  /** Display layout emphasis of the news item. */
  private LayoutEnum layout = null;

  /** Display size hint; valid values are 1, 2 or 3. */
  private Integer size = null;

  /** Files attached to the news item. */
  private List<Node> files = new ArrayList<>();

  /** Timestamp of the last modification. */
  private DateTime modified = null;

  /** Identifier/username of the last modifier. */
  private String modifier = null;

  /** Timestamp of creation. */
  private DateTime created = null;

  /** Identifier/username of the creator. */
  private String creator = null;

  /** Resource URL pointing to the news item. */
  private String url = null;

  /**
   * Get id
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Set id
   *
   * @param id the identifier to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get title
   *
   * @return title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * Set title
   *
   * @param title the localized title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Get content
   *
   * @return content
   */
  public String getContent() {
    return content;
  }

  /**
   * Set content
   *
   * @param content the textual content to set
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Get date
   *
   * @return date
   */
  public LocalDate getDate() {
    return date;
  }

  /**
   * Set date
   *
   * @param date the publication date to set
   */
  public void setDate(LocalDate date) {
    this.date = date;
  }

  /**
   * Get pattern
   *
   * @return pattern
   */
  public PatternEnum getPattern() {
    return pattern;
  }

  /**
   * Set pattern
   *
   * @param pattern the display pattern to set
   */
  public void setPattern(PatternEnum pattern) {
    this.pattern = pattern;
  }

  /**
   * Get layout
   *
   * @return layout
   */
  public LayoutEnum getLayout() {
    return layout;
  }

  /**
   * Set layout
   *
   * @param layout the display layout to set
   */
  public void setLayout(LayoutEnum layout) {
    this.layout = layout;
  }

  /**
   * valid values are 1 / 2 / 3
   *
   * @return size
   */
  public Integer getSize() {
    return size;
  }

  /**
   * Set size
   *
   * @param size the display size hint to set (valid values are 1, 2 or 3)
   */
  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * Get files
   *
   * @return files
   */
  public List<Node> getFiles() {
    return files;
  }

  /**
   * Set files
   *
   * @param files the attached files to set
   */
  public void setFiles(List<Node> files) {
    this.files = files;
  }

  /**
   * Get modified
   *
   * @return modified
   */
  public DateTime getModified() {
    return modified;
  }

  /**
   * Set modified
   *
   * @param modified the last modification timestamp to set
   */
  public void setModified(DateTime modified) {
    this.modified = modified;
  }

  /**
   * Get modifier
   *
   * @return modifier
   */
  public String getModifier() {
    return modifier;
  }

  /**
   * Set modifier
   *
   * @param modifier the identifier of the last modifier to set
   */
  public void setModifier(String modifier) {
    this.modifier = modifier;
  }

  /**
   * Compares this news item with another object for equality. Two {@code News}
   * instances are equal when all of their fields are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code News} with equal fields,
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
    News news = (News) o;
    return (
      Objects.equals(this.id, news.id) &&
      Objects.equals(this.title, news.title) &&
      Objects.equals(this.content, news.content) &&
      Objects.equals(this.date, news.date) &&
      Objects.equals(this.pattern, news.pattern) &&
      Objects.equals(this.layout, news.layout) &&
      Objects.equals(this.size, news.size) &&
      Objects.equals(this.files, news.files) &&
      Objects.equals(this.modified, news.modified) &&
      Objects.equals(this.modifier, news.modifier) &&
      Objects.equals(this.created, news.created) &&
      Objects.equals(this.creator, news.creator) &&
      Objects.equals(this.permissions, news.permissions) &&
      Objects.equals(this.properties, news.properties) &&
      Objects.equals(this.url, news.url)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}.
   *
   * @return the hash code computed from all fields
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      id,
      title,
      content,
      date,
      pattern,
      layout,
      size,
      files,
      modified,
      modifier,
      created,
      creator,
      permissions,
      url
    );
  }

  /**
   * Returns a human-readable representation of this news item, listing all fields.
   *
   * @return a string representation of this {@code News}
   */
  @Override
  public String toString() {
    return (
      "class News {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    content: " +
      toIndentedString(content) +
      "\n" +
      "    date: " +
      toIndentedString(date) +
      "\n" +
      "    pattern: " +
      toIndentedString(pattern) +
      "\n" +
      "    layout: " +
      toIndentedString(layout) +
      "\n" +
      "    size: " +
      toIndentedString(size) +
      "\n" +
      "    files: " +
      toIndentedString(files) +
      "\n" +
      "    modified: " +
      toIndentedString(modified) +
      "\n" +
      "    modifier: " +
      toIndentedString(modifier) +
      "    created: " +
      toIndentedString(created) +
      "    creator: " +
      toIndentedString(creator) +
      "\n" +
      "    permissions: " +
      toIndentedString(permissions) +
      "\n" +
      "    properties: " +
      toIndentedString(properties) +
      "\n" +
      "    url: " +
      toIndentedString(url) +
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
   * @return the permissions
   */
  public Map<String, String> getPermissions() {
    return permissions;
  }

  /**
   * @param permissions the permissions to set
   */
  public void setPermissions(Map<String, String> permissions) {
    this.permissions = permissions;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Get properties
   *
   * @return the additional Alfresco node properties
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Set properties
   *
   * @param properties the additional Alfresco node properties to set
   */
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /**
   * Get created
   *
   * @return the creation timestamp
   */
  public DateTime getCreated() {
    return created;
  }

  /**
   * Set created
   *
   * @param created the creation timestamp to set
   */
  public void setCreated(DateTime created) {
    this.created = created;
  }

  /**
   * Get creator
   *
   * @return the identifier of the creator
   */
  public String getCreator() {
    return creator;
  }

  /**
   * Set creator
   *
   * @param creator the identifier of the creator to set
   */
  public void setCreator(String creator) {
    this.creator = creator;
  }

  /**
   * Enumeration of the supported display patterns for a news item, controlling how
   * its content is rendered. Each constant is associated with its wire (JSON) value.
   */
  public enum PatternEnum {
    TEXT("text"),

    DOCUMENT("document"),

    IMAGE("image"),

    DATE("date"),

    IFRAME("iframe");

    private final String value;

    PatternEnum(String value) {
      this.value = value;
    }

    /**
     * Resolves the enum constant matching the given wire value.
     *
     * @param text the wire (JSON) value to resolve
     * @return the matching {@code PatternEnum}, or {@code null} if none matches
     */
    public static PatternEnum fromValue(String text) {
      for (PatternEnum b : PatternEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    /**
     * Returns the wire (JSON) value of this pattern.
     *
     * @return the string value associated with this constant
     */
    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  /**
   * Enumeration of the supported layout emphases for a news item. Each constant is
   * associated with its wire (JSON) value.
   */
  public enum LayoutEnum {
    NORMAL("normal"),

    IMPORTANT("important"),

    REMINDER("reminder");

    private final String value;

    LayoutEnum(String value) {
      this.value = value;
    }

    /**
     * Resolves the enum constant matching the given wire value.
     *
     * @param text the wire (JSON) value to resolve
     * @return the matching {@code LayoutEnum}, or {@code null} if none matches
     */
    public static LayoutEnum fromValue(String text) {
      for (LayoutEnum b : LayoutEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    /**
     * Returns the wire (JSON) value of this layout.
     *
     * @return the string value associated with this constant
     */
    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }
}
