package io.swagger.model.db;

/**
 * Data transfer object representing a single exported profile entry.
 *
 * <p>An {@code ExportedProfileItem} pairs a CIRCABC profile with its underlying Alfresco group and
 * node references. It is used when profiles are serialized/exported (for example, when transferring
 * or backing up an Interest Group's membership profiles) so that the profile can later be identified
 * and re-associated with the correct Alfresco group and repository nodes.
 */
public class ExportedProfileItem {

  /** The Alfresco authority (group) name, including its prefix (e.g. {@code GROUP_...}). */
  private String prefixedAlfrescoGroup;

  /** The technical name of the profile. */
  private String profileName;

  /** The human-readable display name of the profile. */
  private String name;

  /** The Alfresco node reference of the profile definition node. */
  private String profileRef;

  /** The Alfresco node reference associated with this profile item. */
  private String nodeRef;

  /**
   * Returns the prefixed Alfresco group (authority) name.
   *
   * @return the prefixed Alfresco group name, including its prefix
   */
  public String getPrefixedAlfrescoGroup() {
    return prefixedAlfrescoGroup;
  }

  /**
   * Sets the prefixed Alfresco group (authority) name.
   *
   * @param prefixedAlfrescoGroup the prefixed Alfresco group name to set
   */
  public void setPrefixedAlfrescoGroup(String prefixedAlfrescoGroup) {
    this.prefixedAlfrescoGroup = prefixedAlfrescoGroup;
  }

  /**
   * Returns the technical name of the profile.
   *
   * @return the profile name
   */
  public String getProfileName() {
    return profileName;
  }

  /**
   * Sets the technical name of the profile.
   *
   * @param profileName the profile name to set
   */
  public void setProfileName(String profileName) {
    this.profileName = profileName;
  }

  /**
   * Returns the human-readable display name of the profile.
   *
   * @return the display name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the human-readable display name of the profile.
   *
   * @param name the display name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the Alfresco node reference associated with this profile item.
   *
   * @return the node reference
   */
  public String getNodeRef() {
    return nodeRef;
  }

  /**
   * Sets the Alfresco node reference associated with this profile item.
   *
   * @param nodeRef the node reference to set
   */
  public void setNodeRef(String nodeRef) {
    this.nodeRef = nodeRef;
  }

  /**
   * Returns the Alfresco node reference of the profile definition node.
   *
   * @return the profile node reference
   */
  public String getProfileRef() {
    return profileRef;
  }

  /**
   * Sets the Alfresco node reference of the profile definition node.
   *
   * @param profileRef the profile node reference to set
   */
  public void setProfileRef(String profileRef) {
    this.profileRef = profileRef;
  }
}
