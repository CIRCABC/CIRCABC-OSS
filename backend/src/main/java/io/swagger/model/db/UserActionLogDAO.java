/**
 *
 */
package io.swagger.model.db;

import java.util.Date;

/**
 * Data access object representing a single user action log entry.
 *
 * <p>Each instance captures an audited action performed by a user within CIRCABC, tying together
 * the affected document, the Interest Group context, the action name, the moment it occurred and
 * the user who performed it. It is a plain data holder used to carry action-log rows between the
 * persistence layer and the services that record or report user activity.
 *
 * @author beaurpi
 */
public class UserActionLogDAO {

  /** Identifier of the document (node) the logged action was performed on. */
  private String documentId;

  /** Identifier of the Interest Group in whose context the action took place. */
  private String igId;

  /** Name of the audited action that was performed. */
  private String action;

  /** Date and time at which the action was logged. */
  private Date logDate;

  /** Username of the user who performed the action. */
  private String username;

  /**
   * Returns the identifier of the document the action was performed on.
   *
   * @return the document identifier
   */
  public String getDocumentId() {
    return documentId;
  }

  /**
   * Sets the identifier of the document the action was performed on.
   *
   * @param documentId the document identifier to set
   */
  public void setDocumentId(String documentId) {
    this.documentId = documentId;
  }

  /**
   * Returns the identifier of the Interest Group in whose context the action took place.
   *
   * @return the Interest Group identifier
   */
  public String getIgId() {
    return igId;
  }

  /**
   * Sets the identifier of the Interest Group in whose context the action took place.
   *
   * @param igId the Interest Group identifier to set
   */
  public void setIgId(String igId) {
    this.igId = igId;
  }

  /**
   * Returns the date and time at which the action was logged.
   *
   * @return the log date
   */
  public Date getLogDate() {
    return logDate;
  }

  /**
   * Sets the date and time at which the action was logged.
   *
   * @param logDate the log date to set
   */
  public void setLogDate(Date logDate) {
    this.logDate = logDate;
  }

  /** @return the action */
  public String getAction() {
    return action;
  }

  /** @param action the action to set */
  public void setAction(String action) {
    this.action = action;
  }

  /** @return the username */
  public String getUsername() {
    return username;
  }

  /** @param username the username to set */
  public void setUsername(String username) {
    this.username = username;
  }
}
