/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package io.swagger.model.db;

/**
 * Data access object holding the result of an aggregated log-count query.
 *
 * <p>Each instance represents a single bucket of activity: the number of logged actions ({@link
 * #numberOfActions}) recorded within a given hour period ({@link #hourPeriod}). Instances are
 * typically produced when grouping log entries by time to build usage statistics or activity
 * charts.
 *
 * @author beaurpi
 */
public class LogCountResultDAO {

  /** The hour period this count belongs to, used as the time bucket for the aggregation. */
  private Integer hourPeriod;

  /** The number of logged actions recorded within {@link #hourPeriod}. */
  private Integer numberOfActions;

  /** Creates an empty {@code LogCountResultDAO} with no values set. */
  public LogCountResultDAO() {
    // Default constructor for DAO
  }

  /**
   * Returns the hour period acting as the time bucket for this count.
   *
   * @return the hour period, or {@code null} if not set
   */
  public Integer getHourPeriod() {
    return hourPeriod;
  }

  /**
   * Sets the hour period acting as the time bucket for this count.
   *
   * @param hourPeriod the hour period to set
   */
  public void setHourPeriod(Integer hourPeriod) {
    this.hourPeriod = hourPeriod;
  }

  /**
   * Returns the number of logged actions recorded within the hour period.
   *
   * @return the number of actions, or {@code null} if not set
   */
  public Integer getNumberOfActions() {
    return numberOfActions;
  }

  /**
   * Sets the number of logged actions recorded within the hour period.
   *
   * @param numberOfActions the number of actions to set
   */
  public void setNumberOfActions(Integer numberOfActions) {
    this.numberOfActions = numberOfActions;
  }
}
