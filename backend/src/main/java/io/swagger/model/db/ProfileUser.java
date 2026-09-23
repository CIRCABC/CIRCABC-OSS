package io.swagger.model.db;

/**
 * Database-level model associating a user with an Alfresco group for a given profile.
 *
 * <p>This is a plain data holder (POJO) used to represent the link between a user,
 * identified by their numeric user ID, and the Alfresco group they belong to. It is
 * typically populated from persistence-layer queries related to profile and membership
 * management.
 */
public class ProfileUser {

  /** Numeric identifier of the user. */
  private long userID;

  /** Name of the Alfresco group the user is associated with. */
  private String alfrescoGroup;

  /** Creates an empty {@code ProfileUser} with default field values. */
  public ProfileUser() {}

  /**
   * Creates a {@code ProfileUser} with the given user identifier and Alfresco group.
   *
   * @param userID the numeric identifier of the user
   * @param alfrescoGroup the name of the Alfresco group the user is associated with
   */
  public ProfileUser(long userID, String alfrescoGroup) {
    this.userID = userID;
    this.alfrescoGroup = alfrescoGroup;
  }

  /**
   * Returns the numeric identifier of the user.
   *
   * @return the user ID
   */
  public long getUserID() {
    return userID;
  }

  /**
   * Sets the numeric identifier of the user.
   *
   * @param userID the user ID to set
   */
  public void setUserID(long userID) {
    this.userID = userID;
  }

  /**
   * Returns the name of the Alfresco group the user is associated with.
   *
   * @return the Alfresco group name
   */
  public String getAlfrescoGroup() {
    return alfrescoGroup;
  }

  /**
   * Sets the name of the Alfresco group the user is associated with.
   *
   * @param alfrescoGroup the Alfresco group name to set
   */
  public void setAlfrescoGroup(String alfrescoGroup) {
    this.alfrescoGroup = alfrescoGroup;
  }

  /**
   * Returns a string representation of this {@code ProfileUser}, including the user ID
   * and the associated Alfresco group.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (
      "ProfileUser [userID=" + userID + ", alfrescoGroup=" + alfrescoGroup + "]"
    );
  }
}
