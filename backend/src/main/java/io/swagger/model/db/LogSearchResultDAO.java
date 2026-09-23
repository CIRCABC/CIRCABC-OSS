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
 * Data access object representing a single row of an activity/audit log search result.
 *
 * <p>This is a plain data holder (POJO) that carries the details of a logged action retrieved from
 * the database: when it happened, which activity and service it relates to, the user who performed
 * it, the affected content path, additional free-form information and whether the action succeeded.
 * Instances are typically populated from log query results and exposed to the REST layer.
 *
 * @author Slobodan Filipovic
 */
public class LogSearchResultDAO {

  /** Date and time at which the logged action occurred. */
  private Date logDate;

  /** Human-readable description of the logged activity/action. */
  private String activityDescription;

  /** Human-readable description of the service the action belongs to. */
  private String serviceDescription;

  /** Login name of the user who performed the action. */
  private String userName;

  /** Additional free-form information associated with the log entry. */
  private String info;

  /** Path of the content/node affected by the action. */
  private String path;

  /** Outcome flag of the action: {@code 1} indicates success, any other value indicates an error. */
  private int isOK;

  /**
   * @return the logDate
   */
  public Date getLogDate() {
    return logDate;
  }

  /**
   * @param date the logDate to set
   */
  public void setLogDate(Date date) {
    this.logDate = date;
  }

  /**
   * @return the activityDescription
   */
  public String getActivityDescription() {
    return activityDescription;
  }

  /**
   * @param activityDescription the activityDescription to set
   */
  public void setActivityDescription(String activityDescription) {
    this.activityDescription = activityDescription;
  }

  /**
   * @return the serviceDescription
   */
  public String getServiceDescription() {
    return serviceDescription;
  }

  /**
   * @param serviceDescription the serviceDescription to set
   */
  public void setServiceDescription(String serviceDescription) {
    this.serviceDescription = serviceDescription;
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
   * @return the isOK
   */
  public int getIsOK() {
    return isOK;
  }

  /**
   * @param isOK the isOK to set
   */
  public void setIsOK(int isOK) {
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
   * Returns a textual representation of the action outcome derived from {@link #isOK}.
   *
   * @return {@code "OK"} if {@link #isOK} equals {@code 1}, otherwise {@code "Error"}
   */
  public String getStatus() {
    final String result;
    if (isOK == 1) {
      result = "OK";
    } else {
      result = "Error";
    }
    return result;
  }
}
