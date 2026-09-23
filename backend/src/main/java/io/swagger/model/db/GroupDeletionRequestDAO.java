/**
 *
 */
package io.swagger.model.db;

import java.util.Date;

/**
 * Data access object (persistence model) representing a request to delete an Interest Group.
 *
 * <p>Instances of this class map to a row in the database table that tracks group deletion
 * requests. Each request records who asked for the deletion, the group and category involved, the
 * justification for the request, and the review outcome (agreement state, reviewer and the date the
 * decision was taken). It is a plain mutable bean exposing standard getters and setters; it holds no
 * business logic.
 *
 * @author Eduardo
 */
public class GroupDeletionRequestDAO {

  /** Database primary key of the deletion request; {@code null} for a request not yet persisted. */
  private Integer id = null;

  /** Username of the user who submitted the deletion request. */
  private String from;

  /** Date on which the deletion request was submitted. */
  private Date requestDate;

  /** Reference (node reference) of the category the target group belongs to. */
  private String categoryRef;

  /**
   * Review outcome flag for the request. Defaults to {@code 0} (pending/no decision); other values
   * encode the reviewer's decision (e.g. approved or rejected).
   */
  private Integer agreement = 0;

  /** Username of the reviewer responsible for approving or rejecting the request. */
  private String reviewer;

  /** Free-text justification supplied by the requester explaining why the group should be deleted. */
  private String justification;

  /** Date on which the reviewer's agreement decision was recorded. */
  private Date agreementDate;

  /** Identifier of the Interest Group targeted by the deletion request. */
  private String groupId;

  /** Display title of the target group at the time the request was made. */
  private String title;

  /** Internal name of the target group at the time the request was made. */
  private String name;

  /** Leaders of the target group at the time the request was made. */
  private String leaders;

  /** Description of the target group at the time the request was made. */
  private String description;

  /**
   * Returns the database identifier of this deletion request.
   *
   * @return the primary key, or {@code null} if the request has not been persisted
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the database identifier of this deletion request.
   *
   * @param id the primary key to assign
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Returns the username of the user who submitted the request.
   *
   * @return the requester's username
   */
  public String getFromUsername() {
    return from;
  }

  /**
   * Sets the username of the user who submitted the request.
   *
   * @param fromUsername the requester's username
   */
  public void setFromUsername(String fromUsername) {
    this.from = fromUsername;
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
   * @param requestDate the request date
   */
  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  /**
   * Returns the justification provided for the deletion request.
   *
   * @return the free-text justification
   */
  public String getJustification() {
    return justification;
  }

  /**
   * Sets the justification for the deletion request.
   *
   * @param justification the free-text justification
   */
  public void setJustification(String justification) {
    this.justification = justification;
  }

  /**
   * Returns the reference of the category the target group belongs to.
   *
   * @return the category reference
   */
  public String getCategoryRef() {
    return categoryRef;
  }

  /**
   * Sets the reference of the category the target group belongs to.
   *
   * @param categoryRef the category reference
   */
  public void setCategoryRef(String categoryRef) {
    this.categoryRef = categoryRef;
  }

  /**
   * Returns the username of the reviewer handling the request.
   *
   * @return the reviewer's username
   */
  public String getReviewer() {
    return reviewer;
  }

  /**
   * Sets the username of the reviewer handling the request.
   *
   * @param reviewer the reviewer's username
   */
  public void setReviewer(String reviewer) {
    this.reviewer = reviewer;
  }

  /**
   * Returns the agreement (review outcome) flag for the request.
   *
   * @return the agreement flag; {@code 0} indicates a pending decision
   */
  public Integer getAgreement() {
    return agreement;
  }

  /**
   * Sets the agreement (review outcome) flag for the request.
   *
   * @param agreement the agreement flag to assign
   */
  public void setAgreement(Integer agreement) {
    this.agreement = agreement;
  }

  /**
   * Returns the date on which the agreement decision was recorded.
   *
   * @return the agreement date
   */
  public Date getAgreementDate() {
    return agreementDate;
  }

  /**
   * Sets the date on which the agreement decision was recorded.
   *
   * @param agreementDate the agreement date
   */
  public void setAgreementDate(Date agreementDate) {
    this.agreementDate = agreementDate;
  }

  /**
   * Sets the identifier of the Interest Group targeted by the request.
   *
   * @param groupId the target group identifier
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Returns the identifier of the Interest Group targeted by the request.
   *
   * @return the target group identifier
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Returns the display title of the target group.
   *
   * @return the group title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the display title of the target group.
   *
   * @param title the group title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the internal name of the target group.
   *
   * @return the group name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the internal name of the target group.
   *
   * @param name the group name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the leaders of the target group.
   *
   * @return the group leaders
   */
  public String getLeaders() {
    return leaders;
  }

  /**
   * Sets the leaders of the target group.
   *
   * @param leaders the group leaders
   */
  public void setLeaders(String leaders) {
    this.leaders = leaders;
  }

  /**
   * Returns the description of the target group.
   *
   * @return the group description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of the target group.
   *
   * @param description the group description
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
