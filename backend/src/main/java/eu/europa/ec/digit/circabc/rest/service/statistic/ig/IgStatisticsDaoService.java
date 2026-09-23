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
 *
 * @author Alain Morlet
 */
package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

/**
 * Data-access contract for persisting and retrieving statistics of an Interest Group (IG).
 *
 * <p>Implementations delegate to the underlying MyBatis mapped statements (see the {@code
 * GroupStatistics} SQL namespace) to read and write {@link IgStatisticsParameter} records keyed by
 * the Interest Group identifier. This interface isolates the statistics persistence concerns from
 * the surrounding service layer.
 */
public interface IgStatisticsDaoService {
  /**
   * Retrieves the persisted statistics for a given Interest Group.
   *
   * @param igId the identifier of the Interest Group whose statistics are requested
   * @return the {@link IgStatisticsParameter} associated with the given Interest Group, or {@code
   *     null} if no statistics record exists for it
   */
  IgStatisticsParameter getIgStatisticsById(Long igId);

  /**
   * Inserts a new statistics record for an Interest Group.
   *
   * @param stats the statistics data to persist
   */
  void insertIGStatistics(IgStatisticsParameter stats);

  /**
   * Updates the existing statistics record for an Interest Group.
   *
   * @param stats the statistics data holding the updated values to persist
   */
  void updateIGStatistics(IgStatisticsParameter stats);
}
