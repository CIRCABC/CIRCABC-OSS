package eu.cec.digit.circabc.repo.app.model;

import java.util.Date;

public class GroupLock {

  private Long id;
  private Long igId;
  private String lockMessage;
  private Boolean readOnly;
  private String lockedBy;
  private Date lockedDate;

  public GroupLock() {}

  public GroupLock(
    Long id,
    Long igId,
    String lockMessage,
    Boolean readOnly,
    String lockedBy,
    Date lockedDate
  ) {
    super();
    this.id = id;
    this.igId = igId;
    this.lockMessage = lockMessage;
    this.readOnly = readOnly;
    this.lockedBy = lockedBy;
    this.lockedDate = lockedDate;
  }

  /** @return the id */
  public Long getId() {
    return id;
  }

  /** @param id the id to set */
  public void setId(Long id) {
    this.id = id;
  }

  /** @return the igId */
  public Long getIgId() {
    return igId;
  }

  /** @param igId the igId to set */
  public void setIgId(Long igId) {
    this.igId = igId;
  }

  /** @return the lockMessage */
  public String getLockMessage() {
    return lockMessage;
  }

  /** @param lockMessage the lockMessage to set */
  public void setLockMessage(String lockMessage) {
    this.lockMessage = lockMessage;
  }

  /** @return the readOnly */
  public Boolean getReadOnly() {
    return readOnly;
  }

  /** @param readOnly the readOnly to set */
  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }

  /** @return the lockedBy */
  public String getLockedBy() {
    return lockedBy;
  }

  /** @param lockedBy the lockedBy to set */
  public void setLockedBy(String lockedBy) {
    this.lockedBy = lockedBy;
  }

  /** @return the lockedDate */
  public Date getLockedDate() {
    return lockedDate;
  }

  /** @param lockedDate the lockedDate to set */
  public void setLockedDate(Date lockedDate) {
    this.lockedDate = lockedDate;
  }

  @Override
  public String toString() {
    return (
      "GroupLock [id=" +
      id +
      ", igId=" +
      igId +
      ", lockMessage=" +
      lockMessage +
      ", readOnly=" +
      readOnly +
      ", lockedBy=" +
      lockedBy +
      ", lockedDate=" +
      lockedDate +
      "]"
    );
  }
}
