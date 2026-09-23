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
 * Data access parameter object that groups the criteria used to query activity/audit log entries.
 *
 * <p>Instances of this class act as a simple, mutable container (POJO) whose fields are populated by
 * the caller and then passed down to the persistence layer to filter log records. All criteria are
 * optional; a {@code null} field is expected to be treated as "no constraint" for that dimension by
 * the query that consumes it.
 *
 * @author Slobodan Filipovic
 */
public class LogSearchParameterDAO {

  /** Identifier of the Interest Group whose log entries are being searched. */
  private Long igID;

  /** Lower bound (inclusive) of the date range to search; {@code null} means unbounded. */
  private Date fromDate;

  /** Upper bound (inclusive) of the date range to search; {@code null} means unbounded. */
  private Date toDate;

  /** Filter on the activity description of the log entry. */
  private String activityDescription;

  /** Filter on the service description associated with the log entry. */
  private String serviceDescription;

  /** Filter on the user name that generated the log entry. */
  private String userName;

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
   * @return the fromDate
   */
  public Date getFromDate() {
    return fromDate;
  }

  /**
   * @param dateFrom the fromDate to set
   */
  public void setFromDate(Date dateFrom) {
    this.fromDate = dateFrom;
  }

  /**
   * @return the toDate
   */
  public Date getToDate() {
    return toDate;
  }

  /**
   * @param dateTo the toDate to set
   */
  public void setToDate(Date dateTo) {
    this.toDate = dateTo;
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
}
