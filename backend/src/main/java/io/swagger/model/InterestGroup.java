package io.swagger.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data transfer object (DTO) representing an Interest Group (IG) in CIRCABC.
 *
 * <p>An Interest Group is a collaborative workspace organized under a Category. It aggregates the
 * IGs identity metadata (title, description, contact) together with references to the underlying
 * Alfresco nodes that back each of its services: the Library, the Information section, the
 * Newsgroup and the Events service. It also carries visibility/registration flags, the effective
 * permissions map for the current user and an optional logo URL.
 *
 * <p>This class is a plain serializable model exchanged over the JSON REST API; it holds no
 * business logic beyond value equality, hashing and string rendering.
 */
public class InterestGroup {

  /** Unique identifier (Alfresco node id) of the interest group. */
  private String id = null;

  /** Machine/short name of the interest group. */
  private String name = null;

  /** Localised (i18n) human-readable title of the interest group. */
  private I18nProperty title = new I18nProperty();

  /** Localised (i18n) description of the interest group. */
  private I18nProperty description = new I18nProperty();

  /** Localised (i18n) contact information for the interest group. */
  private I18nProperty contact = new I18nProperty();

  /** Node id of the Library service, or an empty string when not set. */
  private String libraryId;

  /** Node id of the Information service, or an empty string when not set. */
  private String informationId;

  /** Node id of the Newsgroup service, or an empty string when not set. */
  private String newsgroupId;

  /** Node id of the Events service, or an empty string when not set. */
  private String eventId;

  /** Whether the interest group is publicly visible. */
  private Boolean isPublic = null;

  /** Whether the current user is a registered member of the interest group. */
  private Boolean isRegistered = null;

  /** Whether users are allowed to apply for membership of the interest group. */
  private Boolean allowApply = null;

  /** Effective permissions for the current context, keyed by permission name. */
  private Map<String, String> permissions = new HashMap<>();

  /** URL of the interest group logo, or {@code null} when none is defined. */
  private String logoUrl = null;

  /**
   * Creates an empty interest group, initialising the service node id references
   * ({@code libraryId}, {@code informationId}, {@code newsgroupId} and {@code eventId}) to empty
   * strings so they are never {@code null}.
   */
  public InterestGroup() {
    this.informationId = "";
    this.libraryId = "";
    this.eventId = "";
    this.newsgroupId = "";
  }

