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
 * Simple database-layer model that carries search criteria.
 *
 * <p>Currently it holds a single {@code fromDate} bound used to restrict a search to entries
 * created or modified on or after the given date.
 */
public class Search {

  /** Lower date bound of the search range; results should not be older than this date. */
  private Date fromDate;

  /**
   * Returns the lower date bound of the search range.
   *
   * @return the {@code fromDate}, or {@code null} if no lower bound is set
   */
  public Date getFromDate() {
    return fromDate;
  }

  /**
   * Sets the lower date bound of the search range.
   *
   * @param fromDate the date from which results should be included; may be {@code null} to leave
   *     the range unbounded on the lower end
   */
  public void setFromDate(Date fromDate) {
    this.fromDate = fromDate;
  }
}
