/**
 *
 */
package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;

/**
 * Domain model (DTO) representing a request to delete an Interest Group.
 *
 * <p>A group deletion request captures who initiated the deletion, the target group and its
 * category, the current review/agreement state, and supporting metadata used throughout the
 * approval workflow. Instances are typically serialized to/deserialized from JSON when exchanged
 * with the CIRCABC REST API and its Angular frontend.
 *
 * @author eduardo
 */
public class GroupDeletionRequest {

  /** Unique identifier of the deletion request. */
  private Long id = null;

  /** User who submitted the deletion request. */
  private User from = null;

  /** Date and time at which the request was created. */
  private DateTime requestDate = null;

  /** Reference to the Alfresco node of the category the target group belongs to. */
  private String categoryRef = null;

  /** Current agreement/review status of the request (e.g. pending, approved, rejected). */
  private Integer agreement = 0;

  /** User responsible for reviewing and deciding on the request. */
  private User reviewer = null;

  /** Justification provided by the requester explaining why the group should be deleted. */
  private String justification = null;

  /** Date and time at which the agreement (review decision) was recorded. */
  private DateTime agreementDate = null;

  /** Identifier of the Interest Group targeted for deletion. */
  private String groupId = null;

  /** Leaders of the target group, retained for notification and review purposes. */
  private List<User> leaders = new ArrayList<>();

  /** Technical name of the target group. */
  private String name = null;

  /** Human-readable title of the target group. */
  private String title = null;

  /** Description of the target group. */
  private String description = null;

  /** Message explaining why the request was rejected, when applicable. */
  private String rejectedMessage = null;

  /**
   * Adds a single leader to the {@link #leaders} list, initializing the list if necessary.
   *
   * @param leadersItem the leader to add
   * @return this {@code GroupDeletionRequest} instance to allow method chaining
   */
  public GroupDeletionRequest addLeadersItem(User leadersItem) {
    if (this.leaders == null) {
      this.leaders = new ArrayList<>();
    }
    this.leaders.add(leadersItem);
    return this;
  }

  // Getters and Setters

  /**
   * Returns the unique identifier of this deletion request.
   *
   * @return the request id, or {@code null} if not set
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets the unique identifier of this deletion request.
   *
   * @param id the request id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the user who submitted the request.
   *
   * @return the requesting user, or {@code null} if not set
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
   * Returns the date and time the request was created.
   *
   * @return the request date, or {@code null} if not set
   */
  public DateTime getRequestDate() {
    return requestDate;
  }

  /**
   * Sets the date and time the request was created.
   *
   * @param requestDate the request date; must not be {@code null}
   * @throws IllegalArgumentException if {@code requestDate} is {@code null}
   */
  public void setRequestDate(DateTime requestDate) {
    if (requestDate == null) {
      throw new IllegalArgumentException("requestDate cannot be null");
    }
    this.requestDate = requestDate;
  }

  /**
   * Returns the Alfresco node reference of the target group's category.
   *
   * @return the category reference, or {@code null} if not set
   */
  public String getCategoryRef() {
    return categoryRef;
  }

  /**
   * Sets the Alfresco node reference of the target group's category.
   *
   * @param categoryRef the category reference; must not be {@code null}
   * @throws IllegalArgumentException if {@code categoryRef} is {@code null}
   */
  public void setCategoryRef(String categoryRef) {
    if (categoryRef == null) {
      throw new IllegalArgumentException("categoryRef cannot be null");
    }
    this.categoryRef = categoryRef;
  }

  /**
   * Returns the current agreement/review status of the request.
   *
   * @return the agreement status code
   */
  public Integer getAgreement() {
    return agreement;
  }

  /**
   * Sets the current agreement/review status of the request.
   *
   * @param agreement the agreement status code
   */
  public void setAgreement(Integer agreement) {
    this.agreement = agreement;
  }

  /**
   * Returns the user responsible for reviewing the request.
   *
   * @return the reviewer, or {@code null} if not set
   */
  public User getReviewer() {
    return reviewer;
  }

  /**
   * Sets the user responsible for reviewing the request.
   *
   * @param reviewer the reviewer
   */
  public void setReviewer(User reviewer) {
    this.reviewer = reviewer;
  }

  /**
   * Returns the justification provided by the requester.
   *
   * @return the justification text, or {@code null} if not set
   */
  public String getJustification() {
    return justification;
  }

  /**
   * Sets the justification provided by the requester.
   *
   * @param justification the justification text
   */
  public void setJustification(String justification) {
    this.justification = justification;
  }

  /**
   * Returns the date and time the review decision was recorded.
   *
   * @return the agreement date, or {@code null} if not set
   */
  public DateTime getAgreementDate() {
    return agreementDate;
  }

  /**
   * Sets the date and time the review decision was recorded.
   *
   * @param agreementDate the agreement date
   */
  public void setAgreementDate(DateTime agreementDate) {
    this.agreementDate = agreementDate;
  }

  /**
   * Sets the identifier of the Interest Group targeted for deletion.
   *
   * @param groupId the target group id
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Returns the identifier of the Interest Group targeted for deletion.
   *
   * @return the target group id, or {@code null} if not set
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Returns the leaders of the target group.
   *
   * @return the list of leaders (never {@code null})
   */
  public List<User> getLeaders() {
    return leaders;
  }

  /**
   * Sets the leaders of the target group.
   *
   * @param leaders the list of leaders
   */
  public void setLeaders(List<User> leaders) {
    this.leaders = leaders;
  }

  /**
   * Sets the technical name of the target group.
   *
   * @param name the group name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the technical name of the target group.
   *
   * @return the group name, or {@code null} if not set
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the human-readable title of the target group.
   *
   * @param title the group title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the human-readable title of the target group.
   *
   * @return the group title, or {@code null} if not set
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the description of the target group.
   *
   * @param description the group description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the description of the target group.
   *
   * @return the group description, or {@code null} if not set
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the message explaining why the request was rejected.
   *
   * @param rejectedMessage the rejection message
   */
  public void setRejectedMessage(String rejectedMessage) {
    this.rejectedMessage = rejectedMessage;
  }

  /**
   * Returns the message explaining why the request was rejected.
   *
   * @return the rejection message, or {@code null} if not set
   */
  public String getRejectedMessage() {
    return rejectedMessage;
  }
}
