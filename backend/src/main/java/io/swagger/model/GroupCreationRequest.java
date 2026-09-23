package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.joda.time.DateTime;

/**
 * Data transfer object representing a request to create a new Interest Group.
 *
 * <p>A group creation request captures the details submitted by a requesting user (the proposed
 * name, title, description and justification) along with the workflow metadata needed to review
 * and approve it (the assigned reviewer, the agreement status and dates, and the target category).
 * It is exchanged as JSON between the CIRCABC REST API and the Angular frontend.
 */
public class GroupCreationRequest {

  /** Unique identifier of the group creation request. */
  private Integer id = null;

  /** User who submitted the request. */
  private User from = null;

  /** Proposed technical/short name for the group. */
  private String proposedName = null;

  /** Proposed localized display title for the group. */
  private I18nProperty proposedTitle = new I18nProperty();

  /** Proposed localized description for the group. */
  private I18nProperty proposedDescription = new I18nProperty();

  /** Free-text justification explaining why the group should be created. */
  private String justification = null;

  /** User assigned to review and decide on the request. */
  private User reviewer = null;

  /** Agreement/approval status of the request (defaults to {@code 0}). */
  private Integer agreement = 0;

  /** Date on which the request was submitted. */
  private DateTime requestDate = null;

  /** Reference to the category under which the new group would be created. */
  private String categoryRef = null;

  /** Users proposed as leaders/administrators of the new group. */
  private List<User> leaders = new ArrayList<>();

  /** Additional free-text argument recorded during the review workflow. */
  private String argument;

  /** Date on which the agreement/approval decision was made. */
  private DateTime agreementDate;

  /**
   * Gets the unique identifier of the request.
   *
   * @return the request id
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the request.
   *
   * @param id the request id
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Get from
   *
   * @return from
   */
  public User getFrom() {
    return from;
  }

  /**
   * Sets the user who submitted the request.
   *
   * @param from the requesting user
   */
  public void setFrom(User from) {
    this.from = from;
  }

  /**
   * Get proposedName
   *
   * @return proposedName
   */
  public String getProposedName() {
    return proposedName;
  }

  /**
   * Sets the proposed technical/short name for the group.
   *
   * @param proposedName the proposed name
   */
  public void setProposedName(String proposedName) {
    this.proposedName = proposedName;
  }

  /**
   * Get proposedTitle
   *
   * @return proposedTitle
   */
  public I18nProperty getProposedTitle() {
    return proposedTitle;
  }

  /**
   * Sets the proposed localized display title for the group.
   *
   * @param proposedTitle the proposed title
   */
  public void setProposedTitle(I18nProperty proposedTitle) {
    this.proposedTitle = proposedTitle;
  }

  /**
   * Get proposedDescription
   *
   * @return proposedDescription
   */
  public I18nProperty getProposedDescription() {
    return proposedDescription;
  }

  /**
   * Sets the proposed localized description for the group.
   *
   * @param proposedDescription the proposed description
   */
  public void setProposedDescription(I18nProperty proposedDescription) {
    this.proposedDescription = proposedDescription;
  }

  /**
   * Get justification
   *
   * @return justification
   */
  public String getJustification() {
    return justification;
  }

  /**
   * Sets the free-text justification for the request.
   *
   * @param justification the justification text
   */
  public void setJustification(String justification) {
    this.justification = justification;
  }

  /**
   * Get requestDate
   *
   * @return requestDate
   */
  public DateTime getRequestDate() {
    return requestDate;
  }

  /**
   * Sets the date on which the request was submitted.
   *
   * @param date the request date
   */
  public void setRequestDate(DateTime date) {
    this.requestDate = date;
  }

  /**
   * Get categoryRef
   *
   * @return categoryRef
   */
  public String getCategoryRef() {
    return categoryRef;
  }

  /**
   * Sets the reference to the category under which the group would be created.
   *
   * @param categoryRef the category reference
   */
  public void setCategoryRef(String categoryRef) {
    this.categoryRef = categoryRef;
  }

  /**
   * Adds a single user to the list of proposed leaders, initializing the list if needed.
   *
   * @param leadersItem the leader to add
   * @return this instance, to allow method chaining
   */
  public GroupCreationRequest addLeadersItem(User leadersItem) {
    if (this.leaders == null) {
      this.leaders = new ArrayList<>();
    }
    this.leaders.add(leadersItem);
    return this;
  }

