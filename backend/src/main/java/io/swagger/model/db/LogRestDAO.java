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

package io.swagger.model.db;

import java.util.Date;

/**
 * Data Access Object (data holder) representing a single CIRCABC REST audit-log entry as persisted
 * in the database.
 *
 * <p>An instance captures the details of one invocation of a logged REST endpoint: the acting user,
 * the request URL and HTTP status code, the timestamp, a reference to the log message template, and
 * contextual information such as the affected node and up to three name/value path parameters that
 * describe the resource hierarchy involved in the operation.
 *
 * <p>This is a plain mutable bean exposing standard getters and setters; it holds no business logic
 * and is used to transfer log records between the persistence layer and the application.
 */
public class LogRestDAO {

  /** Internal DB-generated sequence identifier of the log entry. */
  private long id; // internal DB-generated sequence id

  /** Identifier of the log message template describing the logged action. */
  private int templateID;

  /** User name of the account that performed the logged request. */
  private String userName;

  /** Date and time at which the logged request occurred. */
  private Date logDate;

  /** HTTP status code returned by the logged request. */
  private int statusCode;

  /** Request URL that was invoked. */
  private String url;

  /** Additional free-form information associated with the log entry. */
  private String info;

  /** Name of the first contextual path parameter. */
  private String pathOneName;

  /** Value of the first contextual path parameter. */
  private String pathOneValue;

  /** Name of the second contextual path parameter. */
  private String pathTwoName;

  /** Value of the second contextual path parameter. */
  private String pathTwoValue;

  /** Name of the third contextual path parameter. */
  private String pathThreeName;

  /** Value of the third contextual path parameter. */
  private String pathThreeValue;

  /** Identifier of the parent of the node affected by the logged request. */
  private String nodeParent;

  /** Path of the node affected by the logged request. */
  private String nodePath;

  /** Database identifier of the node affected by the logged request. */
  private Long nodeID;

  /**
   * @return the id
   */
  public long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * @return the templateID
   */
  public int getTemplateID() {
    return templateID;
  }

  /**
   * @param templateID the templateID to set
   */
  public void setTemplateID(int templateID) {
    this.templateID = templateID;
  }

  /**
   * @return the userName
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName the userName to set
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * @return the logDate
   */
  public Date getLogDate() {
    return logDate;
  }

  /**
   * @param logDate the logDate to set
   */
  public void setLogDate(Date logDate) {
    this.logDate = logDate;
  }

  /**
   * @return the statusCode
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * @param statusCode the statusCode to set
   */
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the info
   */
  public String getInfo() {
    return info;
  }

  /**
   * @param info the info to set
   */
  public void setInfo(String info) {
    this.info = info;
  }

  /**
   * @return the pathOneName
   */
  public String getPathOneName() {
    return pathOneName;
  }

  /**
   * @param pathOneName the pathOneName to set
   */
  public void setPathOneName(String pathOneName) {
    this.pathOneName = pathOneName;
  }

  /**
   * @return the pathOneValue
   */
  public String getPathOneValue() {
    return pathOneValue;
  }

  /**
   * @param pathOneValue the pathOneValue to set
   */
  public void setPathOneValue(String pathOneValue) {
    this.pathOneValue = pathOneValue;
  }

  /**
   * @return the pathTwoName
   */
  public String getPathTwoName() {
    return pathTwoName;
  }

  /**
   * @param pathTwoName the pathTwoName to set
   */
  public void setPathTwoName(String pathTwoName) {
    this.pathTwoName = pathTwoName;
  }

  /**
   * @return the pathTwoValue
   */
  public String getPathTwoValue() {
    return pathTwoValue;
  }

  /**
   * @param pathTwoValue the pathTwoValue to set
   */
  public void setPathTwoValue(String pathTwoValue) {
    this.pathTwoValue = pathTwoValue;
  }

  /**
   * @return the pathThreeName
   */
  public String getPathThreeName() {
    return pathThreeName;
  }

  /**
   * @param pathThreeName the pathThreeName to set
   */
  public void setPathThreeName(String pathThreeName) {
    this.pathThreeName = pathThreeName;
  }

  /**
   * @return the pathThreeValue
   */
  public String getPathThreeValue() {
    return pathThreeValue;
  }

  /**
   * @param pathThreeValue the pathThreeValue to set
   */
  public void setPathThreeValue(String pathThreeValue) {
    this.pathThreeValue = pathThreeValue;
  }

  /**
   * @return the nodeParent
   */
  public String getNodeParent() {
    return nodeParent;
  }

  /**
   * @param nodeParent the nodeParent to set
   */
  public void setNodeParent(String nodeParent) {
    this.nodeParent = nodeParent;
  }

  /**
   * @return the nodePath
   */
  public String getNodePath() {
    return nodePath;
  }

  /**
   * @param nodePath the nodePath to set
   */
  public void setNodePath(String nodePath) {
    this.nodePath = nodePath;
  }

  /**
   * @return the nodeID
   */
  public Long getNodeID() {
    return nodeID;
  }

  /**
   * @param nodeID the nodeID to set
   */
  public void setNodeID(Long nodeID) {
    this.nodeID = nodeID;
  }

  /**
   * Returns a string representation of this log entry including all of its fields, primarily
   * intended for debugging and logging.
   *
   * @return a string representation of this {@code LogRestDAO}
   */
  @Override
  public String toString() {
    return (
      "LogRestDAO{" +
      "id=" +
      id +
      ", templateID=" +
      templateID +
      ", userName='" +
      userName +
      '\'' +
      ", logDate=" +
      logDate +
      ", statusCode=" +
      statusCode +
      ", url='" +
      url +
      '\'' +
      ", info='" +
      info +
      '\'' +
      ", pathOneName='" +
      pathOneName +
      '\'' +
      ", pathOneValue='" +
      pathOneValue +
      '\'' +
      ", pathTwoName='" +
      pathTwoName +
      '\'' +
      ", pathTwoValue='" +
      pathTwoValue +
      '\'' +
      ", pathThreeName='" +
      pathThreeName +
      '\'' +
      ", pathThreeValue='" +
      pathThreeValue +
      '\'' +
      ", nodeParent='" +
      nodeParent +
      '\'' +
      ", nodePath='" +
      nodePath +
      '\'' +
      ", nodeID=" +
      nodeID +
      '}'
    );
  }
}
