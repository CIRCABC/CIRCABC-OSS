package io.swagger.model.db;

/**
 * Data access object representing a single email address belonging to a
 * distribution list.
 *
 * <p>This is a plain data holder (POJO) that maps a distribution email row to a
 * Java object, pairing a numeric identifier with the associated email address.
 */
public class DistributionEmailDAO {

  /** Unique identifier of the distribution email record. */
  private Integer id = null;

  /** The email address of the distribution list recipient. */
  private String emailAddress = null;

  /**
   * @return the id
   */
  public Integer getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * @return the email
   */
  public String getEmailAddress() {
    return emailAddress;
  }

  /**
   * @param emailAddress the email to set
   */
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }
}
