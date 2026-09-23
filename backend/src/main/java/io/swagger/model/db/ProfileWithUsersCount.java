package io.swagger.model.db;

/**
 * A {@link Profile} enriched with the number of users assigned to it.
 *
 * <p>This model extends the base {@code Profile} with an additional aggregate
 * count, allowing callers to retrieve a profile together with how many users
 * currently hold that profile (for example, in a membership listing that shows
 * per-profile user totals).
 */
public class ProfileWithUsersCount extends Profile {

  /** The number of users associated with this profile. */
  private int numberOfUsers;

  /**
   * Returns the number of users associated with this profile.
   *
   * @return the count of users holding this profile
   */
  public int getNumberOfUsers() {
    return numberOfUsers;
  }

  /**
   * Sets the number of users associated with this profile.
   *
   * @param numberOfUsers the count of users holding this profile
   */
  public void setNumberOfUsers(int numberOfUsers) {
    this.numberOfUsers = numberOfUsers;
  }
}
