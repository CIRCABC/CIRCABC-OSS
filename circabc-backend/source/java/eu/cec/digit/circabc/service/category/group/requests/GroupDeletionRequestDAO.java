/**
 *
 */
package eu.cec.digit.circabc.service.category.group.requests;

import java.util.Date;

/** @author Eduardo */
public class GroupDeletionRequestDAO {

  private Integer id = null;

  private String from;

  private Date requestDate;

  private String categoryRef;

  private Integer agreement = 0;

  private String reviewer;

  private String justification;

  private Date agreementDate;

  private String group_id;

  private String title;

  private String name;

  private String leaders;

  private String description;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getFromUsername() {
    return from;
  }

  public void setFromUsername(String fromUsername) {
    this.from = fromUsername;
  }

  public Date getRequestDate() {
    return requestDate;
  }

  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  public String getJustification() {
    return justification;
  }

  public void setJustification(String justification) {
    this.justification = justification;
  }

  public String getCategoryRef() {
    return categoryRef;
  }

  public void setCategoryRef(String categoryRef) {
    this.categoryRef = categoryRef;
  }

  public String getReviewer() {
    return reviewer;
  }

  public void setReviewer(String reviewer) {
    this.reviewer = reviewer;
  }

  public Integer getAgreement() {
    return agreement;
  }

  public void setAgreement(Integer agreement) {
    this.agreement = agreement;
  }

  public Date getAgreementDate() {
    return agreementDate;
  }

  public void setAgreementDate(Date agreementDate) {
    this.agreementDate = agreementDate;
  }

  public void setGroupId(String groupId) {
    this.group_id = groupId;
  }

  public String getGroupId() {
    return group_id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLeaders() {
    return leaders;
  }

  public void setLeaders(String leaders) {
    this.leaders = leaders;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
