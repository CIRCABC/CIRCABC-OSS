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

import org.mybatis.spring.SqlSessionTemplate;

/**
 * Default MyBatis-backed implementation of {@link IgStatisticsDaoService}.
 *
 * <p>This data access object encapsulates the persistence operations for Interest Group (IG)
 * statistics. It delegates all database interaction to a Spring-managed {@link SqlSessionTemplate},
 * invoking the mapped SQL statements defined under the {@code GroupStatistics} MyBatis namespace to
 * read, create and update the statistics record associated with an Interest Group.
 */
public class IgStatisticsDaoServiceImpl implements IgStatisticsDaoService {

  /**
   * MyBatis session template used to execute the mapped SQL statements. Injected by Spring via
   * {@link #setSqlSessionTemplate(SqlSessionTemplate)}.
   */
  private SqlSessionTemplate sqlSessionTemplate = null;

  /**
   * Returns the MyBatis session template used by this DAO.
   *
   * @return the configured {@link SqlSessionTemplate}, or {@code null} if none has been injected
   */
  public SqlSessionTemplate getSqlSessionTemplate() {
    return sqlSessionTemplate;
  }

  /**
   * Sets the MyBatis session template used to execute the mapped SQL statements. Typically called by
   * the Spring container during bean initialization.
   *
   * @param sqlSessionTemplate the {@link SqlSessionTemplate} to use for database access
   */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  /**
   * Retrieves the statistics record for a single Interest Group.
   *
   * <p>Executes the {@code GroupStatistics.selectIGStatisticsById} mapped query.
   *
   * @param igId the identifier of the Interest Group whose statistics are requested
   * @return the {@link IgStatisticsParameter} for the given Interest Group, or {@code null} if no
   *     matching record exists
   */
  @Override
  public IgStatisticsParameter getIgStatisticsById(Long igId) {
    return (IgStatisticsParameter) sqlSessionTemplate.selectOne(
      "GroupStatistics.selectIGStatisticsById",
      igId
    );
  }

  /**
   * Inserts a new Interest Group statistics record.
   *
   * <p>Executes the {@code GroupStatistics.insertIGStatistics} mapped statement.
   *
   * @param stats the statistics data to persist
   */
  @Override
  public void insertIGStatistics(IgStatisticsParameter stats) {
    sqlSessionTemplate.insert("GroupStatistics.insertIGStatistics", stats);
  }

  /**
   * Updates an existing Interest Group statistics record.
   *
   * <p>Executes the {@code GroupStatistics.updateIGStatistics} mapped statement.
   *
   * @param stats the statistics data holding the updated values to persist
   */
  @Override
  public void updateIGStatistics(IgStatisticsParameter stats) {
    sqlSessionTemplate.update("GroupStatistics.updateIGStatistics", stats);
  }
}
