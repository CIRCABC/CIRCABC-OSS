/**
 *
 */
package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;

/** @author eduardo */
public class GroupDeletionRequest {

  private Long id = null;
  private User from = null;
  private DateTime requestDate = null;
  private String categoryRef = null;
  private Integer agreement = 0;
  private User reviewer = null;
  private String justification = null;
  private DateTime agreementDate = null;
  private String groupId = null;
  private List<User> leaders = new ArrayList<>();
  private String name = null;
  private String title = null;
  private String description = null;
  private String rejectedMessage = null;

  public GroupDeletionRequest addLeadersItem(User leadersItem) {
    if (this.leaders == null) {
      this.leaders = new ArrayList<>();
    }
    this.leaders.add(leadersItem);
    return this;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getFrom() {
    return from;
  }

  public void setFrom(User from) {
    this.from = from;
  }

  public DateTime getRequestDate() {
    return requestDate;
  }

  public void setRequestDate(DateTime requestDate) {
    if (requestDate == null) {
      throw new IllegalArgumentException("requestDate cannot be null");
    }
    this.requestDate = requestDate;
  }

  public String getCategoryRef() {
    return categoryRef;
  }

  public void setCategoryRef(String categoryRef) {
    if (categoryRef == null) {
      throw new IllegalArgumentException("categoryRef cannot be null");
    }
    this.categoryRef = categoryRef;
  }

  public Integer getAgreement() {
    return agreement;
  }

  public void setAgreement(Integer agreement) {
    this.agreement = agreement;
  }

  public User getReviewer() {
    return reviewer;
  }

  public void setReviewer(User reviewer) {
    this.reviewer = reviewer;
  }

  public String getJustification() {
    return justification;
  }

  public void setJustification(String justification) {
    this.justification = justification;
  }

  public DateTime getAgreementDate() {
    return agreementDate;
  }

  public void setAgreementDate(DateTime agreementDate) {
    this.agreementDate = agreementDate;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getGroupId() {
    return groupId;
  }

  public List<User> getLeaders() {
    return leaders;
  }

  public void setLeaders(List<User> leaders) {
    this.leaders = leaders;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setRejectedMessage(String rejectedMessage) {
    this.rejectedMessage = rejectedMessage;
  }

  public String getRejectedMessage() {
    return rejectedMessage;
  }
}
