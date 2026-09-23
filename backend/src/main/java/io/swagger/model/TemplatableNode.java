/**
 *
 */
package io.swagger.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight, template-friendly representation of a repository node.
 *
 * <p>This model exposes a flat set of node attributes (name, title, description, audit metadata,
 * keywords, path and URL) plus an open-ended map of additional properties. It is intended to be
 * passed to rendering layers such as FreeMarker notification/email templates, where a simple,
 * self-contained value object is easier to consume than a live Alfresco node reference.
 *
 * @author beaurpi
 */
public class TemplatableNode {

  /** Display name of the node (typically the {@code cm:name} property). */
  private String name;

  /** Human-readable title of the node. */
  private String title;

  /** Free-text description of the node. */
  private String description;

  /** Identifier or display name of the user who last modified the node. */
  private String modifier;

  /** Timestamp of the last modification, rendered as a string. */
  private String modified;

  /** Keywords or tags associated with the node. */
  private String keywords;

  /** Location of the node, expressed as a path. */
  private String path;

  /**
   * Additional, non-fixed node properties keyed by property name. Allows callers and templates to
   * carry arbitrary metadata beyond the explicit fields of this class.
   */
  private Map<String, String> dynamicProperties = new HashMap<>();

  /** URL pointing to the node, for use in links (e.g. within notification templates). */
  private String url;

  /** @return the name */
  public String getName() {
    return name;
  }

  /** @param name the name to set */
  public void setName(String name) {
    this.name = name;
  }

  /** @return the title */
  public String getTitle() {
    return title;
  }

  /** @param title the title to set */
  public void setTitle(String title) {
    this.title = title;
  }

  /** @return the description */
  public String getDescription() {
    return description;
  }

  /** @param description the description to set */
  public void setDescription(String description) {
    this.description = description;
  }

  /** @return the modifier */
  public String getModifier() {
    return modifier;
  }

  /** @param modifier the modifier to set */
  public void setModifier(String modifier) {
    this.modifier = modifier;
  }

  /** @return the modified */
  public String getModified() {
    return modified;
  }

  /** @param modified the modified to set */
  public void setModified(String modified) {
    this.modified = modified;
  }

  /** @return the keywords */
  public String getKeywords() {
    return keywords;
  }

  /** @param keywords the keywords to set */
  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  /** @return the path */
  public String getPath() {
    return path;
  }

  /** @param path the path to set */
  public void setPath(String path) {
    this.path = path;
  }

  /** @return the dynamicProperties */
  public Map<String, String> getDynamicProperties() {
    return dynamicProperties;
  }

  /** @param dynamicProperties the dynamicProperties to set */
  public void setDynamicProperties(Map<String, String> dynamicProperties) {
    this.dynamicProperties = dynamicProperties;
  }

  /** @return the url */
  public String getUrl() {
    return url;
  }

  /** @param url the url to set */
  public void setUrl(String url) {
    this.url = url;
  }
}
