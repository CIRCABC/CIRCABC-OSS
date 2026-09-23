/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package io.swagger.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Serializable data model describing a single auditable action performed within CIRCABC.
 *
 * <p>A {@code LogRecord} captures the contextual details of an operation so that it can be
 * persisted, reported on or displayed in activity logs. It records which Interest Group and
 * document the action targeted, the user who performed it, the type of activity and service
 * involved, a free-form informational message, the repository path, whether the action succeeded
 * and when it occurred.
 *
 * <p>This class is a plain mutable value object (POJO) with standard accessors and mutators; it
 * holds no business logic beyond assembling the {@link #info} message via {@link #addInfo(String)}.
 */
public class LogRecord implements Serializable {

  /** Serialization version identifier used to verify compatibility during deserialization. */
  private static final long serialVersionUID = 7838345879030553825L;

  /** Identifier of the Interest Group the logged action relates to. */
  private Long igID;

  /** Human-readable name of the Interest Group; defaults to an empty string. */
  private String igName = "";

  /** Identifier of the document (node) the logged action relates to, if any. */
  private Long documentID;

  /** Username of the actor who performed the logged action. */
  private String user;

  /** Type of activity performed (e.g. the action or operation name). */
  private String activity;

  /**
   * Accumulator holding the free-form informational message; built incrementally via {@link
   * #addInfo(String)} and reset via {@link #setInfo(String)}.
   */
  private StringBuilder info = new StringBuilder();

  /** Repository path associated with the logged action. */
  private String path;

  /** Name of the CIRCABC service (e.g. Library, Newsgroup) in which the action occurred. */
  private String service;

  /** Flag indicating whether the logged action completed successfully. */
  private boolean isOK;

  /** Timestamp of when the logged action occurred. */
  private Date date;

  /**
   * @return the igID
   */
  public Long getIgID() {
    return igID;
  }

  /**
   * @param igID the igID to set
   */
  public void setIgID(Long igID) {
    this.igID = igID;
  }

  /**
   * @return the documentID
   */
  public Long getDocumentID() {
    return documentID;
  }

  /**
   * @param documentID the documentID to set
   */
  public void setDocumentID(Long documentID) {
    this.documentID = documentID;
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * @return the service
   */
  public String getService() {
    return service;
  }

  /**
   * @param service the service to set
   */
  public void setService(String service) {
    this.service = service;
  }

  /**
   * @return the action
   */
  public String getActivity() {
    return activity;
  }

  /**
   * @param activity the action to set
   */
  public void setActivity(String activity) {
    this.activity = activity;
  }

  /**
   * Returns the accumulated informational message.
   *
   * @return the current contents of the info buffer as a string
   */
  public String getInfo() {
    return info.toString();
  }

  /**
   * Replaces the entire informational message with the given value.
   *
   * @param info the info text to set, clearing any previously accumulated content
   */
  public void setInfo(String info) {
    this.info.delete(0, this.info.length());
    this.info.append(info);
  }

  /**
   * Appends an additional fragment to the informational message, separated by a leading space.
   *
   * @param info the info fragment to append to the existing content
   */
  public void addInfo(String info) {
    this.info.append(" ");
    this.info.append(info);
  }

  /**
   * @return the isOK
   */
  public boolean isOK() {
    return isOK;
  }

  /**
   * @param isOK the isOK to set
   */
  public void setOK(boolean isOK) {
    this.isOK = isOK;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * @return the igName
   */
  public String getIgName() {
    return igName;
  }

  /**
   * @param igName the igName to set
   */
  public void setIgName(String igName) {
    this.igName = igName;
  }

  /**
   * @return the date
   */
  public final Date getDate() {
    return date;
  }

  /**
   * @param date the date to set
   */
  public final void setDate(Date date) {
    this.date = date;
  }

  /**
   * Returns a string representation of this log record listing all of its fields.
   *
   * @return a human-readable representation of the record's state
   */
  @Override
  public String toString() {
    return (
      "LogRecord [igID=" +
      this.igID +
      ", igName=" +
      this.igName +
      ", documentID=" +
      this.documentID +
      ", user=" +
      this.user +
      ", activity=" +
      this.activity +
      ", info=" +
      this.info +
      ", path=" +
      this.path +
      ", service=" +
      this.service +
      ", isOK=" +
      this.isOK +
      ", date=" +
      this.date +
      "]"
    );
  }
}
