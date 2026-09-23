package io.swagger.model.db;

/**
 * Data model (DTO) representing a CIRCABC membership <em>profile</em> within an
 * Interest Group.
 *
 * <p>A profile defines a named set of permissions that can be assigned to
 * members of an Interest Group (IG). Each profile is backed by an underlying
 * Alfresco group and carries the per-service permission levels for the IG's
 * services (Directory, Information, Library, Newsgroup and Events), together
 * with flags controlling its visibility and its import/export behaviour.
 *
 * <p>This is a plain data-holder used to map profile records between the
 * persistence/service layer and the REST API responses; it contains no
 * business logic beyond simple accessors and a string-manipulation helper.
 */
public class Profile {

  /** Identifier of the Interest Group this profile belongs to. */
  private long interestGroupID;
  /** Unique identifier of the profile. */
  private long id;
  /** Name of the underlying Alfresco group backing this profile. */
  private String alfrescoGroup;
  /** Technical name of the profile. */
  private String name;
  /** Human-readable display title of the profile. */
  private String title;
  /** Permission level granted for the Directory service. */
  private String directoryPermission;
  /** Permission level granted for the Information service. */
  private String informationPermission;
  /** Permission level granted for the Library service. */
  private String libraryPermission;
  /** Permission level granted for the Newsgroup service. */
  private String newsgroupPermission;
  /** Permission level granted for the Events service. */
  private String eventPermission;
  /** Whether this profile is flagged as exported. */
  private boolean isExported;
  /** Whether this profile is flagged as imported. */
  private boolean isImported;
  /** Whether this profile is visible to users. */
  private boolean isVisible;
  /** Alfresco {@code NodeRef} string identifying this profile node. */
  private String nodeRef;
  /** Alfresco {@code NodeRef} string of the Interest Group this profile originates from. */
  private String igFromNodeRef;

  /**
   * Creates an empty {@code Profile} with all fields left at their default
   * values.
   */
  public Profile() {
    super();
  }

  /**
   * Creates a fully populated {@code Profile}.
   *
   * @param interestGroupID identifier of the owning Interest Group
   * @param id unique identifier of the profile
   * @param alfrescoGroup name of the backing Alfresco group
   * @param name technical name of the profile
   * @param title human-readable display title
   * @param directoryPermission permission level for the Directory service
   * @param informationPermission permission level for the Information service
   * @param libraryPermission permission level for the Library service
   * @param newsgroupPermission permission level for the Newsgroup service
   * @param eventPermission permission level for the Events service
   * @param isExported whether the profile is flagged as exported
   * @param isImported whether the profile is flagged as imported
   * @param isVisible whether the profile is visible to users
   * @param nodeRef Alfresco {@code NodeRef} of this profile node
   * @param igFromNodeRef Alfresco {@code NodeRef} of the originating Interest Group
   */
  @SuppressWarnings("java:S107") // DTO all-args constructor
  public Profile(
    long interestGroupID,
    long id,
    String alfrescoGroup,
    String name,
    String title,
    String directoryPermission,
    String informationPermission,
    String libraryPermission,
    String newsgroupPermission,
    String eventPermission,
    boolean isExported,
    boolean isImported,
    boolean isVisible,
    String nodeRef,
    String igFromNodeRef
  ) {
    super();
    this.interestGroupID = interestGroupID;
    this.id = id;
    this.alfrescoGroup = alfrescoGroup;
    this.name = name;
    this.title = title;
    this.directoryPermission = directoryPermission;
    this.informationPermission = informationPermission;
    this.libraryPermission = libraryPermission;
    this.newsgroupPermission = newsgroupPermission;
    this.eventPermission = eventPermission;
    this.isExported = isExported;
    this.isImported = isImported;
    this.isVisible = isVisible;
    this.nodeRef = nodeRef;
    this.igFromNodeRef = igFromNodeRef;
  }

  /**
   * Replaces the <em>last</em> occurrence of a substring within the given
   * string.
   *
   * @param string the input string to process; may be {@code null}
   * @param from the substring whose last occurrence should be replaced
   * @param to the replacement value
   * @return {@code null} if {@code string} is {@code null}; the original
   *     string unchanged if {@code from} is not found; otherwise a new string
   *     with the last occurrence of {@code from} replaced by {@code to}
   */
  public static String replaceLast(String string, String from, String to) {
    if (string == null) {
      return null;
    }
    int lastIndex = string.lastIndexOf(from);
    if (lastIndex < 0) {
      return string;
    }
    String tail = string.substring(lastIndex).replaceFirst(from, to);
    return string.substring(0, lastIndex) + tail;
  }

  /**
   * Returns a string representation of this profile listing all of its fields.
   *
   * @return a human-readable description of this profile's state
   */
  @Override
  public String toString() {
    return (
      "Profile [interestGroupID=" +
      interestGroupID +
      ", id=" +
      id +
      ", alfrescoGroup=" +
      alfrescoGroup +
      ", name=" +
      name +
      ", title=" +
      title +
      ", directoryPermission=" +
      directoryPermission +
      ", informationPermission=" +
      informationPermission +
      ", libraryPermission=" +
      libraryPermission +
      ", newsgroupPermission=" +
      newsgroupPermission +
      ", eventPermission=" +
      eventPermission +
      ", isExported=" +
      isExported +
      ", isImported=" +
      isImported +
      ", isVisible=" +
      isVisible +
      ", nodeRef=" +
      nodeRef +
      ", igFromNodeRef=" +
      igFromNodeRef +
      "]"
    );
  }