  /**
   * Get leaders
   *
   * @return leaders
   */
  public List<User> getLeaders() {
    return leaders;
  }

  /**
   * Sets the users proposed as leaders of the new group.
   *
   * @param leaders the proposed leaders
   */
  public void setLeaders(List<User> leaders) {
    this.leaders = leaders;
  }

  /**
   * Gets the user assigned to review the request.
   *
   * @return the reviewer
   */
  public User getReviewer() {
    return reviewer;
  }

  /**
   * Sets the user assigned to review the request.
   *
   * @param reviewer the reviewer
   */
  public void setReviewer(User reviewer) {
    this.reviewer = reviewer;
  }

  /**
   * Gets the agreement/approval status of the request.
   *
   * @return the agreement status
   */
  public Integer getAgreement() {
    return agreement;
  }

  /**
   * Sets the agreement/approval status of the request.
   *
   * @param agreement the agreement status
   */
  public void setAgreement(Integer agreement) {
    this.agreement = agreement;
  }

  /**
   * Compares this request with another object for equality based on all fields.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code GroupCreationRequest} with equal field
   *     values, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupCreationRequest groupCreationRequest = (GroupCreationRequest) o;
    return (
      Objects.equals(this.id, groupCreationRequest.id) &&
      Objects.equals(this.from, groupCreationRequest.from) &&
      Objects.equals(this.proposedName, groupCreationRequest.proposedName) &&
      Objects.equals(this.proposedTitle, groupCreationRequest.proposedTitle) &&
      Objects.equals(
        this.proposedDescription,
        groupCreationRequest.proposedDescription
      ) &&
      Objects.equals(this.justification, groupCreationRequest.justification) &&
      Objects.equals(this.requestDate, groupCreationRequest.requestDate) &&
      Objects.equals(this.categoryRef, groupCreationRequest.categoryRef) &&
      Objects.equals(this.leaders, groupCreationRequest.leaders) &&
      Objects.equals(this.agreement, groupCreationRequest.agreement) &&
      Objects.equals(this.reviewer, groupCreationRequest.reviewer) &&
      Objects.equals(this.argument, groupCreationRequest.argument) &&
      Objects.equals(this.agreementDate, groupCreationRequest.agreementDate)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}, derived from all fields.
   *
   * @return the hash code for this request
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      id,
      from,
      proposedName,
      proposedTitle,
      proposedDescription,
      justification,
      requestDate,
      categoryRef,
      leaders,
      reviewer,
      agreement,
      argument,
      agreementDate
    );
  }

  /**
   * Returns a human-readable, multi-line string representation of this request.
   *
   * @return a string describing all field values
   */
  @Override
  public String toString() {
    return (
      "class GroupCreationRequest {\n" +
      "    id: " +
      toIndentedString(id) +
      "    from: " +
      toIndentedString(from) +
      "\n" +
      "    proposedName: " +
      toIndentedString(proposedName) +
      "\n" +
      "    proposedTitle: " +
      toIndentedString(proposedTitle) +
      "\n" +
      "    proposedDescription: " +
      toIndentedString(proposedDescription) +
      "\n" +
      "    justification: " +
      toIndentedString(justification) +
      "\n" +
      "    requestDate: " +
      toIndentedString(requestDate) +
      "\n" +
      "    categoryRef: " +
      toIndentedString(categoryRef) +
      "\n" +
      "    leaders: " +
      toIndentedString(leaders) +
      "\n" +
      "    agreement: " +
      toIndentedString(agreement) +
      "\n" +
      "    reviewer: " +
      toIndentedString(reviewer) +
      "\n" +
      "    argument: " +
      toIndentedString(argument) +
      "\n" +
      "    agreementDate: " +
      toIndentedString(agreementDate) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Gets the additional free-text argument recorded during review.
   *
   * @return the argument text
   */
  public String getArgument() {
    return argument;
  }

  /**
   * Sets the additional free-text argument recorded during review.
   *
   * @param argument the argument text
   */
  public void setArgument(String argument) {
    this.argument = argument;
  }

  /**
   * Gets the date on which the agreement/approval decision was made.
   *
   * @return the agreement date
   */
  public DateTime getAgreementDate() {
    return agreementDate;
  }

  /**
   * Sets the date on which the agreement/approval decision was made.
   *
   * @param agreementDate the agreement date
   */
  public void setAgreementDate(DateTime agreementDate) {
    this.agreementDate = agreementDate;
  }
}
