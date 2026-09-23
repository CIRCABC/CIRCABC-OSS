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

/**
 * Data access object that extends {@link LogSearchParameterDAO} with pagination information used
 * when querying activity/audit log entries.
 *
 * <p>In addition to the base search criteria inherited from {@link LogSearchParameterDAO}, this DAO
 * carries the pagination window to apply to the result set: the zero/one-based offset of the first
 * record to return ({@code startRecord}) and the maximum number of records per page
 * ({@code pageSize}).
 */
public class LogSearchLimitParameterDAO extends LogSearchParameterDAO {

  /** Offset of the first record to return in the paged result set. */
  private int startRecord;

  /** Maximum number of records to return per page. */
  private int pageSize;

  /**
   * Returns the offset of the first record to return.
   *
   * @return the index of the first record in the paged result set
   */
  public int getStartRecord() {
    return startRecord;
  }

  /**
   * Sets the offset of the first record to return.
   *
   * @param startRecord the index of the first record in the paged result set
   */
  public void setStartRecord(int startRecord) {
    this.startRecord = startRecord;
  }

  /**
   * Returns the maximum number of records per page.
   *
   * @return the page size
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Sets the maximum number of records per page.
   *
   * @param pageSize the page size
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }
}
