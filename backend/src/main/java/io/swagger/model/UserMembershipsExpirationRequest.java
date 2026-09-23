/**
 *
 */
package io.swagger.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Request payload describing an expiration to be applied to a user's Interest Group memberships.
 *
 * <p>This is a plain data-transfer object (DTO) used by the REST layer to carry the information
 * needed to set or update the expiration of a given user's memberships: the target user, the
 * expiration date to apply, and the specific Interest Group profiles (memberships) affected.
 *
 * @author beaurpi
 */
public class UserMembershipsExpirationRequest {

  /** Identifier of the user whose memberships are being expired. */
  private String userId;

  /** Date on which the targeted memberships should expire. */
  private Date expirationDate;

  /** Interest Group profiles (memberships) to which the expiration applies. */
  private List<InterestGroupProfile> memberships = new ArrayList<>();

  /**
   * Returns the identifier of the user whose memberships are being expired.
   *
   * @return the user identifier
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the identifier of the user whose memberships are being expired.
   *
   * @param userId the user identifier
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Returns the date on which the targeted memberships should expire.
   *
   * @return the expiration date
   */
  public Date getExpirationDate() {
    return expirationDate;
  }

  /**
   * Sets the date on which the targeted memberships should expire.
   *
   * @param expirationDate the expiration date
   */
  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  /**
   * Returns the Interest Group profiles (memberships) to which the expiration applies.
   *
   * @return the list of affected memberships
   */
  public List<InterestGroupProfile> getMemberships() {
    return memberships;
  }

  /**
   * Sets the Interest Group profiles (memberships) to which the expiration applies.
   *
   * @param memberships the list of affected memberships
   */
  public void setMemberships(List<InterestGroupProfile> memberships) {
    this.memberships = memberships;
  }
}