  /**
   * Get id
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the interest group.
   *
   * @param id the interest group id to set
   */
  public void setId(String id) {
    this.id = id;
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
   * Sets the machine/short name of the interest group.
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
   * Sets the localised title of the interest group.
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
   * Sets the localised description of the interest group.
   *
   * @param description the description to set
   */
  public void setDescription(I18nProperty description) {
    this.description = description;
  }

  /**
   * Get libraryId
   *
   * @return libraryId
   */
  public String getLibraryId() {
    return libraryId;
  }

  /**
   * Sets the node id of the Library service.
   *
   * @param libraryId the library node id to set
   */
  public void setLibraryId(String libraryId) {
    this.libraryId = libraryId;
  }

  /**
   * Get informationId
   *
   * @return informationId
   */
  public String getInformationId() {
    return informationId;
  }

  /**
   * Sets the node id of the Information service.
   *
   * @param informationId the information node id to set
   */
  public void setInformationId(String informationId) {
    this.informationId = informationId;
  }

  /**
   * Get newsgroupId
   *
   * @return newsgroupId
   */
  public String getNewsgroupId() {
    return newsgroupId;
  }

  /**
   * Sets the node id of the Newsgroup service.
   *
   * @param newsgroupId the newsgroup node id to set
   */
  public void setNewsgroupId(String newsgroupId) {
    this.newsgroupId = newsgroupId;
  }

  /**
   * Get eventId
   *
   * @return eventId
   */
  public String getEventId() {
    return eventId;
  }

  /**
   * Sets the node id of the Events service.
   *
   * @param eventId the event node id to set
   */
  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  /**
   * Get isPublic
   *
   * @return isPublic
   */
  public Boolean getIsPublic() {
    return isPublic;
  }

  /**
   * Sets whether the interest group is publicly visible.
   *
   * @param isPublic {@code true} if the group is public, {@code false} otherwise
   */
  public void setIsPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Get isRegistered
   *
   * @return isRegistered
   */
  public Boolean getIsRegistered() {
    return isRegistered;
  }

  /**
   * Sets whether the current user is a registered member of the interest group.
   *
   * @param isRegistered {@code true} if the user is registered, {@code false} otherwise
   */
  public void setIsRegistered(Boolean isRegistered) {
    this.isRegistered = isRegistered;
  }

  /**
   * Get allowApply
   *
   * @return allowApply
   */
  public Boolean getAllowApply() {
    return allowApply;
  }

  /**
   * Sets whether users are allowed to apply for membership of the interest group.
   *
   * @param allowApply {@code true} if applications are allowed, {@code false} otherwise
   */
  public void setAllowApply(Boolean allowApply) {
    this.allowApply = allowApply;
  }

  /**
   * Compares this interest group with another object for value equality. Two interest groups are
   * equal when all of their fields are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code InterestGroup}, {@code false}
   *     otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InterestGroup interestGroup = (InterestGroup) o;
    return (
      Objects.equals(this.id, interestGroup.id) &&
      Objects.equals(this.name, interestGroup.name) &&
      Objects.equals(this.title, interestGroup.title) &&
      Objects.equals(this.description, interestGroup.description) &&
      Objects.equals(this.libraryId, interestGroup.libraryId) &&
      Objects.equals(this.informationId, interestGroup.informationId) &&
      Objects.equals(this.newsgroupId, interestGroup.newsgroupId) &&
      Objects.equals(this.eventId, interestGroup.eventId) &&
      Objects.equals(this.isPublic, interestGroup.isPublic) &&
      Objects.equals(this.isRegistered, interestGroup.isRegistered) &&
      Objects.equals(this.allowApply, interestGroup.allowApply) &&
      Objects.equals(this.logoUrl, interestGroup.logoUrl) &&
      Objects.equals(this.contact, interestGroup.contact)
    );
  }

  /**
   * Computes a hash code consistent with {@link #equals(java.lang.Object)}.
   *
   * @return the hash code derived from all fields
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      id,
      name,
      title,
      description,
      libraryId,
      informationId,
      newsgroupId,
      eventId,
      isPublic,
      isRegistered,
      allowApply,
      logoUrl,
      contact
    );
  }

  /**
   * Returns a human-readable, multi-line representation of this interest group, primarily intended
   * for debugging and logging.
   *
   * @return a string describing all fields of this interest group
   */
  @Override
  public String toString() {
    return (
      "class InterestGroup {\n" +
      "    id: " +
      toIndentedString(id) +
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
      "    libraryId: " +
      toIndentedString(libraryId) +
      "\n" +
      "    informationId: " +
      toIndentedString(informationId) +
      "\n" +
      "    newsgroupId: " +
      toIndentedString(newsgroupId) +
      "\n" +
      "    eventId: " +
      toIndentedString(eventId) +
      "\n" +
      "    isPublic: " +
      toIndentedString(isPublic) +
      "\n" +
      "    isRegistered: " +
      toIndentedString(isRegistered) +
      "\n" +
      "    allowApply: " +
      toIndentedString(allowApply) +
      "\n" +
      "    logoUrl: " +
      toIndentedString(logoUrl) +
      "\n" +
      "    contact: " +
      toIndentedString(contact) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to render
   * @return the indented string representation of {@code o}
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
   * Returns the URL of the interest group logo.
   *
   * @return the logo URL, or {@code null} if none is defined
   */
  public String getLogoUrl() {
    return logoUrl;
  }

  /**
   * Sets the URL of the interest group logo.
   *
   * @param logoUrl the logo URL to set
   */
  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  /**
   * @return the contact
   */
  public I18nProperty getContact() {
    return contact;
  }

  /**
   * @param contact the contact to set
   */
  public void setContact(I18nProperty contact) {
    this.contact = contact;
  }
}