  /**
   * Returns the unique identifier of this profile.
   *
   * @return the profile id
   */
  public long getId() {
    return id;
  }

  /**
   * Sets the unique identifier of this profile.
   *
   * @param id the profile id to set
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Returns the name of the backing Alfresco group.
   *
   * @return the Alfresco group name
   */
  public String getAlfrescoGroup() {
    return alfrescoGroup;
  }

  /**
   * Sets the name of the backing Alfresco group.
   *
   * @param alfrescoGroup the Alfresco group name to set
   */
  public void setAlfrescoGroup(String alfrescoGroup) {
    this.alfrescoGroup = alfrescoGroup;
  }

  /**
   * Returns the technical name of this profile.
   *
   * @return the profile name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the technical name of this profile.
   *
   * @param name the profile name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the human-readable display title of this profile.
   *
   * @return the profile title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the human-readable display title of this profile.
   *
   * @param title the profile title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the permission level for the Directory service.
   *
   * @return the Directory permission level
   */
  public String getDirectoryPermission() {
    return directoryPermission;
  }

  /**
   * Sets the permission level for the Directory service.
   *
   * @param directoryPermission the Directory permission level to set
   */
  public void setDirectoryPermission(String directoryPermission) {
    this.directoryPermission = directoryPermission;
  }

  /**
   * Returns the permission level for the Information service.
   *
   * @return the Information permission level
   */
  public String getInformationPermission() {
    return informationPermission;
  }

  /**
   * Sets the permission level for the Information service.
   *
   * @param informationPermission the Information permission level to set
   */
  public void setInformationPermission(String informationPermission) {
    this.informationPermission = informationPermission;
  }

  /**
   * Returns the permission level for the Library service.
   *
   * @return the Library permission level
   */
  public String getLibraryPermission() {
    return libraryPermission;
  }

  /**
   * Sets the permission level for the Library service.
   *
   * @param libraryPermission the Library permission level to set
   */
  public void setLibraryPermission(String libraryPermission) {
    this.libraryPermission = libraryPermission;
  }

  /**
   * Returns the permission level for the Newsgroup service.
   *
   * @return the Newsgroup permission level
   */
  public String getNewsgroupPermission() {
    return newsgroupPermission;
  }

  /**
   * Sets the permission level for the Newsgroup service.
   *
   * @param newsgroupPermission the Newsgroup permission level to set
   */
  public void setNewsgroupPermission(String newsgroupPermission) {
    this.newsgroupPermission = newsgroupPermission;
  }

  /**
   * Returns the permission level for the Events service.
   *
   * @return the Events permission level
   */
  public String getEventPermission() {
    return eventPermission;
  }

  /**
   * Sets the permission level for the Events service.
   *
   * @param eventsPermission the Events permission level to set
   */
  public void setEventPermission(String eventsPermission) {
    this.eventPermission = eventsPermission;
  }

  /**
   * Indicates whether this profile is flagged as exported.
   *
   * @return {@code true} if the profile is exported, {@code false} otherwise
   */
  public boolean isExported() {
    return isExported;
  }

  /**
   * Sets whether this profile is flagged as exported.
   *
   * @param isExported {@code true} to mark the profile as exported
   */
  public void setExported(boolean isExported) {
    this.isExported = isExported;
  }

  /**
   * Indicates whether this profile is flagged as imported.
   *
   * @return {@code true} if the profile is imported, {@code false} otherwise
   */
  public boolean isImported() {
    return isImported;
  }

  /**
   * Sets whether this profile is flagged as imported.
   *
   * @param isImported {@code true} to mark the profile as imported
   */
  public void setImported(boolean isImported) {
    this.isImported = isImported;
  }

  /**
   * Returns the Alfresco {@code NodeRef} string identifying this profile node.
   *
   * @return the profile node reference
   */
  public String getNodeRef() {
    return nodeRef;
  }

  /**
   * Sets the Alfresco {@code NodeRef} string identifying this profile node.
   *
   * @param nodeRef the profile node reference to set
   */
  public void setNodeRef(String nodeRef) {
    this.nodeRef = nodeRef;
  }

  /**
   * Returns the identifier of the owning Interest Group.
   *
   * @return the Interest Group id
   */
  public long getInterestGroupID() {
    return interestGroupID;
  }

  /**
   * Sets the identifier of the owning Interest Group.
   *
   * @param interestGroupID the Interest Group id to set
   */
  public void setInterestGroupID(long interestGroupID) {
    this.interestGroupID = interestGroupID;
  }

  /**
   * Indicates whether this profile is visible to users.
   *
   * @return {@code true} if the profile is visible, {@code false} otherwise
   */
  public boolean isVisible() {
    return isVisible;
  }

  /**
   * Sets whether this profile is visible to users.
   *
   * @param isVisible {@code true} to make the profile visible
   */
  public void setVisible(boolean isVisible) {
    this.isVisible = isVisible;
  }

  /**
   * Returns the Alfresco {@code NodeRef} of the Interest Group this profile
   * originates from.
   *
   * @return the originating Interest Group node reference
   */
  public String getIgFromNodeRef() {
    return igFromNodeRef;
  }

  /**
   * Sets the Alfresco {@code NodeRef} of the Interest Group this profile
   * originates from.
   *
   * @param igFromNodeRef the originating Interest Group node reference to set
   */
  public void setIgFromNodeRef(String igFromNodeRef) {
    this.igFromNodeRef = igFromNodeRef;
  }
}
