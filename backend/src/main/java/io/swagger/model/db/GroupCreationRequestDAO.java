/**
 *
 */
package io.swagger.model.db;

import java.util.Date;

/**
 * Data access object (DAO) representing a request to create a new Interest Group.
 *
 * <p>Instances of this class map to a persisted group-creation request record and act as a plain
 * data carrier between the persistence layer and the service/REST layers. A request captures who
 * asked for the group, the details of the proposed group (name, category, leaders, reviewer,
 * justification) and the outcome of the review workflow (agreement status, supporting argument and
 * the date the decision was taken).
 *
 * <p>This class holds no business logic; it only exposes standard getters and setters for its
 * fields.
 *
 * @author beaurpi
 */
public class GroupCreationRequestDAO {

  /** Unique identifier of the request (primary key); {@code null} until persisted. */
  private Integer id = null;

  /** Username of the user who submitted the group-creation request. */
  private String fromUsername;

  /** Name proposed for the Interest Group to be created. */
  private String proposedName;

  /** Date on which the request was submitted. */
  private Date requestDate;

  /** Free-text justification explaining why the group should be created. */
  private String justification;

  /** Reference of the category under which the proposed group would be created. */
  private String categoryReference;

  /** Proposed leader(s) of the group. */
  private String leaders;

  /** Username of the reviewer responsible for handling the request. */
  private String reviewer;

  /**
   * Agreement/review status of the request. Defaults to {@code 0} (e.g. pending / not yet decided).
   */
  private Integer agreement = 0;

  /** Free-text argument supporting the review decision. */
  private String argument;

  /** Date on which the agreement/review decision was taken. */
  private Date agreementDate;

  /**
   * Returns the unique identifier of the request.
   *
   * @return the request id, or {@code null} if not yet persisted
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the request.
   *
   * @param id the request id to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Returns the username of the user who submitted the request.
   *
   * @return the requesting username
   */
  public String getFromUsername() {
    return fromUsername;
  }

  /**
   * Sets the username of the user who submitted the request.
   *
   * @param fromUsername the requesting username to set
   */
  public void setFromUsername(String fromUsername) {
    this.fromUsername = fromUsername;
  }

  /**
   * Returns the name proposed for the new group.
   *
   * @return the proposed group name
   */
  public String getProposedName() {
    return proposedName;
  }

  /**
   * Sets the name proposed for the new group.
   *
   * @param proposedName the proposed group name to set
   */
  public void setProposedName(String proposedName) {
    this.proposedName = proposedName;
  }

  /**
   * Returns the date on which the request was submitted.
   *
   * @return the request date
   */
  public Date getRequestDate() {
    return requestDate;
  }

  /**
   * Sets the date on which the request was submitted.
   *
   * @param requestDate the request date to set
   */
  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  /**
   * Returns the justification for the group-creation request.
   *
   * @return the justification text
   */
  public String getJustification() {
    return justification;
  }

  /**
   * Sets the justification for the group-creation request.
   *
   * @param justification the justification text to set
   */
  public void setJustification(String justification) {
    this.justification = justification;
  }

  /**
   * Returns the reference of the category under which the group would be created.
   *
   * @return the category reference
   */
  public String getCategoryReference() {
    return categoryReference;
  }

  /**
   * Sets the reference of the category under which the group would be created.
   *
   * @param categoryReference the category reference to set
   */
  public void setCategoryReference(String categoryReference) {
    this.categoryReference = categoryReference;
  }

  /**
   * Returns the proposed leader(s) of the group.
   *
   * @return the proposed leaders
   */
  public String getLeaders() {
    return leaders;
  }

  /**
   * Sets the proposed leader(s) of the group.
   *
   * @param leaders the proposed leaders to set
   */
  public void setLeaders(String leaders) {
    this.leaders = leaders;
  }

  /**
   * Returns the username of the reviewer handling the request.
   *
   * @return the reviewer username
   */
  public String getReviewer() {
    return reviewer;
  }

  /**
   * Sets the username of the reviewer handling the request.
   *
   * @param reviewer the reviewer username to set
   */
  public void setReviewer(String reviewer) {
    this.reviewer = reviewer;
  }

  /**
   * Returns the agreement/review status of the request.
   *
   * @return the agreement status
   */
  public Integer getAgreement() {
    return agreement;
  }

  /**
   * Sets the agreement/review status of the request.
   *
   * @param agreement the agreement status to set
   */
  public void setAgreement(Integer agreement) {
    this.agreement = agreement;
  }

  /**
   * Returns the argument supporting the review decision.
   *
   * @return the supporting argument
   */
  public String getArgument() {
    return argument;
  }

  /**
   * Sets the argument supporting the review decision.
   *
   * @param argument the supporting argument to set
   */
  public void setArgument(String argument) {
    this.argument = argument;
  }

  /**
   * Returns the date on which the agreement/review decision was taken.
   *
   * @return the agreement date
   */
  public Date getAgreementDate() {
    return agreementDate;
  }

  /**
   * Sets the date on which the agreement/review decision was taken.
   *
   * @param agreementDate the agreement date to set
   */
  public void setAgreementDate(Date agreementDate) {
    this.agreementDate = agreementDate;
  }
}
