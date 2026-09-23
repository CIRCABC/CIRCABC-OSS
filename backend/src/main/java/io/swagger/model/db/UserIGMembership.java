package io.swagger.model.db;

/**
 * Data model describing a single user's membership within an Interest Group (IG).
 *
 * <p>This is a plain data-transfer object (POJO) that links a user's profile to the IG (and its
 * parent Category) it belongs to, together with the Alfresco authority group backing that
 * membership. It carries both the Alfresco {@code NodeRef} identifiers of the involved nodes and
 * the profile metadata used when resolving or reporting a user's memberships.
 */
public class UserIGMembership {

  /** Alfresco {@code NodeRef} of the Category that contains the Interest Group. */
  String catNodeRef;
  /** Alfresco {@code NodeRef} of the Interest Group the membership applies to. */
  String igNodeRef;
  /** Human-readable name of the profile (role) granted to the user within the IG. */
  String profileName;
  /** Alfresco {@code NodeRef} identifier of the profile node the membership references. */
  String profileNodeRefId;
  /** Name of the Alfresco authority group that backs this profile membership. */
  String alfrescoGroup;
  /** Numeric database identifier of the profile associated with the membership. */
  Long profileId;

  /**
   * Returns the Category {@code NodeRef} that contains the Interest Group.
   *
   * @return the Category node reference
   */
  public String getCatNodeRef() {
    return catNodeRef;
  }

  /**
   * Sets the Category {@code NodeRef} that contains the Interest Group.
   *
   * @param catNodeRef the Category node reference to set
   */
  public void setCatNodeRef(String catNodeRef) {
    this.catNodeRef = catNodeRef;
  }

  /**
   * Returns the Interest Group {@code NodeRef} the membership applies to.
   *
   * @return the Interest Group node reference
   */
  public String getIgNodeRef() {
    return igNodeRef;
  }

  /**
   * Sets the Interest Group {@code NodeRef} the membership applies to.
   *
   * @param igNodeRef the Interest Group node reference to set
   */
  public void setIgNodeRef(String igNodeRef) {
    this.igNodeRef = igNodeRef;
  }

  /**
   * Returns the name of the profile (role) granted to the user within the IG.
   *
   * @return the profile name
   */
  public String getProfileName() {
    return profileName;
  }

  /**
   * Sets the name of the profile (role) granted to the user within the IG.
   *
   * @param profileName the profile name to set
   */
  public void setProfileName(String profileName) {
    this.profileName = profileName;
  }

  /**
   * Returns the name of the Alfresco authority group backing this membership.
   *
   * @return the Alfresco group name
   */
  public String getAlfrescoGroup() {
    return alfrescoGroup;
  }

  /**
   * Sets the name of the Alfresco authority group backing this membership.
   *
   * @param alfrescoGroup the Alfresco group name to set
   */
  public void setAlfrescoGroup(String alfrescoGroup) {
    this.alfrescoGroup = alfrescoGroup;
  }

  /**
   * Returns the numeric database identifier of the associated profile.
   *
   * @return the profile identifier
   */
  public Long getProfileId() {
    return profileId;
  }

  /**
   * Sets the numeric database identifier of the associated profile.
   *
   * @param profileId the profile identifier to set
   */
  public void setProfileId(Long profileId) {
    this.profileId = profileId;
  }

  /**
   * Returns the Alfresco {@code NodeRef} identifier of the referenced profile node.
   *
   * @return the profile node reference identifier
   */
  public String getProfileNodeRefId() {
    return profileNodeRefId;
  }

  /**
   * Sets the Alfresco {@code NodeRef} identifier of the referenced profile node.
   *
   * @param profileNodeRefId the profile node reference identifier to set
   */
  public void setProfileNodeRefId(String profileNodeRefId) {
    this.profileNodeRefId = profileNodeRefId;
  }
}
