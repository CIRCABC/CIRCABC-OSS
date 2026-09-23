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
 * Data access object holding an aggregated count of activities for reporting/statistics.
 *
 * <p>Each instance represents the number of occurrences ({@link #actionNumber}) of a given activity
 * type within a service, grouped by the month in which the activity took place. It is used to carry
 * activity-statistics query results between the persistence layer and the REST/service layers.
 */
public class ActivityCountDAO {

  /** The month (period) to which the aggregated activity count applies. */
  private Date monthActivity;

  /** The service the activity belongs to (e.g. Library, Newsgroup, Events). */
  private String service;

  /** The activity type/name being counted. */
  private String activity;

  /** The identifier of the action associated with the activity. */
  private Integer actionId;

  /** The aggregated number of activities for the given month, service and action. */
  private Integer actionNumber;

  /** Default constructor for DAO. */
  public ActivityCountDAO() {
    // Default constructor for DAO
  }

  /**
   * @return the monthActivity
   */
  public Date getMonthActivity() {
    return monthActivity;
  }

  /**
   * @param monthActivity the monthActivity to set
   */
  public void setMonthActivity(Date monthActivity) {
    this.monthActivity = monthActivity;
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
   * @return the activity
   */
  public String getActivity() {
    return activity;
  }

  /**
   * @param activity the activity to set
   */
  public void setActivity(String activity) {
    this.activity = activity;
  }

  /**
   * @return the actionId
   */
  public Integer getActionId() {
    return actionId;
  }

  /**
   * @param actionId the actionId to set
   */
  public void setActionId(Integer actionId) {
    this.actionId = actionId;
  }

  /**
   * @return the actionNumber
   */
  public Integer getActionNumber() {
    return actionNumber;
  }

  /**
   * @param actionNumber the actionNumber to set
   */
  public void setActionNumber(Integer actionNumber) {
    this.actionNumber = actionNumber;
  }
}
