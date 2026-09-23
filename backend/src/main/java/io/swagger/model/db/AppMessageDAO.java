package io.swagger.model.db;

import java.util.Date;

/**
 * Data access object (DAO) representing an application-wide message (banner)
 * that can be displayed to CIRCABC users.
 *
 * <p>Instances of this class map to the persisted application message records
 * and carry the message text, its severity level, an optional closure date,
 * a flag controlling whether the message is currently shown, and how long it
 * should be displayed. It is a plain data holder with no business logic.
 */
public class AppMessageDAO {

  /** Unique identifier of the application message. */
  private Integer id = null;

  /** The textual content of the message to display to users. */
  private String messageContent = null;

  /** The date at which the message is closed and should no longer be shown. */
  private Date dateClosure = null;

  /** The severity level of the message (e.g. info, warning, error). */
  private String messageLevel = null;

  /** Flag indicating whether the message is currently shown to users. */
  private Boolean showMessage = false;

  /** The duration, in seconds, for which the message should be displayed. */
  private Integer displayTime = null;

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
   * @return the messageContent
   */
  public String getMessageContent() {
    return messageContent;
  }

  /**
   * @param messageContent the messageContent to set
   */
  public void setMessageContent(String messageContent) {
    this.messageContent = messageContent;
  }

  /**
   * @return the dateClosure
   */
  public Date getDateClosure() {
    return dateClosure;
  }

  /**
   * @param dateClosure the dateClosure to set
   */
  public void setDateClosure(Date dateClosure) {
    this.dateClosure = dateClosure;
  }

  /**
   * @return the messageClevel
   */
  public String getMessageLevel() {
    return messageLevel;
  }

  /**
   * @param messageLevel the messageClevel to set
   */
  public void setMessageLevel(String messageLevel) {
    this.messageLevel = messageLevel;
  }

  /**
   * @return the showMessage
   */
  public Boolean getShowMessage() {
    return showMessage;
  }

  /**
   * @param showMessage the showMessage to set
   */
  public void setShowMessage(Boolean showMessage) {
    this.showMessage = showMessage;
  }

  /**
   * @return the displayTime
   */
  public Integer getDisplayTime() {
    return displayTime;
  }

  /**
   * @param displayTime the displayTime to set
   */
  public void setDisplayTime(Integer displayTime) {
    this.displayTime = displayTime;
  }
}
