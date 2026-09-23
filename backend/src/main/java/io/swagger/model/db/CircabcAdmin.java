package io.swagger.model.db;

/**
 * Simple data model representing the CIRCABC administrator account.
 *
 * <p>This holder pairs the numeric identifier of the administrator user with a
 * display name. Instances are created for the fixed {@code "CircaBCAdmin"}
 * account, which is used across the persistence layer to reference the platform
 * administrator.
 *
 * <p>Created by filipsl on 12/07/2017.
 */
public class CircabcAdmin {

  /** Numeric identifier of the administrator user. */
  long userID;

  /** Display name of the administrator; defaults to {@code "CircaBCAdmin"}. */
  String name;

  /**
   * Creates an administrator model for the given user identifier and sets the
   * name to the fixed value {@code "CircaBCAdmin"}.
   *
   * @param userID the numeric identifier of the administrator user
   */
  public CircabcAdmin(long userID) {
    super();
    this.userID = userID;
    this.name = "CircaBCAdmin";
  }

  /**
   * Returns the numeric identifier of the administrator user.
   *
   * @return the administrator user identifier
   */
  public long getUserID() {
    return userID;
  }

  /**
   * Sets the numeric identifier of the administrator user.
   *
   * @param userID the administrator user identifier to set
   */
  public void setUserID(long userID) {
    this.userID = userID;
  }

  /**
   * Returns the display name of the administrator.
   *
   * @return the administrator name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the display name of the administrator.
   *
   * @param name the administrator name to set
   */
  public void setName(String name) {
    this.name = name;
  }
}
