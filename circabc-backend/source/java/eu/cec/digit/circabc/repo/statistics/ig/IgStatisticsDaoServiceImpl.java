package eu.cec.digit.circabc.repo.statistics.ig;

import eu.cec.digit.circabc.service.statistics.ig.IgStatisticsDaoService;
import org.mybatis.spring.SqlSessionTemplate;

public class IgStatisticsDaoServiceImpl implements IgStatisticsDaoService {

  private SqlSessionTemplate sqlSessionTemplate = null;

  public SqlSessionTemplate getSqlSessionTemplate() {
    return sqlSessionTemplate;
  }

  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  @Override
  public IgStatisticsParameter getIgStatisticsById(Long igId) {
    return (IgStatisticsParameter) sqlSessionTemplate.selectOne(
      "GroupStatistics.selectIGStatisticsById",
      igId
    );
  }

  @Override
  public void insertIGStatistics(IgStatisticsParameter stats) {
    sqlSessionTemplate.insert("GroupStatistics.insertIGStatistics", stats);
  }

  @Override
  public void updateIGStatistics(IgStatisticsParameter stats) {
    sqlSessionTemplate.update("GroupStatistics.updateIGStatistics", stats);
  }
}
