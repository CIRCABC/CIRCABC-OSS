package io.swagger.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data transfer object representing a node in the CIRCABC/Alfresco repository.
 *
 * <p>A node is a generic content item such as a document, folder, topic or post.
 * Instances of this class are serialized to JSON by the REST layer and carry the
 * node's identity, type, localized metadata, arbitrary properties, effective
 * permissions and a set of state flags (favourite, guest access, sub-folder
 * presence).
 */
public class Node {

  /** Unique identifier of the node (typically the Alfresco node reference id). */
  private String id = null;

  /** The node's content type (e.g. document, folder). */
  private String type = null;

  /** The CIRCABC service the node belongs to (Library, Newsgroup, Information, Events). */
  private CircabcServiceName service = null;

  /** Human-readable name of the node. */
  private String name = null;

  /** Localized (internationalized) title of the node. */
  private I18nProperty title = new I18nProperty();

  /** Localized (internationalized) description of the node. */
  private I18nProperty description = new I18nProperty();

  /** Arbitrary key/value metadata properties associated with the node. */
  private Map<String, String> properties = new HashMap<>();

  /** Effective permissions for the current user, keyed by permission name. */
  private Map<String, String> permissions = new HashMap<>();

  /** Identifier of the parent node, if any. */
  private String parentId = null;

  /** Notification configuration for the node, expressed as a string value. */
  private String notifications = "";

  /** Whether the node is marked as a favourite by the current user. */
  private Boolean favourite = false;

  /** Whether the node contains sub-folders. */
  private Boolean hasSubFolders = false;

  /** Whether the node is accessible to guest (unauthenticated) users. */
  private Boolean hasGuestAccess = false;

  /**
   * NodeRef of the node in the source system this node was migrated from.
   * Populated only for nodes carrying the {@code ci:migrated} aspect
   * ({@code ci:originalNodeRef} property); {@code null} otherwise.
   */
  private String originalNodeRef = null;

  /**
   * Get id
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the node identifier.
   *
   * @param id the identifier to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get type
   *
   * @return type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the node content type.
   *
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Get name
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the human-readable node name.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
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
   * Sets the localized title.
   *
   * @param title the title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Get description
   *
   * @return description
   */
  public I18nProperty getDescription() {
    return description;
  }

  /**
   * Sets the localized description.
   *
   * @param description the description to set
   */
  public void setDescription(I18nProperty description) {
    this.description = description;
  }

  /**
   * Get properties
   *
   * @return properties
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Sets the arbitrary metadata properties.
   *
   * @param properties the properties to set
   */
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /**
   * Compares this node with another object for equality. Two nodes are considered
   * equal when their identity, type, service, name, localized title and description,
   * properties, notifications and the {@code favourite} and {@code hasSubFolders}
   * flags are all equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object represents an equivalent node,
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
    Node node = (Node) o;
    return (
      Objects.equals(this.id, node.id) &&
      Objects.equals(this.type, node.type) &&
      Objects.equals(this.service, node.service) &&
      Objects.equals(this.name, node.name) &&
      Objects.equals(this.title, node.title) &&
      Objects.equals(this.description, node.description) &&
      Objects.equals(this.properties, node.properties) &&
      Objects.equals(this.notifications, node.notifications) &&
      Objects.equals(this.favourite, node.favourite) &&
      Objects.equals(this.hasSubFolders, node.hasSubFolders)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}.
   *
   * @return the hash code for this node
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      id,
      type,
      service,
      name,
      title,
      description,
      properties,
      notifications,
      favourite,
      hasSubFolders
    );
  }

  /**
   * Returns a human-readable, multi-line string representation of this node.
   *
   * @return a string describing the node's fields
   */
  @Override
  public String toString() {
    return (
      "class Node {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    type: " +
      toIndentedString(type) +
      "\n" +
      "    service: " +
      toIndentedString(service) +
      "\n" +
      "    name: " +
      toIndentedString(name) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    description: " +
      toIndentedString(description) +
      "\n" +
      "    properties: " +
      toIndentedString(properties) +
      "\n" +
      "    notification: " +
      toIndentedString(notifications) +
      "\n" +
      "    favourite: " +
      toIndentedString(favourite) +
      "\n" +
      "    hasSubFolders: " +
      toIndentedString(hasSubFolders) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert
   * @return the indented string representation of the object
   */
  protected String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * @return the parentId
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * @param parentId the parentId to set
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
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
   * @return the notification
   */
  public String getNotifications() {
    return notifications;
  }

  /**
   * @param notifications the notification to set
   */
  public void setNotifications(String notifications) {
    this.notifications = notifications;
  }

  /**
   * @return the favourite
   */
  public Boolean getFavourite() {
    return favourite;
  }

  /**
   * @param favourite the favourite to set
   */
  public void setFavourite(Boolean favourite) {
    this.favourite = favourite;
  }

  /**
   * Returns the CIRCABC service the node belongs to.
   *
   * @return the service
   */
  public CircabcServiceName getService() {
    return service;
  }

  /**
   * Sets the CIRCABC service the node belongs to.
   *
   * @param service the service to set
   */
  public void setService(CircabcServiceName service) {
    this.service = service;
  }

  /**
   * Indicates whether the node contains sub-folders.
   *
   * @return {@code true} if the node has sub-folders, {@code false} otherwise
   */
  public Boolean getHasSubFolders() {
    return hasSubFolders;
  }

  /**
   * Sets whether the node contains sub-folders.
   *
   * @param hasSubFolders the sub-folder flag to set
   */
  public void setHasSubFolders(Boolean hasSubFolders) {
    this.hasSubFolders = hasSubFolders;
  }

  /**
   * Indicates whether the node is accessible to guest (unauthenticated) users.
   *
   * @return {@code true} if guest access is allowed, {@code false} otherwise
   */
  public Boolean getHasGuestAccess() {
    return hasGuestAccess;
  }

  /**
   * Sets whether the node is accessible to guest (unauthenticated) users.
   *
   * @param hasGuestAccess the guest-access flag to set
   */
  public void setHasGuestAccess(Boolean hasGuestAccess) {
    this.hasGuestAccess = hasGuestAccess;
  }

  /**
   * NodeRef of the node in the source system this node was migrated from.
   * Populated only for nodes carrying the {@code ci:migrated} aspect
   * ({@code ci:originalNodeRef} property); {@code null} otherwise.
   *
   * @return the original (source) node reference, or {@code null}
   */
  public String getOriginalNodeRef() {
    return originalNodeRef;
  }

  /**
   * Sets the original (source) node reference.
   *
   * @param originalNodeRef the original node reference to set
   */
  public void setOriginalNodeRef(String originalNodeRef) {
    this.originalNodeRef = originalNodeRef;
  }
}
